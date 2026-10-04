from pathlib import Path
import json,re,shutil,zipfile,hashlib,xml.etree.ElementTree as ET
r=Path.cwd();v=r/'versions/1.1.4-test8/mc26.2';base=r/'versions/1.1.4-test7/mc26.2';out=r/'outputs/20261004_区域替换26.2测试版8'
for rel in ['src/main/java/cn/quit5700/buildingwand/server/WandActions.java','src/main/java/cn/quit5700/buildingwand/server/DeleteWandServer.java','src/main/java/cn/quit5700/buildingwand/server/DeleteTaskState.java','src/client/java/cn/quit5700/buildingwand/client/ClientWandController.java']:
 assert (v/rel).read_bytes()==(base/rel).read_bytes(),rel
count=0
for p in (v/'build/test-results/test').glob('TEST-*.xml'):
 a=ET.parse(p).getroot().attrib;count+=int(a['tests']);assert int(a['failures'])==int(a['errors'])==int(a['skipped'])==0
assert count==173
log=(out/'完整13场景回归.log').read_text(encoding='utf-8');assert 'Q5_MOVE_REGRESSION_PASS: 13 cases' in log and 'Q5_CASE_FAIL' not in log
for ns in ['building_wand','light','pathfinding_beacon']:
 data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in (v/f'src/main/resources/assets/{ns}/lang').glob('*.json')};assert len(data)==8
 for lang,strings in data.items():
  assert strings.keys()==data['en_us'].keys()
  for k,s in data['en_us'].items():assert re.findall(r'%(?:\d+\$)?[sd]',s)==re.findall(r'%(?:\d+\$)?[sd]',strings[k])
files=list((v/'dist').glob('*.jar'));assert len(files)==2;runtime=next(p for p in files if 'sources' not in p.name)
with zipfile.ZipFile(runtime) as z:
 assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.4-test8'
 assert not any('WandSmokeChecks' in n for n in z.namelist())
 assert 'cn/quit5700/buildingwand/server/ReplacementRules.class' in z.namelist()
 assert json.loads(z.read('assets/building_wand/lang/zh_cn.json'))['mode.q5.3']=='区域替换'
result={'版本':'1.1.4-test8','自动测试':173,'独立世界回归':13,'失败':0,'八语言':'键集合及参数一致','移动及删除核心代码':'与已验收测试版7一致','游戏实机':'待用户验收'};(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
text='''# 快捷建筑1.1.4-test8：26.2区域替换测试版

建筑法杖默认单块，普通右键在没有选点时循环单块放置、实心填充、空心外壳、区域替换；有选点时右键取消，运行任务时右键取消并保留已完成部分。

区域替换：进入模式提示可潜行＋右键选择被替换方块。如果没有建造材料，第一次选择全模式共用的建造材料；已有建造材料时选择本模式专用的被替换类型。按类型匹配，忽略朝向等状态；未指定目标时替换所有允许替换的非空气方块。左键两点、再次左键确认。两种材料及模式按每把法杖保存，切换物品保留选点并继续图标闪烁；选点在退出或换维度时清除。

任意模式指向天空等未命中方块位置，潜行＋右键清空建造材料；区域替换同时清空两种选择，其他模式保留本模式专用目标。已经确认的任务固定使用确认时的材料与目标，之后改选不会改变运行任务。

始终跳过红石线路及装置、流体/含水、工作台、容器及所有带方块实体数据的方块、床、作物、门和双格植物、重要功能方块、轨道及附着物。保护作物地面、附着物有效支撑、展示框和画的支撑，并避免新方块与实体相交或移除实体脚下所需支撑。实体不删除。生存保护基岩，创造可替换基岩，但其他保护仍生效。

区域替换不填空气、不替换未匹配类型；同类型材料已存在时不重复替换、不扣材料。生存每成功替换一块消耗一个材料，缺料暂停，背包补料自动继续，右键取消。旧方块不掉落，受保护和未匹配位置不耗材料。分批执行，无人为数量上限。进度分别显示扫描、替换、保护跳过数量，完成时报告替换及保护跳过数量。

已有三模式、空心保留内部、移动红石修复、删除法杖已验收功能保持；Q5模组方块说明台增加区域替换说明并保留现有翻页布局。八语言说明和参数一致。

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric模组加载器）0.19.3；Fabric API（Fabric应用程序接口）0.154.0+26.2；Fabric Loom（Fabric构建插件）1.15.5；Yarn Mappings（Yarn命名映射）未使用，采用Mojang Official Mappings（Mojang官方命名映射）。

完整清理构建及173项测试、13项独立世界回归通过。实际组合模组环境与界面仍待用户验收；其他模组提供的新保护类型需依据其方块实体、标签、信号及交互接口识别，不能宣称覆盖所有未测试模组。测试世界不读取玩家存档，测试类不进入运行包。

正式版与其他版本更新按用户要求暂停，等待用户再次明确叫更新；未更新GitHub，未关机，未修改玩家存档。测试版7仍为之前已验收基线。versions/1.1.4为暂停的未完成候选，不能当作正式交付。
'''
for name in ['README.md','VERSION_INFO.md']:(v/name).write_text(text,encoding='utf-8')
bug=r/'docs/bugs/BUG-20261004-REGION-REPLACEMENT.md';bug.write_text('''# 区域替换新增模式与回归记录

当前项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。
分类：材料/模式状态、任务持久化、方块及实体数据保护。版本：1.1.4-test8/mc26.2。

需求及根因：原三模式只有放置，生存遇已有方块会暂停，不能直接用原实心模式实现替换。替换需独立类型过滤、保护判断、材料来源状态和数量统计，旧建造任务还必须继续兼容。
修改文件：BuildPlan、BuildWandData、BuildNetworking、BuildWandServer、BuildTaskState、ReplacementRules、BuildWandClient、BuildingWandItem、WandSettingsScreen、八语言、ReplacementModeTest、独立世界WandSmokeChecks，版本属性及说明。
实现：新增第四模式3；全模式共用建造材料与替换专用目标分开保存在每把法杖。空气选择使用单独CLEAR（清空）请求并核对服务端视线未命中。先过滤空气/类型/无需变化，再作保护检查，仅成功替换消费材料，不掉落旧方块。任务codec（编解码）新增可选filter（目标）与skipped（保护跳过），旧任务缺字段默认兼容。确认时固定任务材料与过滤类型。切换物品记忆复用已有选区/闪烁流程。
保护：红石、液体/含水、工作台、所有方块实体数据、容器、床、作物/其地面、多方块门/植物、重要功能方块、轨道、附着物和有效支撑；实体空间碰撞与脚下支撑检查，画及展示框支撑检查。保护始终生效；基岩只在生存额外禁止。移动/删除核心代码与测试版7完全一致。
验证：173项自动测试通过，旧正确测试未删除或弱化。13项独立世界回归全部通过，包含原7项移动回归、材料双选择/切换物品/清空、保护类型/含水/基岩、盔甲架碰撞/火把支撑/展示框支撑、替换目标及旧任务缺字段恢复、生存缺料暂停/每块一个材料/补料继续、过滤目标/未匹配保留/空气不填/容器内容/红石保留/跳过数量/不掉落。八语言键与参数一致，成品测试类排除及安装摘要校验。
已尝试失败：getEntities传未指定类型null（空值）造成实体与类型查询重载歧义，改为显式Entity（实体）类型；测试使用旧RED_BED常量编译失败，26.2床已采用颜色集合，改从注册表按固定标识读取同一红床；脚本在版本目录又重复拼接版本路径导致找不到文件，改为与明确工作目录对应的src路径，未发生误写。禁止复用这些失败写法。
测试设计修正：现实世界边界范围的体积大于整型但未超过长整型，新增纯逻辑极限体积用整数坐标极值检查超过长整型的BigInteger（大整数）计算，不修改旧测试预期。
下次避免：替换不能沿用生存放置遇障碍暂停的判定；保护位置应在消耗材料前处理，跳过与完成要分开统计；扩展保存数据使用可选默认字段，不能破坏旧任务；其他模组的全覆盖不靠静态列表承诺。
未验证：用户真实游戏界面和组合模组实机。正式版/其他版本/GitHub更新仍暂停等待用户叫更新。
''',encoding='utf-8')
for name,addition in [('BUG_INDEX.md','\n- [BUG-20261004-REGION-REPLACEMENT](BUG-20261004-REGION-REPLACEMENT.md)：测试版8新增区域替换与数据/实体保护；173项测试及13项独立世界回归通过，实机待验收。\n'),('REGRESSION_CHECKLIST.md','\n- 区域替换：双材料选择与跨物品保留、空气清空、目标只在第四模式使用、保护始终生效、跳过不扣材料、仅成功替换扣一份、不填空气/无掉落、缺料补料继续、数量统计及旧任务兼容。测试版8：173项测试及13项独立世界回归通过。\n')]:
 p=r/'docs/bugs'/name;p.write_text(p.read_text(encoding='utf-8')+addition,encoding='utf-8')
archive=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\区域替换_26.2测试版8_20261004');archive.mkdir(parents=True,exist_ok=True);dist=r/'dist/1.1.4-test8/mc26.2';dist.mkdir(parents=True,exist_ok=True)
for p in files:shutil.copy2(p,dist/p.name);shutil.copy2(p,archive/p.name)
mods=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods');active=list(mods.glob('quick-build*.jar'));assert len(active)==1 and active[0].name=='quick-build-1.1.4-test7-mc26.2-fabric.jar'
staged=mods/(runtime.name+'.staged');shutil.copy2(runtime,staged);assert staged.read_bytes()==runtime.read_bytes();backup=archive/'quick-build-1.1.4-test7-mc26.2-fabric.安装前.jar';shutil.copy2(active[0],backup);assert backup.read_bytes()==active[0].read_bytes();installed=False
try:active[0].unlink()
except PermissionError:print('游戏占用旧包，已暂存新包等待退出')
else:
 try:staged.rename(mods/runtime.name)
 except Exception:shutil.copy2(backup,active[0]);raise
 assert list(mods.glob('quick-build*.jar'))==[mods/runtime.name] and (mods/runtime.name).read_bytes()==runtime.read_bytes();installed=True
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,archive/name)
shutil.copy2(bug,archive/'需求实现与问题记录.md')
for p in out.iterdir():
 if p.is_file():shutil.copy2(p,archive/p.name)
with zipfile.ZipFile(archive/'173项自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in (v/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
with zipfile.ZipFile(archive/'1.1.4-test8-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(x in {'build','.gradle','run','dist','__pycache__'} for x in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [r/'AGENTS.md',bug,*[r/'scripts'/f'{name}_replace_test8.py' for name in ['prepare','implement','localize','test','extend','deliver']]]:z.write(p,p.relative_to(r).as_posix())
status=text+'\n安装状态：'+('已安装测试版8，仅启用一个运行包，摘要与归档一致。' if installed else '游戏占用旧包，测试版8已暂存.jar.staged，当前仍启用测试版7，待退出游戏替换。')+'\n'
(archive/'先读我_交接.md').write_text(status,encoding='utf-8');(archive.parent/'最新状态_建筑移动法杖26.2.md').write_text(status,encoding='utf-8')
with (archive.parent/'先读我_本对话交接.md').open('a',encoding='utf-8') as f:f.write('\n最新交付为区域替换26.2测试版8，见区域替换_26.2测试版8_20261004/先读我_交接.md。其他版本必须等用户叫更新，正式版也仍暂停。\n')
checks={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in archive.iterdir() if p.suffix in {'.jar','.zip'}};(archive/'文件校验.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8');(archive/'安装核验.json').write_text(json.dumps({'已安装':installed,'安装文件':str(mods/runtime.name),'SHA256':checks[runtime.name]},ensure_ascii=False,indent=2),encoding='utf-8');print(json.dumps({'已安装':installed,'交接目录':str(archive),'核验':result},ensure_ascii=False))