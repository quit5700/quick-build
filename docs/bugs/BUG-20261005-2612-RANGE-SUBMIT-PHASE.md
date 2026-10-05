# 26.1.2两点确认后范围框不显示

当前项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。类型：客户端渲染阶段适配。状态：1.1.7完整构建及195项自动测试通过，实际客户端画面待玩家验收。

现象：建筑与删除两点确认后不显示范围框，实际建造/删除正常。上一轮相机坐标修复不包含此问题，不得用服务端测试通过声称视觉已修好。

根因：26.1.2的BuildBoxRenderer与DeleteBoxRenderer在BEFORE_TRANSLUCENT_TERRAIN（透明地形绘制前）向SubmitNodeCollector提交自定义几何，此时FeatureRenderDispatcher的实体/自定义几何绘制已经执行。提交节点本帧不再绘制，帧末clearSubmitNodes清空。正常单点高亮在COLLECT_SUBMITS（收集绘制任务）提交。已对照实际Fabric 23.2.0+c9da2e974c的LevelRendererMixin注入位置，以及实际Minecraft 26.1.2 LevelRenderer字节码顺序，不依赖其他版本推测。

修改文件：versions/1.1.7/mc26.1.2/src/client/java/cn/quit5700/buildingwand/client/BuildBoxRenderer.java、DeleteBoxRenderer.java，两处事件替换。其余游戏逻辑、范围坐标、选区状态、材料、耐久、模型不改。旧1.1.6保留。

验证：新增RangeSubmitPhaseRegressionTest，对生产范围框和单点预览的事件注册作提交时序约束，原代码失败；此测试是渲染阶段结构防回归，不能替代真实GPU客户端画面。实际类字节码与事件注入证据保存于本次交付。完整clean test build通过，195项测试无失败、错误或跳过。运行包字节码确认两个范围框绑定COLLECT_SUBMITS；新旧包逐条目比较仅版本信息和两个范围框类改变。没有运行真实玩家GPU画面，不宣称已实机验收。

其他版本：26.2存在同样晚提交写法，但用户此前功能验收正常，记录为需实测风险，本次不替换其运行包。1.21.11范围框与单点高亮使用相同BEFORE_TRANSLUCENT事件，不套用26.1.2事件名；其余旧版采用直接缓冲绘制，不能仅按事件名判断。推荐后续按版本实测两点框，确认同症状再同步最小修复。

避免重复：提交节点不等于立即绘制，必须核对实际目标版本提交、消费和清空的顺序。检查相机位置不能替代检查绘制阶段；服务端选区执行成功不能替代客户端画面验证。已尝试失败：上一轮仅修相机坐标，未覆盖范围框消失。
