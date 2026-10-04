from pathlib import Path
import json, re, shutil

r=Path(__file__).resolve().parents[1]
root=r/'versions/1.1.6'
versions=['26.2','26.1.2','1.21.11','1.21.8','1.21.4','1.21.1','1.20.4','1.20.1']
queue='''package cn.quit5700.buildingwand.logic;
import java.util.ArrayDeque;
/** One acknowledged action at a time. Timeouts query state; they never repeat mutations. */
public final class BuildRequestQueue<T> {
 public record Dispatch<T>(long sequence,boolean query,T action) {}
 private final ArrayDeque<T> clicks=new ArrayDeque<>();
 private long sequence,waiting,sentAt;private boolean active,queryNeeded;
 public void activate(){deactivate();active=true;queryNeeded=true;}
 public void deactivate(){clicks.clear();waiting=0;active=false;queryNeeded=false;}
 public void reset(){deactivate();sequence=0;}
 public void enqueue(T click){if(active)clicks.addLast(click);}
 public Dispatch<T> next(long tick){
  if(!active)return null;
  if(waiting!=0){if(tick-sentAt<20)return null;waiting=0;queryNeeded=true;}
  boolean query=queryNeeded;
  T action=query?null:clicks.pollFirst();if(!query&&action==null)return null;
  queryNeeded=false;waiting=++sequence;sentAt=tick;
  return new Dispatch<>(waiting,query,action);
 }
 public boolean acknowledge(long received){if(!active||waiting==0||received!=waiting)return false;waiting=0;return true;}
 public long waiting(){return waiting;}
}
'''
client='''package cn.quit5700.buildingwand.client;
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
  RECEIVER((p,c)->c.client().execute(()->{
   var mc=c.client();if(mc.level==null||!p.dimension().equals(mc.level.dimension().DIMENSION().toString()))return;
   if(p.stage()>0)flashing.add(p.wand());else flashing.remove(p.wand());
   if(mc.player==null||!active||!mc.player.getMainHandItem().is(ModItems.BUILDING_WAND)||slot(mc)!=activeSlot)return;
   if(p.sequence()!=requests.waiting())return;
   String id=BuildWandData.id(mc.player.getMainHandItem());
   if((!id.isEmpty()&&!id.equals(p.wand()))||(!activeWand.isEmpty()&&!activeWand.equals(p.wand())))return;
   if(!requests.acknowledge(p.sequence()))return;
   activeWand=p.wand();revision=p.revision();stage=p.stage();first=p.first();second=p.second();
   BuildWandData.synchronize(mc.player.getMainHandItem(),p.wand(),p.mode(),p.material(),p.filter());
  }));
  ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{clear();requests.reset();});
  ClientTickEvents.END_CLIENT_TICK.register(c->{tick++;if(c.level==null||c.player==null){clear();return;}world(c);for(int n=0;n<c.player.getInventory().getContainerSize();n++){var s=c.player.getInventory().getItem(n);if(!s.is(ModItems.BUILDING_WAND))continue;boolean dim=flashing.contains(BuildWandData.id(s))&&tick%40<20;FLASH}});
 }
 public static void cancelAllState(){clear();}
 private static void clear(){revision=0;stage=0;dimension="";activeWand="";activeSlot=-1;active=false;requests.deactivate();box=null;flashing.clear();}
 private static void world(Minecraft c){String next=c.level.dimension().DIMENSION().toString();if(!dimension.equals(next)){clear();dimension=next;}}
 public static void inactive(){active=false;activeSlot=-1;requests.deactivate();box=null;}
 private static int slot(Minecraft c){return c.player.getInventory().SLOT;}
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
  if(dispatch!=null){var click=dispatch.query()?new Click(BuildNetworking.QUERY,BlockPos.ZERO):dispatch.action();SEND(new BuildNetworking.Request(click.action(),click.pos(),revision,dispatch.sequence(),activeWand,slot));}
  if(stage==2){box=DeleteBox.between(first.getX(),first.getY(),first.getZ(),second.getX(),second.getY(),second.getZ());WandPreviewState.clear();}
  else{box=null;WandPreviewState.set(stage==1?List.of(first,target):List.of(target),WandPreviewState.PreviewColor.WHITE);}
 }
}
'''
flash='''var old=s.getOrDefault(DataComponents.CUSTOM_MODEL_DATA,CustomModelData.EMPTY);boolean prev=old.flags().size()>6&&old.flags().get(6);if(prev!=dim){var flags=new ArrayList<>(old.flags());while(flags.size()<=6)flags.add(false);flags.set(6,dim);s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(old.floats(),List.copyOf(flags),old.strings(),old.colors()));}'''
translations={
 'zh_cn':'区域替换放入箱子等受保护方块后，无法再用区域替换替换它们。左键仍可确认，右键取消。',
 'zh_tw':'區域替換放入箱子等受保護方塊後，無法再用區域替換替換它們。左鍵仍可確認，右鍵取消。',
 'en_us':'Chests and other protected blocks placed by region replacement cannot be replaced again in this mode. Left-click to confirm or right-click to cancel.',
 'ja_jp':'領域置換でチェストなどの保護ブロックを配置すると、このモードでは再置換できません。左クリックで確認、右クリックで取消。',
 'ko_kr':'영역 교체로 설치한 상자 등 보호 블록은 이 모드에서 다시 교체할 수 없습니다. 왼쪽 클릭으로 확인하거나 오른쪽 클릭으로 취소하세요.',
 'de_de':'Truhen und andere geschützte Blöcke, die durch Bereichsersetzung platziert werden, können in diesem Modus nicht erneut ersetzt werden. Linksklick bestätigt, Rechtsklick bricht ab.',
 'fr_fr':'Les coffres et autres blocs protégés placés par remplacement de zone ne peuvent plus être remplacés dans ce mode. Clic gauche pour confirmer, clic droit pour annuler.',
 'es_es':'Los cofres y otros bloques protegidos colocados mediante reemplazo de región no se pueden reemplazar otra vez en este modo. Clic izquierdo para confirmar o derecho para cancelar.'}
for v in versions:
 d=root/('mc'+v);modern=v in ['26.2','26.1.2','1.21.11','1.21.8'];legacy=v.startswith('1.20.')
 slot='getSelectedSlot()' if modern else 'selected';dimension='identifier' if v in ['26.2','26.1.2','1.21.11'] else 'location'
 path='src/main/resources/assets/pathfinding_beacon/models/item/id_sequence_reorderer.json'
 if v!='26.2':shutil.copy2(root/'mc26.2'/path,d/path)
 for name in ['BuildBoxRenderer','DeleteBoxRenderer']:
  p=d/f'src/client/java/cn/quit5700/buildingwand/client/{name}.java';t=p.read_text(encoding='utf-8')
  camera='context.worldState().cameraRenderState.pos' if v=='1.21.11' else ('client.gameRenderer.mainCamera().position()' if v in ['26.2','26.1.2'] else ('context.camera().getPosition()' if legacy else 'context.camera().position()'))
  if v=='26.1.2':camera=camera.replace('mainCamera()', 'getMainCamera()')
  if v not in ['26.2','26.1.2']:camera=re.search(r'Vec3 camera = (.+);',(d/'src/client/java/cn/quit5700/buildingwand/client/WandPreviewRenderer.java').read_text(encoding='utf-8')).group(1)
  t=t.replace('Vec3 camera=camera(client);','Vec3 camera='+camera+';');start=t.index('    private static Vec3 camera(');end=t.index('    private static void beam',start);t=t[:start]+t[end:];p.write_text(t,encoding='utf-8')
 if v in ['26.2','26.1.2']:
  p=d/'src/client/java/cn/quit5700/buildingwand/client/WandPreviewRenderer.java';t=p.read_text(encoding='utf-8').replace('Vec3 camera = cameraPosition(client);','Vec3 camera = client.gameRenderer.mainCamera().position();');start=t.index('    private static Vec3 cameraPosition(');t=t[:start]+'}\n';
  if v=='26.1.2':t=t.replace('gameRenderer.mainCamera()', 'gameRenderer.getMainCamera()')
  p.write_text(t,encoding='utf-8')
 p=d/'src/main/java/cn/quit5700/buildingwand/logic/BuildRequestQueue.java';p.write_text(queue,encoding='utf-8')
 t=client.replace('DIMENSION',dimension).replace('SLOT',slot)
 if legacy:
  t=t.replace('import net.minecraft.core.component.DataComponents;','').replace('import net.minecraft.world.item.component.CustomModelData;','')
  t=t.replace('RECEIVER','cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(BuildNetworking.State.ID,BuildNetworking.State::read,').replace('SEND','cn.quit5700.buildingwand.client.LegacyWandClientPackets.send').replace('FLASH','LegacyWandFlash.set(s,dim);')
 else:
  t=t.replace('RECEIVER','ClientPlayNetworking.registerGlobalReceiver(BuildNetworking.State.ID,').replace('SEND','ClientPlayNetworking.send')
  t=t.replace('FLASH','LegacyWandFlash.set(s,dim);' if v=='1.21.1' else flash)
  if v=='1.21.1':t=t.replace('import net.minecraft.world.item.component.CustomModelData;','')
 # Replace macro function opening, preserving the callback's parentheses.
 t=t.replace(',((p,c)', ',(p,c)')
 p=d/'src/client/java/cn/quit5700/buildingwand/client/BuildWandClient.java';p.write_text(t,encoding='utf-8')
 p=d/'src/main/java/cn/quit5700/buildingwand/util/BuildWandData.java';t=p.read_text(encoding='utf-8');kind='Identifier' if v in ['26.2','26.1.2','1.21.11'] else 'ResourceLocation';get='get' if v in ['1.21.1','1.20.4','1.20.1'] else 'getValue'
 update='WandTagData.update(s,t->t.putString("Q5BuildWandId",id));' if legacy else 'CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putString("Q5BuildWandId",id));'
 helper='''
 public static void synchronize(ItemStack s,String id,int mode,String material,String filter){
  UPDATE
  mode(s,mode);WandStackData.setSelectedBlock(s,block(material));replacementTarget(s,block(filter));
 }
 private static net.minecraft.world.level.block.Block block(String id){
  var key=net.minecraft.resources.KIND.tryParse(id);
  if(key==null)return net.minecraft.world.level.block.Blocks.AIR;
  var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.GET(key);
  return block==null?net.minecraft.world.level.block.Blocks.AIR:block;
 }
'''.replace('UPDATE',update).replace('KIND',kind).replace('GET',get)
 t=t.rstrip()[:-1]+helper+'}\n';p.write_text(t,encoding='utf-8')
 p=d/'src/main/java/cn/quit5700/buildingwand/network/BuildNetworking.java';t=p.read_text(encoding='utf-8')
 t=t.replace('long revision,long sequence) implements','long revision,long sequence,String wand,int slot) implements')
 t=t.replace('b.writeLong(revision);b.writeLong(sequence);}', 'b.writeLong(revision);b.writeLong(sequence);b.writeUtf(wand,64);b.writeVarInt(slot);}')
 t=t.replace('b.readBlockPos(),b.readLong(),b.readLong());','b.readBlockPos(),b.readLong(),b.readLong(),b.readUtf(64),b.readVarInt());')
 t=t.replace('long sequence,boolean busy) implements','long sequence,boolean busy,String material,String filter) implements')
 t=t.replace('b.writeBoolean(busy);','b.writeBoolean(busy);b.writeUtf(material,256);b.writeUtf(filter,256);')
 t=t.replace('b.readLong(),b.readLong(),b.readBoolean());','b.readLong(),b.readLong(),b.readBoolean(),b.readUtf(256),b.readUtf(256));')
 p.write_text(t,encoding='utf-8')
 p=d/'src/main/java/cn/quit5700/buildingwand/server/BuildWandServer.java';t=p.read_text(encoding='utf-8')
 t=t.replace(' public static void handle(ServerPlayer p,BuildNetworking.Request r){',''' public static void handle(ServerPlayer p,BuildNetworking.Request r){handle(p,r,state->SEND_STATE);}
 static void handle(ServerPlayer p,BuildNetworking.Request r,java.util.function.Consumer<BuildNetworking.State> reply){'''.replace('SEND_STATE','LegacyWandPackets.send(p,state)' if legacy else 'ServerPlayNetworking.send(p,state)'))
 # Existing legacy imports use fully qualified packet helpers.
 if legacy:t=t.replace('state->LegacyWandPackets.send(p,state)','state->cn.quit5700.buildingwand.network.LegacyWandPackets.send(p,state)')
 t=t.replace('  var tool=p.getMainHandItem();if(!tool.is(ModItems.BUILDING_WAND))return;','  var tool=p.getMainHandItem();if(!tool.is(ModItems.BUILDING_WAND)||r.slot()!=p.getInventory().'+slot+')return;if(!r.wand().isEmpty()&&!r.wand().equals(BuildWandData.id(tool)))return;')
 t=t.replace('  send(p,s,r.sequence());p.inventoryMenu.broadcastChanges();','  reply.accept(state(p,s,r.sequence()));p.inventoryMenu.broadcastChanges();')
 start=t.index(' private static void send(ServerPlayer p,Session s,long seq)');t=t[:start]+''' private static BuildNetworking.State state(ServerPlayer p,Session s,long seq){return new BuildNetworking.State(s.world.dimension().DIMENSION().toString(),s.wand,BuildWandData.mode(p.getMainHandItem()),s.selection.stage(),pos(s.selection.first()),pos(s.selection.second()),s.selection.revision(),seq,BuildTaskState.get(s.world).busy(p.getUUID()),net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(WandStackData.selectedBlock(p.getMainHandItem())).toString(),net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(BuildWandData.replacementTarget(p.getMainHandItem())).toString());}
}
'''.replace('DIMENSION',dimension)
 old='static void warnMultiple(ServerPlayer p){if('
 assert old in t
 t=t.replace(old,'''static void warnMultiple(ServerPlayer p){
  if(BuildWandData.mode(p.getMainHandItem())==3 && WandStackData.selectedBlock(p.getMainHandItem()).defaultBlockState().hasBlockEntity()) {
   var warning=Component.translatable("message.q5.protected_material_warning").withStyle(net.minecraft.ChatFormatting.RED);
   p.sendSystemMessage(warning);ACTIONBAR
  }
  if('''.replace('ACTIONBAR','p.sendOverlayMessage(warning);' if v in ['26.2','26.1.2'] else 'p.displayClientMessage(warning,true);'))
 p.write_text(t,encoding='utf-8')
 for lang,message in translations.items():
  p=d/f'src/main/resources/assets/building_wand/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));data['message.q5.protected_material_warning']=message;p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('八版本模型、相机坐标、串行点击队列、手持同步、双材料回包及箱子提醒已修改。')
