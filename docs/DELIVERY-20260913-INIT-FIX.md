# test4 初始化崩溃修复交付

本地 26.2 已安装 1.1.3-test4。修改范围仅为灯初始化登记入口与回归测试；不修改存档、两点模式语义、工具模型、复制灯玩法或其余七版本。

运行包和源码包位于 mc26.2/dist：quick-build-1.1.3-test4-mc26.2-fabric.jar、quick-build-1.1.3-test4-mc26.2-fabric-sources.jar。

安装目录：G:/game/mcfabric/.minecraft/versions/26.2/mods。旧 test3 已改名为 quick-build-1.1.3-test3-mc26.2-fabric.before-init-fix-20260913.jar.disabled，未删除，仍可恢复但有已知递归崩溃问题。

新包与安装包 SHA-256（完整性校验）：1369309E17BEDB845281B6B0D961703F67C81F149BB1F775C4FC24B0B156900C。旧包备份：3B68B26F6C22CCBB9E7EDDF0E6AF83D3ECA63C0674270E72EABFC5DF0ACFFA32。

完整构建通过；103 项测试、0 失败、0 错误。新增测试检查编译后的初始化调用路径，不能代替实机重载。请完全退出并重启游戏，进入此前出错的世界，检查已供电灯、复制灯和两点范围恢复；本轮未打开或修改用户存档。

GitHub 登录凭据仍无效，远端提交与发行版未同步。代码和记录保存于当前项目，并作本地提交；不执行关机。

依赖：Minecraft（我的世界）26.2、Java（爪哇）25、Fabric Loader（模组加载器）0.19.3、Fabric API（应用程序接口）0.154.0+26.2、Fabric Loom（构建插件）1.15.5；Yarn Mappings（命名映射）未使用，采用 Mojang Official Mappings（官方命名映射）。
