from pathlib import Path
import re
R=Path(__file__).resolve().parents[1]/'versions/1.1.3'
files=['LightMod.java','block/VariableConstantLightBlockEntity.java','client/LightClient.java','client/VariableConstantLightScreen.java','light/AreaLightState.java','light/ConstantLightTaskQueue.java','light/DaylightOcclusion.java','light/RealAreaLightState.java','light/VariableLightTaskState.java','menu/VariableConstantLightMenu.java','mixin/DaylightChunkMixin.java','mixin/NaturalSpawnerMixin.java','mixin/PhantomSpawnerMixin.java','network/AreaSelectionNetworking.java','network/VariableLightNetworking.java']
paths=[Path('src/main/java/cn/quit5700/light')/f for f in files]+[Path('src/client/java/cn/quit5700/light')/f for f in ['client/AreaSelectionClient.java','client/ClientDaylightState.java','compat/jei/HiddenLegacyLightPlugin.java']]
for version in ['1.20.4','1.20.1']:
 P=R/('mc'+version)
 for rel in paths:
  s=(R/'mc1.21.1'/rel).read_text(encoding='utf-8')
  s=s.replace('ResourceLocation.fromNamespaceAndPath(', 'new ResourceLocation(')
  if rel.name=='VariableConstantLightBlockEntity.java':
   s=s.replace('import net.minecraft.core.HolderLookup;','').replace(', HolderLookup.Provider registries','').replace('super.saveAdditional(data, registries)','super.saveAdditional(data)').replace('super.loadAdditional(data, registries)','super.load(data)').replace('protected void loadAdditional(CompoundTag data)', 'public void load(CompoundTag data)')
  if rel.name=='VariableConstantLightScreen.java' and version=='1.20.1':
   s=s.replace('    private Checkbox brightButton;','')
   a=s.index('    private void rebuildBoostCheckbox()');b=s.index('    private void save()',a)
   s=s[:a]+'    private void rebuildBoostCheckbox() {}\n\n'+s[b:]
   s=s.replace('        if (brightButton != null) {\n            if(brightButton.selected()!=(selectedMode==VariableLightMode.HYBRID))rebuildBoostCheckbox();\n            brightButton.visible = false;\n        }','')
  if rel.name in ['AreaLightState.java','RealAreaLightState.java','VariableLightTaskState.java']:
   s=s.replace('import net.minecraft.core.HolderLookup;','').replace(',HolderLookup.Provider registries','').replace('(tag,registries)->','tag->')
   if version=='1.20.1':
    c=rel.stem
    constructor={'AreaLightState':'new AreaLightState(List.of())','RealAreaLightState':'new RealAreaLightState(List.of(),List.of(),List.of())','VariableLightTaskState':'new VariableLightTaskState()'}[c]
    a=s.index('    public static final SavedData.Factory<');b=s.index(';',a)+1;s=s[:a]+s[b:]
    s=s.replace('.computeIfAbsent(TYPE,SAVE_ID)',f'.computeIfAbsent(tag->CODEC.parse(NbtOps.INSTANCE,tag).result().orElseGet(()->{constructor}),()->{constructor},SAVE_ID)')
  if rel.name in ['AreaSelectionNetworking.java','VariableLightNetworking.java']:
   s=s.replace('import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;','').replace('import net.minecraft.network.codec.StreamCodec;','').replace('import net.minecraft.network.protocol.common.custom.CustomPacketPayload;','').replace('RegistryFriendlyByteBuf','FriendlyByteBuf').replace(' implements CustomPacketPayload','')
   s=re.sub(r'\s*PayloadTypeRegistry\.play[CS]2[CS]\(\).register\([^;]+;', '',s)
   s=re.sub(r'public static final Type<\w+> ID\s*=\s*new Type<>\((.+)\);',r'public static final ResourceLocation ID=\1;',s)
   s=re.sub(r'\s*public static final StreamCodec<[^;]+;', '',s)
   s=re.sub(r'\s*(?:@Override )?public Type<\? extends CustomPacketPayload> type\(\)\s*\{return ID;\}', '',s)
   s=re.sub(r'\s*@Override public Type<\? extends CustomPacketPayload> type\(\) \{ return ID; \}', '',s)
   s=s.replace('private static Pick read(', 'public static Pick read(').replace('private static Preview read(', 'public static Preview read(').replace('private static SnapshotPayload read(', 'public static SnapshotPayload read(')
   s=s.replace('private void write(FriendlyByteBuf', 'public void write(FriendlyByteBuf')
   if rel.name=='AreaSelectionNetworking.java':
    s=s.replace('ServerPlayNetworking.registerGlobalReceiver(Pick.ID,(payload,context)->context.server().execute(()->pick(context.player(),payload)));', 'ServerPlayNetworking.registerGlobalReceiver(Pick.ID,(server,player,handler,buffer,response)->{Pick payload=Pick.read(buffer);server.execute(()->pick(player,payload));});')
    s=s.replace('ServerPlayNetworking.send(player,', 'send(player,')
    s=s.replace('    private record Session', '    private static void send(ServerPlayer player,Preview preview){var buffer=PacketByteBufs.create();preview.write(buffer);ServerPlayNetworking.send(player,Preview.ID,buffer);}\n    private record Session')
   else:
    s=s.replace('ServerPlayNetworking.send(player,', 'send(player,')
    s=s.replace('    private static SnapshotPayload snapshot(', '    private static void send(ServerPlayer player,SnapshotPayload payload){var buffer=net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();payload.write(buffer);ServerPlayNetworking.send(player,SnapshotPayload.ID,buffer);}\n\n    private static SnapshotPayload snapshot(')
  if rel.name=='ClientDaylightState.java':
   s=s.replace('(payload, context) -> MAILBOX.offer(payload.entries())','(client,handler,buffer,response) -> MAILBOX.offer(VariableLightNetworking.SnapshotPayload.read(buffer).entries())')
  if rel.name=='AreaSelectionClient.java':
   s=s.replace('(payload,context)->PENDING.set(payload)', '(client,handler,buffer,response)->PENDING.set(AreaSelectionNetworking.Preview.read(buffer))')
   s=s.replace('ClientPlayNetworking.send(new AreaSelectionNetworking.Pick(', 'sendPick(new AreaSelectionNetworking.Pick(')
   s=s.replace('    private static LightBox box(', '    private static void sendPick(AreaSelectionNetworking.Pick pick){var buffer=net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();pick.write(buffer);ClientPlayNetworking.send(AreaSelectionNetworking.Pick.ID,buffer);}\n    private static LightBox box(')
   s=s.replace('v.addVertex(m,p[i],p[i+1],p[i+2]).setColor(color);', 'v.vertex(m,p[i],p[i+1],p[i+2]).color((color>>16)&255,(color>>8)&255,color&255,(color>>>24)).endVertex();')
  (P/rel).parent.mkdir(parents=True,exist_ok=True);(P/rel).write_text(s,encoding='utf-8')
 for p in (P/'src').rglob('*.java'):
  s=p.read_text(encoding='utf-8').replace('.getFirst()', '.get(0)')
  # Deques keep their existing Java 17 operations; list sequenced APIs require adaptation only if encountered.
  p.write_text(s,encoding='utf-8')
 if version=='1.20.1':
  p=P/'build.gradle';s=p.read_text(encoding='utf-8').replace('    mavenCentral()', "    maven { url = 'https://maven.blamejared.com/' }\n    mavenCentral()")
  s=s.replace('    testImplementation platform', '    modCompileOnly "mezz.jei:jei-1.20.1-common-api:15.59.0.212"\n\n    testImplementation platform');p.write_text(s,encoding='utf-8')
