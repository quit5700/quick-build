package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class StructureLayoutTest {
 @Test void fortyNineSolidLeavesLastCell(){var b=new DeleteBox(0,0,0,48,0,0);int groups=0,air=0;for(int x=0;x<=48;x++){var p=StructureLayout.pair(b,x,0,0,0,1);if(p.anchor()){if(p.remainder())air++;else groups++;}}assertEquals(24,groups);assertEquals(1,air);assertTrue(StructureLayout.pair(b,48,0,0,0,1).remainder());}
 @Test void negativeDirectionLeavesOppositeEnd(){var b=new DeleteBox(-48,0,0,0,0,0);assertTrue(StructureLayout.pair(b,-48,0,0,0,1,-1).remainder());var first=StructureLayout.pair(b,0,0,0,0,1,-1);assertTrue(first.anchor());assertEquals(-1,first.mx());assertFalse(StructureLayout.pair(b,-1,0,0,0,1,-1).anchor());}
 @Test void hollowPairsIntoInteriorAndKeepsFarBoundary(){var b=new DeleteBox(0,0,0,5,4,4);assertTrue(StructureLayout.pair(b,0,2,2,0,2).included());assertTrue(StructureLayout.pair(b,4,2,2,0,2).included());assertFalse(StructureLayout.pair(b,2,2,2,0,2).included());assertTrue(StructureLayout.pair(b,2,0,2,0,2).included());}
 @Test void oddHollowStillBuildsWithLastCellEmpty(){var b=new DeleteBox(0,0,0,48,4,48);assertTrue(StructureLayout.fits(b,false,2));assertEquals(0,StructureLayout.axis(b,false,2));assertTrue(StructureLayout.pair(b,48,0,48,0,2).remainder());}
 @Test void verticalPairsStayComplete(){var b=new DeleteBox(0,0,0,0,48,0);var p=StructureLayout.pair(b,0,48,0,1,2,-1);assertEquals(47,p.my());assertTrue(p.anchor());assertTrue(StructureLayout.pair(b,0,0,0,1,2,-1).remainder());}
 @Test void extremeCoordinatesDoNotOverflow(){var b=new DeleteBox(Integer.MIN_VALUE,0,0,Integer.MAX_VALUE,0,0);assertEquals(4294967296L,StructureLayout.length(b,0));var p=StructureLayout.pair(b,Integer.MAX_VALUE,0,0,0,1,-1);assertEquals(Integer.MAX_VALUE-1,p.mx());assertFalse(p.remainder());}

 @Test void hollowTotalCountsActualThicknessAndExcludesAir(){var b=new DeleteBox(0,0,0,5,4,4);assertEquals(java.math.BigInteger.valueOf(132),StructureLayout.total(b,false,2));var odd=new DeleteBox(0,0,0,48,0,48);assertEquals(java.math.BigInteger.valueOf(2352),StructureLayout.total(odd,false,2));}
 @Test void totalMatchesEnumeratedPairsInEveryDirection(){for(int x=1;x<=7;x++)for(int y=1;y<=5;y++)for(int z=1;z<=7;z++)for(boolean vertical:new boolean[]{false,true})for(int mode:new int[]{1,2,3})for(int direction:new int[]{-1,1}){var b=new DeleteBox(0,0,0,x-1,y-1,z-1);int axis=StructureLayout.axis(b,vertical,mode),count=0;for(int xx=0;xx<x;xx++)for(int yy=0;yy<y;yy++)for(int zz=0;zz<z;zz++){var p=StructureLayout.pair(b,xx,yy,zz,axis,mode,direction);if(p.anchor()&&p.included()&&!p.remainder())count+=2;}assertEquals(java.math.BigInteger.valueOf(count),StructureLayout.total(b,vertical,mode),b+" mode="+mode+" vertical="+vertical+" direction="+direction);}}

}
