package cn.quit5700.buildingwand.server;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import java.util.*;
import java.lang.reflect.*;
public final class WandSmokeChecks {
 private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 public static void register(){ServerLifecycleEvents.SERVER_STARTED.register(server->{try{run(server.overworld());System.out.println("Q5_WAND_SMOKE_PASS: wand mode persistence, huge build task codec and owner/wand cancellation");}catch(Throwable e){e.printStackTrace();System.out.println("Q5_WAND_SMOKE_FAIL");}finally{server.halt(false);}});}
 @SuppressWarnings({"rawtypes","unchecked"}) private static void run(ServerLevel w)throws Exception{
  var buildTasks=BuildTaskState.get(w);var owner=UUID.randomUUID();var taskWand=UUID.randomUUID().toString();buildTasks.add(owner,taskWand,Blocks.STONE,new cn.quit5700.buildingwand.logic.DeleteSelection.Point(0,0,0),new cn.quit5700.buildingwand.logic.DeleteSelection.Point(100000,10,100000),2);
  var field=BuildTaskState.class.getDeclaredField("CODEC");field.setAccessible(true);var codec=(com.mojang.serialization.Codec<BuildTaskState>)field.get(null);var encoded=codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,buildTasks).getOrThrow();var restored=codec.parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow();check(restored.busy(owner),"build task did not survive codec");check(!restored.cancel(owner,UUID.randomUUID().toString()),"another wand cancelled task");check(restored.cancel(owner,taskWand)&&!restored.busy(owner),"original wand could not cancel task");buildTasks.cancel(owner,taskWand);
  var wand=new ItemStack(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND);check(cn.quit5700.buildingwand.util.BuildWandData.mode(wand)==0,"default mode wrong");cn.quit5700.buildingwand.util.BuildWandData.mode(wand,2);String id=cn.quit5700.buildingwand.util.BuildWandData.ensure(wand);var copy=wand.copy();check(cn.quit5700.buildingwand.util.BuildWandData.mode(copy)==2&&cn.quit5700.buildingwand.util.BuildWandData.id(copy).equals(id),"wand data not persistent");
 }
}
