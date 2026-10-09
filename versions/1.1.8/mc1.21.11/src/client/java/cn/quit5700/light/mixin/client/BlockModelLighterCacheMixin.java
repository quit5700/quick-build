package cn.quit5700.light.mixin.client;

import cn.quit5700.light.client.ClientDaylightState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.block.BlockModelLighter$Cache")
public abstract class BlockModelLighterCacheMixin {
    private static final int FULL_BRIGHT = 15_728_880;

    @Inject(method = "getLightCoords", at = @At("HEAD"), cancellable = true)
    private void quickBuild$localDaylight(BlockState state, BlockAndTintGetter world, BlockPos pos,
                                          CallbackInfoReturnable<Integer> callback) {
        if (ClientDaylightState.isFullBright(pos)) callback.setReturnValue(FULL_BRIGHT);
    }

    @Inject(method = "getShadeBrightness", at = @At("HEAD"), cancellable = true)
    private void quickBuild$localDaylightShade(BlockState state, BlockAndTintGetter world, BlockPos pos,
                                               CallbackInfoReturnable<Float> callback) {
        if (ClientDaylightState.isFullBright(pos)) callback.setReturnValue(1.0F);
    }
}
