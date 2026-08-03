package cn.quit5700.light.client;

import cn.quit5700.light.logic.VariableConstantLightSettings;
import cn.quit5700.light.logic.VariableLightMode;
import cn.quit5700.light.menu.VariableConstantLightMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class VariableConstantLightScreen extends AbstractContainerScreen<VariableConstantLightMenu> {
    private RadiusSlider radiusSlider;
    private SpacingSlider spacingSlider;
    private int lastMenuRadius;
    private int lastMenuSpacing;
    private VariableLightMode selectedMode;
    private boolean modeEdited;
    private Button modeButton;
    private final Button[] modeOptions = new Button[VariableLightMode.values().length];

    public VariableConstantLightScreen(VariableConstantLightMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 248;
        imageHeight = 176;
    }

    @Override
    protected void init() {
        super.init();
        int x = width / 2 - 100;
        int y = height / 2 - 42;
        radiusSlider = new RadiusSlider(x, y + 30, 200, 20, menu.radius());
        spacingSlider = new SpacingSlider(x, y + 56, 200, 20, menu.spacing());
        lastMenuRadius = menu.radius();
        lastMenuSpacing = menu.spacing();
        selectedMode = menu.mode();
        addRenderableWidget(radiusSlider);
        addRenderableWidget(spacingSlider);
        modeButton = addRenderableWidget(Button.builder(modeMessage(), button -> toggleModeOptions())
                .bounds(8, 8, 150, 20).build());
        for (VariableLightMode mode : VariableLightMode.values()) {
            Button option = addRenderableWidget(Button.builder(modeName(mode), button -> selectMode(mode))
                    .bounds(8, 30 + mode.ordinal() * 22, 150, 20).build());
            option.visible = false;
            modeOptions[mode.ordinal()] = option;
        }
        updateModeControls();
        addRenderableWidget(Button.builder(Component.translatable("text.quick_build.114"),
                button -> save()).bounds(x + 20, y + 112, 75, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("text.quick_build.113"),
                button -> onClose()).bounds(x + 105, y + 112, 75, 20).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        int syncedRadius = menu.radius();
        if (!radiusSlider.userEdited() && syncedRadius != lastMenuRadius) {
            radiusSlider.syncRadius(syncedRadius);
        }
        lastMenuRadius = syncedRadius;
        int syncedSpacing = menu.spacing();
        if (!spacingSlider.userEdited() && syncedSpacing != lastMenuSpacing) {
            spacingSlider.syncSpacing(syncedSpacing);
        }
        lastMenuSpacing = syncedSpacing;
        if (!modeEdited && menu.mode() != selectedMode) {
            selectedMode = menu.mode();
            updateModeControls();
        }
    }

    private void save() {
        submitConfiguration(true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int centerX = width / 2;
        int y = height / 2 - 42;
        graphics.drawCenteredString(font, title, centerX, y + 8, 0xFFFFFF);
        int side = VariableConstantLightSettings.sideLength(radiusSlider.radius());
        graphics.drawCenteredString(font,
                Component.translatable("screen.quick_build.variable_light_size", side, side, side),
                centerX, y + 82, 0xD8EFFF);
        graphics.drawCenteredString(font,
                Component.translatable("screen.quick_build.variable_light_powered",
                        Component.translatable(menu.powered() ? "text.quick_build.075" : "text.quick_build.056")),
                centerX, y + 96, 0xC8C8C8);
        if (selectedMode == VariableLightMode.LOCAL_DAYLIGHT) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.quick_build.variable_light_daylight_warning"),
                    centerX, y + 140, 0xFFB040);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    }

    private Component modeName(VariableLightMode mode) {
        return Component.translatable(switch (mode) {
            case VANILLA_INVISIBLE -> "screen.quick_build.variable_light_mode.vanilla_invisible";
            case LOCAL_DAYLIGHT -> "screen.quick_build.variable_light_mode.local_daylight";
            case HYBRID -> "screen.quick_build.variable_light_mode.hybrid";
        });
    }

    private Component modeMessage() {
        return Component.translatable("screen.quick_build.variable_light_mode", modeName(selectedMode));
    }

    private void toggleModeOptions() {
        boolean show = !modeOptions[0].visible;
        for (Button option : modeOptions) option.visible = show;
    }

    private void selectMode(VariableLightMode mode) {
        selectedMode = mode;
        modeEdited = true;
        for (Button option : modeOptions) option.visible = false;
        updateModeControls();
        if (mode == VariableLightMode.VANILLA_INVISIBLE) submitConfiguration(false);
    }

    private void submitConfiguration(boolean closeAfterSubmit) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    VariableConstantLightSettings.radiusButtonId(radiusSlider.radius()));
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    VariableConstantLightSettings.spacingButtonId(spacingSlider.spacing()));
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    VariableConstantLightSettings.modeButtonId(selectedMode));
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    VariableConstantLightSettings.APPLY_BUTTON_ID);
        }
        if (closeAfterSubmit) onClose();
    }

    private void updateModeControls() {
        if (modeButton != null) modeButton.setMessage(modeMessage());
        if (spacingSlider != null) spacingSlider.active = selectedMode != VariableLightMode.LOCAL_DAYLIGHT;
    }

    private static final class RadiusSlider extends AbstractSliderButton {
        private int radius;
        private boolean userEdited;

        private RadiusSlider(int x, int y, int width, int height, int radius) {
            super(x, y, width, height, Component.empty(),
                    VariableConstantLightSettings.sliderFromRadius(radius));
            this.radius = VariableConstantLightSettings.clampRadius(radius);
            updateMessage();
        }

        private int radius() {
            return radius;
        }

        private boolean userEdited() {
            return userEdited;
        }

        private void syncRadius(int syncedRadius) {
            radius = VariableConstantLightSettings.clampRadius(syncedRadius);
            value = VariableConstantLightSettings.sliderFromRadius(radius);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("screen.quick_build.variable_light_radius", radius));
        }

        @Override
        protected void applyValue() {
            radius = VariableConstantLightSettings.radiusFromSlider(value);
            value = VariableConstantLightSettings.sliderFromRadius(radius);
            userEdited = true;
            updateMessage();
        }
    }

    private static final class SpacingSlider extends AbstractSliderButton {
        private int spacing;
        private boolean userEdited;

        private SpacingSlider(int x, int y, int width, int height, int spacing) {
            super(x, y, width, height, Component.empty(),
                    VariableConstantLightSettings.sliderFromSpacing(spacing));
            this.spacing = VariableConstantLightSettings.clampSpacing(spacing);
            updateMessage();
        }

        private int spacing() { return spacing; }
        private boolean userEdited() { return userEdited; }

        private void syncSpacing(int syncedSpacing) {
            spacing = VariableConstantLightSettings.clampSpacing(syncedSpacing);
            value = VariableConstantLightSettings.sliderFromSpacing(spacing);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("screen.quick_build.variable_light_spacing", spacing));
        }

        @Override
        protected void applyValue() {
            spacing = VariableConstantLightSettings.spacingFromSlider(value);
            value = VariableConstantLightSettings.sliderFromSpacing(spacing);
            userEdited = true;
            updateMessage();
        }
    }
}
