from pathlib import Path
import shutil,re,json
r=Path.cwd();src=r/'versions/1.1.4/mc26.2';base=r/'versions/1.1.3/mc26.2';versions=['26.1.2','1.21.11','1.21.8','1.21.4','1.21.1','1.20.4','1.20.1']
changed=[]
for f in (src/'src').rglob('*'):
 if f.is_file() and 'smoke' not in f.relative_to(src).parts:
  rel=f.relative_to(src);before=base/rel
  if not before.exists() or (f.read_text(encoding='utf-8')!=before.read_text(encoding='utf-8') if f.suffix in ['.java','.json'] else f.read_bytes()!=before.read_bytes()):changed.append(rel)
for v in versions:
 d=r/'versions/1.1.4'/('mc'+v);shutil.copytree(r/'versions/1.1.3'/('mc'+v),d,ignore=shutil.ignore_patterns('build','.gradle','run','dist'))
 f=d/'gradle.properties';f.write_text(re.sub(r'^mod_version=.*$','mod_version=1.1.4',f.read_text(encoding='utf-8'),flags=re.M),encoding='utf-8')
 for rel in changed:
  if rel.name in ['ModItems.java','ModItemGroup.java','AreaSelectionClient.java','MoveUpdates.java','MoveBlockEventsAccessor.java','fabric.mod.json']:continue
  target=d/rel;target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(src/rel,target)
 f=d/'src/main/java/cn/quit5700/buildingwand/registry/ModItems.java';s=f.read_text(encoding='utf-8');itype='Identifier' if 'import net.minecraft.resources.Identifier;' in s else 'ResourceLocation';anchor='    private static final '+itype+' WAND_SETTINGS_STATION_ID';addition=f'    private static final {itype} DELETE_WAND_ID = BuildingWandMod.id("delete_wand");\n    public static final Item DELETE_WAND = Registry.register(BuiltInRegistries.ITEM,DELETE_WAND_ID,new cn.quit5700.buildingwand.item.DeletionWandItem(itemProperties(DELETE_WAND_ID).durability(512)'+('.enchantable(1)' if v not in ['1.21.1','1.20.4','1.20.1'] else '')+'));\n';assert anchor in s;s=s.replace(anchor,addition+anchor);f.write_text(s,encoding='utf-8')
 f=d/'src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java';s=f.read_text(encoding='utf-8');s=re.sub(r'((?:output|entries)\.accept\((?:cn\.quit5700\.buildingwand\.registry\.)?ModItems\.MOVE_WAND\);)',r'\1\n                        output.accept(cn.quit5700.buildingwand.registry.ModItems.DELETE_WAND);',s) if 'output.accept' in s else s.replace('entries.accept(cn.quit5700.buildingwand.registry.ModItems.MOVE_WAND);','entries.accept(cn.quit5700.buildingwand.registry.ModItems.MOVE_WAND);\n entries.accept(cn.quit5700.buildingwand.registry.ModItems.DELETE_WAND);');assert 'DELETE_WAND' in s;f.write_text(s,encoding='utf-8')
 for f in list((d/'src/main/java/cn/quit5700/buildingwand').rglob('*.java'))+list((d/'src/client/java/cn/quit5700/buildingwand').rglob('*.java')):
  s=f.read_text(encoding='utf-8')
  if v!='26.1.2':s=s.replace('PayloadTypeRegistry.clientboundPlay()','PayloadTypeRegistry.playS2C()').replace('PayloadTypeRegistry.serverboundPlay()','PayloadTypeRegistry.playC2S()').replace('GuiGraphicsExtractor','GuiGraphics').replace('extractRenderState(', 'render(').replace('graphics.centeredText(', 'graphics.drawCenteredString(').replace('graphics.text(', 'graphics.drawString(').replace('graphics.outline(', 'graphics.renderOutline(').replace('super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.translatable("text.quick_build.080"));','super(Component.translatable("text.quick_build.080"));')
  if v not in ['26.1.2','1.21.11']:s=s.replace('Identifier','ResourceLocation').replace('.dimension().identifier()', '.dimension().location()').replace('client.getWindow().handle()', 'client.getWindow().getWindow()')
  if v in ['1.21.11','1.21.8']:s=s.replace('new SavedDataType<>(BuildingWandMod.id("build_tasks"),','new SavedDataType<>("building_wand_build_tasks",').replace('new SavedDataType<>(BuildingWandMod.id("delete_tasks"),','new SavedDataType<>("building_wand_delete_tasks",')
  s=re.sub(r'        if\(Boolean.getBoolean\("q5.wandSmoke"\)\)\{.*?\n        \}\n','',s,flags=re.S)
  f.write_text(s,encoding='utf-8')
print(json.dumps({'versions':versions,'overlay':len(changed)},ensure_ascii=False))
