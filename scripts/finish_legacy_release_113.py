from pathlib import Path
import json
R=Path(__file__).resolve().parents[1]/'versions/1.1.3'
for version in ['1.21.1','1.20.4','1.20.1']:
 P=R/('mc'+version)
 p=P/'src/client/java/cn/quit5700/light/client/ClientDaylightState.java';s=p.read_text(encoding='utf-8').replace('minecraft.level.setSectionRangeDirty(next.x,next.y,next.z,next.x,next.y,next.z)','minecraft.levelRenderer.setSectionDirty(next.x,next.y,next.z)');p.write_text(s,encoding='utf-8')
 if version.startswith('1.20'):
  for rel in ['src/main/java/cn/quit5700/light/light/RealAreaLightState.java','src/main/java/cn/quit5700/light/light/VariableLightTaskState.java','src/main/java/cn/quit5700/light/client/VariableConstantLightScreen.java']:
   p=P/rel;s=p.read_text(encoding='utf-8').replace('scans.removeFirst()','scans.remove(0)').replace('tasks.removeFirst()','tasks.remove(0)').replace('tasks.addFirst(', 'tasks.add(0,').replace('.maxWidth(200)','');p.write_text(s,encoding='utf-8')
 p=P/'src/client/java/cn/quit5700/light/client/LegacySwitchIcons.java'
 s='''package cn.quit5700.light.client;
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
        return ((net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager)Minecraft.getInstance().getModelManager()).getModel(LightMod.id("item/"+id.getPath()+"_inventory"));
    }
}
'''
 if version!='1.21.1':s=s.replace('((net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager)Minecraft.getInstance().getModelManager())','Minecraft.getInstance().getModelManager()')
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding='utf-8')
 p=P/'src/client/java/cn/quit5700/light/mixin/client/SwitchIconMixin.java';p.write_text('''package cn.quit5700.light.mixin.client;
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
''',encoding='utf-8')
 p=P/'src/main/java/cn/quit5700/light/client/LightClient.java';s=p.read_text(encoding='utf-8').replace('        ClientDaylightState.initialize();','        LegacySwitchIcons.initialize();\n        ClientDaylightState.initialize();');p.write_text(s,encoding='utf-8')
 p=P/'src/main/resources/quick_build.client.mixins.json';d=json.loads(p.read_text(encoding='utf-8'));d['client'].append('SwitchIconMixin');p.write_text(json.dumps(d,indent=2)+'\n',encoding='utf-8')
