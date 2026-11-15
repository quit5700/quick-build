# 建筑结构文件验证记录
当前项目根目录：C:\Users\quit5700\Documents\mc虚拟线路模组\快捷建筑模组
类型：26.3结构导入接口与验证夹具。
现象与原因：初次夹具调用不存在的MinecraftServer.getStructureManager，26.3正确接口是getStructureTemplateManager；TemplatePathFactory单参数只返回原始资源路径，必须传WORLD_STRUCTURE_LISTER才能得到generated/q5/structure/*.nbt；原版/place template要求整个范围区块加载，测试先添加强加载并等待80 tick，边界检查包含size末端；重复运行时先解除夹具旧票据，避免forceload无变化抛错。
已尝试失败日志保留，均是验证器问题，不是交付结构格式损坏。修改文件为scripts/verify_two_vanilla_structures.py及其输出验证工程，不改生产模组。
预览初次采用了相反面的投影排序，修正为正X/Z可见面并按深度排序；图片已重新生成。车站连廊补齐楼梯连接口，最终数据重新全格比对并在游戏引擎重新放置通过。
验证：两份NBT全部palette及属性在26.3原版注册表通过；每格NBT、Sponge v3和源数据一致；两座原版/place template实际放置通过，无实体及容器库存。不代表全建筑逐步步行和每个WorldEdit版本已实测。
下次避免：不猜26.x方法名和结构路径，不用命令返回构建成功代替检查成功标记，不把预览绘图错误当作结构损坏。只在独立测试世界加载和放置，不修改玩家世界。

更正：test4实机发现26.3原生状态为id/properties，原来的Name/Properties样例并未被原版正确解码。此前仅凭/place template返回成功的结论不足，撤回“原文件实际内容已验证”的表述。修正版另存outputs/20261025_两座原版建筑结构_26.3修正，已补方块种类数量、状态和世界实际逐格验证。旧文件作为失败历史保留，后续使用修正版。详见BUG-20261025-263-BLUEPRINT-STATE-SCHEMA.md。
