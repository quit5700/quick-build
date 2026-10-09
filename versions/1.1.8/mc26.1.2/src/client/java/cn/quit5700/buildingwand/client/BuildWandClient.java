package cn.quit5700.buildingwand.client;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.logic.BuildRequestQueue;
import cn.quit5700.buildingwand.network.BuildNetworking;
import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.util.BuildWandData;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomModelData;
import java.util.*;
public final class BuildWandClient {
 private record Click(int action,BlockPos pos) {}
 private static final BuildRequestQueue<Click> requests=new BuildRequestQueue<>();
 private static long revision,tick;private static int stage,activeSlot=-1;
 private static BlockPos first=BlockPos.ZERO,second=BlockPos.ZERO;
 private static String dimension="",activeWand="";private static boolean active;
 private static DeleteBox box;private static final Set<String> flashing=new HashSet<>();
 public static DeleteBox box(){return box;}
 public static void register(){
  BuildWarningHud.register();
  ClientPlayNetworking.registerGlobalReceiver(BuildNetworking.State.ID,(p,c)->c.client().execute(()->{
   var mc=c.client();if(mc.level==null||!p.dimension().equals(mc.level.dimension().identifier().toString()))return;
   if(p.stage()>0)flashing.add(p.wand());else flashing.remove(p.wand());
   if(mc.player==null||!active||!mc.player.getMainHandItem().is(ModItems.BUILDING_WAND)||slot(mc)!=activeSlot)return;
   if(p.sequence()!=requests.waiting())return;
   String id=BuildWandData.id(mc.player.getMainHandItem());
   if((!id.isEmpty()&&!id.equals(p.wand()))||(!activeWand.isEmpty()&&!activeWand.equals(p.wand())))return;
   if(!requests.acknowledge(p.sequence()))return;
   activeWand=p.wand();revision=p.revision();stage=p.stage();first=p.first();second=p.second();
   if(p.mode()!=3||p.material().equals("minecraft:air"))BuildWarningHud.clear();else BuildWarningHud.show(p.warning());
   BuildWandData.synchronize(mc.player.getMainHandItem(),p.wand(),p.mode(),p.material(),p.filter());
  }));
  ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{clear();requests.reset();});
  ClientTickEvents.END_CLIENT_TICK.register(c->{tick++;if(c.level==null||c.player==null){clear();return;}world(c);for(int n=0;n<c.player.getInventory().getContainerSize();n++){var s=c.player.getInventory().getItem(n);if(!s.is(ModItems.BUILDING_WAND))continue;boolean dim=flashing.contains(BuildWandData.id(s))&&tick%40<20;var old=s.getOrDefault(DataComponents.CUSTOM_MODEL_DATA,CustomModelData.EMPTY);boolean prev=old.flags().size()>6&&old.flags().get(6);if(prev!=dim){var flags=new ArrayList<>(old.flags());while(flags.size()<=6)flags.add(false);flags.set(6,dim);s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(old.floats(),List.copyOf(flags),old.strings(),old.colors()));}}});
 }
 public static void cancelAllState(){clear();}
 private static void clear(){BuildWarningHud.clear();revision=0;stage=0;dimension="";activeWand="";activeSlot=-1;active=false;requests.deactivate();box=null;flashing.clear();}
 private static void world(Minecraft c){String next=c.level.dimension().identifier().toString();if(!dimension.equals(next)){clear();dimension=next;}}
 public static void inactive(){BuildWarningHud.clear();active=false;activeSlot=-1;requests.deactivate();box=null;}
 private static int slot(Minecraft c){return c.player.getInventory().getSelectedSlot();}
 public static void tick(Minecraft c,BlockPos target,BlockPos hit,boolean left,boolean right,boolean shift){
  world(c);String id=BuildWandData.id(c.player.getMainHandItem());int slot=slot(c);
  if(!active||slot!=activeSlot||(!id.isEmpty()&&!id.equals(activeWand))){
   active=true;stage=0;activeWand=id;activeSlot=slot;requests.activate();
   // Fabric's end-tick action can run before vanilla sends the hotbar switch.
   c.getConnection().send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(slot));
  }
  if(!c.mouseHandler.isMouseGrabbed()){box=null;WandPreviewState.clear();return;}
  if(right){if(shift&&hit==null)requests.enqueue(new Click(BuildNetworking.CLEAR,BlockPos.ZERO));else requests.enqueue(new Click(shift?BuildNetworking.SELECT:BuildNetworking.RIGHT,shift?hit:target));}
  else if(left)requests.enqueue(new Click(BuildNetworking.LEFT,target));
  var dispatch=requests.next(tick);
  if(dispatch!=null){var click=dispatch.query()?new Click(BuildNetworking.QUERY,BlockPos.ZERO):dispatch.action();ClientPlayNetworking.send(new BuildNetworking.Request(click.action(),click.pos(),revision,dispatch.sequence(),activeWand,slot));}
  if(stage==2){box=DeleteBox.between(first.getX(),first.getY(),first.getZ(),second.getX(),second.getY(),second.getZ());WandPreviewState.clear();}
  else{box=null;WandPreviewState.set(stage==1?List.of(first,target):List.of(target),WandPreviewState.PreviewColor.WHITE);}
 }
}
