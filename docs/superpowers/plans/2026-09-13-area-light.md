# 两点区域满亮实施计划

> 使用 executing-plans（执行计划）在当前已授权分支逐项实施；不新建用户任务。

目标：增加直接坐标判定的可重选长方体满亮区域。

架构：纯整数 LightBox 定义边界；AreaLightState 保存活动区域；AreaSelectionNetworking 管理短时选择会话；客户端框线预览与分批刷新独立于通光算法。

技术：Minecraft（我的世界）26.2、Java（爪哇）25、Fabric（织物），沿用项目 UTF-8 和中文路径构建方式。

- [x] 新增 `mc26.2/src/test/java/cn/quit5700/light/logic/LightBoxTest.java`，先验证缺少 LightBox 的失败，再实现 `LightBox.of(4,5,6,-1,-2,-3).contains(0,0,0)` 为真、外界为假；单轴超过 512 拒绝。
- [x] 在 `logic/VariableLightMode.java` 追加 AREA_FULL_BRIGHT；`VariableConstantLightSettings` 使用按钮 75 对应新模式、76 开始选择，保留应用按钮 74；复制灯只显示旧三模式。
- [x] 新增 `light/AreaLightState.java`，用源坐标及六个范围坐标序列化活动区域；方块实体保存选区，安装、关闭、拆除与重新载入同步区域状态。
- [x] 新增 `network/AreaSelectionNetworking.java`，服务器持有玩家会话，开始操作必须来自有效灯菜单，端点必须在玩家附近、同维度且不超限；第二点成功后一次提交，失败保留原区。
- [x] 新增 `client/AreaSelectionClient.java`，左键仅选点并阻止破坏，Esc（退出键）取消；以十二条细边框预览当前端点范围。
- [x] 扩展局部白昼快照以携带区域边界；坐标比较直接命中区域，跳过通光缓存。客户端每刻最多标记 128 个相关区块段，忽略未加载区块。
- [x] 增加八语言名称、说明、选择结果与错误提示，完整测试 `./gradlew.bat test`，检查旧灯、复制灯协议与资源。
- [x] 构建 `1.1.3-test3`，校验安装包中的新类及资源；备份 test2 后安装到本地 26.2，保存交付记录和本地提交，报告实机验证与 GitHub 状态。

验收状态：实现、自动测试、安装校验与交付记录完成；实机验收待用户测试，GitHub 因凭据无效未同步。
