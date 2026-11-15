# 说明台公共接口交付记录
当前项目根目录：C:\Users\quit5700\Documents\mc虚拟线路模组\快捷建筑模组
类型：公共接口、客户端分页与跨版本适配。
背景：说明台此前只有内部固定条目，其他模组无公共登记入口。本次新增Q5HelpPage与HelpPageRegistry、可选反射包装器；只改WandSettingsScreen.createPages末尾，原条目和渲染方法逐字节保持。
验证：优先级、加载顺序独立性、条目并列排序、重复更新、模组命名空间隔离、取消、嵌套列表复制和不可变、无效更新原子性、多页及空输入验证。八版完整单元测试及包装器生产API隔离检查通过；未实测客户端实际多模组画面。
防回归：不得将公共API绑定到客户端类或要求未安装快捷建筑也加载实现类；标题与段落都使用调用方翻译键；不能把服务端登记当作客户端同步。
2026-10-22：八版1.1.9-test2说明台公共API v1。保留原物品页API v1，新增说明注册、稳定排序、同条目覆盖更新与取消，图标与标题/多页段落翻译键。客户端说明台末尾追加外部条目，沿用自动分页、布局及已修复背景顺序；不提供外部执行按钮。公共API无客户端依赖，注册在公共初始化中可用；未安装或旧版时包装器返回false，服务器登记不自动同步客户端。当前界面关闭重开读取新登记，图标不存在或空气使用书本后备。法杖、配方、灯、模型和旧测试保持，新增每版10项回归，共1757单元测试通过，实际包装器及生产API三个隔离场景通过，仅Fabric Loader存在性使用替身。客户端实际多模组接入尚未实测。未制作万能宝箱、未增加26.3、不改存档、不转正式版、不上传GitHub。
{
  "26.2": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\26.2\\mods",
    "文件": "quick-build-1.1.9-test2-mc26.2-fabric.jar",
    "SHA256": "1f31679b53c1ab2b5014e35c56cc1604d179f86b5b97a98822cabe019eb72a81",
    "单元测试": 220,
    "本次新增回归": 10,
    "原源码改动": [
      "src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java"
    ],
    "新增源码": [
      "src/test/java/cn/quit5700/quickbuild/api/HelpPageRegistryTest.java",
      "src/main/java/cn/quit5700/quickbuild/api/HelpPageRegistry.java",
      "src/main/java/cn/quit5700/quickbuild/api/Q5HelpPage.java"
    ]
  },
  "26.1.2": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\26.1.2-Fabric\\mods",
    "文件": "quick-build-1.1.9-test2-mc26.1.2-fabric.jar",
    "SHA256": "88f55cf567fa7b3b31dc925c7223be6429c379f0d656632969212957dc30b92c",
    "单元测试": 219,
    "本次新增回归": 10,
    "原源码改动": [
      "src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java"
    ],
    "新增源码": [
      "src/test/java/cn/quit5700/quickbuild/api/HelpPageRegistryTest.java",
      "src/main/java/cn/quit5700/quickbuild/api/HelpPageRegistry.java",
      "src/main/java/cn/quit5700/quickbuild/api/Q5HelpPage.java"
    ]
  },
  "1.21.11": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.21.11-Fabric\\mods",
    "文件": "quick-build-1.1.9-test2-mc1.21.11-fabric.jar",
    "SHA256": "59375a2e2e9f8d9b94a51bdb957c32a480d61415001846a2874e5cd844bbade1",
    "单元测试": 221,
    "本次新增回归": 10,
    "原源码改动": [
      "src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java"
    ],
    "新增源码": [
      "src/test/java/cn/quit5700/quickbuild/api/HelpPageRegistryTest.java",
      "src/main/java/cn/quit5700/quickbuild/api/HelpPageRegistry.java",
      "src/main/java/cn/quit5700/quickbuild/api/Q5HelpPage.java"
    ]
  },
  "1.21.8": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.21.8-Fabric\\mods",
    "文件": "quick-build-1.1.9-test2-mc1.21.8-fabric.jar",
    "SHA256": "7934c5a88d49504215ac2c4f1154b4e4799b32e0344a0307f90b18c410205fcc",
    "单元测试": 221,
    "本次新增回归": 10,
    "原源码改动": [
      "src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java"
    ],
    "新增源码": [
      "src/test/java/cn/quit5700/quickbuild/api/HelpPageRegistryTest.java",
      "src/main/java/cn/quit5700/quickbuild/api/HelpPageRegistry.java",
      "src/main/java/cn/quit5700/quickbuild/api/Q5HelpPage.java"
    ]
  },
  "1.21.4": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.21.4-Fabric\\mods",
    "文件": "quick-build-1.1.9-test2-mc1.21.4-fabric.jar",
    "SHA256": "61ba57d96e15bde5ae30b3812670a1409019bd5c47b6ac547d469815c76e33cc",
    "单元测试": 219,
    "本次新增回归": 10,
    "原源码改动": [
      "src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java"
    ],
    "新增源码": [
      "src/test/java/cn/quit5700/quickbuild/api/HelpPageRegistryTest.java",
      "src/main/java/cn/quit5700/quickbuild/api/HelpPageRegistry.java",
      "src/main/java/cn/quit5700/quickbuild/api/Q5HelpPage.java"
    ]
  },
  "1.21.1": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.21.1-mtr\\mods",
    "文件": "quick-build-1.1.9-test2-mc1.21.1-fabric.jar",
    "SHA256": "af0a74bbfa92ed322564bd9aef6b1bdfeb06a1f2c51d8ad7f2140f66896523ea",
    "单元测试": 219,
    "本次新增回归": 10,
    "原源码改动": [
      "src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java"
    ],
    "新增源码": [
      "src/test/java/cn/quit5700/quickbuild/api/HelpPageRegistryTest.java",
      "src/main/java/cn/quit5700/quickbuild/api/HelpPageRegistry.java",
      "src/main/java/cn/quit5700/quickbuild/api/Q5HelpPage.java"
    ]
  },
  "1.20.4": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.20.4-Fabric-mtr\\mods",
    "文件": "quick-build-1.1.9-test2-mc1.20.4-fabric.jar",
    "SHA256": "ac988e0b5d5bac3d83e6185c6f1c836ae82d0b243197c02e2e3a54d969f23e78",
    "单元测试": 219,
    "本次新增回归": 10,
    "原源码改动": [
      "src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java"
    ],
    "新增源码": [
      "src/test/java/cn/quit5700/quickbuild/api/HelpPageRegistryTest.java",
      "src/main/java/cn/quit5700/quickbuild/api/HelpPageRegistry.java",
      "src/main/java/cn/quit5700/quickbuild/api/Q5HelpPage.java"
    ]
  },
  "1.20.1": {
    "状态": "已安装",
    "目录": "G:\\game\\mcfabric\\.minecraft\\versions\\1.20.1-Fabric-mtr\\mods",
    "文件": "quick-build-1.1.9-test2-mc1.20.1-fabric.jar",
    "SHA256": "c42d1497f99447da3eefb64f36562aaf343b491f701d61a191045d1a91ce4914",
    "单元测试": 219,
    "本次新增回归": 10,
    "原源码改动": [
      "src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java"
    ],
    "新增源码": [
      "src/test/java/cn/quit5700/quickbuild/api/HelpPageRegistryTest.java",
      "src/main/java/cn/quit5700/quickbuild/api/HelpPageRegistry.java",
      "src/main/java/cn/quit5700/quickbuild/api/Q5HelpPage.java"
    ]
  }
}