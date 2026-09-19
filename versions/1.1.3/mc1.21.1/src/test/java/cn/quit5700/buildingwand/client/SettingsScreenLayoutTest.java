package cn.quit5700.buildingwand.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsScreenLayoutTest {
    @Test
    void keepsEveryRenderedAreaPositiveAtCommonGuiSizes() {
        int[][] sizes = {{854, 480}, {427, 240}, {320, 180}, {160, 90}};
        for (int[] size : sizes) {
            SettingsScreenLayout.Layout layout = SettingsScreenLayout.calculate(size[0], size[1]);
            assertTrue(layout.panelWidth() > 0);
            assertTrue(layout.panelHeight() > 0);
            assertTrue(layout.listWidth() > 0);
            assertTrue(layout.contentWidth() > 0);
            assertTrue(layout.contentHeight() > 0);
            assertTrue(layout.itemsPerPage() > 0);
        }
    }

    @Test
    void reducesRowsWhenGuiHeightIsSmall() {
        assertTrue(SettingsScreenLayout.calculate(427, 240).itemsPerPage()
                < SettingsScreenLayout.calculate(854, 480).itemsPerPage());
    }

    @Test
    void saveAndCloseButtonsDoNotOverlap() {
        SettingsScreenLayout.Footer footer = SettingsScreenLayout.footer(
                SettingsScreenLayout.calculate(854, 480));
        assertTrue(footer.saveX() + footer.saveWidth() < footer.closeX());
    }

    @Test
    void limitButtonsStayInsideTheContentFrame() {
        SettingsScreenLayout.Layout layout = SettingsScreenLayout.calculate(854, 480);
        SettingsScreenLayout.LimitArea area = SettingsScreenLayout.limitArea(layout);
        int contentLeft = layout.panelLeft() + layout.listWidth();
        int contentRight = contentLeft + layout.contentWidth();

        assertTrue(area.minusX() >= contentLeft + 8);
        assertTrue(area.plusX() + area.buttonWidth() <= contentRight - 8);
        assertTrue(area.firstRowY() < area.secondRowY());
        assertTrue(area.secondRowY() + 20 < SettingsScreenLayout.footer(layout).y());
    }
}
