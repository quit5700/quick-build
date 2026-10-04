from pathlib import Path
import re,json
r=Path.cwd();root=r/'versions/1.1.4'
for v in ['1.21.4','1.21.1','1.20.4','1.20.1']:
 d=root/('mc'+v)
 for f in (d/'src/main/java/cn/quit5700/buildingwand').rglob('*.java'):
  s=f.read_text(encoding='utf-8');s=re.sub(r'\.getStringOr\(([^,()]+),""\)',r'.getString(\1)',s);s=re.sub(r'\.getIntOr\(([^,()]+),0\)',r'.getInt(\1)',s)
  if v!='1.21.4':s=s.replace('BuiltInRegistries.BLOCK.getValue(', 'BuiltInRegistries.BLOCK.get(')
  if v.startswith('1.20.'):
   s=s.replace('ResourceLocation.fromNamespaceAndPath(', 'new ResourceLocation(').replace('ResourceLocation.parse(', 'new ResourceLocation(')
   if f.name in ['BuildWandData.java','DeleteWandData.java']:
    s=s.replace('import net.minecraft.core.component.DataComponents;','').replace('import net.minecraft.world.item.component.CustomData;','')
    s=re.sub(r'(\w+)\.getOrDefault\(DataComponents.CUSTOM_DATA,CustomData.EMPTY\).copyTag\(\)',r'\1.getOrCreateTag()',s).replace('CustomData.update(DataComponents.CUSTOM_DATA,','WandTagData.update(')
    s=s.replace('stack.hurtAndBreak(payment.damage(),(net.minecraft.server.level.ServerLevel)owner.level(),owner,item->{})','stack.hurtAndBreak(payment.damage(),owner,p->p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND))')
  f.write_text(s,encoding='utf-8')
 if v.startswith('1.20.'):
  f=d/'src/main/java/cn/quit5700/buildingwand/util/WandTagData.java';f.write_text('''package cn.quit5700.buildingwand.util;
public final class WandTagData {public static void update(net.minecraft.world.item.ItemStack stack,java.util.function.Consumer<net.minecraft.nbt.CompoundTag> action){action.accept(stack.getOrCreateTag());}}
''',encoding='utf-8')
 if v=='1.21.4':continue
 client=d/'src/client/java/cn/quit5700/buildingwand/client'
 f=client/'BuildWandClient.java';s=f.read_text(encoding='utf-8');start=s.index('var old=s.getOrDefault(DataComponents.CUSTOM_MODEL_DATA');end=s.index('}}});',start);s=s[:start]+'LegacyWandFlash.set(s,dim);'+s[end+1:];s=s.replace('import net.minecraft.core.component.DataComponents;','').replace('import net.minecraft.world.item.component.CustomModelData;','');f.write_text(s,encoding='utf-8')
 f=client/'DeleteWandClient.java';s=f.read_text(encoding='utf-8');a=s.index('        CustomModelData original=');b=s.index('\n    }',a);s=s[:a]+'        LegacyWandFlash.set(stack,dim);'+s[b:];s=s.replace('import net.minecraft.core.component.DataComponents;','').replace('import net.minecraft.world.item.component.CustomModelData;','');f.write_text(s,encoding='utf-8')
 f=client/'DeleteMeshRefresh.java';s=f.read_text(encoding='utf-8').replace('client.level.setSectionRangeDirty(refresh.x()-1,refresh.minY()-1,refresh.z()-1,refresh.x()+1,refresh.maxY()+1,refresh.z()+1);','{for(int x=refresh.x()-1;x<=refresh.x()+1;x++)for(int z=refresh.z()-1;z<=refresh.z()+1;z++)for(int y=refresh.minY()-1;y<=refresh.maxY()+1;y++)client.levelRenderer.setSectionDirty(x,y,z);}') ;f.write_text(s,encoding='utf-8')
 f=client/'LegacyWandFlash.java';f.write_text('''package cn.quit5700.buildingwand.client;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import cn.quit5700.buildingwand.registry.ModItems;
import java.util.*;
public final class LegacyWandFlash {
 private static final Map<ItemStack,Boolean> DIM=Collections.synchronizedMap(new WeakHashMap<>());
 private static final ThreadLocal<Boolean> GUI=ThreadLocal.withInitial(()->false);
 public static void set(ItemStack stack,boolean dim){DIM.put(stack,dim);}
 public static void context(ItemDisplayContext context){GUI.set(context==ItemDisplayContext.GUI);}
 public static void finish(){GUI.remove();}
 public static void initialize(){Minecraft.getInstance().getItemColors().register((stack,index)->GUI.get()&&DIM.getOrDefault(stack,false)?0x888888:0xFFFFFF,ModItems.BUILDING_WAND,ModItems.DELETE_WAND);}
}
''',encoding='utf-8')
 f=d/'src/client/java/cn/quit5700/buildingwand/BuildingWandClient.java';s=f.read_text(encoding='utf-8').replace('        ClientWandController.register();','        ClientWandController.register();\n        cn.quit5700.buildingwand.client.LegacyWandFlash.initialize();');f.write_text(s,encoding='utf-8')
 f=d/'src/client/java/cn/quit5700/light/mixin/client/WandFlashMixin.java';f.write_text('''package cn.quit5700.light.mixin.client;
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
''',encoding='utf-8')
 f=d/'src/main/resources/quick_build.client.mixins.json';data=json.loads(f.read_text(encoding='utf-8'));data['client'].append('WandFlashMixin');f.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('旧版法杖数据、区段刷新和仅物品栏闪烁完成适配，未改服务器存档格式。')
