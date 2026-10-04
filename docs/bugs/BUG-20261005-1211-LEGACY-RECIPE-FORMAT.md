# 1.21.1部分灯配方仍为新版原料简写

当前项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。类型：版本资源格式适配。状态：已发现，未处理；不属于本批法杖输入/预览五项修复。

现象：1.1.6独立测试服务器能启动、灯及法杖测试能通过，但日志报Parsing error loading recipe，部分light命名空间合成配方未载入。并非Java编译错误或地形卡死。

原因：1.21.1原料需要对象形式，如item字段；该版本部分灯配方从前版保留字符串原料，如minecraft:repeater。现有建筑法杖四种配方已有对象格式，不可据此假定全部配方兼容。

建议：单独批次转换该版本现有灯配方key及ingredients的字符串为item/tag对象，保留材料、数量、结果及配方ID，并在实际服务器配方管理器核验每个本模组配方。不要批量把较新版正确字符串写法改坏，不跳过解析错误。

未修改任何配方文件，本次只记录；本批1564项自动测试及31项每版本世界测试不覆盖所有配方语义兼容，须保留此限制。日志：versions/1.1.6/灯加载回归_mc1.21.1_prepare.log。

报错配方：
- light:variable_constant_light
- light:redstone_energy_remote_switch_magenta_dyed
- light:redstone_energy_remote_switch_yellow_dyed
- light:redstone_energy_remote_switch_blue_dyed
- light:constant_light_block
- light:redstone_energy_remote_switch_orange_dyed
- light:redstone_energy_remote_switch_dyed
- light:redstone_energy_sensor
- light:redstone_energy_remote_switch_red_dyed
- light:redstone_energy_remote_switch_cyan_dyed
- light:redstone_energy_remote_switch_light_blue_dyed
- light:redstone_signal_delay
- light:redstone_energy_remote_switch_pink_dyed
- light:redstone_energy_remote_switch_brown_dyed
- light:redstone_energy_remote_switch_purple_dyed
- light:redstone_energy_remote_switch_light_gray_dyed
- light:redstone_energy_remote_switch_lime_dyed
- light:redstone_energy_connector
- light:redstone_energy_emitter
- light:redstone_energy_remote_switch
- light:redstone_energy_remote_switch_black_dyed
- light:redstone_energy_remote_switch_gray_dyed
- light:redstone_energy_remote_switch_green_dyed
