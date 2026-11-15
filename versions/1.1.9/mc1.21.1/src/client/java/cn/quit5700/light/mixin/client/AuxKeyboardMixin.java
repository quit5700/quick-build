package cn.quit5700.light.mixin.client;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(net.minecraft.client.KeyboardHandler.class)
public abstract class AuxKeyboardMixin {
 @Inject(method="keyPress",at=@At("HEAD"))
 private void q5$escape(long window,int key,int scanCode,int action,int modifiers,CallbackInfo ci){
  if(key==org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE&&action==org.lwjgl.glfw.GLFW.GLFW_PRESS)cn.quit5700.buildingwand.aux.AuxClient.escapePress(window);
 }
}
