package cn.quit5700.buildingwand.logic;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DeleteTaskPersistenceTest {
    private static final String OWNER=UUID.randomUUID().toString();
    @Test void actualSavedJobCodecRoundTripsAndResumesNextPosition() {
        DeleteBox box=new DeleteBox(-17,-3,-17,20,3,20);DeleteCursor cursor=new DeleteCursor(box);
        for(int i=0;i<153;i++)cursor.advance();
        DeleteTaskData data=new DeleteTaskData(OWNER,List.of(-17,-3,-17,20,3,20),List.of(cursor.x(),cursor.y(),cursor.z()),"153","109");
        JsonElement json=DeleteTaskData.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow();
        DeleteTaskData loaded=DeleteTaskData.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
        assertEquals(data,loaded);DeleteCursor restored=loaded.restoreCursor();
        while(!cursor.done()){
            assertFalse(restored.done());assertEquals(cursor.x(),restored.x());assertEquals(cursor.y(),restored.y());assertEquals(cursor.z(),restored.z());
            cursor.advance();restored.advance();
        }
        assertTrue(restored.done());assertEquals(BigInteger.valueOf(153),loaded.visitedCount());assertEquals(BigInteger.valueOf(109),loaded.removedCount());
    }
    @Test void persistedCountersBeyondLongKeepFullPrecision() {
        String large=BigInteger.ONE.shiftLeft(70).toString();
        DeleteTaskData data=new DeleteTaskData(OWNER,List.of(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE),List.of(0,0,0),large,large);
        assertEquals(data,DeleteTaskData.CODEC.parse(JsonOps.INSTANCE,DeleteTaskData.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow()).getOrThrow());
    }
    @Test void invalidProgressAndCoordinatesRejected() {
        List<Integer> b=List.of(0,0,0,1,1,1),c=List.of(0,0,0);
        assertThrows(IllegalArgumentException.class,()->new DeleteTaskData(OWNER,b,c,"8","0"));
        assertThrows(IllegalArgumentException.class,()->new DeleteTaskData(OWNER,b,c,"-1","0"));
        assertThrows(IllegalArgumentException.class,()->new DeleteTaskData(OWNER,b,c,"2","3"));
        assertThrows(IllegalArgumentException.class,()->new DeleteTaskData(OWNER,b,List.of(2,0,0),"0","0"));
        assertThrows(IllegalArgumentException.class,()->new DeleteTaskData("invalid",b,c,"0","0"));
    }
    @Test void savedCoordinateListsCannotBeMutatedExternally() {
        var bounds=new java.util.ArrayList<>(List.of(0,0,0,1,1,1));
        DeleteTaskData data=new DeleteTaskData(OWNER,bounds,List.of(0,0,0),"0","0");bounds.set(0,100);
        assertEquals(0,data.box().minX());assertThrows(UnsupportedOperationException.class,()->data.bounds().set(0,5));
    }
}
