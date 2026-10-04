# 当前项目版本适配参考

## 26.2测试版11：服务端获取服务器

ServerPlayer.getServer（服务端玩家获取服务器）已尝试失败：26.2无此方法。成功方式：先取得当前ServerLevel（服务端世界），再调用世界getServer（获取服务器），如((ServerLevel)player.level()).getServer()。跨维度迭代使用此服务器的getAllLevels（所有世界）。保持Java（爪哇）25与Mojang官方映射，完整版本见versions/1.1.4-test11/mc26.2/VERSION_INFO.md；本项目之前适配记录保存在各版本docs/PORTING_NOTES.md。

## 正式1.1.4八版本适配
优先参考 docs/bugs/BUG-20261004-FORMAL-114-PORTING.md 与 scripts/port_final_114*.py、scripts/fix_final_114*.py。历史纹理遗漏须比较各目标与验收基线，不能只比较26.2版本差量。26.2/26.1.2采用Mojang官方命名；旧版网络、SavedData、静默写入、Java17列表与DataResult接口须分别适配。独立世界用实际实体分区核查未激活区块掉落，断言保持原精确数量。所有测试源码仅-PwandSmoke启用，运行包排除测试类。1.20.1固定FabricAPI0.92.9要求Loader>=0.16.10，构建使用0.16.14。

## 1.1.5旧灯存档加载

BlockEntity.setChanged（方块实体标记修改）会通过世界getChunkAt（获取所在区块）再次加载当前区块；禁止在setLevel（初始化关联世界）迁移分支调用。成功方法：读取时记录迁移待保存，ServerChunkEvents.CHUNK_LOAD（区块已加载事件）直接使用传入LevelChunk（区块）标记保存。26.x至1.21.4使用markUnsaved，1.21.1与1.20.x使用setUnsaved(true)。保护副本灯与未供电灯；必须独立存档保存、迁移重载、再次重载核对写盘，不能仅测新建世界或方块放置。详见docs/bugs/BUG-20261005-LIGHT-LOAD-CHUNK-REENTRY.md及scripts/verify_light_load_115.py。
