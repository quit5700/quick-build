from pathlib import Path
v=Path('versions/1.1.4-test11/mc26.2');p=v/'src/main/java/cn/quit5700/buildingwand/server/BuildWandServer.java';s=p.read_text(encoding='utf-8').replace('p.getServer().getAllLevels()','((ServerLevel)p.level()).getServer().getAllLevels()');p.write_text(s,encoding='utf-8')
p=v/'src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java';s=p.read_text(encoding='utf-8').replace('Button cancel=Button.builder(Component.translatable("button.q5.cancel_all_builds"),', 'Button cancel=Button.builder(Component.literal(fitButtonText(Component.translatable("button.q5.cancel_all_builds").getString(),cancelArea.width()-8)),');s=s.replace('.bounds(cancelArea.x(),cancelArea.y(),cancelArea.width(),20).build();','.bounds(cancelArea.x(),cancelArea.y(),cancelArea.width(),20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("button.q5.cancel_all_builds"))).build();');p.write_text(s,encoding='utf-8')
p=v/'src/test/java/cn/quit5700/buildingwand/client/SettingsCancellationLayoutTest.java';p.write_text('''package cn.quit5700.buildingwand.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SettingsCancellationLayoutTest {
 @Test void cancellationButtonDoesNotOverlapCloseAtCommonScales(){for(var size:new int[][]{{854,480},{427,240},{320,180},{160,90}}){var l=SettingsScreenLayout.calculate(size[0],size[1]);var a=SettingsScreenLayout.cancelArea(l);var f=SettingsScreenLayout.footer(l);assertTrue(a.width()>0);assertTrue(a.x()>=l.panelLeft());assertTrue(a.x()+a.width()+8<=f.closeX());assertEquals(f.y(),a.y());}}
 @Test void cancellationButtonStaysBelowContentPager(){var l=SettingsScreenLayout.calculate(427,240);int start=l.panelTop()+76,bottom=l.panelTop()+l.panelHeight()-44;int budget=SettingsScreenLayout.descriptionBudget(start,bottom);assertTrue(SettingsScreenLayout.contentPagerY(start,budget)+20<=SettingsScreenLayout.cancelArea(l).y());}
}
''',encoding='utf-8')
p=Path('docs/bugs/BUG-20261004-TASK-CANCEL-NATURAL-DROPS.md');p.write_text('''# 建造任务取消、余位容器及自然掉落

当前项目根目录：C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组。
分类：任务保存/取消、容器数据和掉落、自然更新、26.2接口迁移及界面文字。版本1.1.4-test11/mc26.2，正在处理。
根因：旧取消依赖对应法杖身份，遗失后保留任务但不能启动新任务；旧多格余位拒绝覆盖方块实体，旧严格支撑检查与用户最新要求不符；部分原说明与多格材料、空心厚度不一致。
已尝试失败：ServerPlayer.getServer（服务端玩家获取服务器）在26.2不存在，编译失败；改为明确ServerLevel（服务端世界）获取getServer，不重复旧玩家接口。只读查找曾误假定SettingsNetworking与client/BuildingWandClient路径，实际设置使用WandNetworking，初始化类位于buildingwand包根；后续先rg定位。
方案：专用网络请求仅按已认证玩家身份取消跨维度建筑任务，并返回实际数量；释放区块、清理选区/闪烁，保留已完成部分，不取消其他玩家或删除任务。实心/空心余位允许覆盖容器，内容先按游戏容器规则掉落并清空，随后设空气，防止重复掉落；区域替换保护保留。自然放置不因支撑暂停，整组先装配，再用原版形状与破坏方法处理失效，正常扣物品。兼容具有标准上下两格/床式两格属性的模组方块。旧严格装配方法保留为内部回滚校验，玩家任务调用自然放置方法。
验证：待完整测试与独立世界回归，不宣称已修复。
''',encoding='utf-8')
print('修正26.2服务器访问接口；新增取消按钮布局回归')