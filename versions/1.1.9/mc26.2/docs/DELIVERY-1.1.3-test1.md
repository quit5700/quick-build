# 1.1.3-test1 交付记录

2026-09-12，目标 Minecraft（我的世界）26.2。

## 已完成

- 原可变恒亮灯：纯局部白昼受碰撞形状遮挡；无遮挡区域禁止怪物自然生成（包括幻翼），作物和真实光照逻辑不变。已有实体、刷怪笼、袭击、刷怪蛋和指令不受本次拦截影响。原版和混合模式保留旧行为，复制方块未改动。
- ID 顺序重排器：物品栏及手持采用深灰、青色、白色芯片和铜柄的新立体模型。
- 同包保留此前两根魔杖、红石能量连接器及 16 色开关物品栏外观修改。
- 完整构建通过，89 项自动测试无失败；独立服务端帮助模式启动成功。尚未完成游戏内遮挡效果、性能和模型视觉验收。

## 本地文件

- 运行包：`dist/quick-build-1.1.3-test1-mc26.2-fabric.jar`。
- 源码包：`dist/quick-build-1.1.3-test1-mc26.2-fabric-sources.jar`。
- 安装位置：`G:/game/mcfabric/.minecraft/versions/26.2/mods/quick-build-1.1.3-test1-mc26.2-fabric.jar`。
- 运行包 SHA-256：`585310CA388D4D8BD4D3F8436F2B3A1A3BAF4CF907D7C60609003E846BEF0E6D`；安装副本与分发包一致。
- 旧包备份：同一 mods 目录内 `quick-build-1.1.2-mc26.2-fabric.before-daylight-20260912.jar.disabled`，未删除。
- 只安装到 26.2，其他七个版本尚未适配本轮功能。主客机需要同版快照协议。

## 验收边界

玻璃等有碰撞形状的透明方块也遮挡；液体及无碰撞物体不遮挡。遮挡结果缓存，但首次计算和建造刷新仍有资源消耗。不能把自动测试或帮助模式启动当成实机性能及完整注入验收。

## 构建环境

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric 模组加载器）0.19.3；Fabric API（Fabric 应用程序接口）0.154.0+26.2；Fabric Loom（Fabric 构建插件）1.15.5；Yarn Mappings（Yarn 命名映射）未使用，采用 Mojang Official Mappings（Mojang 官方命名映射）。
