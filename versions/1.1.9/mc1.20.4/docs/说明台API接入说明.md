# Q5说明台公共接口 v1

接口自快捷建筑1.1.9-test2起提供，八版共用同一份接入代码。原1.1.9-test1物品页接口保留。

复制examples/QuickBuildHelpCompat.java到自己的模组并调整package（包名）。在本模组初始化入口、注册物品后调用：

```java
QuickBuildHelpCompat.register(
    "your_mod", 100, "terminal",
    "your_mod:terminal", "help.your_mod.terminal.title",
    java.util.List.of(
        java.util.List.of("help.your_mod.terminal.line1", "help.your_mod.terminal.line2"),
        java.util.List.of("help.your_mod.terminal.line3")
    )
);
```

your_mod为调用方自己的模组标识；100是排序值，越小越靠前；terminal为本条说明的唯一标识；your_mod:terminal是展示图标的完整物品标识；title为标题翻译键，外层列表是手动分段页面，内层列表是该页的说明段落翻译键。每页及页面列表都必须非空。传入列表会深复制，之后修改调用方列表不影响登记结果。标题和说明均从调用方语言文件读取，不是直接填写中文。

语言文件例如 assets/your_mod/lang/zh_cn.json（简体中文）：
```json
{
  "help.your_mod.terminal.title": "仓储终端",
  "help.your_mod.terminal.line1": "打开终端查看已连接容器中的物品。",
  "help.your_mod.terminal.line2": "使用连接工具绑定容器。",
  "help.your_mod.terminal.line3": "此处填写自己的真实功能说明。"
}
```

上面只演示文本格式，不代表万能宝箱功能已制作。按项目规则提供其余语言文件；缺少翻译时游戏可能显示翻译键。

## 排列与更新

原快捷建筑说明始终在前，外部说明随后按排序值、模组标识、条目标识稳定排列。同一模组推荐使用同一排序值，保持它的说明连续分组；本次不新增模组标题栏或改变已定稿布局。

“模组标识＋条目标识”是唯一键，重复register（登记）更新整条说明；不同模组同名条目互不覆盖。同一物品可以有不同说明标识，不按图标物品合并。

QuickBuildHelpCompat.unregister("your_mod", "terminal")（取消登记）返回是否移除。更改登记后关闭并重新打开说明台生效，不承诺实时刷新已打开界面。既有换行和自动分页会进一步拆分过长内容，沿用现有左右翻页按钮。只提供文字和物品图标，没有外部执行按钮，也不赋予建造取消权限。

图标未注册或为空气时使用书本作为后备图标，保留说明。图标标识语法错误、空标题、空页或空段落键会拒绝登记，原有条目保持不变。登记数据只保存在当前进程内存，重启后由模组初始化重新登记，不写入玩家存档。

## 可选依赖与安全加载

包装器只引用Fabric Loader（模组加载器）和Java（爪哇）标准类型；未安装快捷建筑或旧版没有说明接口时返回false，调用方继续独立运行。包装器不要作为入口类直接填进fabric.mod.json（模组元数据），应从自己的初始化类调用。不要把快捷建筑实现类打进自己的模组。可选接入在suggests（建议依赖）声明quick_build，不声明必需依赖。

直接依赖接入可调用cn.quit5700.quickbuild.api.Q5HelpPage.register，参数相同；开发环境使用对应游戏版本的快捷建筑包作为modCompileOnly（仅编译模组依赖），fabric.mod.json的depends（必需依赖）声明quick_build >=1.1.9-test2。对外接口参数不引用Minecraft（我的世界）类，因此八版代码一致。

公共入口可以在模组公共初始化阶段登记，不加载客户端类。实际说明展示由客户端完成，需客户端也安装登记说明的模组并提供语言文件。专用服务器单独登记不会把说明传输到客户端。该接口不做库存、按钮、配方或服务器数据同步。

目前测试包含注册表状态及排序、不可变快照、取消和覆盖等回归；游戏内多个实际模组接入的布局仍需实测。运行包安装、构建结果与边界见本测试版安装核验.json。