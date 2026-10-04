from pathlib import Path
import json,re,shutil,zipfile,hashlib,xml.etree.ElementTree as ET
r=Path.cwd();v=r/'versions/1.1.4-test7/mc26.2';base=r/'versions/1.1.4-test6/mc26.2';out=r/'outputs/20261004_移动红石修复26.2测试版7'
changed=[p.relative_to(base).as_posix() for p in (base/'src').rglob('*') if p.is_file() and p.read_bytes()!=(v/p.relative_to(base)).read_bytes()]
assert set(changed)=={'src/main/java/cn/quit5700/buildingwand/server/WandActions.java','src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java'},changed
count=0
for p in (v/'build/test-results/test').glob('TEST-*.xml'):
 a=ET.parse(p).getroot().attrib;count+=int(a['tests']);assert int(a['failures'])==int(a['errors'])==0
assert count==170
before=(out/'修复前_独立世界.log').read_text(encoding='utf-8');after=(out/'修复后_独立世界.log').read_text(encoding='utf-8');assert 'Q5_MOVE_REGRESSION_FAIL: 5' in before and 'Q5_MOVE_REGRESSION_PASS: 7 cases' in after and 'Q5_CASE_FAIL' not in after
for ns in ['building_wand','light','pathfinding_beacon']:
 data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in (v/f'src/main/resources/assets/{ns}/lang').glob('*.json')};assert len(data)==8
 for lang,strings in data.items():
  assert strings.keys()==data['en_us'].keys()
  for k,s in data['en_us'].items():assert re.findall(r'%(?:\d+\$)?[sd]',s)==re.findall(r'%(?:\d+\$)?[sd]',strings[k])
files=list((v/'dist').glob('*.jar'));assert len(files)==2;runtime=next(p for p in files if 'sources' not in p.name)
with zipfile.ZipFile(runtime) as z:
 assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.4-test7'
 assert not any('WandSmokeChecks' in n for n in z.namelist())
 assert 'cn/quit5700/buildingwand/client/MoveBoxRenderer.class' not in z.namelist()
 assert 'MoveBlockEventsAccessor' not in z.read('quick_build.mixins.json').decode('utf-8')
 for marker in [b'movePlacementFlags',b'moveStatesMatch',b'verify_wire_support',b'Move failed:']:assert marker in z.read('cn/quit5700/buildingwand/server/WandActions.class')
bug=r/'docs/bugs/BUG-20261004-MOVE-REDSTONE-PLACEMENT.md';bug.write_text('''# 移动过程中红石更新及完整状态校验误判

当前项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。
分类：核心移动逻辑 / 红石更新 / 方块状态保护。版本1.1.4-test7，自动及独立世界回归通过，用户实机待验收。

现象：用户移动含红石的结构，目标放置失败并恢复。测试版6诊断显示17次verify_target（目标校验）失败，16次红石能量15变0，1次红石线变空气。
根因：Block.UPDATE_CLIENTS（客户端更新）并不禁止放置回调、移除副作用及邻接形状更新。原版红石onPlace（放置回调）会重算能量、更新邻居；结构尚未放完可失去供电或支撑。verifySnapshot又要求全部状态相等，将正常动态能量变化误判失败。独立世界对旧实现复现红石能量提前变化和先放红石后放支撑时红石消失，证实两条路径。

修改：测试版7 WandActions.java，独立回归WandSmokeChecks.java，版本信息及说明；当前项目问题索引与回归清单。
修复方式：清空原区、放置目标和失败恢复使用UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE | UPDATE_SKIP_ALL_SIDEEFFECTS（客户端更新、已知形状、跳过副作用），完整结构及方块实体完成后再校验并进行原有统一邻居更新。仅允许红石线power（能量）差异，类型、连接和其他状态仍严格比较。完整放置后红石线无支撑判失败并恢复，不能将实际丢失忽略为成功。保留诊断和原异常链。

影响：原操作、预览、旋转、数量无上限及权限保持。没有重新增加源区域变化检查、数量限制或大范围二次确认；没有启用测试版3事件/定时任务迁移。不涉及建筑及删除流程，不修改存档。其他生产源码及全部资源与测试版6一致。

验证：旧代码7场景中5项失败，报告保留；修复后7项全部通过：搬移阶段红石能量不变、先放线后放支撑不丢线、允许能量差异、拒绝真实丢块、拒绝无支撑红石、容器31个钻石与水/观察者及回滚、完整线路统一更新后供电。clean test build（清理、测试并构建）成功，170项测试通过；八语言键和参数一致；成品版本及诊断标记检查通过，独立测试类不进入运行包。

已尝试失败及避免重复：旧UPDATE_CLIENTS方式经独立回归确认失败，不能再当作静默放置；不能只删去verifySnapshot或允许任意状态差异。测试服务器启动有Windows性能计数器与平坦世界预设警告，但不影响启动及回归结果，不作为本模组故障修复目标。

限制：独立世界仅Fabric及本模组；用户组合模组环境、大型复杂红石、流体后续运行仍待实机验收。本次没有迁移原有计划事件，不承诺正在运行的活塞/定时电路所有事件完整保留。
''',encoding='utf-8')
for name,addition in [('BUG_INDEX.md','\n- [BUG-20261004-MOVE-REDSTONE-PLACEMENT](BUG-20261004-MOVE-REDSTONE-PLACEMENT.md)：测试版7修正搬移期间红石更新及能量差异误判；170项测试及7项独立世界回归通过，实机待验收。\n'),('REGRESSION_CHECKLIST.md','\n- 移动红石：搬移阶段不提前更新，线先于支撑放置不丢线；仅能量差异可通过，实际丢块和完整结构无支撑须失败；容器内容/流体/失败恢复及完整线路供电须通过。测试版7：170项自动测试及7项独立世界回归通过。\n')]:
 p=r/'docs/bugs'/name;p.write_text(p.read_text(encoding='utf-8')+addition,encoding='utf-8')
p=r/'docs/bugs/BUG-20261004-MOVE-FAILURE-DIAGNOSTIC.md';p.write_text(p.read_text(encoding='utf-8')+'\n后续：依据实机及独立回归根因已在测试版7处理，见BUG-20261004-MOVE-REDSTONE-PLACEMENT.md。\n',encoding='utf-8')
result={'版本':'1.1.4-test7','自动测试':170,'独立世界回归':7,'旧实现独立回归失败':5,'当前失败':0,'八语言':'通过','运行包排除测试类':'通过','生产修改':'仅WandActions搬移标志及红石校验','实际游戏':'待用户验收'};(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
for name in ['README.md','VERSION_INFO.md']:
 p=v/name;p.write_text(p.read_text(encoding='utf-8')+'\n验证结果：完整清理构建及170项测试通过；同一独立世界回归修复前5项失败，修复后7项全部通过。八语言键和参数一致；运行包无独立测试类。用户实机待验收。\n',encoding='utf-8')
archive=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\移动红石修复_26.2测试版7_20261004');archive.mkdir(parents=True,exist_ok=True)
dist=r/'dist/1.1.4-test7/mc26.2';dist.mkdir(parents=True,exist_ok=True)
for p in files:shutil.copy2(p,dist/p.name);shutil.copy2(p,archive/p.name)
mods=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods');active=list(mods.glob('quick-build*.jar'));assert len(active)==1 and active[0].name=='quick-build-1.1.4-test6-mc26.2-fabric.jar'
staged=mods/(runtime.name+'.staged');shutil.copy2(runtime,staged);assert staged.read_bytes()==runtime.read_bytes()
backup=archive/'quick-build-1.1.4-test6-mc26.2-fabric.安装前.jar';shutil.copy2(active[0],backup);assert backup.read_bytes()==active[0].read_bytes()
installed=False
try:active[0].unlink()
except PermissionError:print('旧模组被游戏占用，新包已暂存，待用户退出')
else:
 try:staged.rename(mods/runtime.name)
 except Exception:shutil.copy2(backup,active[0]);raise
 assert list(mods.glob('quick-build*.jar'))==[mods/runtime.name];assert (mods/runtime.name).read_bytes()==runtime.read_bytes();installed=True
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,archive/name)
shutil.copy2(bug,archive/'问题与修复记录.md')
for p in out.iterdir():
 if p.is_file():shutil.copy2(p,archive/p.name)
with zipfile.ZipFile(archive/'170项自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in (v/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
with zipfile.ZipFile(archive/'1.1.4-test7-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(x in {'build','.gradle','run','dist','__pycache__'} for x in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [r/'AGENTS.md',bug,r/'scripts/prepare_move_test7.py',r/'scripts/fix_move_test7.py',r/'scripts/document_move_test7.py',r/'scripts/deliver_move_test7.py']:z.write(p,p.relative_to(r).as_posix())
status='''# 26.2测试版7移动红石修复交接

本次用户授权：保留操作，完整放好结构后再更新红石，区分正常能量变化与实际丢方块。移动数量仍无上限，源区域变化检查、大范围二次确认未恢复。

已处理清空/放置/恢复期间副作用与邻接形状更新；只允许红石线能量差异，类型/连接/其他状态及真实丢块仍检查，完成结构后红石线无支撑会失败并恢复。诊断版6日志保留。其他生产源码及资源与测试版6一致，建筑、删除及Q5说明台保持。

验证：旧实现同一独立世界7场景5项失败，修复后7项全部通过；完整清理构建及170项测试、八语言、运行包测试类排除、安装文件检查通过。实际游戏组合模组及复杂结构仍待用户验收。计划事件与定时任务沿用原方式，不承诺正在运行的装置完整事件迁移。

源码versions/1.1.4-test7/mc26.2，分发dist/1.1.4-test7/mc26.2。新包位置G:/game/mcfabric/.minecraft/versions/26.2/mods/quick-build-1.1.4-test7-mc26.2-fabric.jar；本目录保存完整源码、运行/源码JAR（Java归档）、旧测试版6、170项测试与修复前后独立世界日志及问题记录。

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric模组加载器）0.19.3；Fabric API（Fabric应用程序接口）0.154.0+26.2；Fabric Loom（Fabric构建插件）1.15.5；Yarn Mappings（Yarn命名映射）未使用，采用Mojang Official Mappings（Mojang官方命名映射）。

不修改玩家存档、不关机。其他版本及GitHub正式发布待26.2验收。
'''
status+='\n安装状态：'+('已安装且只启用测试版7，文件与交付成品一致。' if installed else '游戏占用旧包，安装暂缓；新包.jar.staged待退出游戏后替换，当前启用测试版6。')+'\n'
(archive/'先读我_交接.md').write_text(status,encoding='utf-8');(archive.parent/'最新状态_建筑移动法杖26.2.md').write_text(status,encoding='utf-8')
with (archive.parent/'先读我_本对话交接.md').open('a',encoding='utf-8') as f:f.write('\n最新交付26.2测试版7：移动红石修复。见移动红石修复_26.2测试版7_20261004/先读我_交接.md，安装状态以该文档为准。\n')
checks={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in archive.iterdir() if p.suffix in {'.jar','.zip'}};(archive/'文件校验.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8');(archive/'安装核验.json').write_text(json.dumps({'已安装':installed,'安装文件':str(mods/runtime.name),'SHA256':checks[runtime.name]},ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'已安装':installed,'交接目录':str(archive),'核验':result},ensure_ascii=False))