package cn.quit5700.buildingwand.logic;

import org.junit.jupiter.api.Test;
import com.mojang.serialization.JsonOps;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DeleteSurvivalTest {
    @Test void oneDurabilityAndZeroRemainderPermitsExactlyOneHundredRemovals(){
        int remainder=0,damage=511,removed=0;
        while(DeleteWear.capacity(512,damage,remainder)>0){
            var payment=DeleteWear.removed(remainder);remainder=payment.remainder();damage+=payment.damage();removed++;
        }
        assertEquals(100,removed);assertEquals(512,damage);assertEquals(0,remainder);
    }
    @Test void fractionalWearCarriesAcrossJobsAndWandsIndependently(){
        int a=0,b=0,damage=0;
        for(int i=0;i<80;i++)a=DeleteWear.removed(a).remainder();
        assertEquals(20,DeleteWear.capacity(512,511,a));assertEquals(100,DeleteWear.capacity(512,511,b));
        for(int i=0;i<20;i++){var payment=DeleteWear.removed(a);a=payment.remainder();damage+=payment.damage();}
        assertEquals(1,damage);assertEquals(0,a);assertEquals(0,b);
    }
    @Test void mendingDamageReductionRestoresCapacityWithoutDiscardingRemainder(){
        assertEquals(20,DeleteWear.capacity(512,511,80));
        assertEquals(220,DeleteWear.capacity(512,509,80));
        assertEquals(51200,DeleteWear.capacity(512,0,0));
    }
    @Test void bedrockProtectedInSurvivalButNotCreativeAndAirIsFree(){
        assertFalse(DeleteWear.removable(false,true,false));assertTrue(DeleteWear.removable(false,true,true));
        assertFalse(DeleteWear.removable(true,false,false));assertTrue(DeleteWear.removable(false,false,false));
    }
    @Test void allCornerDirectionsBeginAtTheActualFirstPointAndVisitEachBlockOnce(){
        DeleteBox box=new DeleteBox(-17,-2,-17,18,2,18);
        for(int dx:new int[]{-1,1})for(int dy:new int[]{-1,1})for(int dz:new int[]{-1,1}){
            DirectedDeleteCursor cursor=new DirectedDeleteCursor(box,dx,dy,dz);
            assertEquals(dx>0?-17:18,cursor.x());assertEquals(dy>0?-2:2,cursor.y());assertEquals(dz>0?-17:18,cursor.z());
            HashSet<String> visited=new HashSet<>(),finishedChunks=new HashSet<>();String chunk=null;
            while(!cursor.done()){
                assertTrue(box.contains(cursor.x(),cursor.y(),cursor.z()));
                assertTrue(visited.add(cursor.x()+","+cursor.y()+","+cursor.z()));
                String next=cursor.chunkX()+","+cursor.chunkZ();
                if(!next.equals(chunk)){assertFalse(finishedChunks.contains(next));if(chunk!=null)finishedChunks.add(chunk);chunk=next;}
                cursor.advance();
            }
            assertEquals(box.volume().intValueExact(),visited.size());
        }
    }
    @Test void directedPersistencePreservesFirstToSecondDirectionAndToolIdentity(){
        DeleteBox box=new DeleteBox(-17,-2,-17,18,2,18);DirectedDeleteCursor c=new DirectedDeleteCursor(box,-1,-1,-1);
        for(int i=0;i<315;i++)c.advance();String wand=UUID.randomUUID().toString();
        var data=new DeleteTaskData(UUID.randomUUID().toString(),List.of(-17,-2,-17,18,2,18),List.of(c.x(),c.y(),c.z()),"315","99",List.of(-1,-1,-1),wand);
        var loaded=DeleteTaskData.CODEC.parse(JsonOps.INSTANCE,DeleteTaskData.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow(false,e->{throw new IllegalStateException(e);})).getOrThrow(false,e->{throw new IllegalStateException(e);});
        assertEquals(wand,loaded.wand());DirectedDeleteCursor restored=loaded.restoreDirectedCursor();
        while(!c.done()){assertFalse(restored.done());assertEquals(c.x(),restored.x());assertEquals(c.y(),restored.y());assertEquals(c.z(),restored.z());c.advance();restored.advance();}
        assertTrue(restored.done());
    }
    @Test void legacyJobCodecDefaultsToItsOriginalTraversal(){
        var data=new DeleteTaskData(UUID.randomUUID().toString(),List.of(0,0,0,2,2,2),List.of(0,0,0),"0","0");
        var json=DeleteTaskData.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow(false,e->{throw new IllegalStateException(e);}).getAsJsonObject();
        json.remove("directions");json.remove("wand");
        var loaded=DeleteTaskData.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow(false,e->{throw new IllegalStateException(e);});
        assertEquals(List.of(1,1,1),loaded.directions());assertEquals("",loaded.wand());
    }
    @Test void invalidDirectionsRejected(){
        assertThrows(IllegalArgumentException.class,()->new DirectedDeleteCursor(new DeleteBox(0,0,0,1,1,1),0,1,1));
    }
}
