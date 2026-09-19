from pathlib import Path
import shutil,subprocess,json
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'versions/1.1.3-test9/mc26.2'
RELEASE=ROOT/'versions/1.1.3'
VERSIONS=['26.2','26.1.2','1.21.11','1.21.8','1.21.4','1.21.1','1.20.4','1.20.1']
changed=subprocess.check_output(['git','diff','--name-only','175b602','HEAD','--','mc26.2/src'],cwd=ROOT).decode('utf-8').splitlines()
changed=[Path(p).relative_to('mc26.2') for p in changed]
for p in BASE.rglob('*'):
 if p.is_file() and 'src' in p.parts and not any(x in p.parts for x in ['build','.gradle','dist']):
  rel=p.relative_to(BASE)
  if not (ROOT/'mc26.2'/rel).exists() or p.read_bytes()!=(ROOT/'mc26.2'/rel).read_bytes():changed.append(rel)
changed=sorted(set(changed))
for v in VERSIONS:
 dest=RELEASE/('mc'+v)
 if dest.exists():continue
 src=BASE if v=='26.2' else ROOT/('mc'+v)
 shutil.copytree(src,dest,ignore=shutil.ignore_patterns('.gradle','build','dist','run'))
 props=dest/'gradle.properties';s=props.read_text(encoding='utf-8');import re
 s=re.sub(r'^mod_version=.*$', 'mod_version=1.1.3',s,flags=re.M);props.write_text(s,encoding='utf-8')
 if v=='26.2':continue
 for rel in changed:
  # Keep each version's client rendering hooks and registration APIs. Patch these separately.
  if str(rel).replace('\\','/').endswith('ClientWandController.java'):continue
  if 'src/main/java/cn/quit5700/light/registry/' in rel.as_posix():continue
  if rel.as_posix().endswith('ModItemGroup.java'):continue
  if rel.as_posix().endswith('fabric.mod.json'):continue
  source=BASE/rel
  if not source.exists():continue
  if '/assets/' in rel.as_posix() and not ('/assets/light/' in rel.as_posix()):continue
  target=dest/rel;target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,target)
 # Same picking target and input ownership, preserving the rest of the version's wand APIs.
 p=dest/'src/client/java/cn/quit5700/buildingwand/client/ClientWandController.java';s=p.read_text(encoding='utf-8')
 s=s.replace('private static void tick(Minecraft client) {','private static void tick(Minecraft client) {\n        if(cn.quit5700.light.client.AreaSelectionClient.isSelecting()) {\n            wasAttacking=client.options.keyAttack.isDown();wasUsing=client.options.keyUse.isDown();\n            return;\n        }')
 s=s.replace('    private static BlockPos buildingTarget(Minecraft client, HitResult hit) {','    public static BlockPos buildingSelectionTarget(Minecraft client) {\n        return buildingTarget(client,client.player.pick(MAX_DISTANCE,0.0F,false));\n    }\n\n    private static BlockPos buildingTarget(Minecraft client, HitResult hit) {')
 p.write_text(s,encoding='utf-8')
 for name,anchor,addition in [
 ('LightBlocks.java','    public static final VariableConstantLightBlock VARIABLE_CONSTANT_LIGHT = register("variable_constant_light", VariableConstantLightBlock::new);','\n    public static final VariableConstantLightBlock COPY_VARIABLE_CONSTANT_LIGHT = register("copy_variable_constant_light", VariableConstantLightBlock::new);'),
 ('LightItems.java','    public static final BlockItem VARIABLE_CONSTANT_LIGHT_ITEM = block("variable_constant_light", LightBlocks.VARIABLE_CONSTANT_LIGHT);','\n    public static final BlockItem COPY_VARIABLE_CONSTANT_LIGHT_ITEM = block("copy_variable_constant_light", LightBlocks.COPY_VARIABLE_CONSTANT_LIGHT);')]:
  p=dest/'src/main/java/cn/quit5700/light/registry'/name;s=p.read_text(encoding='utf-8');assert anchor in s;s=s.replace(anchor,anchor+addition);p.write_text(s,encoding='utf-8')
 p=dest/'src/main/java/cn/quit5700/light/registry/LightBlockEntities.java';s=p.read_text(encoding='utf-8').replace('VariableConstantLightBlockEntity::new, LightBlocks.VARIABLE_CONSTANT_LIGHT)','VariableConstantLightBlockEntity::new, LightBlocks.VARIABLE_CONSTANT_LIGHT, LightBlocks.COPY_VARIABLE_CONSTANT_LIGHT)');p.write_text(s,encoding='utf-8')
 p=dest/'src/main/resources/fabric.mod.json';data=json.loads(p.read_text(encoding='utf-8'));data['entrypoints']['jei_mod_plugin']=['cn.quit5700.light.compat.jei.HiddenLegacyLightPlugin'];data.setdefault('suggests',{})['jei']='*'
 if 'quick_build.mixins.json' not in data['mixins']:data['mixins'].append('quick_build.mixins.json')
 p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 # Never introduce a creative/recipe entry for the legacy copy.
 for p in (dest/'src/main/resources/data/light').rglob('copy_variable_constant_light.json'):
  if p.parent.name in ['recipe','recipes']:p.unlink()
 # Version-specific API transformations are applied in the following step.
print(json.dumps({'release':str(RELEASE),'versions':VERSIONS,'overlay_files':len(changed)},ensure_ascii=False))
