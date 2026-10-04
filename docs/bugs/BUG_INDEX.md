# 当前项目问题索引

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
