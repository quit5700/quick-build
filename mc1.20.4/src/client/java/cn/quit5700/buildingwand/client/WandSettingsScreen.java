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
        super(Component.literal("快捷建造设置台"));
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
        addRenderableWidget(Button.builder(Component.literal("关闭"), button -> onClose())
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
        String saveLabel = saveFlow.pending() ? "正在保存" : "保存设置";
        Button save = Button.builder(Component.literal(saveLabel), button -> saveSettings())
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
        graphics.drawCenteredString(font, Component.literal("物品"), panelLeft + listWidth / 2, panelTop + 24, 0xFFD8F4FF);

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
            for (var wrapped : font.split(Component.literal(line), contentWidth - 24)) {
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
                    ? "建筑方块上限设置（数字太大，容易造成卡顿）："
                    : "移动方块上限设置（数字太大，容易造成卡顿）：";
            graphics.drawString(font, Component.literal(heading), x, area.headingY(), 0xFFFFDD66);
            if (page.limits() == LimitProfile.BUILDING) {
                graphics.drawString(font, Component.literal("平面上限：" + planeLimit), x, area.firstRowY() + 5, 0xFFD8F4FF);
                graphics.drawString(font, Component.literal("建筑体上限：" + cubeLimit), x, area.secondRowY() + 5, 0xFFD8F4FF);
            } else {
                graphics.drawString(font, Component.literal("移动选区上限：" + moveLimit), x, area.secondRowY() + 5, 0xFFD8F4FF);
            }
            if (!editable) graphics.drawString(font, Component.literal("仅单人玩家或服务器 OP 可修改"), x, panelTop + panelHeight - 22, 0xFFFF7777);
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
                status = Component.literal("正在等待服务器保存。 ");
                ClientPlayNetworking.send(WandNetworking.UPDATE_SETTINGS, WandNetworking.buffer(
                        new WandNetworking.UpdateSettingsPayload(planeLimit, cubeLimit, moveLimit)));
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
        status = Component.literal(accepted ? "设置已保存。" : "设置保存失败，已恢复服务器数值。");
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
                        "Alt+左键：记录准星所指方块作为建筑方块。",
                        "鼠标悬停建筑魔杖时显示当前已选方块。",
                        "左键短按：立即在虚拟方块位置放置一个已选方块。",
                        "左键按住并拖动：按下位置为第一点，松开位置为第二点。",
                        "两点形成一维范围时绘制线，二维范围时绘制平面，三维范围时绘制实心体。"
                ),
                List.of(
                        "Shift+左键拖动：固定绘制平面，可生成水平面或竖直面。",
                        "Shift+左键短按两次：依次记录两个点，按坐标生成实心的线、平面或建筑体。",
                        "Ctrl+左键拖动：绘制实心的线、平面或建筑体。",
                        "Ctrl+左键短按两次：依次记录两个点，绘制中空平面或中空建筑体。",
                        "一维范围不能中空，仍会生成普通实心线。"
                ),
                List.of(
                        "开始绘制后立即显示虚拟方块，松开按键后预览保持不动。",
                        "松开按键后保留虚拟预览，调整完成后左键确认建筑。",
                        "方向键：移动整个虚拟区域。",
                        "Ctrl+上/下：调整整个虚拟区域高度。",
                        "Shift+方向键：移动第二点。",
                        "Shift+Ctrl+上/下：调整第二点高度。",
                        "Shift+右键：取消当前虚拟区域。"
                ),
                List.of(
                        "白色预览表示可以完整放置；红色预览表示当前不能放置。",
                        "创造模式不消耗材料，可以悬空放置并替换已有方块。",
                        "生存模式消耗背包中的已选方块，且不能替换不可替换的已有方块。",
                        "生存模式要求整个结构至少有一个方块与外部支撑相连。",
                        "生存模式材料不足或任一条件不满足时，整个预览变红且一个方块也不放置。",
                        "下方可设置平面和建筑体数量；数值不是人为最大值，但过大容易造成卡顿。"
                )
        ));
        addPaged(result, cn.quit5700.buildingwand.registry.ModItems.MOVE_WAND, LimitProfile.MOVE, true, List.of(
                List.of(
                        "第一次左键：记录第一点。",
                        "第二次左键：记录第二点，并保存两点之间的全部方块和空气。",
                        "记录完成后，原位置保持显示虚拟区域。",
                        "Shift+右键：忘记已记录的区域。",
                        "移动红石装置前请保持装置静止；红石开关的连接状态会被清零。"
                ),
                List.of(
                        "Ctrl+右键：显示已记录区域的移动投影。",
                        "方向键：移动整个虚拟区域；Ctrl+上/下：调整整体高度。",
                        "Alt+左/右：以区域中心旋转90度，向右为顺时针。",
                        "投影位置确定后，左键确认并执行移动。",
                        "移动成功后自动忘记当前记录。",
                        "切换到有记录的移动魔杖时，会提示按Ctrl+右键显示虚拟区域。"
                )
        ));
        add(result, LightItems.REDSTONE_SIGNAL_DELAY_ITEM, LimitProfile.NONE, false,
                "右键打开界面，可输入0至3600秒，或用±0.1/±1秒按钮调整。",
                "默认定向导电：箭尾输入、箭头输出；左右和上下不导电。",
                "五面输出模式：箭尾固定为唯一输入面，其余五面只输出，不能作为输入。",
                "红石信号开启和关闭都会完整延迟，短信号不会丢失。",
                "注意：红石能量感应器不能作为信号输入者，否则会造成信号混乱。",
                "区块卸载时保存模式、设置与待处理变化。");

        add(result, LightItems.REDSTONE_ENERGY_CONNECTOR_ITEM, LimitProfile.NONE, false,
                "Shift+右键设备A，再Shift+右键设备B：连接两个设备。",
                "对已经连接的同一对设备重复操作：断开连接。",
                "选择第一个设备后Shift+右键空气：取消当前选择。",
                "连续两次Shift+右键同一设备：取消当前选择。",
                "第二个设备不符合连接条件时，保留第一个设备等待重新选择。",
                "连接、断开或取消成功后，连接器恢复初始状态。",
                "普通右键不执行连接操作，避免改变开关状态。禁止跨维度连接。");

        for (Item item : cn.quit5700.pathfindingbeacon.registry.ModItems.ROUTE_BLOCK_ITEMS) {
            add(result, item, LimitProfile.NONE, false,
                    "与普通方块相同：右键目标表面放置。所有玩家都可以放置和调整。",
                    "同号方块组成贯穿世界高度的寻路线；同一编号不能放在相同X/Z列。",
                    "生存模式使用木镐或更好的镐正常破坏并掉落。",
                    "/pfcancel <1-30>：删除当前维度对应号码的全部方块和光柱，不产生掉落物。");
        }
        add(result, cn.quit5700.pathfindingbeacon.registry.ModItems.SEQUENCE_REORDERER, LimitProfile.NONE, false,
                "依次右键两个不同的同号有效方块。", "所有玩家都可以连接断开的线路，或调整同一线路的顺序。");

        add(result, LightItems.CONSTANT_LIGHT_BLOCK_ITEM, LimitProfile.NONE, false,
                "右键目标表面放置；相邻红石信号控制开关。", "收到红石信号后提供亮度15、半径14格的无衰减照明。");
        add(result, LightItems.REDSTONE_ENERGY_EMITTER_ITEM, LimitProfile.NONE, false,
                "通过连接器加入无线红石网络。", "网络内开关或感应器触发时，六面输出强度15的红石信号。", "空手右键查看网络摘要；安装 Jade（玉）后可直接查看。");
        for (Item item : LightItems.REMOTE_SWITCH_ITEMS.values()) {
            add(result, item, LimitProfile.NONE, false, "右键切换对应颜色无线网络的红石状态。", "同颜色设备连接数量不限；不同颜色普通设备不能直接合并。");
        }
        add(result, LightItems.REDSTONE_ENERGY_SENSOR_ITEM, LimitProfile.NONE, false,
                "必须使用连接器手动连接，不会自动两两配对。", "两个感应器仅在同轴、中间1至10格无障碍空间时允许连接。",
                "空气和无形原版光源方块可通过，其他方块会阻挡。", "实体经过感应线时输出红石信号，并可衰减传给相邻红石元件。",
                "外部信号断开后不会读取自身刚供电的红石粉。", "感应器允许连接不同颜色网络。");
        add(result, cn.quit5700.buildingwand.registry.ModItems.WAND_SETTINGS_STATION, LimitProfile.NONE, false,
                "右键快捷建造设置台：打开本界面。",
                "查看快捷建筑全部物品的设置、操作方法、命令和注意事项。",
                "左侧按设置优先和功能分组排列；使用下方箭头翻页。");
        return List.copyOf(result);
    }

    private static void add(
            List<ItemPage> pages, Item item, LimitProfile profile, boolean hasSettings, String... lines) {
        String title = new ItemStack(item).getHoverName().getString();
        pages.add(new ItemPage(item, hasSettings ? title + "(有设置)" : title,
                List.of(List.of(lines)), profile));
    }

    private static void addPaged(
            List<ItemPage> pages, Item item, LimitProfile profile, boolean hasSettings,
            List<List<String>> descriptionPages) {
        String title = new ItemStack(item).getHoverName().getString();
        pages.add(new ItemPage(item, hasSettings ? title + "(有设置)" : title,
                List.copyOf(descriptionPages), profile));
    }

    private enum LimitProfile { NONE, BUILDING, MOVE }
    private enum LimitType { PLANE, CUBE, MOVE }
    private record ItemPage(Item item, String title, List<List<String>> descriptionPages, LimitProfile limits) {}
}
