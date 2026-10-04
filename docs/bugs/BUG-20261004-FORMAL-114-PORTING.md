# 正式版1.1.4跨版本交付与外观核查
当前根目录：快捷建筑模组 Git 仓库。
类型：版本接口适配、资源加载、交付。
基线：用户验收测试版11；仅调整取消按钮位置，不改变功能。
已尝试失败：直接覆盖新法杖调用但保留旧 AreaSelectionClient 会遗漏 cancelForDeletionWand；补入同等选择取消方法，保留版本专属渲染。
已尝试失败：1.21.11 照搬26.2 sendOverlayMessage、SavedDataType 标识符和 LevelRenderEvents；按该版本改为 displayClientMessage（动作栏）、字符串存档名称和 WorldRenderEvents，客户端普通消息用 displayClientMessage false。
修改文件：各版本 buildingwand 模块与 AreaSelectionClient、正式适配脚本。
验证：八版本正式clean test build（清理、测试、构建）均通过；每版本27项独立世界回归通过，共216项。发布核验.json记录自动测试数量、八语言、资源及包校验。GitHub尚未发布，等待用户验收。
避免重复：按各目标实际接口适配；保留旧版光源恢复和渲染钩子，不能用新版本整个项目覆盖旧版本。

外观根因：26.2正式1.1.3已含新版建筑/移动法杖PNG，但其他版本仍旧纹理。按26.2相对1.1.3差量无法识别这种历史遗漏；正式交付统一同步全部PNG，并逐文件校验。16色远程开关图片和模型与26.2基线一致，不重新设计。

旧版静默装配：1.21.4及以下没有UPDATE_SKIP_ALL_SIDEEFFECTS。已核对实际游戏LevelChunk字节码，普通setBlock仍调用onPlace/onRemove，不能简单删除标志或假定移动活塞标志等价。新增仅法杖静默写入期间的线程局部守卫及LevelChunk两处回调拦截，保留正常游戏回调；写齐后仍走原确认/邻居更新逻辑。已通过各版本独立世界验证。旧版区块票据按目标TicketType.create/addRegionTicket适配；1.21.8为TicketUse.LOADING。

独立1.21.4世界回归发现：静默跳过旧版onRemove时，方块实体清理原本依赖该回调，导致替换后残留旧实体。仅静默写入、方块种类变化且原方块有实体时显式removeBlockEntity；不触发内容掉落，移动内容仍由快照恢复。严格回归普通石头不受保护、多格警告不拦截和余位内容精确掉落未修改预期。26.2测试环境缺完整FabricAPI元数据，复制既有测试环境的同版本完整API，仅用于独立测试。Java17测试代码按旧DataResult.getOrThrow(false,error)与List.get(0)等价适配，不跳过或削弱断言。

测试环境问题：1.21.4及以下在无真实玩家的SERVER_STARTED阶段，远端区块生成的掉落实体可能处于HIDDEN实体分区，getEntitiesOfClass不返回它们。为严格核查内容是否丢失/重复，独立测试读取实体分区完整实际实体列表（同一包围盒、排除已删除实体），保持13钻石/7金锭和正常掉落数量原断言不变；正式游戏代码不因测试观测接口调整。另1.20.1固定FabricAPI0.92.9要求Loader>=0.16.10，旧构建Loader0.15.11未进行加载验证，已失败；升构建最低Loader为0.16.14，实际玩家目录已为0.19.3，无需修改启动器。
