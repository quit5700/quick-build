from pathlib import Path
import json,re,shutil,zipfile,hashlib,xml.etree.ElementTree as ET
r=Path.cwd();v=r/'versions/1.1.4-test11/mc26.2';base=r/'versions/1.1.4-test10/mc26.2';out=r/'outputs/20261004_任务取消自然掉落26.2测试版11'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def logtext(p):
 data=p.read_bytes();return data.decode('utf-16' if data.startswith((b'\xff\xfe',b'\xfe\xff')) else 'utf-8-sig')
log=logtext(out/'最终27场景回归.log');assert 'Q5_MOVE_REGRESSION_PASS: 27 cases' in log and 'Q5_CASE_FAIL' not in log
assert 'BUILD SUCCESSFUL' in logtext(out/'完整构建与183项测试.log')
tests=0
for p in (v/'build/test-results/test').glob('TEST-*.xml'):
 a=ET.parse(p).getroot().attrib;tests+=int(a['tests']);assert all(int(a[x])==0 for x in ['failures','errors','skipped'])
assert tests==183
unchanged=['server/DeleteWandServer.java','server/DeleteTaskState.java','server/DeleteTaskStore.java','client/ClientWandController.java']
for name in unchanged:
 p=base/'src/main/java/cn/quit5700/buildingwand'/name
 if not p.exists():p=base/'src/client/java/cn/quit5700/buildingwand'/name
 if p.exists():assert p.read_bytes()==(v/p.relative_to(base)).read_bytes(),name
# All pre-existing tests stay unchanged, plus one new geometry test.
for p in (base/'src/test').rglob('*'):
 if p.is_file():assert p.read_bytes()==(v/p.relative_to(base)).read_bytes(),str(p)
name='src/main/java/cn/quit5700/buildingwand/server/WandActions.java';before=(base/name).read_text(encoding='utf-8');after=(v/name).read_text(encoding='utf-8');marker='    public static void captureMoveSelection';assert before[before.index(marker):]==after[after.index(marker):]
for ns in ['building_wand','light','pathfinding_beacon']:
 data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in (v/f'src/main/resources/assets/{ns}/lang').glob('*.json')};assert len(data)==8
 for d in data.values():
  assert d.keys()==data['en_us'].keys()
  for k,s in data['en_us'].items():assert re.findall(r'%(?:\d+\$)?[sd]',s)==re.findall(r'%(?:\d+\$)?[sd]',d[k])
files=list((v/'dist').glob('*.jar'));assert len(files)==2;runtime=next(p for p in files if 'sources' not in p.name)
with zipfile.ZipFile(runtime) as z:
 assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.4-test11'
 assert not any('WandSmokeChecks' in n for n in z.namelist())
 assert 'cn/quit5700/buildingwand/network/BuildNetworking$CancelAll.class' in z.namelist()
 assert 'cn/quit5700/buildingwand/network/BuildNetworking$Cancelled.class' in z.namelist()
 assert not any(b'q5_test_property_pair' in z.read(n) for n in z.namelist() if n.endswith('.class'))
 for cl in ['StructurePlacement','ReplacementRules']:assert f'cn/quit5700/buildingwand/server/{cl}.class' in z.namelist()
 language=json.loads(z.read('assets/building_wand/lang/zh_cn.json'));assert '不能再用区域替换' in language['help.q5.structure_6'];assert '49×49' in language['help.q5.structure_4']
result={'版本':'1.1.4-test11','自动测试':183,'独立世界回归':27,'失败':0,'八语言':'键集合及参数一致','原181项测试':'全部保留原样','移动快照及删除核心':'与测试版10保持一致','成品测试类':'未包含','用户游戏实机':'待重启验收'}
(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
bug=r/'docs/bugs/BUG-20261004-TASK-CANCEL-NATURAL-DROPS.md';bug.write_text(bug.read_text(encoding='utf-8')+'\n最终核验：完整清理构建成功，183项测试全部通过；27项独立世界回归通过且无失败标记；八语言键集合和参数一致；原173项测试文件未修改，移动快照逻辑及删除核心代码未变；成品不含世界测试类。真实客户端待用户验收。\n',encoding='utf-8')
p=r/'docs/bugs/BUG_INDEX.md';p.write_text(p.read_text(encoding='utf-8').replace('27场景通过，待完整构建核验。','183项测试及27项独立世界回归通过，实机待验收。'),encoding='utf-8')
for name in ['README.md','VERSION_INFO.md']:
 p=v/name;p.write_text(p.read_text(encoding='utf-8').replace('核验结果将在交付文件中记录。','完整清理构建、181项自动测试及27项独立世界回归通过；八语言键集合和参数一致，成品不含测试类。'),encoding='utf-8')
a=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\任务取消自然掉落_26.2测试版11_20261004');a.mkdir(parents=True,exist_ok=True);dist=r/'dist/1.1.4-test11/mc26.2';dist.mkdir(parents=True,exist_ok=True)
for p in files:shutil.copy2(p,dist/p.name);shutil.copy2(p,a/p.name)
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,a/name)
shutil.copy2(bug,a/'问题与修复记录.md')
for p in out.iterdir():
 if p.is_file():shutil.copy2(p,a/p.name)
with zipfile.ZipFile(a/'183项自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in (v/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
scriptnames=['implement_tasks_test11.py','localize_tasks_test11.py','fix_tasks_api_test11.py','test_tasks_test11.py','fix_task_tests_test11.py','document_tasks_test11.py','deliver_tasks_test11.py']
with zipfile.ZipFile(a/'1.1.4-test11-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(x in {'build','.gradle','run','dist','__pycache__'} for x in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [r/'AGENTS.md',r/'docs/PORTING_NOTES.md',bug,*[r/'scripts'/n for n in scriptnames]]:z.write(p,p.relative_to(r).as_posix())
mods=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods');old=list(mods.glob('quick-build*.jar'));assert len(old)==1 and old[0].name=='quick-build-1.1.4-test10-mc26.2-fabric.jar'
staged=mods/(runtime.name+'.staged');shutil.copy2(runtime,staged);assert sha(staged)==sha(runtime);backup=a/(old[0].stem+'.安装前.jar');shutil.copy2(old[0],backup);assert sha(old[0])==sha(backup);installed=False
try:old[0].unlink()
except PermissionError:pass
else:
 try:staged.rename(mods/runtime.name)
 except Exception:shutil.copy2(backup,old[0]);raise
 assert len(list(mods.glob('quick-build*.jar')))==1 and sha(mods/runtime.name)==sha(runtime);installed=True
status=(v/'README.md').read_text(encoding='utf-8')+'\n安装状态：'+('测试版11已安装，只有一个快捷建筑运行包，安装摘要与成品一致。' if installed else '游戏占用测试版10；测试版11已暂存.jar.staged，待用户正常退出后替换。')+'\n'
(a/'先读我_交接.md').write_text(status,encoding='utf-8');(a.parent/'最新状态_建筑移动法杖26.2.md').write_text(status,encoding='utf-8')
with (a.parent/'先读我_本对话交接.md').open('a',encoding='utf-8') as f:f.write('\n最新交付：任务取消自然掉落_26.2测试版11_20261004。183项测试及21项世界回归通过；取消本人跨维度建筑任务、余位内容一次掉落及自然支撑规则已核验。其他版本与GitHub仍等待用户叫更新。\n')
checks={p.name:sha(p) for p in a.iterdir() if p.suffix in {'.jar','.zip'}};(a/'文件校验.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8');(a/'安装核验.json').write_text(json.dumps({'已安装':installed,'安装文件':str(mods/runtime.name),'SHA256':sha(runtime)},ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps({'已安装':installed,'交接目录':str(a),'核验':result},ensure_ascii=False))