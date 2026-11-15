# 快捷建筑 1.1.9 正式版

<img src="assets/confirmed/quick-build-approved.png" width="160" alt="快捷建筑正式图标">

用户于2026-11-16确认正式版。支持1.20.1、1.20.4、1.21.1、1.21.4、1.21.8、1.21.10、1.21.11、26.1.2、26.2、26.3。各版完整代码在versions/1.1.9/mc对应版本，安装目标共11个实例。

保留建筑、蓝图、移动、删除法杖、生成箱、照明及信标等功能。生成箱仅由法杖任务生成。各版最新已验收修复分别保留；正式图标复用assets/confirmed中的用户确认原图，不重新设计。

每版的Minecraft（我的世界）、Java（爪哇）、Fabric Loader（模组加载器）、Fabric API（应用程序接口）、Fabric Loom（构建插件）、Gradle（构建工具）及Yarn Mappings（命名映射）情况见该版VERSION_INFO.md；实际安装信息见docs/INSTALLATION_TARGETS.json。

Windows PowerShell（命令外壳）构建：在项目根目录设置 $env:PYTHONUTF8='1' 后运行 python .\scripts\release_119.py build 1.20.1；将末尾版本替换为所需版本。脚本自动为26系列选择Java25，其余使用Java21构建，目标字节码遵循各版Java要求。运行世界回归使用 python .\scripts\release_119.py world 对应版本。

源码、原始图标、全部材质模型、八语言翻译、组件接入文档和针对性测试均保留，可继续修改和增加组件。本地编译JAR依赖置于各版libs；其他声明依赖可由Gradle下载，H盘共享依赖缓存保留。构建产物／测试世界／项目缓存可重新生成，不属于源码。

工具环境路径可按本机安装调整；脚本默认GRADLE_USER_HOME=H:/CodexWork/q5-gradle-cache，Java临时目录=H:/CodexWork/q5-settings-runtime-tmp，避免构建缓存增长到C盘。

Windows（视窗系统）源码布局：辅助模块的物理目录为auxiliary，以避开Windows保留名AUX；Java历史包名保留aux，不影响运行接口和存档。请保留该兼容布局并通过Gradle构建。源码归档中的虚拟ZIP路径仍对应二进制包名。
