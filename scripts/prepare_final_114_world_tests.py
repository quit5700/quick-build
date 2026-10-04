from pathlib import Path
import re,shutil
r=Path.cwd();root=r/'versions/1.1.4';base=root/'mc26.2';ref=(base/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java').read_text(encoding='utf-8');oldrun=r/'versions/1.1.4-test11/mc26.2/run'
for i,v in enumerate(['26.2','26.1.2','1.21.11','1.21.8','1.21.4','1.21.1','1.20.4','1.20.1']):
 d=root/('mc'+v);s=ref
 if v not in ['26.2','26.1.2']:
  s=s.replace('public void sendOverlayMessage(net.minecraft.network.chat.Component message)','public void displayClientMessage(net.minecraft.network.chat.Component message,boolean overlay)')
 if v not in ['26.2','26.1.2','1.21.11']:
  s=s.replace('Identifier','ResourceLocation')
 if v in ['1.21.1','1.20.4','1.20.1']:s=s.replace('BuiltInRegistries.BLOCK.getValue(', 'BuiltInRegistries.BLOCK.get(')
 if v in ['1.21.4','1.21.1','1.20.4','1.20.1']:
  s=s.replace('Block.UPDATE_SKIP_ALL_SIDEEFFECTS','QuietPlacement.FLAG').replace('world.setBlock(', 'QuietPlacement.setBlock(world,').replace('public static void register(){var id=', 'public static void register(){var id=')
 if v in ['1.21.1','1.20.4','1.20.1']:
  s=re.sub(r'\.setId\(net.minecraft.resources.ResourceKey.create\(net.minecraft.core.registries.Registries.(BLOCK|ITEM),id\)\)','',s)
 if v.startswith('1.20.'):
  s=s.replace('ResourceLocation.parse(', 'new ResourceLocation(').replace(')).getOrThrow()', ')).getOrThrow(false,e->{throw new IllegalStateException(e);})')
 if v=='1.20.1':s=s.replace(',net.minecraft.server.level.ClientInformation.createDefault()', '')
 s=s.replace('server.getPlayerList().getPlayersByUUID()', 'players(server)')
 helper=''' @SuppressWarnings("unchecked") private static Map<UUID,net.minecraft.server.level.ServerPlayer> players(net.minecraft.server.MinecraftServer server){try{var f=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");f.setAccessible(true);return (Map<UUID,net.minecraft.server.level.ServerPlayer>)f.get(server.getPlayerList());}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
'''
 s=s.replace(' private interface Case',helper+' private interface Case')
 f=d/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s,encoding='utf-8')
 f=d/'src/main/java/cn/quit5700/buildingwand/BuildingWandMod.java';s=f.read_text(encoding='utf-8')
 if 'q5.wandSmoke' not in s:s=s.replace('    public void onInitialize() {','''    public void onInitialize() {
        if(Boolean.getBoolean("q5.wandSmoke")){
            try{Class.forName("cn.quit5700.buildingwand.server.WandSmokeChecks").getMethod("register").invoke(null);}
            catch(ReflectiveOperationException e){throw new IllegalStateException("Smoke harness unavailable",e);}
        }
''')
 f.write_text(s,encoding='utf-8')
 f=d/'build.gradle';s=f.read_text(encoding='utf-8')
 if "hasProperty('wandSmoke')" not in s:s+='''
if(project.hasProperty('wandSmoke')){sourceSets.main.java.srcDir 'src/smoke/java';loom.runs.server.vmArg '-Dq5.wandSmoke=true'}
tasks.named("jar"){exclude "cn/quit5700/buildingwand/server/WandSmokeChecks*"}
'''
 f.write_text(s,encoding='utf-8')
 run=d/'run';run.mkdir(exist_ok=True);shutil.copy2(oldrun/'eula.txt',run/'eula.txt');props=(oldrun/'server.properties').read_text(encoding='utf-8');props=re.sub(r'^server-port=.*$',f'server-port={25600+i}',props,flags=re.M);props=re.sub(r'^level-name=.*$','level-name=q5-formal-114-regression-world',props,flags=re.M);(run/'server.properties').write_text(props,encoding='utf-8')
print('已保留各版本27项独立测试世界回归，测试类仅显式测试开关启用，运行包排除。')
