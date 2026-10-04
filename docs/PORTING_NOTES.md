# 当前项目版本适配参考

## 26.2测试版11：服务端获取服务器

ServerPlayer.getServer（服务端玩家获取服务器）已尝试失败：26.2无此方法。成功方式：先取得当前ServerLevel（服务端世界），再调用世界getServer（获取服务器），如((ServerLevel)player.level()).getServer()。跨维度迭代使用此服务器的getAllLevels（所有世界）。保持Java（爪哇）25与Mojang官方映射，完整版本见versions/1.1.4-test11/mc26.2/VERSION_INFO.md；本项目之前适配记录保存在各版本docs/PORTING_NOTES.md。
