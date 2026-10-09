package cn.quit5700.light.client;

import cn.quit5700.light.logic.VariableConstantLightSettings;
import cn.quit5700.light.logic.VariableLightMode;
import cn.quit5700.light.logic.VariableLightPresentation;
import cn.quit5700.light.logic.AdjustableLightPolicy;
import cn.quit5700.light.menu.VariableConstantLightMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
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
    private Checkbox brightButton;
    private Button areaButton;
    private final Button[] modeOptions = new Button[VariableLightMode.values().length];

    public VariableConstantLightScreen(VariableConstantLightMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth=248;imageHeight=176;
    }

    @Override
    protected void init() {
        super.init();
        int x = width / 2 - 100;
        int y = menu.isLegacy() ? height / 2 - 42 : Math.max(0, (height - 172) / 2) - 4;
        radiusSlider = new RadiusSlider(x, y + 30, 200, 20, menu.radius());
        spacingSlider = new SpacingSlider(x, y + 56, 200, 20, menu.spacing());
        lastMenuRadius = menu.radius();
        lastMenuSpacing = menu.spacing();
        selectedMode = AdjustableLightPolicy.savedMode(menu.isLegacy(), menu.mode());
        addRenderableWidget(radiusSlider);
        addRenderableWidget(spacingSlider);
        modeButton = addRenderableWidget(Button.builder(modeMessage(), button -> toggleModeOptions())
                .bounds(menu.isLegacy() ? 8 : x, menu.isLegacy() ? 8 : y + 26, menu.isLegacy() ? 150 : 200, 20).build());
        int optionIndex = 0;
        for (VariableLightMode mode : VariableLightMode.values()) {
            Button option = addRenderableWidget(Button.builder(modeName(mode), button -> selectMode(mode))
                    .bounds(8, 30 + optionIndex++ * 22, 150, 20).build());
            option.visible = false;
            modeOptions[mode.ordinal()] = option;
        }
        rebuildBoostCheckbox();
        AreaSelectionClient.hideRange();

        areaButton=addRenderableWidget(Button.builder(Component.translatable("screen.quick_build.area_select"),button->{
            if(minecraft!=null && minecraft.gameMode!=null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,selectedMode==VariableLightMode.AREA_REAL_LIGHT
                        ? VariableConstantLightSettings.SELECT_REAL_AREA_BUTTON_ID : VariableConstantLightSettings.SELECT_AREA_BUTTON_ID);
                onClose();
            }
        }).bounds(x,y+56,200,20).build());
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
            selectedMode = AdjustableLightPolicy.savedMode(menu.isLegacy(), menu.mode());
            updateModeControls();
        }
        updateModeControls();
    }

    private void rebuildBoostCheckbox() {
        if(brightButton!=null)removeWidget(brightButton);
        brightButton=addRenderableWidget(Checkbox.builder(Component.translatable("screen.quick_build.boost_option"),font)
                .pos(width/2-100,height/2+14).maxWidth(200).selected(selectedMode==VariableLightMode.HYBRID)
                .onValueChange((checkbox,enabled)->{
                    selectedMode=enabled?VariableLightMode.HYBRID:VariableLightMode.VANILLA_INVISIBLE;
                    modeEdited=true;
                    updateModeControls();
                }).build());
    }


    private void save() {
        submitConfiguration(true);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int centerX = width / 2;
        int y = menu.isLegacy() ? height / 2 - 42 : Math.max(0, (height - 172) / 2) - 4;
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, y + 12);
        graphics.pose().scale(1.5F, 1.5F);
        graphics.drawCenteredString(font, title, 0, 0, 0xFFFFFFFF);
        graphics.pose().popMatrix();
        int side = VariableConstantLightSettings.sideLength(radiusSlider.radius());
        if(selectedMode==VariableLightMode.AREA_REAL_LIGHT) {
            int lineY=y+138;
            for(var line:font.split(Component.translatable("screen.quick_build.real_area_warning"),220)) {
                graphics.drawString(font,line,centerX-font.width(line)/2,lineY,0xFFFFB040);
                lineY+=9;
            }
        } else if (!areaOptionsOpen()) graphics.drawCenteredString(font,
                selectedMode==VariableLightMode.AREA_REAL_LIGHT ? Component.translatable("screen.quick_build.real_area_warning")
                        : selectedMode==VariableLightMode.AREA_FULL_BRIGHT ? Component.translatable(menu.hasArea()
                                ? "screen.quick_build.area_wand_help" : "screen.quick_build.area_selection_required")
                        : Component.translatable("screen.quick_build.variable_light_size", side, side, side),
                centerX, y + 82, 0xFFD8EFFF);

        if(selectedMode==VariableLightMode.AREA_REAL_LIGHT && !areaOptionsOpen()) graphics.drawCenteredString(font,
                Component.translatable(menu.hasArea() ? "screen.quick_build.area_wand_help" : "screen.quick_build.area_selection_required"), centerX, y + 82, 0xFFD8EFFF);
        graphics.drawCenteredString(font,
                Component.translatable("screen.quick_build.variable_light_powered",
                        Component.translatable(menu.powered() ? "text.quick_build.075" : "text.quick_build.056")),
                centerX, y + 96, 0xFFC8C8C8);
        if (selectedMode == VariableLightMode.LOCAL_DAYLIGHT) {
            graphics.drawCenteredString(font,
                    Component.translatable(menu.isLegacy() ? "screen.quick_build.variable_light_legacy_warning" : "screen.quick_build.variable_light_daylight_warning"),
                    centerX, y + 140, 0xFFFFB040);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY) {}

    private Component modeName(VariableLightMode mode) {
        return Component.translatable(switch (mode) {
            case VANILLA_INVISIBLE -> "screen.quick_build.variable_light_mode.vanilla_invisible";
            case LOCAL_DAYLIGHT -> "screen.quick_build.variable_light_mode.local_daylight";
            case HYBRID -> "screen.quick_build.variable_light_mode.hybrid";
            case AREA_FULL_BRIGHT -> "screen.quick_build.variable_light_mode.area";
            case AREA_REAL_LIGHT -> "screen.quick_build.variable_light_mode.real_area";
        });
    }

    private Component modeMessage() {
        return Component.translatable("screen.quick_build.variable_light_mode", modeName(VariableLightPresentation.primaryMode(menu.isLegacy(), selectedMode)));
    }

    private void toggleModeOptions() {
        boolean show = !modeOptions[AdjustableLightPolicy.modes(menu.isLegacy()).getFirst().ordinal()].visible;
        for (VariableLightMode mode : VariableLightMode.values()) {
            Button option = modeOptions[mode.ordinal()];
            if (option != null) option.visible = show && AdjustableLightPolicy.modes(menu.isLegacy()).contains(mode);
        }
    }

    private void selectMode(VariableLightMode mode) {
        selectedMode = mode;
        modeEdited = true;
        for (Button option : modeOptions) if (option != null) option.visible = false;
        updateModeControls();
        if (mode == VariableLightMode.VANILLA_INVISIBLE) submitConfiguration(false);
    }

    private void submitConfiguration(boolean closeAfterSubmit) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    VariableConstantLightSettings.radiusButtonId(radiusSlider.radius()));
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    VariableConstantLightSettings.spacingButtonId(menu.isLegacy() ? spacingSlider.spacing() : 1));
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    VariableConstantLightSettings.modeButtonId(selectedMode));
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    VariableConstantLightSettings.APPLY_BUTTON_ID);
        }
        if (closeAfterSubmit) onClose();
    }

    private boolean areaOptionsOpen() {
        return !menu.isLegacy() && modeOptions[VariableLightMode.AREA_FULL_BRIGHT.ordinal()] != null
                && modeOptions[VariableLightMode.AREA_FULL_BRIGHT.ordinal()].visible;
    }

    private void updateModeControls() {
        if (modeButton != null) modeButton.setMessage(modeMessage());
        int visibleIndex = 0;
        for (VariableLightMode mode : VariableLightMode.values()) {
            Button option = modeOptions[mode.ordinal()];
            if (option == null) continue;
            if (AdjustableLightPolicy.modes(menu.isLegacy()).contains(mode)) {
                int y = menu.isLegacy() ? 30 : Math.max(0, (height - 172) / 2) + 44;
                option.setY(y + visibleIndex++ * 22);
                option.setX(menu.isLegacy() ? 8 : width / 2 - 100);
                option.setWidth(menu.isLegacy() ? 150 : 200);
            }
            else option.visible = false;
        }
        boolean area=selectedMode.isAreaMode() && !menu.isLegacy();
        if(radiusSlider!=null){radiusSlider.active=menu.isLegacy();radiusSlider.visible=menu.isLegacy();}
        if(areaButton!=null)areaButton.visible=area && !areaOptionsOpen();
        if (spacingSlider != null) {spacingSlider.active = menu.isLegacy() && selectedMode != VariableLightMode.LOCAL_DAYLIGHT; spacingSlider.visible=menu.isLegacy();}
        if (brightButton != null) {
            if(brightButton.selected()!=(selectedMode==VariableLightMode.HYBRID))rebuildBoostCheckbox();
            brightButton.visible = false;
        }
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
