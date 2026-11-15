package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.registry.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.phys.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.*;

public final class AuxClient {
 public static CompoundTag state=new CompoundTag();public static final Set<String> selected=new LinkedHashSet<>();public static String active="";public static AuxScreens.Base ui;
 public static BlockPos first,second;public static List<AuxRow> preview=List.of();public static CompoundTag imported;
 private static boolean wasAttack,wasUse,held,escape;private static String currentDimension="";private static boolean transform,attached,translate;
 private static List<AuxRow> original=List.of();private static BlockPos connectionNormal,pivot,delta=BlockPos.ZERO;private static Vec3 start;private static int axis=1,turns,baseTurns,distance=8;private static BlockPos moveBase=BlockPos.ZERO;private static float yaw,pitch;private static ListTag rotations=new ListTag();private static double startAngle;
 private static List<AuxRow> parsed=List.of(),importBase=List.of();
 private static BlockPos wheel=BlockPos.ZERO;
 private static final AuxNet.Inbox inbox=new AuxNet.Inbox();
 public static void register(){ClientPlayNetworking.registerGlobalReceiver(AuxNet.Part.ID,(packet,context)->Minecraft.getInstance().execute(()->{try{var data=inbox.accept(packet);if(data!=null)receive(data.getStringOr("Action",""),data.getCompoundOrEmpty("Data"));}catch(Exception e){if(Minecraft.getInstance().player!=null)Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.translatable("aux.q5.failed"),false);}}));ClientTickEvents.END_CLIENT_TICK.register(AuxClient::tick);ClientPlayConnectionEvents.JOIN.register((handler,sender,mc)->send("query",new CompoundTag()));ClientPlayConnectionEvents.DISCONNECT.register((handler,mc)->{state=new CompoundTag();parsed=List.of();selected.clear();active="";cancel();first=null;second=null;held=false;});AuxRenderer.register();}
 public static List<AuxRow> rows(){var level=Minecraft.getInstance().level;if(level==null)return List.of();String dimension=level.dimension().location().toString();return parsed.stream().filter(row->row.data.getStringOr("Dim",dimension).equals(dimension)).toList();}
 public static void startImport(CompoundTag data){var missing=AuxFiles.missing(data);if(!missing.isEmpty()&&Minecraft.getInstance().player!=null)Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.translatable("aux.q5.missing_list",String.join(", ",missing)),false);imported=data;importBase=AuxFiles.preview(data,BlockPos.ZERO);}

 public static void send(String action,CompoundTag n){for(var part:AuxNet.packets(action,n))ClientPlayNetworking.send(part);}
 public static CompoundTag ids(Collection<String> ids){var n=new CompoundTag();n.put("Ids",AuxServer.strings(ids));return n;}
 public static CompoundTag id(String id){var n=new CompoundTag();n.putString("Id",id);return n;}
 private static void receive(String action,CompoundTag data){
  if(data.contains("Rows")){state=data;var parsedRows=new ArrayList<AuxRow>();for(var raw:data.getListOrEmpty("Rows"))parsedRows.add(new AuxRow((CompoundTag)raw));parsed=List.copyOf(parsedRows);}else data.forEach((key,value)->state.put(key,value));var ids=new HashSet<String>();for(var row:rows())ids.add(row.id());selected.retainAll(ids);if(!ids.contains(active))active=rows().isEmpty()?"":rows().getLast().id();
  var mc=Minecraft.getInstance();if(action.equals("open"))mc.setScreen(new AuxScreens.Main());else if(action.equals("box"))mc.setScreen(new AuxScreens.Box(data.getLongOr("OpenBox",0)));else if(ui instanceof AuxScreens.Main||ui instanceof AuxScreens.Box)ui.refresh();
 }
 public static BlockPos target(Minecraft mc){return cn.quit5700.buildingwand.client.ClientWandController.buildingSelectionTarget(mc);}
 private static void tick(Minecraft mc){
  boolean holding=mc.player!=null&&mc.level!=null&&mc.player.getMainHandItem().is(ModItems.AUX_WAND);
  if(!holding){if(held){cancel();}held=false;wasAttack=mc.options.keyAttack.isDown();wasUse=mc.options.keyUse.isDown();return;}
  String dimension=mc.level.dimension().location().toString();if(!currentDimension.equals(dimension)){currentDimension=dimension;held=false;active="";selected.clear();first=null;second=null;cancel();}
  if(!held){send("query",new CompoundTag());held=true;}
  boolean attack=mc.options.keyAttack.isDown(),use=mc.options.keyUse.isDown();boolean left=attack&&!wasAttack,right=use&&!wasUse;wasAttack=attack;wasUse=use;
  if(ui!=null)return;
  boolean esc=InputConstants.isKeyDown(mc.getWindow().getWindow(),InputConstants.KEY_ESCAPE);if(esc&&!escape)cancel();escape=esc;
  if(mc.screen!=null)return;
  if(imported!=null){var at=target(mc);var imports=new ArrayList<AuxRow>();for(var source:importBase){var copy=source.copy();copy.translate(at.getX(),at.getY(),at.getZ());imports.add(copy);}preview=imports;if(left){var n=new CompoundTag();n.put("Blueprint",imported);put(n,at);send("import",n);imported=null;preview=List.of();}else if(right){imported=null;preview=List.of();}return;}
  if(transform){
   if(!attached){if(left){BlockPos hit=pick(mc,original);if(hit!=null){pivot=AuxFacing.centerBlock(original);connectionNormal=AuxFacing.connectionNormal(original,hit,mc.player.getEyePosition(),mc.player.getEyePosition().add(mc.player.getLookAngle().scale(64)));start=mc.player.position();yaw=mc.player.getYRot();pitch=mc.player.getXRot();startAngle=angle(mc.player.getEyePosition().subtract(Vec3.atCenterOf(pivot)));attached=true;hint("attached");}}else if(right)cancel();return;}
   if(right){translate=!translate;start=mc.player.position();yaw=mc.player.getYRot();pitch=mc.player.getXRot();startAngle=angle(mc.player.getEyePosition().subtract(Vec3.atCenterOf(pivot)));if(translate){moveBase=delta;wheel=BlockPos.ZERO;}else baseTurns=turns;hint(translate?"mode_move":"mode_rotate");}
   if(translate){Vec3 movement=mc.player.position().subtract(start);delta=new BlockPos((int)Math.round(movement.x),(int)Math.round(movement.y),(int)Math.round(movement.z)).offset(wheel).offset(moveBase);}
   else {rotations=AuxFacing.steps(connectionNormal,AuxFacing.direction(mc.player.getEyePosition().subtract(Vec3.atCenterOf(pivot.offset(delta)))));turns=0;}

   var transformed=new ArrayList<AuxRow>();for(var r:original){var copy=r.copy();AuxRotationGesture.apply(copy,rotations,pivot);copy.rotate(axis,pivot,turns);copy.translate(delta.getX(),delta.getY(),delta.getZ());transformed.add(copy);}preview=transformed;
   if(left)commitTransform();return;
  }
  if(right){var n=new CompoundTag();n.putBoolean("Open",true);send("query",n);return;}
  if(!left)return;
  if(state.getBooleanOr("Micro",false)){var row=rows().stream().filter(r->r.id().equals(active)).toList();var hit=pick(mc,row);if(hit!=null){var n=id(active);put(n,hit);send("micro",n);}return;}
  if(active.isEmpty()){mc.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("aux.q5.add_first"),true);return;}
  BlockPos at=target(mc);if(second!=null){first=null;second=null;}if(first==null){first=at;second=null;hint("first_next");}else{var n=id(active);put(n,first);n.putInt("X2",at.getX());n.putInt("Y2",at.getY());n.putInt("Z2",at.getZ());send("create",n);second=at;}
 }
 public static void put(CompoundTag n,BlockPos p){n.putInt("X",p.getX());n.putInt("Y",p.getY());n.putInt("Z",p.getZ());}
 private static BlockPos pick(Minecraft mc,List<AuxRow> rows){Vec3 eye=mc.player.getEyePosition(),look=mc.player.getLookAngle();for(double length=0;length<64;length+=.1){var pos=BlockPos.containing(eye.add(look.scale(length)));for(var row:rows){if(!row.data.getBooleanOr("Visible",true))continue;int[] local=row.local(pos);if(row.contains(local[0],local[1],local[2])){boolean boundary=false;for(var direction:net.minecraft.core.Direction.values()){var next=row.local(pos.relative(direction));if(!row.contains(next[0],next[1],next[2])){boundary=true;break;}}if(boundary)return pos;}}}return null;}
 public static void beginTransform(){original=rows().stream().filter(r->r.data.getBooleanOr("Placed",false)&&selected.contains(r.id())).map(AuxRow::copy).toList();if(original.isEmpty())return;first=null;second=null;baseTurns=0;rotations=new ListTag();moveBase=BlockPos.ZERO;wheel=BlockPos.ZERO;transform=true;attached=false;translate=false;turns=0;delta=BlockPos.ZERO;Minecraft.getInstance().setScreen(null);hint("connect_next");}
 private static double angle(Vec3 p){return axis==0?Math.atan2(p.y,p.z):axis==1?Math.atan2(p.z,p.x):Math.atan2(p.y,p.x);}
 public static boolean scroll(double amount){if(!transform||amount==0)return false;if(translate&&attached){Vec3 d=Minecraft.getInstance().player.getLookAngle().scale(amount>0?1:-1);wheel=wheel.offset((int)Math.round(d.x),(int)Math.round(d.y),(int)Math.round(d.z));}return true;}
 public static void hint(String key,Object... args){var player=Minecraft.getInstance().player;if(player!=null)player.displayClientMessage(net.minecraft.network.chat.Component.translatable("aux.q5."+key,args),false);}
 public static void freezeRotation(){if(turns!=0){var step=new CompoundTag();step.putInt("Axis",axis);step.putInt("Turns",turns);rotations.add(step);}turns=0;baseTurns=0;var mc=Minecraft.getInstance();yaw=mc.player.getYRot();pitch=mc.player.getXRot();startAngle=angle(mc.player.getEyePosition().subtract(Vec3.atCenterOf(pivot)));}
 public static void commitTransform(){if(!transform||!attached)return;freezeRotation();var n=ids(selected);n.put("Rotations",rotations.copy());n.putInt("Axis",axis);n.putInt("Turns",0);n.putInt("PX",pivot.getX());n.putInt("PY",pivot.getY());n.putInt("PZ",pivot.getZ());n.putInt("DX",delta.getX());n.putInt("DY",delta.getY());n.putInt("DZ",delta.getZ());send("transform",n);cancel();}
 public static Vec3 rayEnd(){if(!transform)return null;var mc=Minecraft.getInstance();return attached&&pivot!=null?Vec3.atCenterOf(pivot.offset(delta)):mc.player.getEyePosition().add(mc.player.getLookAngle().scale(64));}
 public static void cancel(){transform=false;attached=false;original=List.of();preview=List.of();imported=null;importBase=List.of();wheel=BlockPos.ZERO;moveBase=BlockPos.ZERO;baseTurns=0;turns=0;rotations=new ListTag();delta=BlockPos.ZERO;}
 public static List<AuxRow> drawing(){
  var all=new ArrayList<AuxRow>();if(state.getBooleanOr("Shown",true)){if(!preview.isEmpty()){for(var r:rows())if(!transform||!selected.contains(r.id()))all.add(r);all.addAll(preview);}else all.addAll(rows());}
  var mc=Minecraft.getInstance();if(mc.level!=null)all.addAll(BuildProjection.rows(state,mc.level.dimension().location().toString()));return all;
 }
}
