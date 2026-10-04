package cn.quit5700.buildingwand.client;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.network.BuildNetworking;
import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.util.BuildWandData;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;


import java.util.*;
public final class BuildWandClient {
 private static long sequence,revision,tick;private static int stage;private static BlockPos first=BlockPos.ZERO,second=BlockPos.ZERO;private static String dimension="",activeWand="";private static boolean active,pending;private static DeleteBox box;private static final Set<String> flashing=new HashSet<>();
 public static DeleteBox box(){return box;}
 public static void register(){
  cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(BuildNetworking.State.ID,BuildNetworking.State::read,(p,c)->c.client().execute(()->{
   var mc=c.client();if(mc.level==null||!p.dimension().equals(mc.level.dimension().location().toString()))return;
   if(p.stage()>0)flashing.add(p.wand());else flashing.remove(p.wand());
   if(p.sequence()!=sequence||mc.player==null||!mc.player.getMainHandItem().is(ModItems.BUILDING_WAND))return;
   var id=BuildWandData.id(mc.player.getMainHandItem());if(!id.isEmpty()&&!id.equals(p.wand()))return;
   activeWand=p.wand();revision=p.revision();stage=p.stage();first=p.first();second=p.second();pending=false;
  }));
  ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{clear();sequence=0;});
  ClientTickEvents.END_CLIENT_TICK.register(c->{tick++;if(c.level==null||c.player==null){clear();return;}String next=c.level.dimension().location().toString();if(!dimension.equals(next)){clear();dimension=next;}for(int n=0;n<c.player.getInventory().getContainerSize();n++){var s=c.player.getInventory().getItem(n);if(!s.is(ModItems.BUILDING_WAND))continue;boolean dim=flashing.contains(BuildWandData.id(s))&&tick%40<20;LegacyWandFlash.set(s,dim);}});
 }
 public static void cancelAllState(){clear();}
 private static void clear(){revision=0;stage=0;dimension="";activeWand="";active=false;pending=false;box=null;flashing.clear();}
 public static void inactive(){active=false;box=null;}
 public static void tick(Minecraft c,BlockPos target,BlockPos hit,boolean left,boolean right,boolean shift){
  String id=BuildWandData.id(c.player.getMainHandItem());if(!active||(!id.isEmpty()&&!id.equals(activeWand))){active=true;stage=0;activeWand=id;send(BuildNetworking.QUERY,BlockPos.ZERO);}
  if(!c.mouseHandler.isMouseGrabbed()){box=null;WandPreviewState.clear();return;}
  if(!pending){if(right){if(shift&&hit==null)send(BuildNetworking.CLEAR,BlockPos.ZERO);else send(shift?BuildNetworking.SELECT:BuildNetworking.RIGHT,shift?hit:target);}else if(left)send(BuildNetworking.LEFT,target);}
  if(stage==2){box=DeleteBox.between(first.getX(),first.getY(),first.getZ(),second.getX(),second.getY(),second.getZ());WandPreviewState.clear();}
  else{box=null;WandPreviewState.set(stage==1?List.of(first,target):List.of(target),WandPreviewState.PreviewColor.WHITE);}
 }
 private static void send(int action,BlockPos pos){pending=true;cn.quit5700.buildingwand.client.LegacyWandClientPackets.send(new BuildNetworking.Request(action,pos,revision,++sequence));}
}
