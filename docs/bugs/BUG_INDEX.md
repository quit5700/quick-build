# 当前项目问题索引

- [测试16源码归档版本占位符](BUG-20261027-SOURCE-JAR-VERSION-TEST16.md)：九版源码包补齐UTF-8元数据展开；十版内部版本与最终源码审计通过。

- [测试16：建筑任务高亮与十版本同步](FEATURE-20261027-BUILD-PROJECTION-TEST16.md)：十版3159单元、600世界/600配方、76080完整客户端、照明三阶段及缓存定向验证完成；安装见本批记录，用户待验收。

- [26.3测试15生成箱与固定连接面](BUG-20261027-BOX-CONNECTION-FACE-TEST15.md)：三维近位生成箱与实际连接面旋转；修改前四项失败，310单元、60世界/60配方、7602客户端及照明三阶段通过，已安装测试15，用户待验收。

- [26.3测试14预览深度及箱子确认](BUG-20261027-PREVIEW-FLICKER-TEST14.md)：共面深度冲突回归、完整网格切换；创造点箱按钮、生存收齐材料启动。304单元、60世界/60配方、6365客户端通过，用户待验收。

- [26.3测试13信标自由材料](FEATURE-20261027-BEACON-ANY-MATERIAL-TEST13.md)：按用户新需求移除五矿物建造限制；303单元、60世界/60配方及实际客户端生成检查通过，玩家待验收。

- [26.3测试12建筑生成箱](FEATURE-20261027-BUILD-SUPPLY-BOX-TEST12.md)：建筑法杖生存范围任务复用生成箱，蓝图法杖八语言更名；302单元、60世界/60配方、6250实际客户端及照明三阶段通过，用户待验收。

- [测试11十版本适配](BUG-20261027-TEST11-VERSION-PORT.md)：独立版本接口、中文路径测试运行库、Java17并发测试及原生生成任务Name/id差异；十版自动回归与安装完成，四版弹窗遮挡复验通过，用户待验收；详见各版交付核验。

- [辅助魔杖弹窗与五角星轮廓](BUG-20261025-AUX-UI-STAR.md)：26.3测试6；统一相关弹窗、无阴影文字及窗口适配，保留五角星立体并改善外轮廓，264单元、59世界/60配方及3882项实际界面/生成检查通过并安装，玩家待验收。

- [BUG-20260916-AREA-SWITCH-LATENCY](BUG-20260916-AREA-SWITCH-LATENCY.md)：同版本局部白昼与两点假照明响应差异；9月18日核对录像、日志及存档，录像主灯最终仍为局部白昼，需先确认两点模式实际生效；根因未确认、未修复。

- [BUG-20260914-LIGHT-TEXT-ALPHA](BUG-20260914-LIGHT-TEXT-ALPHA.md)：test6 警告文字不可见，六位颜色遗漏不透明度；test7 颜色回归先失败后通过，实机待验收。

- [BUG-20260913-LIGHT-INIT-REENTRY](BUG-20260913-LIGHT-INIT-REENTRY.md)：test3 供电灯初始化递归崩溃；test4 最小修复、103 项测试通过，实机重进存档待验收。

- [BUG-20260913-AREA-LIGHT](BUG-20260913-AREA-LIGHT.md)：test3 两点区域满亮、按钮协议与 26.2 接口适配；101 项测试通过，实机待验收。

- [BUG-20260913-CONNECTED-LIGHT](BUG-20260913-CONNECTED-LIGHT.md)：test1 实机漏光和黑影反馈；通光算法重做、混合并入原版、副本补齐；97 项测试通过，实机待验收。

- [BUG-20260912-局部白昼遮挡与生成规则](BUG-20260912-DAYLIGHT.md)：26.2 本轮功能实现、接口兼容与启动验证记录；自动测试通过，实机验收待完成。

- [BUG-20260919-ADJUSTABLE-LIGHT-MIGRATION](BUG-20260919-ADJUSTABLE-LIGHT-MIGRATION.md)：可调恒亮灯两模式、复制灯兼容隐藏和16色图标；测试版9完整125项测试通过，实机待验收。

- [BUG-20260919-RELEASE-PORTING](BUG-20260919-RELEASE-PORTING.md)：1.1.3 八版本适配、历史光源恢复与加载配置回归，详见正式版发布核验。

- [BUG-20261003-DELETE-WAND-26-API](BUG-20261003-DELETE-WAND-26-API.md)：删除法杖 26.2 区块坐标访问器、客户端鼠标状态和 UTF-8 构建输出适配。

- [BUG-20261004-DELETE-WAND-STATE-RENDER](BUG-20261004-DELETE-WAND-STATE-RENDER.md)：删除选区跨持物保留、翻页布局、耐久与局部渲染刷新；160项测试通过，实机待验收。

- [BUG-20261004-WAND-BUILD-MOVE-UNBOUNDED](BUG-20261004-WAND-BUILD-MOVE-UNBOUNDED.md)：建筑三模式与无上限分批任务、移动副作用及计划事件迁移、Q5说明台；完整170项测试，独立世界验证，实机待验收。


- [BUG-20261004-MOVE-LEGACY-RESTORE](BUG-20261004-MOVE-LEGACY-RESTORE.md)：原区域变化检查拦截；测试版4按用户要求恢复原移动实现，仅移除数量上限。170项测试及代码对照通过，实机待验收。

- [BUG-20261004-MOVE-TRANSLATION-LITERAL](BUG-20261004-MOVE-TRANSLATION-LITERAL.md)：测试版5修复移动提示直接显示翻译键；修复前回归失败、修复后170项测试通过。

- [BUG-20261004-MOVE-FAILURE-DIAGNOSTIC](BUG-20261004-MOVE-FAILURE-DIAGNOSTIC.md)：测试版6增加移动失败及恢复日志；170项测试通过，失败根因待复现。

- [BUG-20261004-MOVE-REDSTONE-PLACEMENT](BUG-20261004-MOVE-REDSTONE-PLACEMENT.md)：测试版7修正搬移期间红石更新及能量差异误判；170项测试及7项独立世界回归通过，实机待验收。

- [BUG-20261004-REGION-REPLACEMENT](BUG-20261004-REGION-REPLACEMENT.md)：测试版8新增区域替换与数据/实体保护；173项测试及13项独立世界回归通过，实机待验收。

- [BUG-20261004-BUILD-MATERIAL-HINT](BUG-20261004-BUILD-MATERIAL-HINT.md)：测试版9修正旧材料选择提示；173项测试通过，代码未改，实机待重启验收。

- [BUG-20261004-STRUCTURE-PLACEMENT](BUG-20261004-STRUCTURE-PLACEMENT.md)：测试版10特殊结构成组、单箱与连接校正；修正水平相邻箱子误判；181项测试及21项独立世界回归通过，实机待验收。

- [BUG-20261004-TASK-CANCEL-NATURAL-DROPS](BUG-20261004-TASK-CANCEL-NATURAL-DROPS.md)：测试版11跨维度取消本人建造、余位容器精确掉落与自然支撑规则；183项测试及27项独立世界回归通过，实机待验收。

- [BUG-20261004-FORMAL-114-PORTING](BUG-20261004-FORMAL-114-PORTING.md)：正式1.1.4八版本接口适配、历史法杖纹理遗漏、旧版静默装配及独立世界实体观测验证；交付正在最终核验。

- [BUG-20261005-LIGHT-LOAD-CHUNK-REENTRY](BUG-20261005-LIGHT-LOAD-CHUNK-REENTRY.md)：26.1.2旧灯迁移初始化setChanged导致当前区块加载重入等待；活动游戏两次线程快照确认，1.1.5八版本修复及独立存档重载通过，原玩家世界待验收。

- [BUG-20261005-WAND-CLIENT-INTERACTIONS](BUG-20261005-WAND-CLIENT-INTERACTIONS.md)：重排器模型、法杖框错位、首次切换失效、双材料交互与箱子提醒；1.1.6八版本验证中。

- [BUG-20261005-1211-LEGACY-RECIPE-FORMAT](BUG-20261005-1211-LEGACY-RECIPE-FORMAT.md)：1.21.1部分灯旧配方原料格式不兼容，独立测试日志发现，另批待处理。

- [BUG-20261005-2612-RANGE-SUBMIT-PHASE](BUG-20261005-2612-RANGE-SUBMIT-PHASE.md)：26.1.2确认范围框提交晚于绘制，1.1.7仅修正该版两个渲染事件，195项测试通过，画面待玩家验收。

- [BUG-20261005-1218-LIGHT-CACHE-REENTRANT](BUG-20261005-1218-LIGHT-CACHE-REENTRANT.md)：1.21.8世界读取回调清空灯缓存导致空指针，真实服务器旧代码复现，1.1.8-test1四项新回归、197项测试及31项世界回归通过，玩家待验收。

- [BUG-20261005-RECIPE-SYNC-MISSING](BUG-20261005-RECIPE-SYNC-MISSING.md)：后三版缺31个寻路物品配方，1.1.8-test2补齐并安装；四版各58配方实际输入输出匹配通过。
- [BUG-20261005-BUILD-WEAR-RECIPE-TEST2](BUG-20261005-BUILD-WEAR-RECIPE-TEST2.md)：四版建筑法杖512耐久、100块耗1、经验修补、创造不扣、多格完整放置后损坏停任务；801项单元测试及156项世界回归通过，已安装并写入确认存档1点耐久杖。

- [BUG-20261005-SURVIVAL-SOLID-OCCUPIED](BUG-20261005-SURVIVAL-SOLID-OCCUPIED.md)：生存实心遇已有方块暂停；1.1.8-test3四版明确清单、容器掉落与回滚修复，801项单元及188项世界回归通过，已安装。

- [BUG-20261006-REGION-TORCH-MATERIAL](BUG-20261006-REGION-TORCH-MATERIAL.md)：火把作为区域替换材料时支撑保护导致间隔保留原木、警告遗漏；test4四版801项单元、196项世界回归通过并安装，已有保护方块仍跳过，1耐久杖已放入背包。

- [BUG-20261007-REPLACE-STALE-FILTER-WARNING-HUD](BUG-20261007-REPLACE-STALE-FILTER-WARNING-HUD.md)：只选火把替换钻石0块、旧筛选及材料/筛选识别、红字覆盖浅蓝提示；test6四版801项单元、216项世界回归通过并安装，新增完整请求回归，客户端提示位置待实测。

- [BUG-20261007-BUILD-SURVIVAL-AUX-TARGET](BUG-20261007-BUILD-SURVIVAL-AUX-TARGET.md)：生存白色辅助位置从已有方块偏移到相邻格；test7仅修正四版建筑目标函数，旧每版6失败、新共104源码回放通过，801项单元与216项世界回归通过并安装。

- [区域替换重复红字](BUG-20261007-DUPLICATE-WARNING.md)：test8四版验证安装完成，仅去聊天重复及改5秒。

- [区域受保护材料支撑](BUG-20261007-REGION-PROTECTED-MATERIAL-SUPPORT.md)：test9四版验证安装完成。

- [1.21.4逐版待办检查](BUG-20261007-1214-PENDING-CHECKLIST.md)：1.1.8-test10验证并安装。

- [1.21.1灯崩溃与逐版待办](BUG-20261007-1211-LIGHT-CRASH-PENDING.md)：1.1.8-test11验证并安装。

- [掉落物误触发实体保护](BUG-20261007-DROPPED-ITEM-REPLACE-PROTECTION.md)：六版test12通过。

- [1.21.1删除法杖图标](BUG-20261007-1211-DELETE-INVENTORY-MODEL.md)：test13，更新完成。

- [1.20双版兼容与累计待办](BUG-20261007-120X-CARPET-RECIPES-PENDING.md)：test14；退出探查结论见交付。

- [1.21.1法杖图标透明](BUG-20261008-1211-WAND-ALPHA.md)：test15，已安装。

- [悬浮说明及说明台虚化](BUG-20261008-TOOLTIP-SCREEN-BLUR.md)：八版test16，安装状态见记录。

- [公共物品页接口](BUG-20261012-ITEM-PAGE-API.md)：1.1.9-test1，新增80项排序/去重回归。

- [说明台公共API交付](FEATURE-20261022-HELP-PAGE-API.md)：test2，八版验证及安装状态见记录。

- [26.3基础适配](BUG-20261024-263-PORTING.md)：test3已验证安装，辅助魔杖仍待实现。

- [26.3结构字段与文件基础](BUG-20261025-263-BLUEPRINT-STATE-SCHEMA.md)：test4，仅26.3；移动导出已实现，辅助法杖尚未实现。

- [26.3辅助魔杖、投影与生成箱](BUG-20261025-AUX-WAND.md)：1.1.9-test5实现，250单元、59世界回归、60配方及独立整合客户端验证通过。

- [辅助比例、零初始行和截面朝下](BUG-20261026-AUX-ASPECT-ROTATION.md)：26.3测试7：比例、零初始行、向下旋转修复，272单元、59世界/60配方及独立实机验证通过。
# 2026-10-26追加

- [26.3七项操作问题与中心朝向旋转](BUG-20261026-AUX-SEVEN-ISSUES.md)：测试8已安装，278单元、59世界/60配方、8525实机检查及小尺寸外观复核通过，待用户验收。最终旋转为固定中心方块、按90度朝向玩家所在位置。


- [26.3信标塔形状](FEATURE-20261026-BEACON-TOWER.md)：测试9已安装，288单元、59世界/60配方及809实机检查通过；菜单紧接金字塔，旧编号不变，1至4层原版结构。

- [26.3信标塔随选区扩展](FEATURE-20261026-BEACON-SELECTION-SCALING.md)：测试10已安装，取消建筑4层上限，295单元、59世界/60配方及6115实机检查通过；原版有效等级仍最多4。

- 2026-10-31测试17：大蓝图并存时新建筑高亮及时出现；箱子首次信息完整；真实完成后箱子和高亮清除；生成箱优先选区近玩家外缘。逐版结果见BUG-20261031-BUILD-PREVIEW-LIFECYCLE-TEST17.md。

- 2026-11-04测试18：生成箱物品名与方块名八语言同步，十版真实说明台名称检查通过；详见BUG-20261104-GENERATION-BOX-ITEM-NAME-TEST18.md。

- [26.1.2 建筑范围与蓝图选点未绘制](BUG-20261108-2612-AUX-RANGE-TEST20.md)：测试20修正客户端提交阶段，逐项验证见记录。

- [旧版设置台正文被背景覆盖或模糊](BUG-20261114-LEGACY-SETTINGS-BACKGROUND-TEST21.md)，测试21按版本修正原生背景绘制顺序，逐版验证和安装状态见交付核验。

- [测试22：1.21.1 蓝图拖动与十版生成箱获取规则](BUG-20261114-DRAG-GENERATION-BOX-TEST22.md)，按用户要求修复移动预览并取消生成箱配方和普通获取入口，逐版结果见交付核验。

- [11实例同步与新增实例依赖差异](BUG-20261115-INSTANCE-DEPENDENCY.md)，安装目标以 docs/INSTALLATION_TARGETS.md 为准。

- [1.21.1 蓝图旋转／移动回退编辑及快速输入遗漏](BUG-20261115-AUX-TRANSFORM-INPUT-TEST23.md)：测试23修正当前行选择、原生按下事件及取消，347项单元、60项世界、59项配方和独立整合环境实际鼠标验证通过。

- [测试24：1.20.1／1.20.4蓝图旋转／移动](BUG-20261115-LEGACY-AUX-TRANSFORM-TEST24.md)：当前行选择与短按输入，681项单元、120项世界、118项配方及两版实际鼠标流程通过。

- [Windows保留名aux源码目录](BUG-20261116-WINDOWS-AUX-SOURCE-PATH.md)：正式源码物理目录适配，二进制包名保留，逐版重新构建及Git索引核验。
