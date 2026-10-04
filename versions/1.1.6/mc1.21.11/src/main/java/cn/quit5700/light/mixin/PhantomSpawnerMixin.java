package cn.quit5700.light.mixin;

import cn.quit5700.light.light.VariableLightTaskState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Phantoms use a separate natural-spawn loop, not NaturalSpawner. */
@Mixin(PhantomSpawner.class)
public abstract class PhantomSpawnerMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void quickBuild$protectFromPhantoms(ServerLevel world, Entity entity) {
        if (!VariableLightTaskState.get(world).preventsNaturalMonsterSpawn(world, entity.blockPosition()))
            world.addFreshEntityWithPassengers(entity);
    }
}
