package cn.quit5700.light.mixin.client;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemRenderer.class)
public abstract class WandFlashMixin {
 @Inject(method="render",at=@At("HEAD")) private void q5$begin(ItemStack stack,ItemDisplayContext context,boolean left,PoseStack pose,MultiBufferSource buffers,int light,int overlay,BakedModel model,CallbackInfo ci){cn.quit5700.buildingwand.client.LegacyWandFlash.context(context);}
 @Inject(method="render",at=@At("RETURN")) private void q5$end(ItemStack stack,ItemDisplayContext context,boolean left,PoseStack pose,MultiBufferSource buffers,int light,int overlay,BakedModel model,CallbackInfo ci){cn.quit5700.buildingwand.client.LegacyWandFlash.finish();}
}
