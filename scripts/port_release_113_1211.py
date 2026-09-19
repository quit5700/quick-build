from pathlib import Path
R=Path(__file__).resolve().parents[1]/'versions/1.1.3'
P=R/'mc1.21.1'
files=['LightMod.java','block/VariableConstantLightBlockEntity.java','client/LightClient.java','client/VariableConstantLightScreen.java','light/AreaLightState.java','light/ConstantLightTaskQueue.java','light/DaylightOcclusion.java','light/RealAreaLightState.java','light/VariableLightTaskState.java','menu/VariableConstantLightMenu.java','mixin/DaylightChunkMixin.java','mixin/NaturalSpawnerMixin.java','mixin/PhantomSpawnerMixin.java','network/AreaSelectionNetworking.java','network/VariableLightNetworking.java']
paths=[Path('src/main/java/cn/quit5700/light')/f for f in files]+[Path('src/client/java/cn/quit5700/light')/f for f in ['client/AreaSelectionClient.java','client/ClientDaylightState.java','compat/jei/HiddenLegacyLightPlugin.java']]
for rel in paths:
 s=(R/'mc1.21.4'/rel).read_text(encoding='utf-8')
 if rel.name=='AreaSelectionClient.java':
  s=s.replace('world.isClientSide() && selecting ? net.minecraft.world.InteractionResult.FAIL:net.minecraft.world.InteractionResult.PASS);\n        ClientTickEvents', 'world.isClientSide() && selecting ? net.minecraft.world.InteractionResultHolder.fail(player.getItemInHand(hand)):net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand)));\n        ClientTickEvents')
 (P/rel).parent.mkdir(parents=True,exist_ok=True);(P/rel).write_text(s,encoding='utf-8')
p=P/'build.gradle';s=p.read_text(encoding='utf-8').replace('    mavenCentral()', "    maven { url = 'https://maven.blamejared.com/' }\n    mavenCentral()")
s=s.replace('    testImplementation platform', '    modCompileOnly "mezz.jei:jei-1.21.1-common-api:19.56.0.441"\n\n    testImplementation platform');p.write_text(s,encoding='utf-8')
