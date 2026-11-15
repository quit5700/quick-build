# 当前项目版本适配参考

## 26.2测试版11：服务端获取服务器

ServerPlayer.getServer（服务端玩家获取服务器）已尝试失败：26.2无此方法。成功方式：先取得当前ServerLevel（服务端世界），再调用世界getServer（获取服务器），如((ServerLevel)player.level()).getServer()。跨维度迭代使用此服务器的getAllLevels（所有世界）。保持Java（爪哇）25与Mojang官方映射，完整版本见versions/1.1.4-test11/mc26.2/VERSION_INFO.md；本项目之前适配记录保存在各版本docs/PORTING_NOTES.md。

## 正式1.1.4八版本适配
优先参考 docs/bugs/BUG-20261004-FORMAL-114-PORTING.md 与 scripts/port_final_114*.py、scripts/fix_final_114*.py。历史纹理遗漏须比较各目标与验收基线，不能只比较26.2版本差量。26.2/26.1.2采用Mojang官方命名；旧版网络、SavedData、静默写入、Java17列表与DataResult接口须分别适配。独立世界用实际实体分区核查未激活区块掉落，断言保持原精确数量。所有测试源码仅-PwandSmoke启用，运行包排除测试类。1.20.1固定FabricAPI0.92.9要求Loader>=0.16.10，构建使用0.16.14。

## 1.1.5旧灯存档加载

BlockEntity.setChanged（方块实体标记修改）会通过世界getChunkAt（获取所在区块）再次加载当前区块；禁止在setLevel（初始化关联世界）迁移分支调用。成功方法：读取时记录迁移待保存，ServerChunkEvents.CHUNK_LOAD（区块已加载事件）直接使用传入LevelChunk（区块）标记保存。26.x至1.21.4使用markUnsaved，1.21.1与1.20.x使用setUnsaved(true)。保护副本灯与未供电灯；必须独立存档保存、迁移重载、再次重载核对写盘，不能仅测新建世界或方块放置。详见docs/bugs/BUG-20261005-LIGHT-LOAD-CHUNK-REENTRY.md及scripts/verify_light_load_115.py。

## 1.1.6法杖范围框与客户端交互

范围框禁止反射猜相机方法名及脚部回退；26.2编译期mainCamera，26.1.2编译期getMainCamera，1.21.11渲染上下文cameraRenderState，较旧版本context.camera对应API。已尝试失败：26.1.2直接照搬mainCamera，必须核对实际版本类接口。资源比对必须包含物品JSON模型，不仅PNG。请求先原版同步手持槽位，再发带ID和槽位的法杖命令；排队保留查询期间点击，超时仅QUERY不重复变更；回包包含权威模式、两种材料。详见BUG-20261005-WAND-CLIENT-INTERACTIONS.md，真实handle请求/编码测试不能被工具函数直调替代。

## 1.1.8-test6独立警告提示
26.2直接使用Minecraft.screen字段已尝试失败：此版本不提供该公开字段。禁止猜测getScreen反射回退。采用各版既有ClientWandController已使用的mouseHandler.isMouseGrabbed判断游戏视角状态；鼠标未被游戏捕获时隐藏提示。26.x文本使用GuiGraphicsExtractor.text，1.21.x使用GuiGraphics.drawString，HudElementRegistry在OVERLAY_MESSAGE之后插入。

1.21.11/1.21.8直接使用ServerPlayer.sendOverlayMessage已尝试失败。两版已成功基线使用displayClientMessage(component,true)，独立红色警告应移除对应displayClientMessage调用，不能只删除26.x同名位置。改动两版必须连同新旧API检查，禁止盲目复制26.x新增行。

## 1.21.4待办适配

Inventory.setSelectedSlot已尝试失败：本版使用selected字段赋值。NBT用getInt，存档保留SavedData.Factory；静默放置必须复用QuietPlacement封装。独立HUD使用本地实际API HudLayerRegistrationCallback/IdentifiedLayer，不存在新版HudElementRegistry。完整验证见BUG-20261007-1214-PENDING-CHECKLIST.md。

## 1.21.1逐版待办与灯缓存

沿用成功缓存身份检查，保留本版通光API；Item.Properties无新enchantable，建筑杖覆写getEnchantmentValue。HudRenderCallback为本版现有接口，不存在新版分层HUD。配方ResourceLocation查找与item/tag原料；测试落物恢复本版allItems实体分区观察，不使用无法观测隐藏分区的getEntitiesOfClass。完整记录见BUG-20261007-1211-LIGHT-CRASH-PENDING.md。


## 26.3成功适配
Java25、Loom1.17.21、Gradle9.6.0、Loader0.19.5、API0.163.0。Block不再要求codec/simpleCodec；红石类为RedstoneWireBlock；PushReaction.IMMOVEABLE保持不可推动。客户端使用InputConstants.isKeyDown(int)，旧org.lwjgl.glfw.GLFW运行类已移除；仅compileOnly补旧类已尝试失败。完整原整合客户端运行验证是必要检查。详见BUG-20261024-263-PORTING.md。

## 26.3原版结构文件验证
服务端获取结构管理器为getStructureTemplateManager；worldTemplates().createAndValidatePathToStructure需传WORLD_STRUCTURE_LISTER，实际generated目录下使用structure（单数）及.nbt后缀。原版/place template可加载超过结构方块GUI尺寸的文件，但范围区块需预先加载。本项目两座原版结构已实际放置通过，详见BUG-20261025-STRUCTURE-FILE-VALIDATION.md。

## 26.3结构NBT字段（test4）
NbtUtils使用id/properties，旧Name/Properties必须显式规范化。不能只看/place template返回；必须检查引擎调色板、数量及世界实际方块。JUnit涉及Minecraft归档时，沿用ASCII临时目录同步测试依赖，不要求用户改中文工程目录。详见BUG-20261025-263-BLUEPRINT-STATE-SCHEMA.md。

## 26.3辅助投影与生成箱（1.1.9-test5）
submitBlockModel末参数是描边色，透明需材质与顶点颜色；非空模型部件可能无几何。特殊方块实体提取须匹配shouldRenderOffScreen类型，使用独立姿态栈。床色按注册表获取；配方需category（分类）。26.3床已使用普通几何，不再假设是旧版床方块实体。参见docs/bugs/BUG-20261025-AUX-WAND.md。

## 26.3测试7输入与布局
辅助旋转不能在三轴一律读取yaw（左右朝向）；横轴使用pitch（俯仰），切轴保存组合旋转序列，并在服务端整体验证提交。查询接口仅返回现有行。界面不叠加深色装饰，保留测试6原版灰色风格与整数像素处理。参见BUG-20261026-AUX-ASPECT-ROTATION.md。

## 26.3测试8
辅助选点复用建筑4格射线，测试目标必须在范围内；中心方块支点加玩家相对位置替代旧yaw/pitch（左右朝向/俯仰）累加旋转。预览与服务端仍共用整数90度变换。Jade配置名称需提供config.jade.plugin_light.redstone_energy_network八语言翻译。详情见BUG-20261026-AUX-SEVEN-ISSUES.md。

## 26.3测试9信标塔
存档形状追加编号12，界面SHAPE_ORDER负责插入位置；不可重排旧枚举。默认多材料形状须同时适配渲染、生成按钮与文件导出，不能因Material为空跳过。实际26.3原生beacon_base_blocks标签为铁、金、钻石、绿宝石、下界合金五种。详见FEATURE-20261026-BEACON-TOWER.md。

## 26.3测试10选区层数
用户已取消测试9的建筑4层上限。AuxBeacon.levels只取h-1与(min(w,d)-1)/2，不再min(4)。原版效果等级独立保持4，生成箱现有计划不改写。详见FEATURE-20261026-BEACON-SELECTION-SCALING.md，旧测试9文档仅作历史。

## 测试11十版本同步（2026-10-27，执行中）

测试11取消默认铁块，未选材的底座和顶部均为纯色、不生成；选择材料后顶部信标独立计料。用户已恢复1.21.10并授权全部十版同步，测试10的“暂停”只作历史记录。

九个旧版NbtUtils.writeBlockState仍保存Name/Properties，生成任务执行校验与解码必须兼容该原生格式，不能只读取规范文件的id/properties。统一入口AuxMaterials.stateId/readState，规范文件无需改格式，原有任务保留。相关失败证据和回归见BUG-20261027-TEST11-VERSION-PORT.md。

26.3原生左键编号1、26.2及更旧0，探针按版处理；生产界面采用原生按钮，不改按钮逻辑。26.1.2按既有mouseHandler.isMouseGrabbed保护输入，别调用不存在的gui.screen。旧版NBT桥接不能复制可变子节点；1.20.1压缩读取以GZIPInputStream与带NbtAccounter的DataInput读取保留上限。Java17线程池不实现AutoCloseable，测试finally中关闭并等待，不能放宽并发保存断言。

适配时原有模型、旧配方与掉落表保留本版成功基线，仅追加新功能资源；1.20.x使用复数recipes/loot_tables目录与旧结果item字段，1.21.1的配方键为对象，1.21.4开始本项目使用直接字符串。模型渲染保留真实四边形，1.20透明度用顶点转发处理；特殊方块实体用各版原生渲染接口。

## 26.3测试12建筑箱供料

仅26.3改动，保留独立版本基线。BuildTaskState与AuxState通过箱任务标识关联，材料上限使用常数空间估算，按实际放置逐件扣料；完成及取消由不递归通知的入口退料。任务旧存档缺少supply字段默认转箱，内部add接口继续供原核心回归验证，玩家范围入口使用addBox/addCreative。八语言更名蓝图法杖，注册ID不变。

无网络连接的服务端测试玩家不能执行真实生成箱同步。旧火把入口回归需先断言新箱供料任务创建，再以原低层生存接口验证相同材质/过滤/扣料预期；真实箱投料、取消及存档重进在独立实际客户端中验证。不能见到Gradle成功就声称世界检查通过，必须检查逐项失败标记和最终测试标记。26.3床从注册表按minecraft:red_bed读取，不能使用不存在的Blocks.RED_BED常量。详情见FEATURE-20261027-BUILD-SUPPLY-BOX-TEST12.md。


## 26.3测试13自由材料信标塔

五矿物白名单原先同时在材料页、服务端编辑、AuxRow读取、AuxBeacon取材以及切形状清材料五条路径生效；用户现要求放开，需要一起移除。采用普通蓝图的BlockItem（方块物品）目录及通用注册表校验。顶部仍单独使用原版信标，普通底座只负责造型，不能改写原版激活。保留默认未选材及最大完整层数，其他九版未同步。旧“拒绝石头”测试因需求改变更新为普通材料接受且顶部不变，原五矿物与几何测试继续保留。


## 26.3测试14预览深度与统一生成箱

AuxRenderer彩色面、轮廓与材质模型不能共面绘制：实际顶点回归捕获原始错误，填充和边线按法向分别外移0.003格。分批网格固定裁剪中心并以完整缓冲切换。范围任务统一addBox，创造由箱内execute启动，生存收齐Need材料后启动。buildRunning检查存量部分供料与游戏模式变化，禁止模式切换免费生成。最后一件材料使用后不能立即将Running清零，成组方块游标尚需收尾，否则床已生成仍卡住；实际客户端验证_1失败证据保留，验证_2通过。其他九版未改。


## 26.3测试15：生成箱三维近位与实际连接面

放箱必须先检查人物附近三维空位，同高度环形搜索会漏掉近处上下空间、让浅范围内部玩家得到远角箱。近位仍排除建造范围、碰撞体、占用、越界及权限；外部原搜索仅兜底。蓝图旋转从真实连接射线命中面保存世界法向，不得固定使用第一项负Z面。水平回转优先Y轴，避免180度先绕X轴造成上下翻转；同组所有行共用中心支点与旋转序列，不重算尺寸或形状。原4项失败证据、6项新增单元检查和实际客户端操作回归见BUG-20261027-BOX-CONNECTION-FACE-TEST15.md。仅26.3，不据此覆盖其他版本。


## 测试16十版本：任务投影与源码元数据

建筑任务投影通过既有所有者Jobs同步PreviewBounds、PreviewCursor、PreviewDirection和PreviewMode。客户端仅生成只读显示行，不加入Rows或导出文件。遍历顺序为分区Z、分区X、X、Z、Y，分别乘原方向比较，负坐标须按右移16格区块处理；空心模式只显示原壳。进度变化用待发送标记，在刷新边界送出最后变化。缓存中的建筑面按最新任务与游标过滤，不等待大蓝图新网格构建后才移除取消面。十版SavedData工厂、保存ID、旧Name/id材质解码、界面及顶点接口分别保留。

旧版sourcesJar不会自动使用processResources展开结果，须显式UTF-8展开版本并登记版本任务输入。最终审计必须打开运行包及源码包核对内部版本和源码，不只检查文件名。26.3控件左键编号1，其他版本0，遵循本文件既有成功记录。


## 1.1.9-test17 十版本

测试17：生成箱首次同步携带完整高亮信息；建筑高亮优先绘制，大蓝图未处理完也显示；完成或取消时随任务清除。生成箱优先放在选区靠近玩家一侧的外缘，避开选区、障碍、玩家和其他任务箱，边缘附近没有安全空位时回退备用搜索。

本批逐版执行合计3199项单元测试、600项世界场景、600项配方匹配、190项真实客户端生命周期检查。真实客户端包含首次箱子完整数据、反向游标和中空模式、大蓝图并存时高亮出现、空手保持、进度、取消、实际建完5块后箱子及高亮消失，以及保存退出。没有重复沿用测试16的76080项结果冒充本批验证。

共用规则分别适配本版NBT（命名二进制标签）接口与原生渲染回调，不能直接覆盖版本专用接口。普通蓝图完整缓冲不能阻塞新建筑任务高亮；首次箱子任务消息必须携带实际范围、方向、游标及模式。位置策略改变只作用于新箱子。详细红灯复现、最终源码对应证据和限制见docs/bugs/BUG-20261031-BUILD-PREVIEW-LIFECYCLE-TEST17.md。


## 测试18名称与界面探针

生成箱同时维护block（方块）与item（物品）两种名称键，八语言同时遗漏一个键不会被原键集合一致检查发现，须核对实际物品名来源。26.2主菜单创建ItemStack（物品堆）出现Components not bound yet（组件尚未绑定）；26系列名称探针应先进入本项目独立测试世界副本，再检查真实说明台。界面访问器沿用本版成功方式，或直接保存已打开的Screen（界面）对象；不混用旧mc.screen和新mc.gui.screen()。详见BUG-20261104-GENERATION-BOX-ITEM-NAME-TEST18.md。

## 测试22补充

1.21.1 蓝图拖动应区分几何内容与姿态。已有网格可按源行和当前行的相对姿态映射，纯平移复用面方向与边界，不重新计算拓扑；旋转时映射面朝向和材料状态。预览扫描键按几何、旋转及局部视点的16格分区更新，避免每个平移步都重置，也保留大位移后的可见范围刷新。多个重叠行保留各自行归属，在当前世界坐标去重材料模型。检查实际绘制帧，游戏刻可能没有对应的渲染帧。

生成箱按用户明确约定仅由法杖任务生成，十版取消配方、创造栏入口、物品掉落和手动放置。保留注册身份兼容旧存档与说明页。配方回归改为其余59项实际匹配加生成箱缺失检查，不能重新添加生成箱配方。

本轮中文源码路径和中文测试路径继续使用。Gradle 9 与 Java 25 的工作进程参数包含目录链接解析后的中文缓存路径时曾无法加载 GradleWorkerMain。英文目录链接不足以覆盖此情况，使用 H:/CodexWork/q5-gradle-cache 实体缓存目录和 H:/CodexWork/q5-settings-runtime-tmp 实体临时目录验证通过。未修改全局系统编码。详见 docs/bugs/BUG-20261114-DRAG-GENERATION-BOX-TEST22.md。
## 测试23输入及整合验证

1.21.1 不只在游戏刻轮询鼠标按住状态；原生 onPress 入口按当时界面状态过滤，并保留事件直到消费。菜单点击与世界点击需隔离，取消清空队列。退出键使用 keyPress 原生入口，保留原暂停菜单。独立整合副本在开发映射下的 Iris 注入选择器曾失败，普通实际加载方式使用原实例库和全部活动模组副本通过；不要把开发映射失败当作原游戏冲突或删改原模组。详见 BUG-20261115-AUX-TRANSFORM-INPUT-TEST23.md。

## 测试24旧版输入移植

1.20.1／1.20.4使用Java17集合接口适配测试23选择与事件队列，不复制新版本网络包实现。原生MouseHandler.onPress、KeyboardHandler.keyPress分别登记，按当时界面状态过滤。1.20.4整合副本的VisibleTraders要求Java21，不能仅依据游戏基础Java17选择整个整合环境。Python经cmd嵌入引号的绝对-I路径方案失败，测试初始化脚本改用中文工作目录中的固定相对文件名。详见测试24缺陷记录。
