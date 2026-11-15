package cn.quit5700.quickbuild.api;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
/** Public API v1. Item ids remain owned and registered by the caller's mod. */
public final class Q5ItemPage {
 public static final int API_VERSION=1;
 private static final ItemPageRegistry REGISTRY=new ItemPageRegistry();
 private Q5ItemPage(){}
 public static void register(String modId,int order,String groupId,List<String> itemIds){REGISTRY.register(modId,order,groupId,itemIds);}
 public static boolean unregister(String modId,String groupId){return REGISTRY.unregister(modId,groupId);}
 /** Called during tab rebuild, after every mod has registered its items. */
 public static void appendTo(List<Item> original,Consumer<Item> output){
  var seen=new HashSet<String>();
  for(Item item:original){String id=BuiltInRegistries.ITEM.getKey(item).toString();if(seen.add(id))output.accept(item);}
  for(String id:REGISTRY.orderedItems(seen)){
   ResourceLocation key=ResourceLocation.parse(id);
   if(BuiltInRegistries.ITEM.containsKey(key)){Item item=BuiltInRegistries.ITEM.get(key);if(item!=null&&item!=Items.AIR)output.accept(item);}
  }
 }
}
