package cn.quit5700.buildingwand.client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SettingsCancellationLayoutTest {
 @Test void cancellationButtonDoesNotOverlapCloseAtCommonScales(){for(var size:new int[][]{{854,480},{427,240},{320,180},{160,90}}){var l=SettingsScreenLayout.calculate(size[0],size[1]);var a=SettingsScreenLayout.cancelArea(l);var f=SettingsScreenLayout.footer(l);assertTrue(a.width()>0);assertTrue(a.x()>=l.panelLeft());assertTrue(a.x()+a.width()+8<=f.closeX());assertEquals(f.y(),a.y());}}
 @Test void cancellationButtonStaysBelowContentPager(){var l=SettingsScreenLayout.calculate(427,240);int start=l.panelTop()+76,bottom=l.panelTop()+l.panelHeight()-44;int budget=SettingsScreenLayout.descriptionBudget(start,bottom);assertTrue(SettingsScreenLayout.contentPagerY(start,budget)+20<=SettingsScreenLayout.cancelArea(l).y());}
}
