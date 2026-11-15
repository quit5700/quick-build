# 公共物品页接口与验证
当前项目根目录：C:\Users\quit5700\Documents\mc虚拟线路模组\快捷建筑模组
类型：物品页扩展、状态排序及版本适配。
需求：其他独立模组加入快捷建筑物品页，稳定顺序与去重。旧实现只有本模组displayItems固定列表，没有统一登记接口。新增通用独立注册表和八版适配门面，只改ModItemGroup汇总输出。
测试：排序优先级、加载顺序独立性、分组并列、组内顺序与去重、与原物品去重、同组更新、列表不可变、取消登记、错误更新原子性、空组及实例隔离。各版10项新增及原完整单元测试通过。可选接入示例Java17编译通过，接入包装器三个隔离场景通过，存在性及门面采用替身，不代表实际游戏整合运行。
已尝试失败：新增测试采用中文非法标识，触发项目全Java源码禁止中文规则，失败日志保留；改为同样非法的大写标识，不改生产行为、不弱化旧测试。1.21.4的注册表get返回Optional引用，不能套用1.21.1返回Item的接口，改用getValue；失败日志保留，准备脚本同步修正。不得以UI编译成功宣称真实客户端已验收。
2026-10-12：八版1.1.9-test1新增通用公共物品页接口API v1。原物品顺序保留在前，外部按排序值、模组标识、分组标识排列；组内保持顺序，完整物品标识去重，同组重复登记完整更新，可取消登记。使用字符串物品标识隔离各版本映射，可选反射包装器支持未安装和旧版时独立运行。原玩法源码、模型材质和旧测试逐字节保持，仅原ModItemGroup加入列表汇总；新增Q5ItemPage、ItemPageRegistry和10项回归。新接口不支持固定格子或刷新已打开界面；通过本接口之外第三方事件不承诺统一去重和排序。未制作万能宝箱，未升级正式版，未上传GitHub，未改存档。客户端实际多模组接入画面尚未实测。
{
  "26.2": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\26.2\\mods",
    "文件": "quick-build-1.1.9-test1-mc26.2-fabric.jar",
    "SHA256": "13b64639cbef884c21538766055aa2ec63484a9a43a899e4efeac4969b034a8c",
    "单元测试": 210,
    "新增接口测试": 10,
    "原源码变更": [
      "src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"
    ]
  },
  "26.1.2": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\26.1.2-Fabric\\mods",
    "文件": "quick-build-1.1.9-test1-mc26.1.2-fabric.jar",
    "SHA256": "e1efe661a4e4dd28e4e57fa8c228538d385a27d78b69d11553481e46bb06a515",
    "单元测试": 209,
    "新增接口测试": 10,
    "原源码变更": [
      "src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"
    ]
  },
  "1.21.11": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.21.11-Fabric\\mods",
    "文件": "quick-build-1.1.9-test1-mc1.21.11-fabric.jar",
    "SHA256": "4a1604332decc8f1fe0c37c53d044c9b04847be9b74de7bf626fd2d6d859c9e6",
    "单元测试": 211,
    "新增接口测试": 10,
    "原源码变更": [
      "src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"
    ]
  },
  "1.21.8": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.21.8-Fabric\\mods",
    "文件": "quick-build-1.1.9-test1-mc1.21.8-fabric.jar",
    "SHA256": "af42ca1465d70a3e928649caaa5682a161f21d8b4b08582dd901e4c38a0c451a",
    "单元测试": 211,
    "新增接口测试": 10,
    "原源码变更": [
      "src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"
    ]
  },
  "1.21.4": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.21.4-Fabric\\mods",
    "文件": "quick-build-1.1.9-test1-mc1.21.4-fabric.jar",
    "SHA256": "ba392f7fe6b9403508ab32a92e7d975779f8a0462bf50bf188d3d931a0afbed7",
    "单元测试": 209,
    "新增接口测试": 10,
    "原源码变更": [
      "src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"
    ]
  },
  "1.21.1": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.21.1-mtr\\mods",
    "文件": "quick-build-1.1.9-test1-mc1.21.1-fabric.jar",
    "SHA256": "0414d55f0c2ce412e78f646a2d6c1b9adfa63c217418b6946fa23ce53065e3d5",
    "单元测试": 209,
    "新增接口测试": 10,
    "原源码变更": [
      "src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"
    ]
  },
  "1.20.4": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.20.4-Fabric-mtr\\mods",
    "文件": "quick-build-1.1.9-test1-mc1.20.4-fabric.jar",
    "SHA256": "7347569790d9a4e141b3859d4076531d172587256fba10832669ce7d87641d14",
    "单元测试": 209,
    "新增接口测试": 10,
    "原源码变更": [
      "src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"
    ]
  },
  "1.20.1": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.20.1-Fabric-mtr\\mods",
    "文件": "quick-build-1.1.9-test1-mc1.20.1-fabric.jar",
    "SHA256": "4823dde217206c64922c0f0c04758d8bacaa8e1a49184a6942762cb2cd63659f",
    "单元测试": 209,
    "新增接口测试": 10,
    "原源码变更": [
      "src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"
    ]
  }
}