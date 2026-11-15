package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.server.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import java.util.*;

/** All construction, material accounting and ownership checks run on the server. */
public final class AuxServer {
 private static final Map<UUID,Planner> planners=new HashMap<>();
 public static void register(){ServerTickEvents.END_SERVER_TICK.register(server->{var saved=AuxState.get(server);for(String id:List.copyOf(saved.root.keySet())){var p=server.getPlayerList().getPlayer(UUID.fromString(id));if(p==null)continue;try{var plan=planners.get(p.getUUID());if(plan!=null&&plan.step()){planners.remove(p.getUUID());plan.finish();saved.setDirty();sync(p,"state");}tick(p);}catch(Exception error){planners.remove(p.getUUID());message(p,errorKey(error));sync(p,"state");}}});net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(s->planners.clear());net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->planners.remove(handler.player.getUUID()));}
 private static String errorKey(Throwable error){String message=String.valueOf(error.getMessage());if(message.contains("Beacon bounds"))return "aux.q5.beacon_bounds";if(message.contains("Invalid beacon base"))return "aux.q5.beacon_base";if(message.contains("Pure color"))return "aux.q5.need_material";if(message.contains("Missing material"))return "aux.q5.missing";if(message.contains("Conflicting material"))return "aux.q5.conflict";if(message.contains("Incomplete multi")||message.contains("Unsupported block")||message.contains("Unsupported sideways"))return "aux.q5.incomplete";if(message.contains("No exterior"))return "aux.q5.no_box";if(message.contains("Insufficient durability"))return "aux.q5.wear";if(message.contains("Protected target"))return "aux.q5.protected";return "aux.q5.invalid";}
 public static void message(ServerPlayer p,String key,Object... args){p.sendSystemMessage(Component.translatable(key,args));}
 private static CompoundTag state(ServerPlayer p){return AuxState.get(p.level().getServer()).player(p.getUUID());}
 public static void sync(ServerPlayer p,String action){AuxNet.send(p,action,view(p,!action.equals("jobs")));}
 public static CompoundTag view(ServerPlayer p){return view(p,true);}
 public static CompoundTag view(ServerPlayer p,boolean includeRows){var source=state(p);var copy=new CompoundTag();copy.putBoolean("Shown",source.getBooleanOr("Shown",true));copy.putBoolean("Micro",source.getBooleanOr("Micro",false));if(includeRows)copy.put("Rows",source.getListOrEmpty("Rows").copy());var jobs=new ListTag();for(var raw:source.getListOrEmpty("Jobs")){var original=(CompoundTag)raw;var j=new CompoundTag();original.forEach((key,value)->{if(!key.equals("Plan"))j.put(key,value.copy());});if(!buildJob(original))j.putInt("Total",original.getListOrEmpty("Plan").size());jobs.add(j);}copy.put("Jobs",jobs);return copy;}

 private static String dimension(ServerPlayer p){return p.level().dimension().identifier().toString();}
 private static ListTag rows(ServerPlayer p){return state(p).getListOrEmpty("Rows");}
 private static CompoundTag row(ServerPlayer p,String id){for(var raw:rows(p)){var row=(CompoundTag)raw;if(row.getStringOr("Id","").equals(id))return row;}throw new IllegalArgumentException("Unknown row");}
 private static List<String> ids(CompoundTag n){var result=new ArrayList<String>();for(var t:n.getListOrEmpty("Ids"))result.add(((StringTag)t).value());return result;}
 public static ListTag strings(Collection<String> ids){var list=new ListTag();for(String id:ids)list.add(StringTag.valueOf(id));return list;}
 private static boolean held(ServerPlayer p){return p.getMainHandItem().is(ModItems.AUX_WAND)&&p.mayBuild()&&!p.isSpectator();}
 public static void command(ServerPlayer p,String action,CompoundTag n){
  try{
   if (action.equals("save_box")) {
    var token = UUID.fromString(n.getStringOr("Token", ""));
    cn.quit5700.buildingwand.network.BlueprintNetworking.begin(p, token, () -> exportBox(p, n.getStringOr("Id", "")));
    return;
   }
   if(action.equals("box")){openBox(p,new BlockPos(n.getIntOr("X",0),n.getIntOr("Y",0),n.getIntOr("Z",0)));return;}
   if(action.equals("deposit")||action.equals("withdraw")||action.equals("execute")||action.equals("cancel_job")){bankCommand(p,action,n);return;}
   if(action.equals("query")){sync(p,n.getBooleanOr("Open",false)?"open":"state");return;}
   if(!held(p))return;
   if(action.equals("flags")){state(p).putBoolean("Shown",n.getBooleanOr("Shown",true));state(p).putBoolean("Micro",n.getBooleanOr("Micro",false));}
   else if(action.equals("add")){var r=AuxRow.blank();r.putString("Dim",dimension(p));rows(p).add(r);}
   else if(action.equals("create")){
    CompoundTag target=row(p,n.getStringOr("Id",""));if(!target.getStringOr("Dim","").equals(dimension(p)))throw new IllegalArgumentException("Unavailable row");if(locked(p,target.getStringOr("Id","")))throw new IllegalArgumentException("Row in use");
    var first=new BlockPos(n.getIntOr("X",0),n.getIntOr("Y",0),n.getIntOr("Z",0));var second=new BlockPos(n.getIntOr("X2",0),n.getIntOr("Y2",0),n.getIntOr("Z2",0));
    var fresh=AuxRow.create(first,second);for(String key:List.of("Id","Color","Shape","Solid","Visible","Material"))if(target.contains(key))fresh.put(key,target.get(key).copy());fresh.putString("Dim",dimension(p));new AuxRow(fresh);for(String key:List.copyOf(target.keySet()))target.remove(key);fresh.forEach((k,v)->target.put(k,v));
    if(!p.isCreative())p.getMainHandItem().hurtAndBreak(1,p,net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    message(p,"aux.q5.selection_next");
   }else if(action.equals("edit")){
    var r=row(p,n.getStringOr("Id",""));if(locked(p,r.getStringOr("Id","")))throw new IllegalArgumentException("Row in use");
    if(n.contains("Color"))r.putInt("Color",Math.floorMod(n.getIntOr("Color",0),16));if(n.contains("Shape")){int shape=Math.floorMod(n.getIntOr("Shape",0),AuxRow.SHAPES.length);if(shape==AuxBeacon.SHAPE&&r.getBooleanOr("Placed",false)&&AuxBeacon.levels(r.getIntOr("W",1),r.getIntOr("H",1),r.getIntOr("D",1))==0)throw new IllegalArgumentException("Beacon bounds too small");r.putInt("Shape",shape);r.remove("Blueprint");if(shape==AuxBeacon.SHAPE){r.putBoolean("Solid",true);}}
    for(String key:List.of("Solid","Visible"))if(n.contains(key))r.putBoolean(key,key.equals("Solid")&&r.getIntOr("Shape",0)==AuxBeacon.SHAPE||n.getBooleanOr(key,true));
    if(n.contains("Material")){String id=n.getStringOr("Material","");if(!id.isEmpty()){var identifier=Identifier.parse(id);if(!BuiltInRegistries.BLOCK.containsKey(identifier)||!(BuiltInRegistries.BLOCK.getValue(identifier).asItem() instanceof BlockItem))throw new IllegalArgumentException("Not a placeable item");}r.putString("Material",id);}
   }else if(action.equals("delete")){var list=ids(n);for(String id:list){cancelRow(p,id);rows(p).removeIf(t->((CompoundTag)t).getStringOr("Id","").equals(id));}planners.remove(p.getUUID());}
   else if(action.equals("micro")){var r=row(p,n.getStringOr("Id",""));if(!locked(p,r.getStringOr("Id",""))&&r.getStringOr("Dim","").equals(dimension(p)))new AuxRow(r).remove(new BlockPos(n.getIntOr("X",0),n.getIntOr("Y",0),n.getIntOr("Z",0)));}
   else if(action.equals("transform")){
    for(String id:ids(n)){var raw=row(p,id);if(locked(p,id)||!raw.getStringOr("Dim","").equals(dimension(p)))throw new IllegalArgumentException("Unavailable row");}
    var staged=new LinkedHashMap<String,AuxRow>();for(String id:ids(n)){var r=new AuxRow(row(p,id)).copy();var pivot=new BlockPos(n.getIntOr("PX",0),n.getIntOr("PY",0),n.getIntOr("PZ",0));if(n.contains("Rotations"))AuxRotationGesture.apply(r,n.getListOrEmpty("Rotations"),pivot);else r.rotate(Math.floorMod(n.getIntOr("Axis",1),3),pivot,n.getIntOr("Turns",0));r.translate(n.getIntOr("DX",0),n.getIntOr("DY",0),n.getIntOr("DZ",0));for(int x:new int[]{0,r.w-1})for(int y:new int[]{0,r.h-1})for(int z:new int[]{0,r.d-1})r.world(x,y,z);staged.put(id,r);}staged.forEach((id,r)->{var target=row(p,id);for(String key:List.of("X","Y","Z","Matrix"))target.put(key,r.data.get(key).copy());});
   }else if(action.equals("import")){
    var source=cn.quit5700.buildingwand.blueprint.BlueprintReader.vanilla(n.getCompoundOrEmpty("Blueprint"));var aux=source.getCompoundOrEmpty("Q5Aux");
    if(aux.getIntOr("FormatVersion",1)!=1)throw new IllegalArgumentException("Unknown blueprint version");
    var imported=aux.getListOrEmpty("Rows");if(imported.isEmpty()){
     var r=AuxRow.blank();var size=source.getListOrEmpty("size");r.putInt("W",((IntTag)size.get(0)).intValue());r.putInt("H",((IntTag)size.get(1)).intValue());r.putInt("D",((IntTag)size.get(2)).intValue());r.put("Blueprint",source);r.putBoolean("Placed",true);imported.add(r);
    }
    int x=n.getIntOr("X",0),y=n.getIntOr("Y",0),z=n.getIntOr("Z",0);var staged=new ArrayList<CompoundTag>();
    for(var t:imported){var r=((CompoundTag)t).copy();r.putString("Id",UUID.randomUUID().toString());r.putString("Dim",dimension(p));r.putInt("X",Math.addExact(x,r.getIntOr("X",0)));r.putInt("Y",Math.addExact(y,r.getIntOr("Y",0)));r.putInt("Z",Math.addExact(z,r.getIntOr("Z",0)));new AuxRow(r);staged.add(r);}if(!p.isCreative()&&p.getMainHandItem().getMaxDamage()-p.getMainHandItem().getDamageValue()<staged.size())throw new IllegalArgumentException("Insufficient durability");for(var r:staged)rows(p).add(r);if(!p.isCreative())p.getMainHandItem().hurtAndBreak(staged.size(),p,net.minecraft.world.entity.EquipmentSlot.MAINHAND);
   }else if(action.equals("generate")){
    if(planners.containsKey(p.getUUID())){message(p,"aux.q5.planning");return;}var selected=new ArrayList<AuxRow>();for(String id:ids(n)){var r=new AuxRow(row(p,id).copy());if(locked(p,id)||!r.data.getStringOr("Dim","").equals(dimension(p)))throw new IllegalArgumentException("Unavailable row");if(!r.data.getBooleanOr("Placed",false))throw new IllegalArgumentException("No shape");if(r.beacon()&&r.data.getStringOr("Material","").isEmpty())throw new IllegalArgumentException("Pure color has no material");selected.add(r);}if(selected.isEmpty())return;planners.put(p.getUUID(),new Planner(p,selected));message(p,"aux.q5.planning");
   }else throw new IllegalArgumentException("Unknown action");
   AuxState.get(p.level().getServer()).setDirty();sync(p,"state");
  }catch(Exception error){message(p,errorKey(error));sync(p,"state");}
 }
 public static boolean buildJob(CompoundTag job){return job.getStringOr("Kind","").equals("Build");}
 public static String createBuildBox(ServerPlayer p,String wand,cn.quit5700.buildingwand.logic.DeleteBox bounds,Block material,long maximum){return createBuildBox(p,wand,bounds,material,maximum,new cn.quit5700.buildingwand.logic.DirectedDeleteCursor(bounds,1,1,1),1);}
 public static String createBuildBox(ServerPlayer p,String wand,cn.quit5700.buildingwand.logic.DeleteBox bounds,Block material,long maximum,cn.quit5700.buildingwand.logic.DirectedDeleteCursor cursor,int mode){
  for(var raw:state(p).getListOrEmpty("Jobs")){var old=(CompoundTag)raw;if(buildJob(old)&&old.getStringOr("Wand","").equals(wand)&&old.getStringOr("Dim","").equals(dimension(p)))return old.getStringOr("Id","");}
  String item=BuiltInRegistries.ITEM.getKey(material.asItem()).toString();if(material.asItem()==Items.AIR)throw new IllegalArgumentException("Missing material item");
  BlockPos box=null;var world=p.level();
  box=BuildBoxPlacement.find(bounds,p.blockPosition(),at->world.isInWorldBounds(at)&&world.getWorldBorder().isWithinBounds(at)&&world.mayInteract(p,at)&&world.isEmptyBlock(at)&&!p.getBoundingBox().intersects(new net.minecraft.world.phys.AABB(at))&&state(p).getListOrEmpty("Jobs").stream().noneMatch(raw->((CompoundTag)raw).getLongOr("Box",0)==at.asLong()&&((CompoundTag)raw).getStringOr("Dim","").equals(dimension(p))));
  if(box==null)throw new IllegalArgumentException("No exterior chest position");var job=new CompoundTag();String id=UUID.randomUUID().toString();job.putString("Id",id);job.putString("Kind","Build");job.putString("Wand",wand);job.putString("Dim",dimension(p));job.putLong("Box",box.asLong());job.put("Ids",new ListTag());job.put("Plan",new ListTag());var need=new CompoundTag();need.putLong(item,Math.max(0,maximum));job.put("Need",need);job.put("Required",need.copy());job.put("Bank",new CompoundTag());job.putLong("Total",Math.max(0,maximum));job.putLong("Cursor",0);job.putBoolean("Running",!p.isCreative()&&maximum<=0);
  job.putIntArray("PreviewBounds",new int[]{bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX(),bounds.maxY(),bounds.maxZ()});job.putIntArray("PreviewCursor",new int[]{cursor.x(),cursor.y(),cursor.z()});job.putIntArray("PreviewDirection",new int[]{cursor.dx(),cursor.dy(),cursor.dz()});job.putInt("PreviewMode",mode);
  if(!world.setBlock(box,AuxBlocks.GENERATION_BOX.defaultBlockState(),3))throw new IllegalStateException("Chest placement failed");state(p).getListOrEmpty("Jobs").add(job);AuxState.get(world.getServer()).setDirty();sync(p,"jobs");message(p,"message.q5.build_box_ready",box.getX(),box.getY(),box.getZ(),maximum);return id;
 }
 public static void updateBuildPreview(ServerPlayer p,String id,cn.quit5700.buildingwand.logic.DeleteBox bounds,cn.quit5700.buildingwand.logic.DirectedDeleteCursor cursor,int mode){
  var j=job(p,id);var before=j.getIntArray("PreviewCursor").orElse(new int[0]);var next=new int[]{cursor.x(),cursor.y(),cursor.z()};boolean first=!j.contains("PreviewBounds");
  j.putIntArray("PreviewBounds",new int[]{bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX(),bounds.maxY(),bounds.maxZ()});j.putIntArray("PreviewCursor",next);j.putIntArray("PreviewDirection",new int[]{cursor.dx(),cursor.dy(),cursor.dz()});j.putInt("PreviewMode",mode);
  if(first||!java.util.Arrays.equals(before,next)){j.putBoolean("PreviewDirty",true);AuxState.get(p.level().getServer()).setDirty();}if(j.getBooleanOr("PreviewDirty",false)&&(first||p.level().getGameTime()%5==0)){j.putBoolean("PreviewDirty",false);AuxState.get(p.level().getServer()).setDirty();sync(p,"jobs");}
 }
 public static boolean validBuildBox(ServerPlayer p,String id){for(var raw:state(p).getListOrEmpty("Jobs")){var j=(CompoundTag)raw;if(j.getStringOr("Id","").equals(id))return buildJob(j)&&j.getStringOr("Dim","").equals(dimension(p))&&p.level().getBlockState(BlockPos.of(j.getLongOr("Box",0))).is(AuxBlocks.GENERATION_BOX);}return false;}
 public static boolean buildRunning(ServerPlayer p,String id){var j=job(p,id);boolean free=j.getBooleanOr("Creative",false);if(free&&!p.isCreative()){j.putBoolean("Creative",false);var need=j.getCompoundOrEmpty("Need");var bank=j.getCompoundOrEmpty("Bank");j.putBoolean("Running",need.keySet().stream().allMatch(key->bank.getLongOr(key,0)>=need.getLongOr(key,0)));AuxState.get(p.level().getServer()).setDirty();sync(p,"jobs");}else if(p.isCreative()&&!free&&j.getBooleanOr("Running",false)){j.putBoolean("Running",false);AuxState.get(p.level().getServer()).setDirty();sync(p,"jobs");}if(!p.isCreative()&&j.getBooleanOr("Running",false)){var need=j.getCompoundOrEmpty("Need");var bank=j.getCompoundOrEmpty("Bank");if(need.keySet().stream().anyMatch(key->bank.getLongOr(key,0)<need.getLongOr(key,0))){j.putBoolean("Running",false);AuxState.get(p.level().getServer()).setDirty();sync(p,"jobs");}}return j.getBooleanOr("Running",false);}
 public static long buildCredit(ServerPlayer p,String id){var j=job(p,id);return j.getBooleanOr("Running",false)?j.getCompoundOrEmpty("Bank").keySet().stream().mapToLong(k->j.getCompoundOrEmpty("Bank").getLongOr(k,0)).sum():0;}
 public static void spendBuildCredit(ServerPlayer p,String id){var j=job(p,id);var bank=j.getCompoundOrEmpty("Bank");var need=j.getCompoundOrEmpty("Need");String item=need.keySet().stream().findFirst().orElseThrow();long balance=bank.getLongOr(item,0);if(balance<1)throw new IllegalStateException("Insufficient construction credit");bank.putLong(item,balance-1);need.putLong(item,Math.max(0,need.getLongOr(item,0)-1));j.putLong("Cursor",Math.addExact(j.getLongOr("Cursor",0),1));AuxState.get(p.level().getServer()).setDirty();if(p.level().getGameTime()%10==0||balance==1)sync(p,"jobs");}
 public static void finishBuildBox(ServerPlayer p,String id){var list=state(p).getListOrEmpty("Jobs");for(var raw:List.copyOf(list)){var j=(CompoundTag)raw;if(j.getStringOr("Id","").equals(id)&&buildJob(j)){cancelJob(p,j,false);list.remove(j);sync(p,"jobs");return;}}}
 private static boolean locked(ServerPlayer p,String id){for(var t:state(p).getListOrEmpty("Jobs"))if(ids((CompoundTag)t).contains(id))return true;var plan=planners.get(p.getUUID());return plan!=null&&plan.rows.stream().anyMatch(r->r.id().equals(id));}
 public static boolean ownsBox(ServerPlayer p,BlockPos pos){for(var t:state(p).getListOrEmpty("Jobs")){var j=(CompoundTag)t;if(j.getLongOr("Box",0)==pos.asLong()&&j.getStringOr("Dim","").equals(dimension(p)))return true;}return false;}
 public static java.util.concurrent.CompletableFuture<byte[]> exportBox(ServerPlayer p, String id) {
  var job = job(p, id);
  var box = BlockPos.of(job.getLongOr("Box", 0));
  if (!job.getStringOr("Dim", "").equals(dimension(p)) || box.distSqr(p.blockPosition()) > 100
          || !p.level().hasChunkAt(box) || !p.level().getBlockState(box).is(AuxBlocks.GENERATION_BOX)) {
   throw new IllegalArgumentException("Generation box unavailable");
  }
  if (buildJob(job)) return BuildTaskState.get((ServerLevel)p.level()).exportBox(p, id);
  return GenerationBoxBlueprint.plan(job.getListOrEmpty("Plan"));
 }
 public static void openBox(ServerPlayer p,BlockPos pos){if(pos.distSqr(p.blockPosition())>100||!ownsBox(p,pos))return;var data=view(p);data.putLong("OpenBox",pos.asLong());AuxNet.send(p,"box",data);}
 private static CompoundTag job(ServerPlayer p,String id){for(var t:state(p).getListOrEmpty("Jobs")){var j=(CompoundTag)t;if(j.getStringOr("Id","").equals(id))return j;}throw new IllegalArgumentException("Unknown generation job");}
 private static void bankCommand(ServerPlayer p,String action,CompoundTag n){try{
  var j=job(p,n.getStringOr("Id",""));BlockPos box=BlockPos.of(j.getLongOr("Box",0));if(!dimension(p).equals(j.getStringOr("Dim",""))||box.distSqr(p.blockPosition())>100||!p.level().getBlockState(box).is(AuxBlocks.GENERATION_BOX))return;
  if(action.equals("cancel_job")){cancelJob(p,j);state(p).getListOrEmpty("Jobs").remove(j);sync(p,"state");return;}
  if(j.getBooleanOr("Running",false))return;var bank=j.getCompoundOrEmpty("Bank");var need=j.getCompoundOrEmpty("Need");
  if(action.equals("execute")){if(p.isCreative())j.putBoolean("Creative",true);else return;j.putBoolean("Running",true);}
  else if(action.equals("deposit")){int slot=n.getIntOr("Slot",-1);if(slot<0||slot>=36)return;var stack=p.getInventory().getItem(slot);if(stack.isEmpty()||stack.has(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA)||stack.get(net.minecraft.core.component.DataComponents.CONTAINER)!=null&&stack.get(net.minecraft.core.component.DataComponents.CONTAINER).nonEmptyItems().iterator().hasNext())return;String item=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();long deficit=need.getLongOr(item,0)-bank.getLongOr(item,0);int count=(int)Math.min(stack.getCount(),Math.max(0,deficit));if(count>0){stack.shrink(count);bank.putLong(item,bank.getLongOr(item,0)+count);}}
  else if(action.equals("withdraw")){String item=n.getStringOr("Item","");long count=Math.min(64,bank.getLongOr(item,0));if(count>0){var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)),(int)count);p.getInventory().add(stack);if(!stack.isEmpty())net.minecraft.world.Containers.dropItemStack(p.level(),p.getX(),p.getY(),p.getZ(),stack);bank.putLong(item,bank.getLongOr(item,0)-count);}}
  boolean ready=!need.keySet().isEmpty()&&need.keySet().stream().allMatch(id->bank.getLongOr(id,0)>=need.getLongOr(id,0));if(ready&&(!buildJob(j)||!p.isCreative()))j.putBoolean("Running",true);
  p.inventoryMenu.broadcastChanges();AuxState.get(p.level().getServer()).setDirty();sync(p,"jobs");
 }catch(Exception error){message(p,"aux.q5.failed");}}
 private static void refund(ServerPlayer p,String item,long count){refundAt(p,item,count,dimension(p),p.blockPosition());}
 private static void refundAt(ServerPlayer p,String item,long count,String dimension,BlockPos at){if(count<=0)return;var list=state(p).getListOrEmpty("Refunds");for(var raw:list){var r=(CompoundTag)raw;if(r.getStringOr("Item","").equals(item)&&r.getStringOr("Dim","").equals(dimension)&&r.getLongOr("Pos",0)==at.asLong()){r.putLong("Count",Math.addExact(r.getLongOr("Count",0),count));AuxState.get(p.level().getServer()).setDirty();return;}}var n=new CompoundTag();n.putString("Item",item);n.putString("Dim",dimension);n.putLong("Pos",at.asLong());n.putLong("Count",count);list.add(n);AuxState.get(p.level().getServer()).setDirty();}
 private static void drainRefunds(ServerPlayer p){var list=state(p).getListOrEmpty("Refunds");int budget=16;for(var raw:List.copyOf(list)){var r=(CompoundTag)raw;var id=Identifier.parse(r.getStringOr("Item",""));if(!BuiltInRegistries.ITEM.containsKey(id))continue;var world=p.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse(r.getStringOr("Dim",dimension(p)))));if(world==null)continue;var at=BlockPos.of(r.getLongOr("Pos",0));world.getChunk(at);var type=BuiltInRegistries.ITEM.getValue(id);long count=r.getLongOr("Count",0);while(count>0&&budget-->0){int amount=(int)Math.min(count,type.getDefaultMaxStackSize());net.minecraft.world.Containers.dropItemStack(world,at.getX()+.5,at.getY()+.5,at.getZ()+.5,new ItemStack(type,amount));count-=amount;}r.putLong("Count",count);if(count==0)list.remove(r);AuxState.get(p.level().getServer()).setDirty();if(budget<=0)break;}}
 private static void cancelJob(ServerPlayer p,CompoundTag j){cancelJob(p,j,true);}
 private static void cancelJob(ServerPlayer p,CompoundTag j,boolean notify){var world=p.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse(j.getStringOr("Dim",dimension(p)))));if(world==null)throw new IllegalStateException("Missing refund dimension");if(notify&&buildJob(j))BuildTaskState.get(world).cancelFromBox(p.getUUID(),j.getStringOr("Id",""),j.getStringOr("Wand",""));var box=BlockPos.of(j.getLongOr("Box",0));var bank=j.getCompoundOrEmpty("Bank");for(String item:bank.keySet())refundAt(p,item,bank.getLongOr(item,0),j.getStringOr("Dim",dimension(p)),box);j.put("Bank",new CompoundTag());int built=j.getIntOr("Cursor",0);if(!buildJob(j)&&built>0&&j.getIntOr("UpdateCursor",0)<built){var update=new CompoundTag();update.putString("Dim",j.getStringOr("Dim",dimension(p)));update.put("Plan",j.getListOrEmpty("Plan"));update.putInt("Limit",built);update.putInt("UpdateCursor",j.getIntOr("UpdateCursor",0));state(p).getListOrEmpty("Updates").add(update);}if(world.getBlockState(box).is(AuxBlocks.GENERATION_BOX))world.setBlock(box,Blocks.AIR.defaultBlockState(),3);AuxState.get(p.level().getServer()).setDirty();}
 public static int cancelAll(ServerPlayer p){var jobs=state(p).getListOrEmpty("Jobs");int size=jobs.size();for(var t:List.copyOf(jobs))cancelJob(p,(CompoundTag)t);jobs.clear();planners.remove(p.getUUID());sync(p,"state");return size;}
 private static void cancelRow(ServerPlayer p,String id){for(var t:List.copyOf(state(p).getListOrEmpty("Jobs"))){var j=(CompoundTag)t;if(!ids(j).contains(id))continue;var remaining=ids(j);remaining.remove(id);if(remaining.isEmpty()){cancelJob(p,j);state(p).getListOrEmpty("Jobs").remove(j);continue;}j.put("Ids",strings(remaining));var plan=j.getListOrEmpty("Plan");int cursor=j.getIntOr("Cursor",0);var need=new CompoundTag();for(int i=cursor;i<plan.size();i++){var unit=plan.getCompoundOrEmpty(i);var owners=ids(unit);owners.remove(id);unit.put("Ids",strings(owners));if(!owners.isEmpty()){String item=unit.getStringOr("Item","");if(unit.getIntOr("Count",1)>0)need.putLong(item,need.getLongOr(item,0)+unit.getIntOr("Count",1));}}j.put("Need",need);var bank=j.getCompoundOrEmpty("Bank");if(!need.keySet().isEmpty()&&need.keySet().stream().allMatch(item->bank.getLongOr(item,0)>=need.getLongOr(item,0)))j.putBoolean("Running",true);for(String item:List.copyOf(bank.keySet())){long excess=Math.max(0,bank.getLongOr(item,0)-need.getLongOr(item,0));if(excess>0){refundAt(p,item,excess,j.getStringOr("Dim",dimension(p)),BlockPos.of(j.getLongOr("Box",0)));bank.putLong(item,bank.getLongOr(item,0)-excess);}}}}
 private record Cell(BlockState state,Set<String> rows){}
 private static final class Planner {
  final ServerPlayer p;final List<AuxRow> rows;final LinkedHashMap<BlockPos,Cell> cells=new LinkedHashMap<>();int rowIndex;long cursor;
  Planner(ServerPlayer p,List<AuxRow> rows){this.p=p;this.rows=rows;}
  boolean step(){long end=System.nanoTime()+4_000_000;int budget=4096;while(rowIndex<rows.size()&&budget-->0&&System.nanoTime()<end){var row=rows.get(rowIndex);if(cursor>=row.volume()){rowIndex++;cursor=0;continue;}int[] local=row.coordinates(cursor++);for(var entry:AuxMaterials.unit(row,local[0],local[1],local[2]).entrySet()){var at=entry.getKey();var state=entry.getValue();if(!p.level().isInWorldBounds(at)||!p.level().getWorldBorder().isWithinBounds(at)||!p.level().mayInteract(p,at)||BuildOverwriteRules.blocks(p.level().getBlockState(at),p.isCreative()))throw new IllegalArgumentException("Protected target");var existing=cells.get(at);if(existing!=null){if(!existing.state.equals(state))throw new IllegalArgumentException("Conflicting materials");existing.rows.add(row.id());}else cells.put(at,new Cell(state,new LinkedHashSet<>(List.of(row.id()))));}}return rowIndex==rows.size();}
  void finish(){
   Map<String,String> parent=new HashMap<>();for(var r:rows)parent.put(r.id(),r.id());for(var e:cells.entrySet()){String owner=e.getValue().rows.iterator().next();for(String id:e.getValue().rows)join(parent,owner,id);for(var d:Direction.values()){var other=cells.get(e.getKey().relative(d));if(other!=null)join(parent,owner,other.rows.iterator().next());}}
   var groups=new LinkedHashMap<String,LinkedHashMap<BlockPos,Cell>>();for(var e:cells.entrySet())groups.computeIfAbsent(find(parent,e.getValue().rows.iterator().next()),key->new LinkedHashMap<>()).put(e.getKey(),e.getValue());
   var prepared=new ArrayList<CompoundTag>();for(var group:groups.values()){
    var plan=new ListTag();var need=new CompoundTag();Set<BlockPos> visited=new HashSet<>();var sourceIds=new LinkedHashSet<String>();
    for(var e:group.entrySet()){
     sourceIds.addAll(e.getValue().rows);if(!visited.add(e.getKey()))continue;var desired=new LinkedHashMap<BlockPos,BlockState>();desired.put(e.getKey(),e.getValue().state);var owners=new LinkedHashSet<>(e.getValue().rows);var s=e.getValue().state;BlockPos mate=null;
     if(s.hasProperty(BedBlock.PART))mate=e.getKey().relative(s.getValue(BedBlock.PART)==BedPart.FOOT?s.getValue(BedBlock.FACING):s.getValue(BedBlock.FACING).getOpposite());
     else if(s.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))mate=e.getKey().relative(s.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==DoubleBlockHalf.LOWER?Direction.UP:Direction.DOWN);
     if(mate!=null){var other=group.get(mate);if(other==null||other.state.getBlock()!=s.getBlock()||s.hasProperty(BedBlock.PART)&&(other.state.getValue(BedBlock.PART)==s.getValue(BedBlock.PART)||other.state.getValue(BedBlock.FACING)!=s.getValue(BedBlock.FACING))||s.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)&&other.state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==s.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF))throw new IllegalArgumentException("Incomplete multi block");visited.add(mate);desired.put(mate,other.state);owners.addAll(other.rows);}
     if(desired.entrySet().stream().allMatch(c->p.level().getBlockState(c.getKey()).equals(c.getValue())))continue;
     Item item=s.is(Blocks.WATER)?Items.WATER_BUCKET:s.is(Blocks.LAVA)?Items.LAVA_BUCKET:s.getBlock().asItem();if(item==Items.AIR&&!s.isAir())throw new IllegalArgumentException("No material item");String id=s.isAir()?"":BuiltInRegistries.ITEM.getKey(item).toString();var unit=new CompoundTag();unit.putString("Item",id);unit.putInt("Count",cost(s));unit.put("Ids",strings(owners));var states=new ListTag();for(var c:desired.entrySet()){var b=new CompoundTag();b.putLong("Pos",c.getKey().asLong());b.put("State",NbtUtils.writeBlockState(c.getValue()));states.add(b);}unit.put("Cells",states);plan.add(unit);if(cost(s)>0)need.putLong(id,need.getLongOr(id,0)+cost(s));
    }
    if(plan.isEmpty()){rows(p).removeIf(t->sourceIds.contains(((CompoundTag)t).getStringOr("Id","")));continue;}
    BlockPos anchor=rows.stream().filter(r->sourceIds.contains(r.id())).findFirst().map(r->r.world(r.data.getIntOr("AX",0),r.data.getIntOr("AY",0),r.data.getIntOr("AZ",0))).orElseThrow();BlockPos box=null;
    outer:for(int radius=1;radius<=64;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;var candidate=anchor.offset(dx,0,dz);if(!cells.containsKey(candidate)&&p.level().isEmptyBlock(candidate)&&p.level().isInWorldBounds(candidate)&&p.level().getWorldBorder().isWithinBounds(candidate)&&p.level().mayInteract(p,candidate)&&prepared.stream().noneMatch(j->j.getLongOr("Box",0)==candidate.asLong())){box=candidate;break outer;}}
    if(box==null)throw new IllegalArgumentException("No exterior chest position");var j=new CompoundTag();j.putString("Id",UUID.randomUUID().toString());j.put("Ids",strings(sourceIds));j.put("Plan",plan);j.put("Need",need);j.put("Required",need.copy());j.put("Bank",new CompoundTag());j.putBoolean("Running",need.isEmpty());j.putBoolean("Creative",p.isCreative());j.putLong("Box",box.asLong());j.putString("Dim",dimension(p));prepared.add(j);
   }
   for(var j:prepared){var box=BlockPos.of(j.getLongOr("Box",0));if(!p.level().setBlock(box,AuxBlocks.GENERATION_BOX.defaultBlockState(),3))throw new IllegalStateException("Chest placement failed");state(p).getListOrEmpty("Jobs").add(j);}message(p,"aux.q5.prepared",prepared.size());
  }
 }
 private static int cost(BlockState state){if(state.isAir())return 0;if(state.hasProperty(SlabBlock.TYPE)&&state.getValue(SlabBlock.TYPE)==SlabType.DOUBLE)return 2;for(var property:state.getProperties())if(Set.of("layers","candles","pickles","eggs","flower_amount").contains(property.getName())&&state.getValue(property) instanceof Integer n)return Math.max(1,n);return 1;}
 private static String find(Map<String,String> p,String key){while(!p.get(key).equals(key))key=p.get(key);return key;}
 private static void join(Map<String,String> p,String a,String b){p.put(find(p,b),find(p,a));}
 private static boolean updatePlan(ServerLevel world,CompoundTag task,int limit,int budget){var plan=task.getListOrEmpty("Plan");int cursor=task.getIntOr("UpdateCursor",0);long end=System.nanoTime()+4_000_000;while(cursor<limit&&budget-->0&&System.nanoTime()<end){var unit=plan.getCompoundOrEmpty(cursor++);if(ids(unit).isEmpty())continue;for(var raw:unit.getListOrEmpty("Cells")){var cell=(CompoundTag)raw;var pos=BlockPos.of(cell.getLongOr("Pos",0));world.getChunk(pos);AuxPlacement.update(world,pos);}}task.putInt("UpdateCursor",cursor);return cursor>=limit;}
 private static void drainUpdates(ServerPlayer p){var list=state(p).getListOrEmpty("Updates");for(var raw:List.copyOf(list)){var task=(CompoundTag)raw;var world=p.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse(task.getStringOr("Dim",dimension(p)))));if(world!=null){if(updatePlan(world,task,task.getIntOr("Limit",0),128))list.remove(task);AuxState.get(p.level().getServer()).setDirty();}break;}}
 private static void tick(ServerPlayer p){drainRefunds(p);drainUpdates(p);var saved=AuxState.get(p.level().getServer());var jobs=state(p).getListOrEmpty("Jobs");boolean dirty=false,rowsChanged=false;
  for(var raw:List.copyOf(jobs)){var j=(CompoundTag)raw;var world=p.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse(j.getStringOr("Dim",dimension(p)))));if(world==null)continue;var box=BlockPos.of(j.getLongOr("Box",0));if(!world.hasChunkAt(box))continue;
   if(!world.getBlockState(box).is(AuxBlocks.GENERATION_BOX)){cancelJob(p,j);jobs.remove(j);dirty=true;continue;}if(buildJob(j)){if(!BuildTaskState.get((ServerLevel)p.level()).hasBox(p.getUUID(),j.getStringOr("Id",""),j.getStringOr("Wand",""))){cancelJob(p,j);jobs.remove(j);dirty=true;}continue;}if(!j.getBooleanOr("Running",false)||world!=p.level()||!p.mayBuild()||p.isSpectator())continue;
   var plan=j.getListOrEmpty("Plan");int index=j.getIntOr("Cursor",0),budget=128;long deadline=System.nanoTime()+4_000_000;
   while(index<plan.size()&&budget-->0&&System.nanoTime()<deadline){var unit=plan.getCompoundOrEmpty(index);if(ids(unit).isEmpty()){index++;continue;}var desired=new LinkedHashMap<BlockPos,BlockState>();boolean blocked=false;
    for(var t:unit.getListOrEmpty("Cells")){var c=(CompoundTag)t;var at=BlockPos.of(c.getLongOr("Pos",0));world.getChunk(at);blocked|=!world.mayInteract(p,at)||!world.getWorldBorder().isWithinBounds(at)||BuildOverwriteRules.blocks(world.getBlockState(at),j.getBooleanOr("Creative",false)&&p.isCreative());var tag=c.getCompoundOrEmpty("State");if(!BuiltInRegistries.BLOCK.containsKey(Identifier.parse(AuxMaterials.stateId(tag)))){j.putBoolean("Running",false);message(p,"aux.q5.invalid");return;}desired.put(at,AuxMaterials.readState(tag));}
    if(blocked){j.putBoolean("Running",false);message(p,"aux.q5.protected");break;}
    boolean unchanged=desired.entrySet().stream().allMatch(e->world.getBlockState(e.getKey()).equals(e.getValue()));String item=unit.getStringOr("Item","");var bank=j.getCompoundOrEmpty("Bank");boolean free=j.getBooleanOr("Creative",false)&&p.isCreative();
    if(!unchanged&&!free&&bank.getLongOr(item,0)<unit.getIntOr("Count",1)){j.putBoolean("Running",false);break;}
    if(!unchanged&&!AuxPlacement.place(world,desired)){j.putBoolean("Running",false);message(p,"aux.q5.failed");break;}
    if(!unchanged&&!free&&unit.getIntOr("Count",1)>0){bank.putLong(item,bank.getLongOr(item,0)-unit.getIntOr("Count",1));if(item.equals("minecraft:water_bucket")||item.equals("minecraft:lava_bucket"))refund(p,"minecraft:bucket",unit.getIntOr("Count",1));}var need=j.getCompoundOrEmpty("Need");if(unit.getIntOr("Count",1)>0)need.putLong(item,Math.max(0,need.getLongOr(item,0)-unit.getIntOr("Count",1)));index++;dirty=true;
   }
   j.putInt("Cursor",index);
   if(index==plan.size()&&(dirty=true)&&updatePlan(world,j,plan.size(),128)){var completed=ids(j);cancelJob(p,j);jobs.remove(j);rows(p).removeIf(t->completed.contains(((CompoundTag)t).getStringOr("Id","")));message(p,"aux.q5.complete");dirty=true;rowsChanged=true;}
  }
  if(dirty){saved.setDirty();if(rowsChanged)sync(p,"state");else if(p.level().getGameTime()%10==0||jobs.isEmpty())sync(p,"jobs");}
 }
}
