package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.logic.*;
import cn.quit5700.buildingwand.network.BuildNetworking;
import cn.quit5700.buildingwand.util.*;
import cn.quit5700.buildingwand.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.Blocks;
import java.util.*;
public final class BuildWandServer {
 private static final class Session {final UUID owner;final ServerLevel world;final String wand;final DeleteSelection selection=new DeleteSelection(java.util.concurrent.ThreadLocalRandom.current().nextLong(1,Long.MAX_VALUE/2));Session(UUID owner,ServerLevel world,String wand){this.owner=owner;this.world=world;this.wand=wand;}}
 private static final Map<String,Session> sessions=new HashMap<>();private static final Map<UUID,Long> sequences=new HashMap<>();
 public static void register(){
  ServerTickEvents.END_SERVER_TICK.register(server->{sessions.values().removeIf(s->{var p=server.getPlayerList().getPlayer(s.owner);return p==null||p.level()!=s.world;});for(var world:server.getAllLevels())BuildTaskState.get(world).tick(world);});
  ServerPlayConnectionEvents.DISCONNECT.register((h,s)->{sessions.values().removeIf(v->v.owner.equals(h.player.getUUID()));sequences.remove(h.player.getUUID());});
  ServerLifecycleEvents.SERVER_STOPPING.register(s->{for(var w:s.getAllLevels())BuildTaskState.get(w).release();sessions.clear();sequences.clear();});
 }
 public static void handle(ServerPlayer p,BuildNetworking.Request r){handle(p,r,state->ServerPlayNetworking.send(p,state));}
 static void handle(ServerPlayer p,BuildNetworking.Request r,java.util.function.Consumer<BuildNetworking.State> reply){
  if(r.sequence()<=sequences.getOrDefault(p.getUUID(),0L))return;sequences.put(p.getUUID(),r.sequence());
  var tool=p.getMainHandItem();if(!tool.is(ModItems.BUILDING_WAND)||r.slot()!=p.getInventory().getSelectedSlot())return;if(!r.wand().isEmpty()&&!r.wand().equals(BuildWandData.id(tool)))return;String wand=BuildWandData.ensure(tool);ServerLevel w=(ServerLevel)p.level();String key=p.getUUID()+"/"+wand;
  Session s=sessions.get(key);if(s==null||s.world!=w){s=new Session(p.getUUID(),w,wand);sessions.put(key,s);}var sel=s.selection;var tasks=BuildTaskState.get(w);
  if(r.action()==BuildNetworking.RIGHT){
   if(tasks.cancel(p.getUUID(),wand)){message(p,"message.q5.build_cancelled");sel.cancel();}
   else if(sel.stage()>0){sel.cancel();message(p,"message.q5.selection_cancelled");}
   else{BuildWandData.mode(tool,(BuildWandData.mode(tool)+1)%4);p.displayClientMessage(Component.translatable("message.q5.mode",Component.translatable(BuildWandData.modeKey(BuildWandData.mode(tool)))),true);if(BuildWandData.mode(tool)==3){BuildWandData.replacementTarget(tool,Blocks.AIR);p.sendSystemMessage(Component.translatable("message.q5.replace_hint"));warnMultiple(p);}}
  }else if(r.action()==BuildNetworking.CLEAR){
   if(p.pick(4.0,0.0F,false).getType()==net.minecraft.world.phys.HitResult.Type.MISS){
    clearMaterials(p);
   }
  }else if(r.action()==BuildNetworking.SELECT){
   if(reachable(p,w,r.pos())&&!w.getBlockState(r.pos()).isAir()){
    pickMaterial(p,r.pos());
   }
  }
  else if(r.action()==BuildNetworking.LEFT&&p.mayBuild()&&!p.isSpectator()){
   int mode=BuildWandData.mode(tool);
   if(tasks.busy(p.getUUID()))message(p,"message.q5.build_busy");
   else if(WandStackData.selectedBlock(tool)==Blocks.AIR)message(p,"text.quick_build.109");
   else if(mode==0){if(reachable(p,w,r.pos()))WandActions.placeSingle(p,r.pos());}
   else if(sel.stage()==2&&sel.revision()==r.revision()){
    var a=sel.first();var b=sel.second();var box=DeleteBox.between(a.x(),a.y(),a.z(),b.x(),b.y(),b.z());
    if(!StructurePlacement.fits(WandStackData.selectedBlock(tool),box,mode)){message(p,"message.q5.multiblock_dimensions");}
    else if(valid(w,box)){sel.confirm(r.revision());tasks.add(p.getUUID(),wand,WandStackData.selectedBlock(tool),a,b,mode,mode==3?BuildWandData.replacementTarget(tool):Blocks.AIR);message(p,mode==3?"message.q5.replace_start":"message.q5.build_start");}else message(p,"text.quick_build.099");
   }else if(reachable(p,w,r.pos())&&sel.pick(new DeleteSelection.Point(r.pos().getX(),r.pos().getY(),r.pos().getZ()),r.revision()))message(p,mode==3?(sel.stage()==1?"message.q5.replace_first":"message.q5.replace_confirm"):(sel.stage()==1?"message.q5.build_first":"message.q5.build_confirm"));
  }
  if(r.action()==BuildNetworking.LEFT&&sel.stage()==2)warnMultiple(p);
  reply.accept(state(p,s,r.sequence(),r.action()));p.inventoryMenu.broadcastChanges();
 }
 public static int cancelAll(ServerPlayer p){int count=0;for(var w:((ServerLevel)p.level()).getServer().getAllLevels())count+=BuildTaskState.get(w).cancelAll(p.getUUID());sessions.values().removeIf(s->s.owner.equals(p.getUUID()));return count;}
 static void pickMaterial(ServerPlayer p,BlockPos pos){
  var tool=p.getMainHandItem();var state=p.level().getBlockState(pos);if(state.isAir())return;
  boolean replacement=BuildWandData.mode(tool)==3;
  var selected=WandStackData.selectedBlock(tool);
  boolean refresh=selected==Blocks.AIR||selected==state.getBlock()||ReplacementRules.protectedMaterial((ServerLevel)p.level(),pos,state,p.isCreative());
  if(replacement&&!refresh){BuildWandData.replacementTarget(tool,state.getBlock());p.displayClientMessage(Component.translatable("message.q5.replace_target",Component.translatable(state.getBlock().getDescriptionId())).withStyle(net.minecraft.ChatFormatting.AQUA),true);}
  else{if(replacement)BuildWandData.replacementTarget(tool,Blocks.AIR);WandActions.selectBlock(p,pos);}
  warnMultiple(p);
 }
 static void warnMultiple(ServerPlayer p){
  if(BuildWandData.mode(p.getMainHandItem())==3&&StructurePlacement.multiple(WandStackData.selectedBlock(p.getMainHandItem())))p.sendSystemMessage(Component.translatable("message.q5.multiblock_warning").withStyle(net.minecraft.ChatFormatting.RED));}
 static void clearMaterials(ServerPlayer p){var tool=p.getMainHandItem();WandStackData.setSelectedBlock(tool,Blocks.AIR);if(BuildWandData.mode(tool)==3)BuildWandData.replacementTarget(tool,Blocks.AIR);message(p,BuildWandData.mode(tool)==3?"message.q5.replace_clear":"message.q5.material_clear");}
 private static boolean reachable(ServerPlayer p,ServerLevel w,BlockPos pos){return !w.isOutsideBuildHeight(pos)&&w.getWorldBorder().isWithinBounds(pos)&&w.mayInteract(p,pos)&&p.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))<=36;}
 private static boolean valid(ServerLevel w,DeleteBox b){return !w.isOutsideBuildHeight(b.minY())&&!w.isOutsideBuildHeight(b.maxY())&&w.getWorldBorder().isWithinBounds(new BlockPos(b.minX(),b.minY(),b.minZ()))&&w.getWorldBorder().isWithinBounds(new BlockPos(b.maxX(),b.maxY(),b.maxZ()));}
 public static void message(ServerPlayer p,String key,Object... args){p.displayClientMessage(Component.translatable(key,args),true);}
 private static BlockPos pos(DeleteSelection.Point p){return p==null?BlockPos.ZERO:new BlockPos(p.x(),p.y(),p.z());}
 private static BuildNetworking.State state(ServerPlayer p,Session s,long seq,int action){return new BuildNetworking.State(s.world.dimension().location().toString(),s.wand,BuildWandData.mode(p.getMainHandItem()),s.selection.stage(),pos(s.selection.first()),pos(s.selection.second()),s.selection.revision(),seq,BuildTaskState.get(s.world).busy(p.getUUID()),net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(WandStackData.selectedBlock(p.getMainHandItem())).toString(),net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(BuildWandData.replacementTarget(p.getMainHandItem())).toString(),(action==BuildNetworking.SELECT||action==BuildNetworking.RIGHT||action==BuildNetworking.LEFT&&s.selection.stage()==2)&&BuildWandData.mode(p.getMainHandItem())==3&&ReplacementRules.protectedMaterial(s.world,p.blockPosition(),WandStackData.selectedBlock(p.getMainHandItem()).defaultBlockState(),p.isCreative())?"message.q5.protected_material_warning":"");}
}
