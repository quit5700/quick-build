package cn.quit5700.light.mixin.client;

import cn.quit5700.light.client.ClientDaylightState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    private static final int FULL_BRIGHT = 15_728_880;

    @Inject(method = "getPackedLightCoords", at = @At("HEAD"), cancellable = true)
    private void quickBuild$localDaylight(T entity, float partialTick, CallbackInfoReturnable<Integer> callback) {
        BlockPos probePos = BlockPos.containing(entity.getLightProbePosition(partialTick));
        if (ClientDaylightState.isFullBright(probePos)) callback.setReturnValue(FULL_BRIGHT);
    }
}
