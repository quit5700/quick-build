package cn.quit5700.light.mixin.client;
import cn.quit5700.light.client.ClientDaylightState;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ChestRenderer.class)
public abstract class ChestRendererMixin {
    private static final int FULL_BRIGHT = 15_728_880;
    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("RETURN"))
    private void quickBuild$localDaylightAfterChestCombine(BlockEntity blockEntity, ChestRenderState state, float partialTick,
                                                            Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress,
                                                            CallbackInfo callback) {
        if (ClientDaylightState.isFullBright(blockEntity.getBlockPos())) state.lightCoords = FULL_BRIGHT;
    }
}
