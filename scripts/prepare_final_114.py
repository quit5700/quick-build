from pathlib import Path
import shutil,re,json
r=Path.cwd();old=r/'versions/1.1.4';arch=r/'outputs/正式版准备/旧候选_1.1.4';arch.parent.mkdir(parents=True,exist_ok=True)
if old.exists() and not arch.exists():
 assert old.resolve().is_relative_to(r.resolve());shutil.move(str(old),str(arch))
p=r/'versions/1.1.4/mc26.2';shutil.copytree(r/'versions/1.1.4-test11/mc26.2',p,ignore=shutil.ignore_patterns('build','.gradle','run','dist'))
f=p/'gradle.properties';f.write_text(f.read_text(encoding='utf-8').replace('mod_version=1.1.4-test11','mod_version=1.1.4'),encoding='utf-8')
f=p/'src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java';s=f.read_text(encoding='utf-8');a=s.index('        SettingsScreenLayout.CancelArea cancelArea=');b=s.index('        addRenderableWidget(Button.builder(Component.translatable("text.quick_build.057")',a);cancel=s[a:b];s=s[:a]+s[b:];cancel=cancel.replace('cancelArea(layoutBounds())','cancelArea(layoutBounds(),height)');a=s.index('        next.active = contentPage + 1 < visibleDescriptionPages.size();');b=s.index('\n    }',a);s=s[:b]+'\n        if(page.item()==cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND && contentPage==0){\n'+cancel+'        }'+s[b:];f.write_text(s,encoding='utf-8')
f=p/'src/client/java/cn/quit5700/buildingwand/client/SettingsScreenLayout.java';s=f.read_text(encoding='utf-8');s=re.sub(r'    static CancelArea cancelArea\(Layout layout\)\{.*?\}', '    static CancelArea cancelArea(Layout layout,int textHeight){int x=layout.panelLeft+layout.listWidth+74;return new CancelArea(x,contentPagerY(layout.panelTop+76,textHeight),Math.max(1,Math.min(110,layout.contentWidth-86)));}',s);f.write_text(s,encoding='utf-8')
f=p/'src/test/java/cn/quit5700/buildingwand/client/SettingsCancellationLayoutTest.java';f.write_text('''package cn.quit5700.buildingwand.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SettingsCancellationLayoutTest {
 @Test void cancellationIsRightOfArrowsWithinContent(){for(var size:new int[][]{{854,480},{427,240},{320,180}}){var l=SettingsScreenLayout.calculate(size[0],size[1]);var a=SettingsScreenLayout.cancelArea(l,39);assertTrue(a.width()>0);assertTrue(a.x()>=l.panelLeft()+l.listWidth()+66+8);assertTrue(a.x()+a.width()<=l.panelLeft()+l.listWidth()+l.contentWidth()-12);assertEquals(SettingsScreenLayout.contentPagerY(l.panelTop()+76,39),a.y());}}
 @Test void cancellationFollowsTextHeight(){var l=SettingsScreenLayout.calculate(427,240);assertEquals(39,SettingsScreenLayout.cancelArea(l,78).y()-SettingsScreenLayout.cancelArea(l,39).y());}
}
''',encoding='utf-8')
print('已保留旧候选，建立正式26.2源码，按钮限建筑第一页，改为箭头右侧随文布局。')
