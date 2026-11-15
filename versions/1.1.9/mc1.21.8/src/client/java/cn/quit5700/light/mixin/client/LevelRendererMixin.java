package cn.quit5700.light.mixin.client;
import cn.quit5700.light.client.ClientDaylightState;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    private static final int FULL_BRIGHT = 15_728_880;
    @Inject(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I", at = @At("HEAD"), cancellable = true)
    private static void quickBuild$localDaylight(BlockAndTintGetter world, BlockPos pos, CallbackInfoReturnable<Integer> callback) {
        if (ClientDaylightState.isFullBright(pos)) callback.setReturnValue(FULL_BRIGHT);
    }
    @Inject(method = "getLightColor(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At("HEAD"), cancellable = true)
    private static void quickBuild$localDaylightWithState(LevelRenderer.BrightnessGetter brightnessGetter,
                                                           BlockAndTintGetter world, BlockState state, BlockPos pos,
                                                           CallbackInfoReturnable<Integer> callback) {
        if (ClientDaylightState.isFullBright(pos)) callback.setReturnValue(FULL_BRIGHT);
    }
}
