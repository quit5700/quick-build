package cn.quit5700.light.client;
import cn.quit5700.light.LightMod;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;

/** GUI-only model selection; held, dropped and block models remain unchanged. */
public final class LegacySwitchIcons {
    private static final Set<String> NAMES=Set.of("", "_orange","_magenta","_light_blue","_yellow","_lime","_pink","_gray","_light_gray","_cyan","_purple","_blue","_brown","_green","_red","_black").stream().map(s->"redstone_energy_remote_switch"+s).collect(Collectors.toUnmodifiableSet());
    public static void initialize(){ModelLoadingPlugin.register(context->context.addModels(NAMES.stream().map(s->LightMod.id("item/"+s+"_inventory")).toList()));}
    public static BakedModel model(BakedModel original,ItemStack stack,ItemDisplayContext context){
        if(context!=ItemDisplayContext.GUI)return original;
        ResourceLocation id=BuiltInRegistries.ITEM.getKey(stack.getItem());
        if(!id.getNamespace().equals("light") || !NAMES.contains(id.getPath()))return original;
        return Minecraft.getInstance().getModelManager().getModel(LightMod.id("item/"+id.getPath()+"_inventory"));
    }
}
