from pathlib import Path
R=Path(__file__).resolve().parents[1]/'versions/1.1.3'
P=R/'mc1.21.8'
for relative in ['src/main/java/cn/quit5700/light/client/VariableConstantLightScreen.java','src/client/java/cn/quit5700/light/client/AreaSelectionClient.java']:
 (P/relative).write_text((R/'mc1.21.11'/relative).read_text(encoding='utf-8'),encoding='utf-8')
for p in (P/'src').rglob('*.java'):
 s=p.read_text(encoding='utf-8')
 s=s.replace('net.minecraft.resources.Identifier','net.minecraft.resources.ResourceLocation').replace('Identifier','ResourceLocation')
 s=s.replace('PayloadTypeRegistry.clientboundPlay()', 'PayloadTypeRegistry.playS2C()').replace('PayloadTypeRegistry.serverboundPlay()', 'PayloadTypeRegistry.playC2S()')
 s=s.replace('ServerEntityLevelChangeEvents','ServerEntityWorldChangeEvents').replace('AFTER_PLAYER_CHANGE_LEVEL','AFTER_PLAYER_CHANGE_WORLD').replace('PlayerLookup.level(world)','PlayerLookup.world(world)')
 s=s.replace('(world, chunk, newChunk)', '(world, chunk)').replace('chunk.getPos().x()', 'chunk.getPos().x').replace('chunk.getPos().z()', 'chunk.getPos().z').replace('.dimension().identifier()', '.dimension().location()')
 if p.name in ['AreaLightState.java','RealAreaLightState.java','VariableLightTaskState.java']:
  for key in ['area_light_ranges','real_area_lights','variable_light_tasks']:s=s.replace(f'LightMod.id("{key}")',f'"light_{key}"')
 if p.name=='AreaSelectionClient.java':
  s=s.replace('client.rendering.v1.world.WorldRenderEvents','client.rendering.v1.WorldRenderEvents').replace('WorldRenderEvents.BEFORE_TRANSLUCENT','WorldRenderEvents.AFTER_TRANSLUCENT').replace('context.matrices()', 'context.matrixStack()').replace('client.getWindow().handle()', 'client.getWindow().getWindow()')
  s=s.replace('net.minecraft.client.renderer.rendertype.RenderTypes','net.minecraft.client.renderer.RenderType')
  s=s.replace('context.commandQueue().submitCustomGeometry(pose,RenderTypes.debugQuads(),(p,buffer)->{','var buffer=context.consumers().getBuffer(RenderType.debugQuads());\n            var p=pose.last();\n            {').replace('});pose.popPose();','}pose.popPose();')
 p.write_text(s,encoding='utf-8')
p=P/'build.gradle';s=p.read_text(encoding='utf-8');s=s.replace('    mavenCentral()', "    maven { url = 'https://maven.blamejared.com/' }\n    mavenCentral()")
s=s.replace('    testImplementation platform', '    modCompileOnly "mezz.jei:jei-1.21.8-fabric-api:24.2.0.6"\n    modCompileOnly "mezz.jei:jei-1.21.8-common-api:24.2.0.6"\n\n    testImplementation platform')
p.write_text(s,encoding='utf-8')
