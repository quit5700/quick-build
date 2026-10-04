package cn.quit5700.light.mixin;

import cn.quit5700.light.light.VariableLightTaskState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Inject(method = "isValidSpawnPostitionForType", at = @At("HEAD"), cancellable = true)
    private static void quickBuild$daylightProtection(ServerLevel world, MobCategory category,
            StructureManager structures, ChunkGenerator generator, MobSpawnSettings.SpawnerData spawn,
            BlockPos.MutableBlockPos pos, double distance, CallbackInfoReturnable<Boolean> callback) {
        if (category == MobCategory.MONSTER
                && VariableLightTaskState.get(world).preventsNaturalMonsterSpawn(world, pos))
            callback.setReturnValue(false);
    }
}
