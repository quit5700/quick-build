# 两点区域满亮：实现与防回归记录

当前项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。

状态：26.2 的 1.1.3-test3 自动验证通过，实际游戏验收待完成；不标记旧局部白昼延迟已修复。

## 现象与原因

旧局部白昼需要连通搜索及分批刷新，大范围出现等待。本轮按用户批准新增不看遮挡的选区模式，不改变旧模式语义。新模式使用六个坐标边界比较，避免区域逐格连通搜索；实际区块网格刷新仍有成本。

接口适配：首次编译尝试 ServerPlayer.displayClientMessage 以及客户端 screen 字段失败，属于 26.2 官方映射接口不匹配，标记为“已尝试失败”。改为服务器 sendSystemMessage 发送翻译组件，以及已有项目使用的 GLFW 按键状态方式检查退出键。不要再次套用其他版本的这两个接口。

协议风险：模式按序号直接映射会令新增模式占用旧应用按钮 74。保留 74，显式分配模式 75 和选区 76，并新增不碰撞测试。

## 文件与修复方式

- logic/LightBox.java 及测试：规范化两点、包含边界、负坐标、512 格上限和溢出检查。
- VariableLightMode、VariableConstantLightSettings、VariableLightPresentation 及测试：追加模式，保持旧存档序号与复制灯旧菜单。
- AreaLightState、VariableConstantLightBlockEntity、ConstantLightTaskQueue、VariableLightTaskState：独立保存选区，更新活动区域，接入现有自然生成拦截。
- AreaSelectionNetworking、VariableLightNetworking：服务器验证端点和选点会话，同步区域与选择结果。
- AreaSelectionClient、ClientDaylightState、菜单与设置页：左键选点、取消、十二边框预览、直接坐标满亮及有界刷新队列。
- 八种语言资源、README、VERSION_INFO：玩家文字采用翻译键，补充操作与限制。

上述源码位于 mc26.2/src 下，不修改其他七版本或复制灯的既有行为。

## 验证与限制

先运行 LightBox 测试确认缺少实现时失败，再实现并通过。最终 gradlew.bat -Dfile.encoding=UTF-8 build --console=plain 成功，测试报告合计 101 项、0 失败、0 错误。构建在当前中文项目路径下完成。安装包与源码包已在 mc26.2/dist 生成。

未完成实机验收：框线和选点、取消后保持旧区、开关灯、重选、重进存档、维度切换、多人同步、自然刷怪、作物、渲染优化兼容及最大选区性能。不得把自动测试通过等同于这些运行行为已验证。

下次避免：不得通过提高光照传播预算解决无障碍选区模式；不得复用按钮 74；不得清空所有旧通光缓存来刷新一个区域；不得自动加载远处区块；不得声称零资源消耗。
