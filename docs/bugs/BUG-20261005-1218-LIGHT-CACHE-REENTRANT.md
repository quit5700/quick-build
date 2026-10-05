# 1.21.8灯光缓存计算期间失效崩溃

当前项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。分类：世界更新回调重入/缓存状态。版本：1.1.8-test1，仅1.21.8本次授权修复，自动及独立服务器验证通过，玩家存档待验收。

现象：用户提供2026-10-05 10:19错误包，实际加载quick_build 1.1.6，在服务端tick崩溃。DaylightOcclusion.java第58行source.volume.complete()空指针。不是法杖绘制问题。

根因：推进ConnectedLightVolume时读取世界，方块或区块更新回调可在同线程重入changed/chunkChanged并清空source.volume。synchronized是可重入锁，不能阻止同线程回调清空缓存。advance返回后再次访问可变字段即空指针。真实独立服务器回归在生产advanceSource入口的通光回调清空缓存，旧代码同异常复现：四项3通过/1失败。该回归模拟失效回调，不声称重放了用户存档的具体触发方块。

修改：DaylightOcclusion.tick把单灯推进抽成advanceSource以供真实回归直接调用；推进前保存本轮ConnectedLightVolume引用，完成后仅在source.volume仍为该对象时发布完成，失效则下一轮重新创建计算。不恢复已经失效的旧缓存，不改变灯模式、区域、预算或存档。测试仅在原有lightLoadSmoke独立启动入口内注册，运行包排除全部SmokeChecks类。

修改文件：versions/1.1.8-test1/mc1.21.8内DaylightOcclusion.java、src/smoke/java内DaylightReentrantSmokeChecks.java及LightLoadSmokeChecks.java、build.gradle测试配置、gradle.properties版本和版本说明。LightMod生产入口最终保持1.1.6字节码。

已尝试失败：普通JUnit测试运行类路径缺少Minecraft类；直接追加compileClasspath以及映射jar仍无法在该项目测试任务中载入BlockPos。没有把这些类路径失败当作BUG复现；改为独立真实Minecraft服务器回归。失败日志保留，不再次重复该方案。

验证：四项直接覆盖回调清空后无崩溃、下一轮重算、计算期间替换缓存不发布旧结果、正常完成只发布一次、预算及部分计算续算。实际结果：旧代码3/4，缓存清空案例同样空指针；修复后test1为4/4。完整197项测试、31项世界回归和六灯三阶段磁盘重载全部通过；运行包仅DaylightOcclusion类及版本信息变化，两个内部类仅源文件行号调试元数据变化，字段及方法字节码一致。实际玩家存档仍需验收。实际玩家存档未修改，其他已验收版本未修改，GitHub等待用户通知。

避免重复：缓存字段在世界回调前后的身份可能变化，不能仅靠进入同步块时非空判断。无效计算不得重新写回，不能仅补null判断而错误发布旧结果。生产入口必须有回调失效回归，基础连通算法测试不足以覆盖世界重入。
