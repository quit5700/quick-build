package cn.quit5700.buildingwand.aux;
import net.minecraft.core.BlockPos;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AuxTransformSelectionTest {
 @BeforeAll static void version(){SharedConstants.tryDetectVersion();}
 private AuxRow row(){return new AuxRow(AuxRow.create(BlockPos.ZERO,new BlockPos(4,3,2)));}
 @Test void currentPlacedShapeEntersTransformWithoutCheckbox(){var a=row();assertEquals(List.of(a.id()),AuxTransformSelection.choose(List.of(a),Set.of(),a.id()).stream().map(AuxRow::id).toList());}
 @Test void soleShapeIsUsableWhenActiveIdIsStale(){var a=row();assertEquals(1,AuxTransformSelection.choose(List.of(a),Set.of(),"stale").size());}
 @Test void explicitMultiSelectionNeverAddsActiveRow(){var a=row();var b=row();var c=row();assertEquals(Set.of(a.id(),b.id()),new HashSet<>(AuxTransformSelection.choose(List.of(a,b,c),Set.of(a.id(),b.id()),c.id()).stream().map(AuxRow::id).toList()));}
 @Test void selectedBlankRowDoesNotTransformUnselectedShape(){var a=row();var blank=new AuxRow(AuxRow.blank());assertTrue(AuxTransformSelection.choose(List.of(a,blank),Set.of(blank.id()),a.id()).isEmpty());}
 @Test void currentBlankRowDoesNotFallBackToAnotherShape(){var a=row();var blank=new AuxRow(AuxRow.blank());assertTrue(AuxTransformSelection.choose(List.of(a,blank),Set.of(),blank.id()).isEmpty());}
 @Test void ambiguousOrEmptySelectionCannotTransform(){var a=row();var b=row();assertTrue(AuxTransformSelection.choose(List.of(a,b),Set.of(),"stale").isEmpty());assertTrue(AuxTransformSelection.choose(List.of(),Set.of(),"").isEmpty());}
 @Test void returnedRowsAreSnapshots(){var a=row();var chosen=AuxTransformSelection.choose(List.of(a),Set.of(a.id()),a.id()).getFirst();chosen.translate(9,0,0);assertEquals(BlockPos.ZERO,a.world(0,0,0));}
}
