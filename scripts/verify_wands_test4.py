from pathlib import Path
import json,re,xml.etree.ElementTree as ET,zipfile,hashlib
root=Path.cwd();v=root/'versions/1.1.4-test4/mc26.2';base=root/'versions/1.1.4-test2/mc26.2';out=root/'outputs/20261004_移动恢复原模式26.2测试版4';out.mkdir(parents=True,exist_ok=True)
relative='src/main/java/cn/quit5700/buildingwand/server/WandActions.java';a=(base/relative).read_text(encoding='utf-8');b=(v/relative).read_text(encoding='utf-8')
restriction='''        int limit = WandSettingsState.get(server).moveLimit();
        if (positions.size() > limit) {
            fail(player, "message.quick_build.selection_too_large", limit);
            sendSelectionResult(player, false);
            return;
        }
'''
a=a[a.index('    public static void captureMoveSelection'):].replace(restriction,'',1);b=b[b.index('    public static void captureMoveSelection'):];assert a==b,'Moving methods differ beyond removing the original count guard'
relative='src/client/java/cn/quit5700/buildingwand/client/ClientWandController.java';a=(base/relative).read_text(encoding='utf-8');b=(v/relative).read_text(encoding='utf-8');assert a[a.index('    private static void tickMoveWand'):]==b[b.index('    private static void tickMoveWand'):],'Movement controls changed'
relative='src/main/resources/quick_build.mixins.json';assert json.loads((base/relative).read_text(encoding='utf-8'))==json.loads((v/relative).read_text(encoding='utf-8')),'extra movement mixin still active'
count=failures=errors=0
for p in (v/'build/test-results/test').glob('TEST-*.xml'):
 data=ET.parse(p).getroot().attrib;count+=int(data['tests']);failures+=int(data['failures']);errors+=int(data['errors'])
assert count==170 and not failures and not errors
for ns in ['building_wand','light','pathfinding_beacon']:
 data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in (v/f'src/main/resources/assets/{ns}/lang').glob('*.json')};assert len(data)==8
 for lang,strings in data.items():
  assert set(strings)==set(data['en_us'])
  for key,text in data['en_us'].items():assert re.findall(r'%(?:\d+\$)?[sd]',text)==re.findall(r'%(?:\d+\$)?[sd]',strings[key])
for p in (v/'src/main/resources').rglob('*.json'):json.loads(p.read_text(encoding='utf-8'))
files=list((v/'dist').glob('*.jar'));assert len(files)==2
runtime=next(p for p in files if 'sources' not in p.name)
with zipfile.ZipFile(runtime) as z:
 assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.4-test4'
 assert 'cn/quit5700/buildingwand/client/MoveBoxRenderer.class' not in z.namelist()
 assert not any('WandSmokeChecks' in n for n in z.namelist())
 assert 'cn/quit5700/buildingwand/server/BuildTaskState.class' in z.namelist()
result={'版本':'1.1.4-test4','自动测试':count,'失败':failures,'错误':errors,'原移动服务端代码对照':'除数量上限判断外完全一致','原移动客户端代码对照':'完全一致','移动混入配置':'恢复原配置','建筑三模式':'保留','说明台与无上限说明':'保留并更新','八语言及参数':'通过','文件':{p.name:{'字节':p.stat().st_size,'SHA256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in files}}
(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False,indent=2))