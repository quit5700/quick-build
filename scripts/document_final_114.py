from pathlib import Path
import json,re
r=Path.cwd();root=r/'versions/1.1.4';versions=['26.2','26.1.2','1.21.11','1.21.8','1.21.4','1.21.1','1.20.4','1.20.1'];deps=[]
for v in versions:
 d=root/('mc'+v);props=dict(line.split('=',1) for line in (d/'gradle.properties').read_text(encoding='utf-8').splitlines() if '=' in line and not line.startswith('#'));cfg={k:props.get(k,'未配置') for k in ['minecraft_version','java_version','loader_version','fabric_version','loom_version','yarn_mappings']};deps.append({'Minecraft':v,**cfg})
 text=f'''快捷建筑模组正式版 1.1.4 / Minecraft（我的世界）{v}

本地正式版基于实机验收的测试版11，取消全部建造按钮仅出现在建筑魔杖说明第一页，位于左右翻页按钮右侧并随文字高度排列。其他已验收操作保持。

建筑魔杖默认单块，另有实心、空心、区域替换；潜行＋右键选材料，左键操作，右键取消选点/任务或在无选点时切换模式。模式、材料与选点按法杖记忆；选点时物品栏图标闪烁。生存缺料暂停，补料继续。实心/空心多格末端余位清为空气，原容器内容掉落一次；区域替换保护原容器、红石、流体及工作方块。标准上下两格、床式头尾属性的模组方块支持成组放置，私有结构及三格以上需单独适配，不能保证通用识别。

移动魔杖保留用户验收的整体移动与回滚操作，不设数量上限；静默装配完整结构后再通知更新。删除法杖保留两点选择、第三次左键确认和右键取消，生存跳过基岩，每成功删除100块耗1耐久，总512耐久，可经验修补。

取消全部建造仅取消本人跨维度全部建筑任务，保留已完成部分。不会删除其他玩家任务，也不取消删除法杖任务。

已补齐七个版本历史遗漏的建筑、移动法杖纹理；16色开关遵循确认外观，黑色圆头保持黑色。运行包只放一个到游戏 mods（模组）目录；sources JAR（源码包）用于维护，不放入游戏。

Minecraft（我的世界）：{v}
Java（爪哇运行环境）：{props['java_version']}
Fabric Loader（Fabric 模组加载器）：{props['loader_version']}
Fabric API（Fabric 应用程序接口）：{props['fabric_version']}
Fabric Loom（Fabric 构建插件）：{props['loom_version']}
Yarn Mappings（Yarn 命名映射）：未使用；采用 Mojang Official Mappings（Mojang 官方命名映射），对应 Minecraft {v}，配置 {props['yarn_mappings']}。

交付状态：完整构建、独立世界回归和安装核验正在执行，结果以正式发布核验.json及安装核验.json为准。GitHub暂不同步，等待玩家测试后明确通知。
'''
 (d/'README.md').write_text(text,encoding='utf-8');(d/'VERSION_INFO.md').write_text(text,encoding='utf-8')
root.joinpath('依赖版本.json').write_text(json.dumps(deps,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
root.joinpath('README.md').write_text('快捷建筑模组正式版1.1.4。本地八版本交付：'+', '.join(versions)+'。每版本保留运行包和源码包。游戏内只装运行包。取消按钮按已确认预览调整，补齐建筑及移动法杖纹理，功能沿用验收版本。GitHub等待用户测试后通知。验证与安装状态参见发布核验.json、安装核验.json。\n',encoding='utf-8')
print('八版本依赖信息、运行包/源码包用途和正式交付说明已编写。')
