# 当前项目问题索引

- [BUG-20260916-AREA-SWITCH-LATENCY](BUG-20260916-AREA-SWITCH-LATENCY.md)：同版本局部白昼与两点假照明响应差异；9月18日核对录像、日志及存档，录像主灯最终仍为局部白昼，需先确认两点模式实际生效；根因未确认、未修复。

- [BUG-20260914-LIGHT-TEXT-ALPHA](BUG-20260914-LIGHT-TEXT-ALPHA.md)：test6 警告文字不可见，六位颜色遗漏不透明度；test7 颜色回归先失败后通过，实机待验收。

- [BUG-20260913-LIGHT-INIT-REENTRY](BUG-20260913-LIGHT-INIT-REENTRY.md)：test3 供电灯初始化递归崩溃；test4 最小修复、103 项测试通过，实机重进存档待验收。

- [BUG-20260913-AREA-LIGHT](BUG-20260913-AREA-LIGHT.md)：test3 两点区域满亮、按钮协议与 26.2 接口适配；101 项测试通过，实机待验收。

- [BUG-20260913-CONNECTED-LIGHT](BUG-20260913-CONNECTED-LIGHT.md)：test1 实机漏光和黑影反馈；通光算法重做、混合并入原版、副本补齐；97 项测试通过，实机待验收。

- [BUG-20260912-局部白昼遮挡与生成规则](BUG-20260912-DAYLIGHT.md)：26.2 本轮功能实现、接口兼容与启动验证记录；自动测试通过，实机验收待完成。

- [BUG-20260919-ADJUSTABLE-LIGHT-MIGRATION](BUG-20260919-ADJUSTABLE-LIGHT-MIGRATION.md)：可调恒亮灯两模式、复制灯兼容隐藏和16色图标；测试版9完整125项测试通过，实机待验收。

- [BUG-20260919-RELEASE-PORTING](BUG-20260919-RELEASE-PORTING.md)：1.1.3 八版本适配、历史光源恢复与加载配置回归，详见正式版发布核验。
