package cn.quit5700.buildingwand.aux;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AuxUiLayoutTest {
 @Test void mainPanelKeepsItsProportionWithRoomBelowTheAddButton(){for(int h:new int[]{284,384,410}){int width=AuxUiLayout.mainWidth(900,h);assertTrue(width/(double)h<=1.6);}}
 @Test void backpackAndHotbarUseTheRealInventorySlotOrder(){var actual=new java.util.HashSet<Integer>();for(int display=0;display<36;display++)actual.add(AuxUiLayout.inventorySlot(display));assertEquals(36,actual.size());assertEquals(9,AuxUiLayout.inventorySlot(0));assertEquals(35,AuxUiLayout.inventorySlot(26));assertEquals(0,AuxUiLayout.inventorySlot(27));assertEquals(8,AuxUiLayout.inventorySlot(35));}

 @Test void highGlobalGuiScaleNeverCollapsesTheLocalTransformToZero(){
  for(int gui:new int[]{1,2,3,4,8})for(int pixels:new int[]{1,2}){float scale=AuxUiLayout.renderScale(pixels,gui);assertTrue(scale>0);assertEquals(pixels,scale*gui,.0001);}
  assertEquals(.5f,AuxUiLayout.renderScale(2,4));
 }
 @Test void actualWindowsKeepIntegralTextPixelsAndPanelsInsideTheWindow(){
  for(int[] screen:new int[][]{{1828,952},{1280,800},{960,600},{800,500},{640,400},{3840,2160}}){
   int scale=AuxUiLayout.pixelScale(screen[0],screen[1]);assertTrue(scale==1||scale==2);
   for(int preferred:new int[]{340,390,430,640})assertTrue(AuxUiLayout.panelWidth(screen[0]/scale,preferred)<=screen[0]/scale-16);
   for(int preferred:new int[]{150,240,330,380})assertTrue(AuxUiLayout.panelHeight(screen[1]/scale,preferred)<=screen[1]/scale-16);
  }
 }
 @Test void pagesRemainBoundedAfterRowsAreDeletedOrWindowIsResized(){
  assertEquals(0,AuxUiLayout.page(50,0,1));assertEquals(2,AuxUiLayout.page(50,12,4));assertEquals(0,AuxUiLayout.page(-1,12,4));
  assertEquals(1,AuxUiLayout.rows(226,108,66,50));assertTrue(AuxUiLayout.compact(376));assertFalse(AuxUiLayout.compact(640));
 }
}
