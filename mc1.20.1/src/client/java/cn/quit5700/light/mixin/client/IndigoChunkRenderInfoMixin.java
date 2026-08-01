package cn.quit5700.light.mixin.client;

import cn.quit5700.light.client.ClientDaylightState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.impl.client.indigo.renderer.render.ChunkRenderInfo", remap = false)
public abstract class IndigoChunkRenderInfoMixin {
    private static final int FULL_BRIGHT = 15_728_880;

    @Inject(method = "cachedBrightness", at = @At("HEAD"), cancellable = true, remap = false)
    private void quickBuild$localDaylightBrightness(BlockPos pos, BlockState state,
                                                     CallbackInfoReturnable<Integer> callback) {
        if (ClientDaylightState.isFullBright(pos)) callback.setReturnValue(FULL_BRIGHT);
    }

    @Inject(method = "cachedAoLevel", at = @At("HEAD"), cancellable = true, remap = false)
    private void quickBuild$localDaylightAmbientOcclusion(BlockPos pos, BlockState state,
                                                          CallbackInfoReturnable<Float> callback) {
        if (ClientDaylightState.isFullBright(pos)) callback.setReturnValue(1.0F);
    }
}
