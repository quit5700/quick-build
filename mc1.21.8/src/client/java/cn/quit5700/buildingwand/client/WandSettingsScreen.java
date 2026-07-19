package cn.quit5700.buildingwand.client;

import cn.quit5700.buildingwand.logic.WandLimitMath;
import cn.quit5700.buildingwand.logic.SettingsSaveFlow;
import cn.quit5700.buildingwand.network.WandNetworking;
import cn.quit5700.light.registry.LightItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class WandSettingsScreen extends Screen {
    private static WandSettingsScreen activeScreen;
    private final boolean editable;
    private final List<ItemPage> pages;
    private int planeLimit;
    private int cubeLimit;
    private int moveLimit;
    private int selectedIndex;
    private int listPage;
    private int contentPage;
    private final SettingsSaveFlow saveFlow = new SettingsSaveFlow();
    private Component status = Component.empty();
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int listWidth;
    private int contentWidth;
    private int contentHeight;
    private int itemsPerPage;

    public WandSettingsScreen(int planeLimit, int cubeLimit, int moveLimit, boolean editable) {
        super(Component.translatable("text.quick_build.080"));
        this.planeLimit = planeLimit;
        this.cubeLimit = cubeLimit;
        this.moveLimit = moveLimit;
        this.editable = editable;
        this.pages = createPages();
        activeScreen = this;
    }

    @Override
    protected void init() {
        SettingsScreenLayout.Layout layout = SettingsScreenLayout.calculate(width, height);
        panelWidth = layout.panelWidth();
        panelHeight = layout.panelHeight();
        panelLeft = layout.panelLeft();
        panelTop = layout.panelTop();
        listWidth = layout.listWidth();
        contentWidth = layout.contentWidth();
        contentHeight = layout.contentHeight();
        itemsPerPage = layout.itemsPerPage();

        int pageCount = pageCount();
        listPage = Math.min(listPage, pageCount - 1);
        int first = listPage * itemsPerPage;
        int last = Math.min(first + itemsPerPage, pages.size());
        int y = panelTop + 38;
        for (int index = first; index < last; index++) {
            int pageIndex = index;
            ItemPage page = pages.get(index);
            String marker = index == selectedIndex ? "> " : "";
            String label = fitButtonText(marker + page.title(), listWidth - 28);
            addRenderableWidget(Button.builder(Component.literal(label), button -> {
                        selectedIndex = pageIndex;
                        contentPage = 0;
                        status = Component.empty();
                        rebuildWidgets();
                    })
                    .bounds(panelLeft + 8, y, listWidth - 16, 20)
                    .build());
            y += 24;
        }

        Button previous = Button.builder(Component.literal("<"), button -> {
                    listPage--;
                    rebuildWidgets();
                }).bounds(panelLeft + 8, panelTop + panelHeight - 52, 28, 20).build();
        previous.active = listPage > 0;
        addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), button -> {
                    listPage++;
                    rebuildWidgets();
                }).bounds(panelLeft + listWidth - 36, panelTop + panelHeight - 52, 28, 20).build();
        next.active = listPage + 1 < pageCount;
        addRenderableWidget(next);

        ItemPage selected = pages.get(selectedIndex);
        contentPage = Math.min(contentPage, selected.descriptionPages().size() - 1);
        addContentPageButtons(selected);
        if (selected.limits() != LimitProfile.NONE) addLimitButtons(selected.limits());

        SettingsScreenLayout.Footer footer = SettingsScreenLayout.footer(layoutBounds());
        addRenderableWidget(Button.builder(Component.translatable("text.quick_build.057"), button -> onClose())
                .bounds(footer.closeX(), footer.y(), footer.closeWidth(), 20).build());
    }

    private void addLimitButtons(LimitProfile profile) {
        SettingsScreenLayout.LimitArea area = SettingsScreenLayout.limitArea(layoutBounds());
        if (profile == LimitProfile.BUILDING) {
            addLimitRow(LimitType.PLANE, area.firstRowY(), area);
            addLimitRow(LimitType.CUBE, area.secondRowY(), area);
        } else {
            addLimitRow(LimitType.MOVE, area.secondRowY(), area);
        }
        SettingsScreenLayout.Footer footer = SettingsScreenLayout.footer(layoutBounds());
        String saveLabel = saveFlow.pending() ? "text.quick_build.185" : "text.quick_build.032";
        Button save = Button.builder(Component.translatable(saveLabel), button -> saveSettings())
                .bounds(footer.saveX(), footer.y(), footer.saveWidth(), 20).build();
        save.active = editable && !saveFlow.pending();
        addRenderableWidget(save);
    }

    private void addLimitRow(LimitType type, int y, SettingsScreenLayout.LimitArea area) {
        Button minus = Button.builder(Component.literal("-"), button -> changeLimit(type, false))
                .bounds(area.minusX(), y, area.buttonWidth(), 20).build();
        Button plus = Button.builder(Component.literal("+"), button -> changeLimit(type, true))
                .bounds(area.plusX(), y, area.buttonWidth(), 20).build();
        minus.active = editable;
        plus.active = editable;
        addRenderableWidget(minus);
        addRenderableWidget(plus);
    }

    private void addContentPageButtons(ItemPage page) {
        if (page.descriptionPages().size() <= 1) return;
        int y = SettingsScreenLayout.limitArea(layoutBounds()).headingY() - 28;
        Button previous = Button.builder(Component.literal("<"), button -> {
            contentPage--;
            rebuildWidgets();
        }).bounds(contentLeft() + 12, y, 24, 20).build();
        previous.active = contentPage > 0;
        addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), button -> {
            contentPage++;
            rebuildWidgets();
        }).bounds(contentLeft() + 42, y, 24, 20).build();
        next.active = contentPage + 1 < page.descriptionPages().size();
        addRenderableWidget(next);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickProgress) {
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xF020232A);
        graphics.renderOutline(panelLeft, panelTop, panelWidth, panelHeight, 0xFF66CCFF);
        graphics.drawCenteredString(font, title, width / 2, panelTop + 10, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("text.quick_build.145"), panelLeft + listWidth / 2, panelTop + 24, 0xFFD8F4FF);

        int contentLeft = contentLeft();
        int contentTop = panelTop + 34;
        graphics.fill(contentLeft, contentTop, contentLeft + contentWidth, panelTop + panelHeight - 36, 0x8A101216);
        graphics.renderOutline(contentLeft, contentTop, contentWidth, contentHeight, 0xFF4D5966);

        ItemPage page = pages.get(selectedIndex);
        int x = contentLeft + 12;
        int y = contentTop + 12;
        graphics.renderItem(new ItemStack(page.item()), x, y);
        String contentPageLabel = page.descriptionPages().size() > 1
                ? "  " + (contentPage + 1) + "/" + page.descriptionPages().size() : "";
        graphics.drawString(font, Component.literal(page.title() + contentPageLabel), x + 24, y + 5, 0xFFFFFFFF);
        y += 30;
        int descriptionBottom = page.limits() == LimitProfile.NONE
                ? panelTop + panelHeight - 68
                : SettingsScreenLayout.limitArea(layoutBounds()).headingY() - 32;
        boolean descriptionFull = false;
        for (String line : page.descriptionPages().get(contentPage)) {
            for (var wrapped : font.split(Component.translatable(line), contentWidth - 24)) {
                if (y > descriptionBottom) {
                    descriptionFull = true;
                    break;
                }
                graphics.drawString(font, wrapped, x, y, 0xFFE6E6E6);
                y += 13;
            }
            if (descriptionFull) break;
            y += 3;
        }

        if (page.limits() != LimitProfile.NONE) {
            SettingsScreenLayout.LimitArea area = SettingsScreenLayout.limitArea(layoutBounds());
            String heading = page.limits() == LimitProfile.BUILDING
                    ? "text.quick_build.071"
                    : "text.quick_build.159";
            graphics.drawString(font, Component.translatable(heading), x, area.headingY(), 0xFFFFDD66);
            if (page.limits() == LimitProfile.BUILDING) {
                graphics.drawString(font, Component.translatable("text.quick_build.104", planeLimit), x, area.firstRowY() + 5, 0xFFD8F4FF);
                graphics.drawString(font, Component.translatable("text.quick_build.072", cubeLimit), x, area.secondRowY() + 5, 0xFFD8F4FF);
            } else {
                graphics.drawString(font, Component.translatable("text.quick_build.166", moveLimit), x, area.secondRowY() + 5, 0xFFD8F4FF);
            }
            if (!editable) graphics.drawString(font, Component.translatable("text.quick_build.074"), x, panelTop + panelHeight - 22, 0xFFFF7777);
        }
        if (!status.getString().isEmpty()) {
            graphics.drawCenteredString(font, status, contentLeft + contentWidth / 2, panelTop + 22, 0xFFFFDD66);
        }

        int pageCount = pageCount();
        graphics.drawCenteredString(font, Component.literal((listPage + 1) + " / " + pageCount),
                panelLeft + listWidth / 2, panelTop + panelHeight - 46, 0xFFC8C8C8);
        super.render(graphics, mouseX, mouseY, tickProgress);
    }

    private int contentLeft() { return panelLeft + listWidth; }

    private SettingsScreenLayout.Layout layoutBounds() {
        return new SettingsScreenLayout.Layout(panelLeft, panelTop, panelWidth, panelHeight,
                listWidth, contentWidth, contentHeight, itemsPerPage);
    }

    private int pageCount() {
        return Math.max(1, (pages.size() + itemsPerPage - 1) / itemsPerPage);
    }

    private String fitButtonText(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String suffix = "...";
        return font.plainSubstrByWidth(text, Math.max(1, maxWidth - font.width(suffix))) + suffix;
    }

    private void changeLimit(LimitType type, boolean increase) {
        switch (type) {
            case PLANE -> planeLimit = WandLimitMath.change(planeLimit, increase, 16);
            case CUBE -> cubeLimit = WandLimitMath.change(cubeLimit, increase, 64);
            case MOVE -> moveLimit = WandLimitMath.change(moveLimit, increase, 64);
        }
        rebuildWidgets();
    }

    private void saveSettings() {
        switch (saveFlow.request()) {
            case SEND -> {
                status = Component.translatable("text.quick_build.186");
                ClientPlayNetworking.send(new WandNetworking.UpdateSettingsPayload(
                        planeLimit, cubeLimit, moveLimit));
            }
            case NONE -> {
                return;
            }
        }
        rebuildWidgets();
    }

    public void onSettingsSaved(boolean accepted, int planeLimit, int cubeLimit, int moveLimit) {
        this.planeLimit = planeLimit;
        this.cubeLimit = cubeLimit;
        this.moveLimit = moveLimit;
        saveFlow.complete();
        status = Component.translatable(accepted ? "text.quick_build.116" : "text.quick_build.115");
        rebuildWidgets();
    }

    public static void applySavedSettings(
            boolean accepted, int planeLimit, int cubeLimit, int moveLimit) {
        if (activeScreen != null) {
            activeScreen.onSettingsSaved(accepted, planeLimit, cubeLimit, moveLimit);
        }
    }

    @Override
    public void onClose() {
        if (activeScreen == this) {
            activeScreen = null;
        }
        super.onClose();
    }

    private static List<ItemPage> createPages() {
        List<ItemPage> result = new ArrayList<>();
        addPaged(result, cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND, LimitProfile.BUILDING, true, List.of(
                List.of(
                        "text.quick_build.017",
                        "text.quick_build.127",
                        "text.quick_build.193",
                        "text.quick_build.192",
                        "text.quick_build.090"
                ),
                List.of(
                        "text.quick_build.028",
                        "text.quick_build.027",
                        "text.quick_build.021",
                        "text.quick_build.020",
                        "text.quick_build.156"
                ),
                List.of(
                        "text.quick_build.077",
                        "text.quick_build.128",
                        "text.quick_build.048",
                        "text.quick_build.018",
                        "text.quick_build.023",
                        "text.quick_build.022",
                        "text.quick_build.024"
                ),
                List.of(
                        "text.quick_build.031",
                        "text.quick_build.040",
                        "text.quick_build.120",
                        "text.quick_build.121",
                        "text.quick_build.118",
                        "text.quick_build.146"
                )
        ));
        addPaged(result, cn.quit5700.buildingwand.registry.ModItems.MOVE_WAND, LimitProfile.MOVE, true, List.of(
                List.of(
                        "text.quick_build.045",
                        "text.quick_build.043",
                        "text.quick_build.070",
                        "text.quick_build.025",
                        "text.quick_build.160"
                ),
                List.of(
                        "text.quick_build.019",
                        "text.quick_build.049",
                        "text.quick_build.016",
                        "text.quick_build.134",
                        "text.quick_build.158",
                        "text.quick_build.107"
                )
        ));
        add(result, LightItems.REDSTONE_SIGNAL_DELAY_ITEM, LimitProfile.NONE, false,
                "text.quick_build.178",
                "text.quick_build.096",
                "text.quick_build.144",
                "text.quick_build.065",
                "text.quick_build.188",
                "text.quick_build.112");

        add(result, LightItems.REDSTONE_ENERGY_CONNECTOR_ITEM, LimitProfile.NONE, false,
                "text.quick_build.026",
                "text.quick_build.047",
                "text.quick_build.150",
                "text.quick_build.089",
                "text.quick_build.044",
                "text.quick_build.082",
                "text.quick_build.105");

        for (Item item : cn.quit5700.pathfindingbeacon.registry.ModItems.ROUTE_BLOCK_ITEMS) {
            add(result, item, LimitProfile.NONE, false,
                    "text.quick_build.182",
                    "text.quick_build.131",
                    "text.quick_build.119",
                    "text.quick_build.011");
        }
        add(result, cn.quit5700.pathfindingbeacon.registry.ModItems.SEQUENCE_REORDERER, LimitProfile.NONE, false,
                "text.quick_build.157", "text.quick_build.129");

        add(result, LightItems.CONSTANT_LIGHT_BLOCK_ITEM, LimitProfile.NONE, false,
                "text.quick_build.180", "text.quick_build.123");
        add(result, LightItems.REDSTONE_ENERGY_EMITTER_ITEM, LimitProfile.NONE, false,
                "text.quick_build.130", "text.quick_build.138", "text.quick_build.079");
        for (Item item : LightItems.REMOTE_SWITCH_ITEMS.values()) {
            add(result, item, LimitProfile.NONE, false, "text.quick_build.181", "text.quick_build.132");
        }
        add(result, LightItems.REDSTONE_ENERGY_SENSOR_ITEM, LimitProfile.NONE, false,
                "text.quick_build.034", "text.quick_build.092",
                "text.quick_build.078", "text.quick_build.122",
                "text.quick_build.135", "text.quick_build.054");
        add(result, cn.quit5700.buildingwand.registry.ModItems.WAND_SETTINGS_STATION, LimitProfile.NONE, false,
                "text.quick_build.179",
                "text.quick_build.038",
                "text.quick_build.191");
        return List.copyOf(result);
    }

    private static void add(
            List<ItemPage> pages, Item item, LimitProfile profile, boolean hasSettings, String... lines) {
        String title = new ItemStack(item).getHoverName().getString();
        pages.add(new ItemPage(item, hasSettings ? title + Component.translatable("text.quick_build.006").getString() : title,
                List.of(List.of(lines)), profile));
    }

    private static void addPaged(
            List<ItemPage> pages, Item item, LimitProfile profile, boolean hasSettings,
            List<List<String>> descriptionPages) {
        String title = new ItemStack(item).getHoverName().getString();
        pages.add(new ItemPage(item, hasSettings ? title + Component.translatable("text.quick_build.006").getString() : title,
                List.copyOf(descriptionPages), profile));
    }

    private enum LimitProfile { NONE, BUILDING, MOVE }
    private enum LimitType { PLANE, CUBE, MOVE }
    private record ItemPage(Item item, String title, List<List<String>> descriptionPages, LimitProfile limits) {}
}
