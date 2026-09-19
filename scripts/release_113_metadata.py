from pathlib import Path
import json
R=Path(__file__).resolve().parents[1]
for p in (R/'versions/1.1.3').iterdir():
 if not p.is_dir():continue
 props=dict(line.split('=',1) for line in (p/'gradle.properties').read_text(encoding='utf-8').splitlines() if '=' in line and not line.startswith('#'))
 f=p/'src/main/resources/quick_build.mixins.json';d=json.loads(f.read_text(encoding='utf-8'));d['compatibilityLevel']='JAVA_'+props['java_version'];f.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 if props['minecraft_version'].startswith('1.20.'):
  old=p/'src/main/resources/data/light/loot_table/blocks/copy_variable_constant_light.json'
  target=p/'src/main/resources/data/light/loot_tables/blocks/copy_variable_constant_light.json';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(old.read_bytes())
 f=p/'src/test/java/cn/quit5700/light/client/LocalDaylightRenderCoverageTest.java'
 if f.exists():
  s=f.read_text(encoding='utf-8')
  needle='        assertTrue(manifest.contains("\\"mixins\\": [\\"quick_build.client.mixins.json\\"]"));'
  replacement='''        var registrations = com.google.gson.JsonParser.parseString(manifest).getAsJsonObject().getAsJsonArray("mixins");
        assertTrue(registrations.asList().stream().anyMatch(e -> e.isJsonPrimitive() && e.getAsString().equals("quick_build.client.mixins.json")));
        assertTrue(registrations.asList().stream().anyMatch(e -> e.isJsonPrimitive() && e.getAsString().equals("quick_build.mixins.json")));'''
  # Same client-registration expectation, now parsing JSON and additionally requiring common hooks.
  if needle in s:f.write_text(s.replace(needle,replacement),encoding='utf-8')
 text=f'''# 快捷建筑 1.1.3 正式版

Minecraft（我的世界）：{props['minecraft_version']}。
Java（爪哇）：{props['java_version']}。
Fabric Loader（Fabric 模组加载器）：{props['loader_version']}。
Fabric API（Fabric 应用程序接口）：{props['fabric_version']}。
Fabric Loom（Fabric 构建插件）：{props['loom_version']}。
Yarn Mappings（Yarn 命名映射）：未使用；使用 Mojang Official Mappings（Mojang 官方命名映射）。

可调恒亮灯保留“两点区域满亮”和“两点真实照明”，两点选区支持建筑法杖定位及拖选确认。普通旧灯模式迁移为两点区域满亮，未保存区域时需重新选区。复制可变恒亮灯从创造页、配方和可选 JEI（物品配方查询）入口隐藏，保留注册标识以兼容旧存档。

16 色远程开关仅菜单图标采用长方形底座和统一黑色圆头，手持、掉落和世界中的模型保持原样。设置页保留已验收位置布局，提供八种语言。

26.2 的测试版9已获用户实机认可，本正式版沿用该功能。其他版本需以对应构建报告为自动验证依据；不把编译或自动测试描述为实机验收。暂停的两点照明延迟优化未纳入。

运行包与源码包位于本工程 dist（分发目录）。游戏仅安装对应版本的运行包，源码包用于开发和归档。
'''
 (p/'VERSION_INFO.md').write_text(text,encoding='utf-8')
 (p/'README.md').write_text(text,encoding='utf-8')
