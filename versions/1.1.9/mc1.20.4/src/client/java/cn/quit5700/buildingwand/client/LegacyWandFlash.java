package cn.quit5700.buildingwand.client;
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
 public static void initialize(){net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register((stack,index)->GUI.get()&&DIM.getOrDefault(stack,false)?0x888888:0xFFFFFF,ModItems.BUILDING_WAND,ModItems.DELETE_WAND);}
}
