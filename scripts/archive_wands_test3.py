from pathlib import Path
import json,shutil,hashlib,zipfile
root=Path.cwd();v=root/'versions/1.1.4-test3/mc26.2';out=root/'outputs/20261004_建筑移动法杖26.2测试版3';archive=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\建筑移动法杖_26.2测试版3_20261004');archive.mkdir(parents=True,exist_ok=True)
for p in out.iterdir():
 if p.is_file():shutil.copy2(p,archive/p.name)
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,archive/name)
shutil.copy2(root/'docs/bugs/BUG-20261004-WAND-BUILD-MOVE-UNBOUNDED.md',archive/'问题与修复记录.md')
with zipfile.ZipFile(archive/'1.1.4-test3-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(part in {'build','.gradle','run','dist','__pycache__'} for part in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [root/'AGENTS.md',root/'docs/bugs/BUG-20261004-WAND-BUILD-MOVE-UNBOUNDED.md',root/'scripts/verify_wands_test3.py',root/'scripts/archive_wands_test3.py']:z.write(p,p.relative_to(root).as_posix())
with zipfile.ZipFile(archive/'170项自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in (v/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
text='''# 建筑与移动法杖26.2测试版3交接，2026年10月4日

当前源码：versions/1.1.4-test3/mc26.2。游戏已安装G:/game/mcfabric/.minecraft/versions/26.2/mods/quick-build-1.1.4-test3-mc26.2-fabric.jar，只有一个启用中的快捷建筑包。旧测试版2包在本目录安装前归档。重启游戏才会加载新版本。

本轮用户已授权处理：建筑潜行右键选材料、禁止攻击挖掘、右键三模式、默认单块每杖保存、两点确认实心与空心、内部保留、跨物品选区保留和闪烁、取消所有建筑上限、逐列分批、缺材料暂停自动补料继续、原杖右键取消保留已建部分；移动取消上限但不分批，控制红石与流体副作用，32768格以上提示内存和耗时并再次确认；设置台改名Q5模组方块说明台并写明操作、去掉上限按钮。以上已实现。

建筑已确认任务保存进维度，原法杖不在背包、离线、离开维度时暂停，返回继续；材料固定为任务确认时选材，暂停期间改选其他材料不改变任务。普通右键优先取消该杖任务，其次取消选点，最后切换模式；潜行右键优先选材料。任务不提供撤销已建方块。

移动使用完整内存快照、不分批，巨型区域仍受机器实际内存限制。搬移时跳过放置、形状、邻居副作用；完整结构恢复后只显式通知边界，迁移原计划更新和方块事件、活塞事件方向随旋转。源区域容器或状态在选取后变化时拒绝覆盖旧快照，需重选。外部红石接线和流体环境改变后正常物理行为不可能保证与原位置完全一致。中途断电/进程崩溃不保证跨进程原子恢复，此为用户取消分批中断方案后的范围。

完整构建成功，170项自动测试通过，八语言274个建筑命名空间键与参数一致。独立服务器实际世界测试通过：红石线/观察者、水/岩浆/含水、容器、计划方块/流体更新、活塞事件旋转、回滚、模式保存、大范围任务序列化与原杖取消。区块重载时曾复现未展开SavedTick漏读，已修复并同世界重跑通过，失败与成功日志均保留。生产JAR（Java归档）不包含独立测试类。

游戏中三模式、闪烁、界面、补料继续和取消仍待用户实机验收，不能把服务器验证当成界面验收。建议先用少量材料确认暂停、补料继续和右键取消，再试复杂红石/流体建筑搬移。删除法杖测试版2已由用户验收，其功能本轮保留。未修改玩家存档、未重复放测试杖，未执行关机。

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric 模组加载器）0.19.3；Fabric API（Fabric 应用程序接口）0.154.0+26.2；Fabric Loom（Fabric 构建插件）1.15.5；Yarn Mappings（Yarn 命名映射）未使用，Mojang Official Mappings（Mojang 官方命名映射）。

其他游戏版本和GitHub正式发行版仍暂缓，先验收26.2。本目录有最终源码ZIP（压缩包）、运行/源码JAR（Java归档）、测试结果、日志、说明与校验。构建：Java25下gradlew.bat clean test build。独立世界测试：在run/mods放同版完整Fabric API，准备仅回环监听的测试server.properties及eula.txt，然后gradlew.bat -PwandSmoke runServer；检查Q5_WAND_SMOKE_PASS标志，不能只看Gradle退出码。实现过程脚本是一次性的，不应重放覆盖最终源码。
'''
(archive/'先读我_交接.md').write_text(text,encoding='utf-8');(archive.parent/'最新状态_建筑移动法杖26.2.md').write_text(text,encoding='utf-8')
with (archive.parent/'先读我_本对话交接.md').open('a',encoding='utf-8') as f:f.write('\n\n2026年10月4日最新进度：建筑移动法杖_26.2测试版3_20261004，已安装26.2，170项测试与独立世界验证通过，等待用户实机验收。查看最新状态_建筑移动法杖26.2.md。\n')
files={p.name:{'字节':p.stat().st_size,'SHA256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in archive.iterdir() if p.is_file() and p.suffix in {'.jar','.zip'}}
(archive/'文件校验.json').write_text(json.dumps(files,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
installed=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods\quick-build-1.1.4-test3-mc26.2-fabric.jar');assert hashlib.sha256(installed.read_bytes()).hexdigest()==files[installed.name]['SHA256']
print(json.dumps({'交接目录':str(archive),'安装校验':'通过','保存文件':list(files)},ensure_ascii=False,indent=2))