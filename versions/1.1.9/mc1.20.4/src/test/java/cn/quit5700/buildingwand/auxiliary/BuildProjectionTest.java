package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.logic.*;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BuildProjectionTest {
 @BeforeAll static void version(){net.minecraft.SharedConstants.tryDetectVersion();}
 static CompoundTag state(){var state=new CompoundTag();var jobs=new ListTag();var j=new CompoundTag();j.putString("Id","test");j.putString("Kind","Build");j.putString("Dim","minecraft:overworld");j.putIntArray("PreviewBounds",new int[]{-17,2,-17,0,4,0});j.putIntArray("PreviewCursor",new int[]{-17,2,-17});j.putIntArray("PreviewDirection",new int[]{1,1,1});jobs.add(j);state.put("Jobs",jobs);return state;}
 @Test void confirmationShowsWholePendingRegionWithoutEditableRows(){var rows=BuildProjection.rows(state(),"minecraft:overworld");assertEquals(1,rows.size());assertTrue(rows.get(0).contains(0,0,0));assertTrue(rows.get(0).contains(17,2,17));assertFalse(state().contains("Rows"));}
 @Test void progressMatchesActualChunkCursorInAllEightDirections(){
  var box=new DeleteBox(-17,2,-17,0,4,0);for(int dx:new int[]{-1,1})for(int dy:new int[]{-1,1})for(int dz:new int[]{-1,1}){
   var cursor=new DirectedDeleteCursor(box,dx,dy,dz);var visited=new HashSet<BlockPos>();var n=AuxRow.create(new BlockPos(-17,2,-17),new BlockPos(0,4,0));n.putBoolean("BuildPreview",true);n.putIntArray("PreviewDirection",new int[]{dx,dy,dz});int tick=0;
   while(!cursor.done()){n.putIntArray("PreviewCursor",new int[]{cursor.x(),cursor.y(),cursor.z()});if(tick++%37==0){var row=new AuxRow(n.copy());for(int x=0;x<18;x++)for(int y=0;y<3;y++)for(int z=0;z<18;z++)assertEquals(!visited.contains(row.world(x,y,z)),row.contains(x,y,z));}visited.add(new BlockPos(cursor.x(),cursor.y(),cursor.z()));cursor.advance();}
   assertEquals(972,visited.size());
  }
 }
 @Test void hollowModeNeverHighlightsInteriorEvenAtProgressFront(){var s=state();var j=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(s,"Jobs"),0);j.putInt("PreviewMode",2);var row=BuildProjection.rows(s,"minecraft:overworld").get(0);assertFalse(row.contains(8,1,8));assertTrue(row.contains(0,1,8));}
 @Test void switchingHeldItemsAndHiddenBlueprintFlagDoNotRemoveTaskData(){var s=state();s.putBoolean("Shown",false);assertEquals(1,BuildProjection.rows(s,"minecraft:overworld").size());}
 @Test void cancelledCompletedAndOtherDimensionTasksLeaveNoProjection(){var s=state();assertTrue(BuildProjection.rows(s,"minecraft:the_nether").isEmpty());s.put("Jobs",new ListTag());assertTrue(BuildProjection.rows(s,"minecraft:overworld").isEmpty());}
 @Test void saveReloadRetainsProjectionAndUnrelatedBlueprintJobIsIgnored(){var s=state();assertEquals(1,BuildProjection.rows(s.copy(),"minecraft:overworld").size());cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(s,"Jobs"),0).putString("Kind","Blueprint");assertTrue(BuildProjection.rows(s,"minecraft:overworld").isEmpty());}
 @Test void staleBufferedFacesDisappearImmediatelyAfterCancelOrProgress(){
  var s=state();var row=BuildProjection.rows(s,"minecraft:overworld").get(0);var at=row.world(0,0,0);assertTrue(BuildProjection.visible(row,at));assertFalse(BuildProjection.visible(null,at));
  var j=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(s,"Jobs"),0);j.putIntArray("PreviewCursor",new int[]{-17,3,-17});var advanced=BuildProjection.rows(s,"minecraft:overworld").get(0);assertFalse(BuildProjection.visible(advanced,at));assertTrue(BuildProjection.visible(advanced,at.above()));
 }
 @Test void newBuildProjectionPrecedesExistingLargeBlueprint(){var build=BuildProjection.rows(state(),"minecraft:overworld").get(0);var normal=new AuxRow(AuxRow.create(BlockPos.ZERO,new BlockPos(127,127,127)));var ordered=BuildProjection.renderingOrder(List.of(normal,build));assertSame(build,ordered.get(0));assertSame(normal,ordered.get(1));assertEquals(List.of(normal),BuildProjection.renderingOrder(List.of(normal)));}
}
