# BUG-20261004-WAND-BUILD-MOVE-UNBOUNDED

当前项目根目录：C:\Users\quit5700\Documents\mc虚拟线路模组\快捷建筑模组。
分类：核心状态、数据保存、版本接口、构建、资源语言；范围versions/1.1.4-test3/mc26.2。

## 现象和根本原因

旧建筑操作需要多种组合键，取消上限后旧ShapeBuilder（形状列表生成器）会把体积展开为内存列表。改为常量空间游标与持久任务，选区客户端只绘制端点或整体边框。
旧移动使用UPDATE_CLIENTS（只发客户端更新）并不能禁止形状更新和放置回调，分步清空或恢复可能触发红石、流体；旧位置计划更新、方块事件也没有迁移。26.2的UPDATE_SKIP_ALL_SIDEEFFECTS（跳过全部副作用）与UPDATE_KNOWN_SHAPE（保留形状）用于单次搬移，并在完成后恢复边界和计划更新。
旧快照确认后源方块或容器发生变化可能被旧数据覆盖，新增确认时重新比对。
代码审核发现两个潜在回归：不同维度清理时重置网络序号会使服务端拒绝新请求；对分离源目标使用简单OR判边界会把内部全部误判。分别保留连接序号，使用MoveUpdatePlan（搬移边界计划）并增加测试。

## 已尝试失败与修复

Gradle（构建工具）任务compileClientJava（客户端编译）不存在，本项目通过main源集包含客户端目录；改用compileJava（编译）与test build（测试并构建），不得重复调用不存在的任务。
ChunkPos.asLong（区块坐标转长整型）在26.2不存在，javap（Java类接口检查器）确认使用pack；PistonBaseBlock（活塞基座）需引入block.piston子包。已修复编译通过。
语言测试首次失败：动态拼接mode.q5.被扫描为不存在的完整键。使用明确枚举modeKey返回三个完整翻译键，不删除或放宽原测试正确预期。
PowerShell双引号内嵌Python命令因引号截断在执行前语法失败，没有修改文件。改为UTF-8脚本文件调用；该内嵌方式已尝试失败，后续禁止重复。
独立平坦测试世界初建缺少layers（地形层）配置产生一次日志提示，服务器正常生成并运行；OSHI（系统信息库）取进程资源警告为环境诊断输出，不是模组异常。日志保留。

## 修改文件与修复方式

BuildPlan、BuildWandData、BuildNetworking、BuildWandServer、BuildTaskState、BuildWandClient、BuildBoxRenderer实现三模式、每杖记忆、服务器选点、GUI（图形界面）闪烁、逐列建造、材料暂停补充继续和右键取消。
WandActions、MoveUpdates、MoveBlockEventsAccessor、MoveSizeNotice、MoveUpdatePlan、MoveBoxRenderer取消移动上限，控制原子搬移副作用，迁移定时事件、旋转活塞事件方向、失败恢复和大范围警告。
WandSettingsScreen与八语言资源更新说明台、模式名称和操作说明，删除可见上限控件。原配置读取结构保留以兼容旧存档，不再限制建造和移动。
新增BuildPlanTest、MoveUpdatePlanTest，保持旧正确断言，并扩展LocalizationResourcesTest覆盖新键。
src/smoke/java只在-PwandSmoke（独立世界测试参数）编译，用真实服务器世界验证副作用控制、状态、容器、事件迁移和回滚；生产运行包明确排除测试类。

## 验证与限制

针对当前模块10项新增测试通过，完整回归170项通过，既有160项保留。独立服务器世界测试通过；补测水、岩浆、含水方块、观察者状态与定时更新、红石线、容器内容、活塞事件旋转、回滚及每杖模式保存。
玩家实际界面、闪烁和补料/取消操作尚需实机验收。测试证明搬移过程不对半完成结构执行正常邻居更新；不能承诺外部线路或流体环境变化不会影响搬移完成后的正常行为，也不能保证第三方特殊模组方块所有副作用都可自动隔离。
移动不分批，需要完整快照，实际内存仍受机器限制；警告不是硬上限。崩溃/断电期间移动事务不具备跨进程恢复，本次用户已经取消分批中断恢复方案。

## 防止重复

不再用完整坐标列表做大范围建筑预览；不按扫描空气扣材料；自动继续固定确认时材料；缺料右键取消不能变为切模式；维度切换不能重置连接网络序号；搬移不能仅设置UPDATE_CLIENTS；源容器变化后不能覆盖旧数据；原区块计划更新不能遗留或复制两份。

## 实际区块重载回归

扩展独立世界重载测试首次失败：block tick not moved（计划方块更新没有搬走），虽Gradle返回成功但测试标志为FAIL（失败），不能据构建退出码误报。根因是LevelChunkTicks.getAll只遍历已展开队列，重载区块的SavedTick仍在pendingTicks中。MoveUpdates现在先调用LevelChunk.unpackTicks(world.getGameTime())，再读取计划更新。修复后同一重载世界中的红石/观察者、流水/岩浆/含水、容器与活塞事件测试均通过。首次失败日志和修复后日志留存。下次复制区块事件必须同时覆盖已加载与已保存待展开更新。

测试世界在线身份密钥请求偶遇SSL握手失败，独立离线回环服务器正常完成测试；这是外部认证网络问题，不修改模组逻辑绕过。
