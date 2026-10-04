from pathlib import Path
import json,hashlib,zipfile,shutil,datetime
root=Path.cwd(); v=root/'versions/1.1.4-test2/mc26.2'; out=root/'outputs/20261003_删除法杖26.2测试版2'; archive=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\删除法杖_26.2测试版2_20261004')
archive.mkdir(parents=True,exist_ok=True)
for p in out.glob('*.json'):shutil.copy2(p,archive/p.name)
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,archive/name)
shutil.copy2(root/'docs/bugs/BUG-20261004-DELETE-WAND-STATE-RENDER.md',archive/'问题修复记录.md')
xmls=list((v/'build/test-results/test').glob('TEST-*.xml'))
with zipfile.ZipFile(archive/'自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in xmls:z.write(p,p.name)
with zipfile.ZipFile(archive/'1.1.4-test2-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(part in {'build','.gradle','run','dist','__pycache__'} for part in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for name in ['put_delete_wand_in_inventory.py','update_delete_wand_test2_resources.py','verify_delete_wand_test2.py','archive_delete_wand_test2.py']:
  p=root/'scripts'/name
  if p.exists():z.write(p,'scripts/'+name)
 z.write(root/'scripts/tests/test_inventory_nbt.py','scripts/tests/test_inventory_nbt.py')
 for p in [root/'AGENTS.md',root/'docs/bugs/BUG-20261004-DELETE-WAND-STATE-RENDER.md']:
  z.write(p,p.relative_to(root).as_posix())
files={p.name:{'字节':p.stat().st_size,'SHA256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in archive.iterdir() if p.is_file() and p.suffix in {'.jar','.zip'}}
(archive/'文件校验.json').write_text(json.dumps(files,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
installed=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods\quick-build-1.1.4-test2-mc26.2-fabric.jar')
assert hashlib.sha256(installed.read_bytes()).hexdigest()==files[installed.name]['SHA256']
text='''# 删除法杖26.2测试版2交接，2026年10月4日

代码：versions/1.1.4-test2/mc26.2，保留测试版1。运行包已安装到G:/game/mcfabric/.minecraft/versions/26.2/mods/quick-build-1.1.4-test2-mc26.2-fabric.jar，该目录仅一个启用中的快捷建筑包。旧测试版1运行包已移入本交接目录。

七项累计要求已实现：正文后翻页按钮；删除后局部渲染刷新；切换持物保留选区；选点期间背包图标闪烁；生存基岩保护、100块计费、512耐久、每杖余数及经验修补；预计耗尽红字确认并从第一点推进、损坏立即停止；1点耐久测试杖直接放背包。

存档“新的世界 (10)”第3快捷栏已放入测试杖，damage（损伤）511、余数0、无附魔，原物品字节保留。用户明确不要存档备份，本次未创建。法杖标识及写入前后校验保存在测试杖放入记录.json。不要再次运行写入脚本，避免重复放入。

完整构建成功，160项Java（爪哇语言）测试通过，3项背包编辑测试通过，8种语言243键和参数一致。实际游戏画面尚未验证，浅蓝表面具体渲染内部根因尚未确定，仅基于F3+A恢复证据增加局部网格刷新。用户需要重启游戏再测试；存档目前为创造模式，耐久测试需切换生存。

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric 模组加载器）0.19.3；Fabric API（Fabric 应用程序接口）0.154.0+26.2；Fabric Loom（Fabric 构建插件）1.15.5；Yarn Mappings（Yarn 命名映射）未使用，采用Mojang Official Mappings（Mojang 官方命名映射）。

其他游戏版本及GitHub（代码托管平台）正式发行版未更新，等待26.2用户验收。此前关机要求已完成一次，本轮不得重复执行。
'''
(archive/'先读我_交接.md').write_text(text,encoding='utf-8')
(archive.parent/'最新状态_删除法杖26.2.md').write_text(text,encoding='utf-8')
print(json.dumps({'交接目录':str(archive),'已安装文件SHA256':files[installed.name]['SHA256'],'保存文件':list(files)},ensure_ascii=False,indent=2))