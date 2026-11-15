package cn.quit5700.light.mixin.client;

import cn.quit5700.light.client.ClientDaylightState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderState.class)
public abstract class BlockEntityRenderStateMixin {
    private static final int FULL_BRIGHT = 15_728_880;

    @Inject(method = "extractBase", at = @At("RETURN"))
    private static void quickBuild$localDaylight(BlockEntity blockEntity, BlockEntityRenderState state,
                                                  ModelFeatureRenderer.CrumblingOverlay breakProgress,
                                                  CallbackInfo callback) {
        if (ClientDaylightState.isFullBright(blockEntity.getBlockPos())) state.lightCoords = FULL_BRIGHT;
    }
}
