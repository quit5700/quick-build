# 供电灯初始化递归崩溃

类型：方块实体生命周期、核心逻辑回归。当前项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。

状态：test4 代码修正、自动回归与本地安装完成；实际存档重进待验收，不标记实机验证完成。

## 现象与证据

用户提供 G:/game/mcfabric/minecraft-exported-crash-info-2026-09-13T15-31-13.zip。仅只读检查日志，不执行包内 launch.bat（启动脚本），不读取或修改存档。

minecraft.log（游戏日志）记录快捷建筑 1.1.3-test3，反复出现调用环：Level.getBlockEntity → AreaLightState.update:38 → VariableConstantLightBlockEntity.setLevel:87 → LevelChunk.setBlockEntity → addAndRegisterBlockEntity → getBlockEntity。

## 根本原因与排查

灯的 setLevel（关联世界）发生在区块登记该对象之前；test3 新增的区域更新重新按位置查询它，触发再次创建／登记，重复进入初始化。供电条件成立就会进入该路径，不限于两点模式；复制灯共用该实体类，也需回归。

另外核对了客户端同步和区域数据读取入口：同步仅从已存的源列表与区域列表生成快照，没有该位置的方块实体查询；日志也直接指向区域登记而非数据解码。采用当前对象直接登记，无需修改存档或增加延迟任务。

## 修改文件和方式

- mc26.2/src/main/java/cn/quit5700/light/block/VariableConstantLightBlockEntity.java：setLevel 改为 AreaLightState.update(this)。
- mc26.2/src/main/java/cn/quit5700/light/light/AreaLightState.java：增加仅接收现有灯对象的入口；原世界加坐标入口用于正常运行更新，委托现有对象入口。保留原模式、复制灯、供电及选区有效性条件。
- mc26.2/src/test/java/cn/quit5700/light/logic/AreaLightInitializationTest.java：使用 Java（爪哇）25 类文件接口读取真实编译产物，约束初始化必须调用对象入口；对象入口调用白名单阻止重新加入世界查询或生命周期重入。
- gradle.properties、README.md、VERSION_INFO.md、问题索引和交付文档：升级至 1.1.3-test4 并记录验证范围。

## 验证

新增两项测试在修改前均失败；生产代码修改后定向测试通过。第一次完整构建有 1 项本地化测试失败：新增测试断言的中文字符串也在仓库扫描范围。仅改断言为英文，未放宽或跳过旧测试；这是测试文本规则问题，与游戏中文路径无关。

最终完整构建成功，103 项测试、0 失败、0 错误。中文项目路径下 UTF-8 编译成功。旧模式与复制灯呈现测试通过。安装包与分发包 SHA-256 一致。

限制：未构建可运行原存档的实机自动复现环境；本轮反馈回路是用户真实崩溃调用栈加编译调用路径回归，不是完整世界加载测试。仍需实机验证加载供电灯、放置／拆除、重进存档、复制灯与两点范围恢复。

## 避免重复

已尝试失败：初始化期间 update(level, position) 再查自身。禁止恢复此方案；不得用扩大线程栈、捕获递归异常、删除灯或修改存档规避。新增调用必须检查注册顺序。不能用纯坐标单元测试替代生命周期验证，也不能把 103 项测试通过宣传为实机已修复。
