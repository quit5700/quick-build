package cn.quit5700.light.mixin.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(ItemRenderer.class)
public abstract class SwitchIconMixin {
    @ModifyVariable(method="render",at=@At("HEAD"),argsOnly=true)
    private BakedModel quickBuild$inventoryIcon(BakedModel original,ItemStack stack,ItemDisplayContext context,boolean left,PoseStack pose,MultiBufferSource buffers,int light,int overlay,BakedModel model){
        return cn.quit5700.light.client.LegacySwitchIcons.model(original,stack,context);
    }
}
