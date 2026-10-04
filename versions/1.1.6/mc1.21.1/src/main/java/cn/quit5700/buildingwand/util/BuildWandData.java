package cn.quit5700.buildingwand.util;
import cn.quit5700.buildingwand.logic.BuildPlan;
import cn.quit5700.buildingwand.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;
public final class BuildWandData {
 public static String id(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("Q5BuildWandId");}
 public static String ensure(ItemStack s){String id=id(s);if(id.isEmpty()){id=UUID.randomUUID().toString();String next=id;CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putString("Q5BuildWandId",next));}return id;}
 public static String modeKey(int mode){return switch(BuildPlan.mode(mode)){case 1->"mode.q5.1";case 2->"mode.q5.2";case 3->"mode.q5.3";default->"mode.q5.0";};}
 public static int mode(ItemStack s){return BuildPlan.mode(s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("Q5BuildMode"));}
 public static void mode(ItemStack s,int mode){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt("Q5BuildMode",BuildPlan.mode(mode)));}
 public static net.minecraft.world.level.block.Block replacementTarget(ItemStack s){
  String id=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("Q5ReplaceTarget");
  var key=net.minecraft.resources.ResourceLocation.tryParse(id);if(key==null)return net.minecraft.world.level.block.Blocks.AIR;
  var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(key);return block==null?net.minecraft.world.level.block.Blocks.AIR:block;
 }
 public static void replacementTarget(ItemStack s,net.minecraft.world.level.block.Block block){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putString("Q5ReplaceTarget",net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString()));}
 public static ItemStack find(ServerPlayer p,String id){for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.is(ModItems.BUILDING_WAND)&&id.equals(id(s)))return s;}return ItemStack.EMPTY;}

 public static void synchronize(ItemStack s,String id,int mode,String material,String filter){
  CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putString("Q5BuildWandId",id));
  mode(s,mode);WandStackData.setSelectedBlock(s,block(material));replacementTarget(s,block(filter));
 }
 private static net.minecraft.world.level.block.Block block(String id){
  var key=net.minecraft.resources.ResourceLocation.tryParse(id);
  if(key==null)return net.minecraft.world.level.block.Blocks.AIR;
  var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(key);
  return block==null?net.minecraft.world.level.block.Blocks.AIR:block;
 }
}
