from pathlib import Path
import json,re,shutil,zipfile,hashlib,xml.etree.ElementTree as ET
r=Path.cwd();v=r/'versions/1.1.4-test9/mc26.2';b=r/'versions/1.1.4-test8/mc26.2'
for p in (b/'src').rglob('*.java'):
 assert p.read_bytes()==(v/p.relative_to(b)).read_bytes(),str(p)
n=0
for p in (v/'build/test-results/test').glob('TEST-*.xml'):
 a=ET.parse(p).getroot().attrib;n+=int(a['tests']);assert all(int(a[k])==0 for k in ['failures','errors','skipped'])
assert n==173
for p in (b/'src/main/resources').rglob('*'):
 if not p.is_file():continue
 q=v/p.relative_to(b)
 if p.parent.name=='lang' and p.parent.parent.name=='building_wand':
  before=json.loads(p.read_text(encoding='utf-8'));after=json.loads(q.read_text(encoding='utf-8'))
  assert before.keys()==after.keys();assert {k for k in before if before[k]!=after[k]}=={'text.quick_build.109'}
 else:assert p.read_bytes()==q.read_bytes(),str(p)
for ns in ['building_wand','light','pathfinding_beacon']:
 data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in (v/f'src/main/resources/assets/{ns}/lang').glob('*.json')};assert len(data)==8
 for d in data.values():
  assert d.keys()==data['en_us'].keys()
  for k,s in data['en_us'].items():assert re.findall(r'%(?:\d+\$)?[sd]',s)==re.findall(r'%(?:\d+\$)?[sd]',d[k])
files=list((v/'dist').glob('*.jar'));assert len(files)==2;runtime=next(p for p in files if 'sources' not in p.name)
with zipfile.ZipFile(runtime) as z:
 assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.4-test9'
 assert not any('WandSmokeChecks' in x for x in z.namelist())
 assert json.loads(z.read('assets/building_wand/lang/zh_cn.json'))['text.quick_build.109']=='尚未选择建造材料，请按潜行键＋右键点击方块选择材料，再左键记录第一点。'
out=r/'outputs/20261004_材料选择提示26.2测试版9';out.mkdir(parents=True,exist_ok=True)
bug=r/'docs/bugs/BUG-20261004-BUILD-MATERIAL-HINT.md'
bug.write_text('''# 建筑法杖材料选择提示修正

项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。
分类：玩家提示文字。版本：1.1.4-test9/mc26.2。
现象：未选择建造材料时，左键仍提示旧的Alt＋左键选择方块操作。
根因：BuildWandServer（建筑法杖服务端）材料为空分支使用text.quick_build.109翻译键，该键内容在操作变更时漏更新。
修改文件：building_wand/lang下八语言的text.quick_build.109；版本属性与版本说明。所有Java（爪哇）源码与测试版8逐字节一致。
修复：提示改为潜行键＋右键选择材料，再左键记录第一点。替换及保护规则未改；用户已确认模组方块未被替换无需处理。
验证：173项自动测试全部通过；八语言键集合及参数一致；资源对照确认仅该提示变化；成品版本与测试类排除核验通过。真实游戏显示待用户重启验收。
避免：操作改动时应同步检查缺少材料等错误分支的旧提示，不只修改正常流程说明。普通文字修改不新增重复自动测试。
''',encoding='utf-8')
for name,line in [('BUG_INDEX.md','- [BUG-20261004-BUILD-MATERIAL-HINT](BUG-20261004-BUILD-MATERIAL-HINT.md)：测试版9修正旧材料选择提示；173项测试通过，代码未改，实机待重启验收。'),('REGRESSION_CHECKLIST.md','- 材料未选择提示：潜行键＋右键选择材料，再左键记录第一点。测试版9八语言及173项测试核验通过；实际显示待重启验收。')]:
 p=r/'docs/bugs'/name;p.write_text(p.read_text(encoding='utf-8')+'\n'+line+'\n',encoding='utf-8')
p=r/'docs/bugs/BUG-20261004-REGION-REPLACEMENT.md';p.write_text(p.read_text(encoding='utf-8')+'\n用户确认模组方块未被替换无需处理；本轮仅修正旧材料选择提示，见BUG-20261004-BUILD-MATERIAL-HINT.md，保护规则保持。\n',encoding='utf-8')
a=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\材料选择提示_26.2测试版9_20261004');a.mkdir(parents=True,exist_ok=True)
d=r/'dist/1.1.4-test9/mc26.2';d.mkdir(parents=True,exist_ok=True)
for p in files:shutil.copy2(p,d/p.name);shutil.copy2(p,a/p.name)
mods=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods');old=list(mods.glob('quick-build*.jar'));assert len(old)==1 and old[0].name=='quick-build-1.1.4-test8-mc26.2-fabric.jar'
staged=mods/(runtime.name+'.staged');shutil.copy2(runtime,staged);assert staged.read_bytes()==runtime.read_bytes()
backup=a/(old[0].stem+'.安装前.jar');shutil.copy2(old[0],backup);assert old[0].read_bytes()==backup.read_bytes();installed=False
try:old[0].unlink()
except PermissionError:pass
else:
 try:staged.rename(mods/runtime.name)
 except Exception:shutil.copy2(backup,old[0]);raise
 assert (mods/runtime.name).read_bytes()==runtime.read_bytes();installed=True
result={'版本':'1.1.4-test9','自动测试':n,'失败':0,'八语言':'键及参数一致','Java源码':'与测试版8逐字节一致','功能资源':'仅八语言109提示变化','已安装':installed,'安装文件':str(mods/runtime.name),'SHA256':hashlib.sha256(runtime.read_bytes()).hexdigest(),'实机显示':'待重启验收','其他版本和GitHub':'暂停等待用户叫更新'}
(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8');shutil.copy2(out/'完整核验.json',a/'完整核验.json')
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,a/name)
shutil.copy2(bug,a/'问题记录.md')
with zipfile.ZipFile(a/'173项自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in (v/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
with zipfile.ZipFile(a/'1.1.4-test9-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(x in {'build','.gradle','run','dist','__pycache__'} for x in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [r/'AGENTS.md',bug,r/'scripts/prepare_material_hint_test9.py',r/'scripts/deliver_material_hint_test9.py']:z.write(p,p.relative_to(r).as_posix())
status=(v/'README.md').read_text(encoding='utf-8')+'\n安装状态：'+('测试版9已安装，模组目录仅一个快捷建筑运行包。' if installed else '游戏占用测试版8，测试版9已暂存，待正常退出后替换。')+'\n173项测试通过，所有Java源码未变。其他版本及GitHub仍暂停；不关机、不修改存档。\n'
(a/'先读我_交接.md').write_text(status,encoding='utf-8');(a.parent/'最新状态_建筑移动法杖26.2.md').write_text(status,encoding='utf-8')
with (a.parent/'先读我_本对话交接.md').open('a',encoding='utf-8') as f:f.write('\n最新交付：材料选择提示_26.2测试版9_20261004；仅修正旧提示，保护规则不变。其他版本等待用户叫更新。\n')
checks={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in a.iterdir() if p.suffix in {'.jar','.zip'}};(a/'文件校验.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps(result,ensure_ascii=False))