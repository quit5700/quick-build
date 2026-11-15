package cn.quit5700.buildingwand.aux;
import org.junit.jupiter.api.*;
import net.minecraft.core.BlockPos;
import static org.junit.jupiter.api.Assertions.*;
class AuxBeaconUnassignedTest {
 @BeforeAll static void detect(){net.minecraft.SharedConstants.tryDetectVersion();}
 private static AuxRow row(){var n=AuxRow.create(BlockPos.ZERO,new BlockPos(8,4,8));n.putInt("Shape",AuxBeacon.SHAPE);return new AuxRow(n);}
 @Test void newAndLegacyImplicitMaterialRowsRemainUnassigned(){var row=row();assertNull(row.material(0,0,0));assertNull(row.material(4,4,4));assertTrue(row.contains(4,4,4));}
 @Test void explicitMaterialIsRetainedAndTopBeaconRemainsIndependent(){var n=row().data.copy();n.putString("Material","minecraft:gold_block");var row=new AuxRow(n);assertEquals("minecraft:gold_block",cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(row.material(0,0,0),"id",""));assertEquals("minecraft:beacon",cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(row.material(4,4,4),"id",""));}
 @Test void clearingMaterialRestoresPureColorForBaseAndBeacon(){var n=row().data.copy();n.putString("Material","minecraft:iron_block");n.putString("Material","");var row=new AuxRow(n);assertNull(row.material(0,0,0));assertNull(row.material(4,4,4));}
}
