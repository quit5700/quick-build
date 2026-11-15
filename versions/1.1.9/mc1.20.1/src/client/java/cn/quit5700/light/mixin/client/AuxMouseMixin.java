package cn.quit5700.light.mixin.client;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(net.minecraft.client.MouseHandler.class)
public abstract class AuxMouseMixin {
 @Inject(method="onPress",at=@At("HEAD"))
 private void q5$press(long window,int button,int action,int modifiers,CallbackInfo ci){
  if(action==org.lwjgl.glfw.GLFW.GLFW_PRESS)cn.quit5700.buildingwand.aux.AuxClient.mousePress(window,button);
 }
 @Inject(method="onScroll",at=@At("HEAD"),cancellable=true)
 private void q5$scroll(long window,double horizontal,double vertical,CallbackInfo ci){if(cn.quit5700.buildingwand.aux.AuxClient.ui==null&&cn.quit5700.buildingwand.aux.AuxClient.scroll(vertical))ci.cancel();}
}
