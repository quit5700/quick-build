from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
P=ROOT/'versions/1.1.3/mc1.21.11'
for p in (P/'src').rglob('*.java'):
 s=p.read_text(encoding='utf-8')
 s=s.replace('PayloadTypeRegistry.clientboundPlay()', 'PayloadTypeRegistry.playS2C()').replace('PayloadTypeRegistry.serverboundPlay()', 'PayloadTypeRegistry.playC2S()')
 s=s.replace('(world, chunk, newChunk)', '(world, chunk)').replace('chunk.getPos().x()', 'chunk.getPos().x').replace('chunk.getPos().z()', 'chunk.getPos().z')
 if p.name in ['AreaLightState.java','RealAreaLightState.java','VariableLightTaskState.java']:
  for key in ['area_light_ranges','real_area_lights','variable_light_tasks']:
   s=s.replace(f'LightMod.id("{key}")',f'"light_{key}"')
 if p.name=='VariableConstantLightScreen.java':
  s=s.replace('GuiGraphicsExtractor','GuiGraphics').replace('super(menu, inventory, title, 248, 176);','super(menu, inventory, title);\n        imageWidth=248;imageHeight=176;')
  s=s.replace('extractLabels(', 'renderLabels(').replace('extractRenderState(', 'render(').replace('graphics.centeredText(', 'graphics.drawCenteredString(').replace('graphics.text(font,line,centerX-font.width(line)/2,lineY,0xFFFFB040);','graphics.drawString(font,line,centerX-font.width(line)/2,lineY,0xFFFFB040);')
  s=s.replace('    private Component modeName(', '    @Override\n    protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY) {}\n\n    private Component modeName(')
 if p.name=='AreaSelectionClient.java':
  s=s.replace('client.rendering.v1.level.LevelRenderEvents','client.rendering.v1.world.WorldRenderEvents').replace('LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN','WorldRenderEvents.BEFORE_TRANSLUCENT').replace('context.poseStack()', 'context.matrices()').replace('context.submitNodeCollector()', 'context.commandQueue()')
 p.write_text(s,encoding='utf-8')
