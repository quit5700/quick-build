# 26.3结构文件方块状态字段及验证补强
项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组
类别：版本适配、文件读写、中文路径、测试夹具。

现象：首次实机移动导出完成，但读取报Missing block identifier（缺少方块标识）；先前示例建筑虽然/place template命令返回成功，无法据此证明实际导入了正确方块。
根本原因：26.3的NbtUtils.writeBlockState/readBlockState改为id/properties小写字段；旧格式Name/Properties未做转换。旧验证器只人工校验旧字段和命令返回，没有核对原版引擎读出的内容，因此漏过全空气风险。
修改：仅versions/1.1.9-test4/mc26.3下新增BlueprintFiles、BlueprintNbt、BlueprintReader、BlueprintNetworking及BlueprintSaveScreen；WandActions增加服务端选区快照导出；两个初始化入口和ClientWandController接入；八语言提示和说明台同步；build.gradle完善真实NBT测试运行类路径。其他八版没有修改。
修复：读兼容Name/Properties及id/properties，统一转换为26.3原生小写字段后写出；遇到两个命名字段冲突拒绝；不复制方块实体库存/实体；保留缺失模组标识。示例建筑另存修正版，旧版保留。
文件路径动态取当前实例；非法/保留Windows文件名拒绝，中文及空格测试通过；同名二次确认，原子覆盖，不允许路径穿越。

已尝试失败：仅用编译成功、字符串调色板校验或/place template成功返回认定格式正确；后续不得采用。必须原版StructureTemplate加载后比对方块数量和状态，并核对实际世界非空气方块。
已尝试失败：JUnit只加入minecraftNamedRuntime而未复制其中文路径下Minecraft归档，类发现仍报NoClassDefFoundError。沿用ASCII临时测试输出机制，将Minecraft测试归档也同步后测试；产品本身仍支持中文路径，不要求用户迁移路径。
测试夹具修正：新增第三份导出文件时，原静态强加载边界和二分资源名失效；改为按全部文件尺寸自动计算包含末端的范围，每份夹具使用唯一资源名。失败日志保留，不据此修改产品功能。
测试源码中文路径样例以Unicode转义保留实际中文测试值，未弱化既有禁止Java硬编码中文的回归测试。

验证：235单元全通过，包含13新增测试；完整城堡两格式70870非空气格逐格一致；真实整合客户端右键操作、中文文件、分包、库存排除、保存退出通过；与实际Litematica原生打包器交叉验证。两座建筑与法杖真实导出文件通过原版结构引擎调色板/数量和实际放置检查。
未验证：任意历史版本的数据升级、多服务器并发及超大文件；辅助魔杖UI与生成箱尚未实现，不能声称已交付。

安装记录：只读核验其他版本时改为直接使用test2安装清单，不按版本名推测目录。首次跨盘Path.rename备份失败，旧包未改变；改为复制、SHA256校验后移除旧包，再在同盘提交新包。原包备份保留在版本目录，安装完成校验全部一致。重复导出请求在忙碌时立即回复失败，避免新窗口无限等待；该保护不改变正常导出流程。
