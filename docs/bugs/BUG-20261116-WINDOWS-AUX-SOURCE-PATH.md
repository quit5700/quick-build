# 正式版源码的 Windows 保留名目录

当前项目根目录：H:/CodexWork/快捷建筑模组；本次是原快捷建筑Git仓库的正式源码迁移，错误记录仅保存在该项目。

类型：Windows文件命名／源码分发／Git索引。现象：PowerShell和Python能读取src下的aux/AuxClickInput.java，Java构建及测试通过，但Git hash-object和add均报告No such file or directory（没有该文件或目录）。因此不能把“本地可编译”等同于“Windows可正常检出源码”。

根因：物理源码目录aux使用了Windows保留设备名。已尝试失败：设置core.longpaths=true，仍不能索引。控制实验：将完全相同的文件内容复制到auxiliary目录后，git hash-object立即成功，返回8596a310afc9f6b0780d6030ea0be4952b5c8cfe。不是中文内容或文件缺失问题。

修复：十版src/main、src/client、src/test下的物理aux目录改为auxiliary，文件内容、Java包名cn.quit5700.buildingwand.aux及二进制接口保持原样，避免更名网络类型或破坏旧存档和扩展调用。Gradle仍递归编译全部Java源文件；sourcesJar单独将auxiliary目录映射回虚拟ZIP中的aux包路径，保留调试源码与类名对应。实际开发请使用Git仓库或H盘完整源码目录，勿依赖将源码归档解包为Windows同名保留目录。

涉及文件：各版源码物理目录、各版build.gradle中的源码归档路径规则；不修改生产Java逻辑。辅助脚本normalize_windows_sources_119.py按项目根目录检查后移动，并记录目录及变更前运行类哈希。先前已经完成世界回归的版本需要重新完整单元测试和构建，再逐个比较生产.class内容；后续版本在首次正式构建前完成目录适配。不得拿旧布局构建结果代替最终源码可构建证据。

验证以正式版验证目录中的windows-layout、build及binary-before-layout报告为准；Git索引与远端提交完成后才能清理旧工程。最终状态同步在发行核验说明中。

下次避免：新增源码目录、数据文件名和资源文件名先检查CON、PRN、AUX、NUL、COM1–9、LPT1–9等Windows保留名，不以Java能创建目录作为可分发证据。兼容历史二进制包名时优先改变物理源码布局，避免无关接口重构。

最终验证：十版源码物理目录均已适配。先前五版重新完整单元测试及构建通过，全部生产.class字节逐个与调整前相同；后五版在首次正式构建前适配并完成全部测试。正式源码Git索引已成功，不再出现AUX路径无法打开。
