package cn.quit5700.light.client;

import cn.quit5700.light.menu.RedstoneDelayMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public final class RedstoneDelayScreen extends AbstractContainerScreen<RedstoneDelayMenu> {
    private EditBox seconds;
    private int lastMenuTicks;
    private boolean userEdited;
    private boolean syncingFromServer;
    private Button modeButton;

    public RedstoneDelayScreen(RedstoneDelayMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 248, 176);
    }

    @Override protected void init() {
        super.init();
        int x = width / 2 - 110;
        int y = height / 2 - 52;
        seconds = new EditBox(font, x + 60, y + 30, 100, 20, Component.literal("延迟秒数"));
        seconds.setMaxLength(6);
        seconds.setValue(format(menu.delayTicks()));
        lastMenuTicks = menu.delayTicks();
        seconds.setResponder(value -> {
            if (!syncingFromServer) userEdited = true;
        });
        addRenderableWidget(seconds);
        addRenderableWidget(button("-1秒", x, y + 58, () -> adjust(-20)));
        addRenderableWidget(button("-0.1秒", x + 56, y + 58, () -> adjust(-2)));
        addRenderableWidget(button("+0.1秒", x + 112, y + 58, () -> adjust(2)));
        addRenderableWidget(button("+1秒", x + 168, y + 58, () -> adjust(20)));
        modeButton = Button.builder(modeText(), button -> toggleMode()).bounds(x + 30, y + 90, 160, 20).build();
        addRenderableWidget(modeButton);
        addRenderableWidget(Button.builder(Component.literal("确定"), button -> confirm()).bounds(x + 30, y + 118, 75, 20).build());
        addRenderableWidget(Button.builder(Component.literal("取消"), button -> onClose()).bounds(x + 115, y + 118, 75, 20).build());
        setInitialFocus(seconds);
    }

    private Button button(String text, int x, int y, Runnable action) {
        return Button.builder(Component.literal(text), button -> action.run()).bounds(x, y, 52, 20).build();
    }

    private void adjust(int ticks) {
        userEdited = true;
        int value = Math.max(0, Math.min(72000, parseTicks() + ticks));
        seconds.setValue(format(value));
    }

    @Override protected void containerTick() {
        super.containerTick();
        int syncedTicks = menu.delayTicks();
        if (!userEdited && syncedTicks != lastMenuTicks) {
            syncingFromServer = true;
            seconds.setValue(format(syncedTicks));
            syncingFromServer = false;
        }
        lastMenuTicks = syncedTicks;
        modeButton.setMessage(modeText());
    }

    private int parseTicks() {
        try { return (int) Math.round(Double.parseDouble(seconds.getValue()) * 20.0); }
        catch (NumberFormatException ignored) { return menu.delayTicks(); }
    }

    private static String format(int ticks) { return String.format(Locale.ROOT, "%.1f", ticks / 20.0); }

    private Component modeText() {
        return Component.literal("输出模式：" + (menu.omnidirectional() ? "五面输出" : "定向输出"));
    }

    private void toggleMode() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId, RedstoneDelayMenu.TOGGLE_MODE_BUTTON_ID);
        }
    }

    private void confirm() {
        int ticks = Math.max(0, Math.min(72000, parseTicks()));
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ticks);
        }
        onClose();
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = width / 2;
        int y = height / 2 - 52;
        graphics.centeredText(font, title, x, y + 8, 0xFFFFFF);
        graphics.text(font, "延迟：", x - 110, y + 36, 0xE0E0E0);
        graphics.centeredText(font, Component.literal("输入：" + (menu.inputPowered() ? "开" : "关")
                + "    输出：" + (menu.outputPowered() ? "开" : "关")), x, y + 78, 0xC8C8C8);
    }
}
