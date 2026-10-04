from pathlib import Path
import json,zipfile,shutil,hashlib
r=Path.cwd();v=r/'versions/1.1.4-test4/mc26.2';out=r/'outputs/20261004_移动恢复原模式26.2测试版4';archive=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\移动恢复原模式_26.2测试版4_20261004')
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,archive/name)
shutil.copy2(out/'完整核验.json',archive/'完整核验.json');shutil.copy2(r/'docs/bugs/BUG-20261004-MOVE-LEGACY-RESTORE.md',archive/'问题与修复记录.md')
with zipfile.ZipFile(archive/'1.1.4-test4-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(part in {'build','.gradle','run','dist','__pycache__'} for part in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [r/'AGENTS.md',r/'scripts/verify_wands_test4.py',r/'scripts/archive_wands_test4.py',r/'docs/bugs/BUG-20261004-MOVE-LEGACY-RESTORE.md']:z.write(p,p.relative_to(r).as_posix())
 for relative in ['src/main/java/cn/quit5700/buildingwand/server/WandActions.java','src/client/java/cn/quit5700/buildingwand/client/ClientWandController.java','src/main/resources/quick_build.mixins.json']:z.write(r/'versions/1.1.4-test2/mc26.2'/relative,'对照基线_测试版2/'+relative)
with zipfile.ZipFile(archive/'170项自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in (v/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
text='''# 2026年10月4日：26.2测试版4为当前安装版

用户最终要求：移动法杖保留原模式，只取消数量上限和设置。已以测试版2为基线恢复移动服务端方法（仅删除数量检查），客户端移动方法与原混入配置恢复，逐字对照通过。测试版3“原区域变化”拦截、大范围二次确认、额外事件迁移/副作用处理和简化移动预览均不再启用。没有采用确认时刷新快照的替代方案。

建筑法杖三模式、每杖模式保存、跨物品选区与图标闪烁、无上限分批建造、缺料暂停自动补料继续、执行法杖右键取消保留已建部分，以及Q5模组方块说明台名称和说明均保留。删除法杖用户已验收的功能保留。

新版本源码versions/1.1.4-test4/mc26.2，分发dist/1.1.4-test4/mc26.2。已安装G:/game/mcfabric/.minecraft/versions/26.2/mods/quick-build-1.1.4-test4-mc26.2-fabric.jar；旧测试版3在本目录安装前归档。游戏目录只启用新包，需重启游戏测试。完整构建及170项测试通过，八语言键与占位符一致，源码对照证明移动仅移除上限。实际游戏仍待验收。测试版3的红石/流体新增保护测试不能作为测试版4保证。

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric 模组加载器）0.19.3；Fabric API（Fabric 应用程序接口）0.154.0+26.2；Fabric Loom（Fabric 构建插件）1.15.5；Yarn Mappings（Yarn 命名映射）未使用，采用Mojang Official Mappings（Mojang 官方命名映射）。

本次未修改玩家存档，没有重复放物品，没有关机。其他游戏版本及GitHub正式发行版暂缓。本目录保存最终源码、运行/源码JAR（Java归档）、170项测试结果、基线对照记录、说明及文件校验；源码ZIP（压缩包）内另存三个测试版2对照文件。旧移动纯逻辑辅助文件和测试保留但不挂接到本版流程，避免删除历史正确测试。
'''
(archive/'先读我_交接.md').write_text(text,encoding='utf-8');(archive.parent/'最新状态_建筑移动法杖26.2.md').write_text(text,encoding='utf-8')
with (archive.parent/'先读我_本对话交接.md').open('a',encoding='utf-8') as f:f.write('\n\n最新版本已改为26.2测试版4：移动恢复原模式，仅取消上限。见移动恢复原模式_26.2测试版4_20261004/先读我_交接.md。\n')
files={p.name:{'字节':p.stat().st_size,'SHA256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in archive.iterdir() if p.is_file() and p.suffix in {'.jar','.zip'}};(archive/'文件校验.json').write_text(json.dumps(files,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
installed=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods\quick-build-1.1.4-test4-mc26.2-fabric.jar');assert hashlib.sha256(installed.read_bytes()).hexdigest()==files[installed.name]['SHA256']
print(json.dumps({'交接目录':str(archive),'安装校验':'通过'},ensure_ascii=False))