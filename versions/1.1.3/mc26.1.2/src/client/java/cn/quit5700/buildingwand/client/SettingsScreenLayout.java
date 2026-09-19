package cn.quit5700.buildingwand.client;

final class SettingsScreenLayout {
    private static final int MAX_PANEL_WIDTH = 660;
    private static final int MAX_PANEL_HEIGHT = 350;
    private static final int MAX_ITEMS_PER_PAGE = 10;

    private SettingsScreenLayout() {}

    static Layout calculate(int screenWidth, int screenHeight) {
        int panelWidth = Math.max(1, Math.min(MAX_PANEL_WIDTH, screenWidth - 16));
        int panelHeight = Math.max(1, Math.min(MAX_PANEL_HEIGHT, screenHeight - 16));
        int panelLeft = Math.max(0, (screenWidth - panelWidth) / 2);
        int panelTop = Math.max(0, (screenHeight - panelHeight) / 2);
        int listWidth = Math.min(168, Math.max(72, panelWidth * 2 / 5));
        listWidth = Math.min(listWidth, Math.max(1, panelWidth - 40));
        int contentWidth = Math.max(1, panelWidth - listWidth - 16);
        int contentHeight = Math.max(1, panelHeight - 70);
        int itemsPerPage = Math.max(1, Math.min(MAX_ITEMS_PER_PAGE, (panelHeight - 112) / 24));
        return new Layout(panelLeft, panelTop, panelWidth, panelHeight, listWidth,
                contentWidth, contentHeight, itemsPerPage);
    }

    static Footer footer(Layout layout) {
        int y = layout.panelTop + layout.panelHeight - 28;
        int closeWidth = 90;
        int saveWidth = 110;
        int closeX = layout.panelLeft + layout.panelWidth - 98;
        int saveX = closeX - saveWidth - 8;
        return new Footer(saveX, saveWidth, closeX, closeWidth, y);
    }

    static LimitArea limitArea(Layout layout) {
        int contentLeft = layout.panelLeft + layout.listWidth;
        int contentRight = contentLeft + layout.contentWidth;
        int buttonWidth = 24;
        int plusX = contentRight - 8 - buttonWidth;
        int minusX = plusX - 30;
        int secondRowY = layout.panelTop + layout.panelHeight - 58;
        return new LimitArea(
                layout.panelTop + layout.panelHeight - 108,
                secondRowY - 26,
                secondRowY,
                minusX,
                plusX,
                buttonWidth
        );
    }

    record Layout(int panelLeft, int panelTop, int panelWidth, int panelHeight, int listWidth,
                  int contentWidth, int contentHeight, int itemsPerPage) {
    }

    record Footer(int saveX, int saveWidth, int closeX, int closeWidth, int y) {
    }

    record LimitArea(int headingY, int firstRowY, int secondRowY,
                     int minusX, int plusX, int buttonWidth) {
    }
}
