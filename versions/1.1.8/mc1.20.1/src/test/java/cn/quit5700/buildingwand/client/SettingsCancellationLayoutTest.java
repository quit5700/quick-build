package cn.quit5700.buildingwand.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SettingsCancellationLayoutTest {
 @Test void cancellationIsRightOfArrowsWithinContent(){for(var size:new int[][]{{854,480},{427,240},{320,180}}){var l=SettingsScreenLayout.calculate(size[0],size[1]);var a=SettingsScreenLayout.cancelArea(l,39);assertTrue(a.width()>0);assertTrue(a.x()>=l.panelLeft()+l.listWidth()+66+8);assertTrue(a.x()+a.width()<=l.panelLeft()+l.listWidth()+l.contentWidth()-12);assertEquals(SettingsScreenLayout.contentPagerY(l.panelTop()+76,39),a.y());}}
 @Test void cancellationFollowsTextHeight(){var l=SettingsScreenLayout.calculate(427,240);assertEquals(39,SettingsScreenLayout.cancelArea(l,78).y()-SettingsScreenLayout.cancelArea(l,39).y());}
}
