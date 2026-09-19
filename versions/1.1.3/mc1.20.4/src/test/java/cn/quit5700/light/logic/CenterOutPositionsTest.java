package cn.quit5700.light.logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CenterOutPositionsTest {
    private int distance(int coordinate,int size) {return Math.max(0,Math.max((size-1)/2-coordinate,coordinate-size/2));}
    @Test void oddEvenThinAndIrregularBoxesAreUniqueCompleteAndLayerOrdered() {
        for(int sx=1;sx<=9;sx++)for(int sy=1;sy<=8;sy++)for(int sz=1;sz<=7;sz++) {
            var visited=new java.util.HashSet<AreaSelectionRules.Point>();int last=-1;
            for(int i=0;i<sx*sy*sz;i++) {
                var p=CenterOutPositions.position(sx,sy,sz,i);
                assertTrue(p.x()>=0 && p.x()<sx && p.y()>=0 && p.y()<sy && p.z()>=0 && p.z()<sz);
                assertTrue(visited.add(p));
                int layer=Math.max(distance(p.x(),sx),Math.max(distance(p.y(),sy),distance(p.z(),sz)));
                assertTrue(layer>=last);last=layer;
            }
            assertEquals(sx*sy*sz,visited.size());
        }
    }
    @Test void bothLightingPassesStartAtSelectionCenterIncludingNegativeCoordinates() {
        var box=LightBox.of(-64,-64,-64,64,64,64);
        var center=new AreaSelectionRules.Point(0,0,0);
        assertEquals(center,RealAreaPolicy.scanPosition(box,0,2));
        assertEquals(center,RealAreaPolicy.scanPosition(box,RealAreaPolicy.centerSparseCount(box),2));
        var even=LightBox.of(-8,-8,-8,7,7,7);
        assertEquals(new AreaSelectionRules.Point(-1,-1,-1),RealAreaPolicy.scanPosition(even,0,2));
    }
    @Test void fullMaximumDensePassCoversAllCellsWithoutDuplicates() {
        int size=129;var seen=new java.util.BitSet(size*size*size);int last=-1;
        for(int i=0;i<size*size*size;i++) {
            var p=CenterOutPositions.position(size,size,size,i);
            int key=(p.x()*size+p.y())*size+p.z();assertFalse(seen.get(key));seen.set(key);
            int layer=Math.max(Math.abs(p.x()-64),Math.max(Math.abs(p.y()-64),Math.abs(p.z()-64)));
            assertTrue(layer>=last);last=layer;
        }
        assertEquals(size*size*size,seen.cardinality());
    }
    @Test void sparseGridRemainsAnchoredToSelectedCenterWithUnequalSides() {
        for(var box:java.util.List.of(LightBox.of(-16,-8,-32,15,7,31),LightBox.of(0,0,0,128,128,128))) {
            int cx=box.minX()+(box.maxX()-box.minX())/2,cy=box.minY()+(box.maxY()-box.minY())/2,cz=box.minZ()+(box.maxZ()-box.minZ())/2;
            int last=-1;var seen=new java.util.HashSet<AreaSelectionRules.Point>();
            for(int i=0;i<RealAreaPolicy.centerSparseCount(box);i++) {
                var p=RealAreaPolicy.scanPosition(box,i,2);assertTrue(seen.add(p));
                assertTrue(box.contains(p.x(),p.y(),p.z()));
                int layer=Math.max(Math.abs(p.x()-cx),Math.max(Math.abs(p.y()-cy),Math.abs(p.z()-cz)));
                assertTrue(layer>=last);last=layer;
            }
        }
    }
}
