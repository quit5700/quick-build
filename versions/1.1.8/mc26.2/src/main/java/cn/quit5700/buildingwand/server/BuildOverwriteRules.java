package cn.quit5700.buildingwand.server;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
/** Explicit user-approved list; no replaceability or hardness inference. */
public final class BuildOverwriteRules {
 private static final Set<String> PROTECTED=Set.of("minecraft:bedrock","minecraft:command_block","minecraft:chain_command_block","minecraft:repeating_command_block","minecraft:structure_block","minecraft:barrier","minecraft:structure_void","minecraft:jigsaw","minecraft:light","minecraft:test_block","minecraft:test_instance_block","minecraft:end_portal_frame","minecraft:end_portal","minecraft:end_gateway","minecraft:trial_spawner","minecraft:vault");
 public static boolean blocks(BlockState state,boolean creative){return !creative&&PROTECTED.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());}
}
