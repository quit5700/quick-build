package cn.quit5700.buildingwand.aux;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AuxNativePlanSchemaTest {
 @Test void nativeSavedPlanUsesName(){var n=new CompoundTag();n.putString("Name","minecraft:gold_block");assertEquals("minecraft:gold_block",AuxMaterials.stateId(n));}
 @Test void canonicalBlueprintIdStillAccepted(){var n=new CompoundTag();n.putString("id","minecraft:beacon");assertEquals("minecraft:beacon",AuxMaterials.stateId(n));}
 @Test void canonicalIdTakesPrecedenceAndUnknownStaysEmpty(){var n=new CompoundTag();assertEquals("",AuxMaterials.stateId(n));n.putString("Name","minecraft:stone");n.putString("id","minecraft:oak_log");assertEquals("minecraft:oak_log",AuxMaterials.stateId(n));}
}
