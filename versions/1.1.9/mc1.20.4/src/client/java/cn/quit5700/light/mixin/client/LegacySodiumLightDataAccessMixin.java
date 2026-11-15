package cn.quit5700.light.mixin.client;

import cn.quit5700.light.client.ClientDaylightState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.model.light.data.LightDataAccess", remap = false)
public abstract class LegacySodiumLightDataAccessMixin {
    private static final int FULL_BRIGHT_DATA = 0x11000FFF;

    @Inject(method = "compute(III)I", at = @At("HEAD"), cancellable = true, remap = false)
    private void quickBuild$localDaylight(int x, int y, int z,
                                          CallbackInfoReturnable<Integer> callback) {
        if (ClientDaylightState.isFullBright(x, y, z)) callback.setReturnValue(FULL_BRIGHT_DATA);
    }
}
