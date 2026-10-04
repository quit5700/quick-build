from pathlib import Path
import json,re,shutil,zipfile,hashlib,xml.etree.ElementTree as ET
r=Path.cwd();v=r/'versions/1.1.4-test6/mc26.2';base=r/'versions/1.1.4-test5/mc26.2';out=r/'outputs/20261004_移动诊断26.2测试版6';out.mkdir(parents=True,exist_ok=True)
changed=[]
for p in (base/'src').rglob('*'):
 if p.is_file() and p.read_bytes()!=(v/p.relative_to(base)).read_bytes():changed.append(p.relative_to(base).as_posix())
assert changed==['src/main/java/cn/quit5700/buildingwand/server/WandActions.java'],changed
count=0
for p in (v/'build/test-results/test').glob('TEST-*.xml'):
 a=ET.parse(p).getroot().attrib;count+=int(a['tests']);assert int(a['failures'])==int(a['errors'])==0
assert count==170
for ns in ['building_wand','light','pathfinding_beacon']:
 data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in (v/f'src/main/resources/assets/{ns}/lang').glob('*.json')};assert len(data)==8
 for lang,strings in data.items():
  assert strings.keys()==data['en_us'].keys()
  for k,s in data['en_us'].items():assert re.findall(r'%(?:\d+\$)?[sd]',s)==re.findall(r'%(?:\d+\$)?[sd]',strings[k])
files=list((v/'dist').glob('*.jar'));assert len(files)==2;runtime=next(p for p in files if 'sources' not in p.name)
with zipfile.ZipFile(runtime) as z:
 assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.4-test6'
 bytecode=z.read('cn/quit5700/buildingwand/server/WandActions.class')
 for marker in [b'Move failed:',b'Move rollback failed:',b'Move rollback completed:',b'expectedBlock=',b'actualState=',b'pos=']:assert marker in bytecode
bug=r/'docs/bugs/BUG-20261004-MOVE-FAILURE-DIAGNOSTIC.md'
bug.write_text('''# 移动失败缺少诊断日志

项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。
类型：移动执行异常 / 可观测性。状态：诊断版已交付，移动失败根因待实机复现。
现象：测试版5显示“目标结构放置失败，原区域已恢复，请调整位置后重试”。用户截图确认失败，但现有日志未包含导致失败的异常。
已确认原因：executeMove捕获运行时异常后直接恢复并提示，没有输出异常；这只能解释缺少诊断信息，不能据此判定移动失败根因。
修改文件：测试版6 WandActions.java、gradle.properties、README.md、VERSION_INFO.md及当前项目问题索引与回归清单。
处理：增加失败阶段、维度、玩家编号、起点与目标点、旋转、数量、异常堆栈；清空、放置、方块实体恢复及结果校验失败补充具体坐标、方块标识、预期与实际状态。保留原异常为原因链。恢复成功单独记录，恢复失败记录后按原逻辑抛出异常。不记录完整容器物品数据。
影响：没有修改移动权限、数量、预览、放置标志、顺序和回滚规则；其他生产文件、资源与测试版5逐文件一致。建筑与删除功能保持。
验证：完整构建及170项测试通过；八语言键与参数一致；成品字节码含全部诊断标记，版本及安装文件校验通过。失败日志实际输出、具体失败原因仍待重启游戏重复原操作后确认。不要宣称移动故障已修复。
下次避免：执行异常不能只显示通用提示，必须保留具体步骤和原异常；用户要求恢复原模式时，不顺便新增移动限制。安装前参照跨盘归档记录，复制旧包并校验后再删除原包。
本轮读取路径问题：仓库根无src/main对应文件，已按当前版本目录读取；后续禁止假设仓库根就是版本源码目录。
''',encoding='utf-8')
for name,addition in [('BUG_INDEX.md','\n- [BUG-20261004-MOVE-FAILURE-DIAGNOSTIC](BUG-20261004-MOVE-FAILURE-DIAGNOSTIC.md)：测试版6增加移动失败及恢复日志；170项测试通过，失败根因待复现。\n'),('REGRESSION_CHECKLIST.md','\n- 移动诊断：重现放置失败后，日志须包含阶段、坐标、预期/实际方块状态与异常堆栈，恢复结果另行记录；诊断版不改变原移动规则。测试版6自动回归170项通过，失败输出待实机。\n')]:
 p=r/'docs/bugs'/name;p.write_text(p.read_text(encoding='utf-8')+addition,encoding='utf-8')
result={'版本':'1.1.4-test6','测试':count,'失败':0,'源码对照':'仅WandActions新增异常诊断，其他源码及资源与测试版5一致','诊断成品标记':'通过','八语言':'通过','移动故障根因':'待实机复现读取日志'};(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
archive=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\移动诊断_26.2测试版6_20261004');archive.mkdir(parents=True,exist_ok=True)
dist=r/'dist/1.1.4-test6/mc26.2';dist.mkdir(parents=True,exist_ok=True)
for p in files:shutil.copy2(p,dist/p.name);shutil.copy2(p,archive/p.name)
mods=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods');active=list(mods.glob('quick-build*.jar'));assert len(active)==1 and active[0].name=='quick-build-1.1.4-test5-mc26.2-fabric.jar'
staged=mods/(runtime.name+'.staged');shutil.copy2(runtime,staged);assert staged.read_bytes()==runtime.read_bytes()
backup=archive/'quick-build-1.1.4-test5-mc26.2-fabric.安装前.jar';shutil.copy2(active[0],backup);assert backup.read_bytes()==active[0].read_bytes();active[0].unlink();staged.rename(mods/runtime.name)
assert list(mods.glob('quick-build*.jar'))==[mods/runtime.name]
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,archive/name)
shutil.copy2(bug,archive/'问题与诊断记录.md');shutil.copy2(out/'完整核验.json',archive/'完整核验.json')
with zipfile.ZipFile(archive/'170项自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in (v/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
with zipfile.ZipFile(archive/'1.1.4-test6-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(x in {'build','.gradle','run','dist','__pycache__'} for x in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [r/'AGENTS.md',bug,r/'scripts/prepare_move_diagnostic_test6.py',r/'scripts/deliver_move_diagnostic_test6.py']:z.write(p,p.relative_to(r).as_posix())
status='''# 26.2测试版6移动诊断交接

当前安装版为1.1.4-test6。用户授权增加移动失败诊断日志，保留现有操作；不能将本次诊断交付当作已修复移动失败。失败根因待用户重启后重复原移动操作、读取latest.log（最新日志）确认。

日志记录失败阶段、维度、玩家编号、第一点、目标点、旋转、数量、失败坐标、预期/实际方块状态、异常堆栈，并单独记录恢复完成或恢复失败。不记录完整容器数据。日志英文标签对照：clear_source清空原位置、place_target放置目标、verify_target校验目标、update_routes更新寻路、restore_block_entity恢复方块实体、Move rollback completed原区域恢复完成、Move rollback failed原区域恢复失败。

完整构建、170项测试、八语言参数校验通过。只有WandActions增加诊断，其余生产源码与资源保持测试版5。实际失败日志和移动根因仍待验证。

安装目录G:/game/mcfabric/.minecraft/versions/26.2/mods，仅启用测试版6；旧测试版5归档在本目录。源码versions/1.1.4-test6/mc26.2，分发dist/1.1.4-test6/mc26.2。交接目录保存完整源码、运行及源码JAR（Java归档）、测试和问题记录。

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric模组加载器）0.19.3；Fabric API（Fabric应用程序接口）0.154.0+26.2；Fabric Loom（Fabric构建插件）1.15.5；Yarn Mappings（Yarn命名映射）未使用，采用Mojang Official Mappings（Mojang官方命名映射）。

没有修改存档、没有关机，其他版本和GitHub正式发行仍待26.2验收。移动保持测试版4恢复的原模式，不重新启用测试版3额外检查及红石流体迁移方案。
'''
(archive/'先读我_交接.md').write_text(status,encoding='utf-8');(archive.parent/'最新状态_建筑移动法杖26.2.md').write_text(status,encoding='utf-8')
with (archive.parent/'先读我_本对话交接.md').open('a',encoding='utf-8') as f:f.write('\n最新安装版：26.2测试版6移动诊断。见移动诊断_26.2测试版6_20261004/先读我_交接.md；移动失败根因待实机复现。\n')
checks={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in archive.iterdir() if p.suffix in {'.jar','.zip'}};(archive/'文件校验.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8');assert hashlib.sha256((mods/runtime.name).read_bytes()).hexdigest()==checks[runtime.name]
print(json.dumps({'安装文件':str(mods/runtime.name),'交接目录':str(archive),'核验':result},ensure_ascii=False))