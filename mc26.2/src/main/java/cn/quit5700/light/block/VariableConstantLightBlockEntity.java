package cn.quit5700.light.block;

import cn.quit5700.light.light.ConstantLightTaskQueue;
import cn.quit5700.light.light.VariableLightTaskState;
import cn.quit5700.light.logic.VariableConstantLightSettings;
import cn.quit5700.light.logic.VariableLightMode;
import cn.quit5700.light.menu.VariableConstantLightMenu;
import cn.quit5700.light.registry.LightBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class VariableConstantLightBlockEntity extends BlockEntity implements MenuProvider {
    private int radius = VariableConstantLightSettings.DEFAULT_RADIUS;
    private int spacing = VariableConstantLightSettings.DEFAULT_SPACING;
    private VariableLightMode mode = VariableLightMode.VANILLA_INVISIBLE;
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> radius;
                case 1 -> getBlockState().getValue(VariableConstantLightBlock.POWERED) ? 1 : 0;
                case 2 -> spacing;
                case 3 -> mode.ordinal();
                case 4 -> isLegacy() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) setRadius(value);
            if (index == 2) setSpacing(value);
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public VariableConstantLightBlockEntity(BlockPos pos, BlockState state) {
        super(LightBlockEntities.VARIABLE_CONSTANT_LIGHT, pos, state);
    }

    public int radius() {
        return radius;
    }

    public int spacing() {
        return spacing;
    }

    public VariableLightMode mode() { return mode; }
    public boolean isLegacy() { return getBlockState().is(cn.quit5700.light.registry.LightBlocks.COPY_VARIABLE_CONSTANT_LIGHT); }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel
                && getBlockState().getValue(VariableConstantLightBlock.POWERED)) {
            VariableLightTaskState taskState = VariableLightTaskState.get(serverLevel);
            taskState.registerExisting(worldPosition, radius, spacing, mode);
            taskState.setLegacy(worldPosition, isLegacy());
            if (mode.usesDaylightRendering()) taskState.syncClients(serverLevel);
        }
    }

    public void setRadius(int requestedRadius) {
        setConfiguration(requestedRadius, spacing, mode);
    }

    public void setSpacing(int requestedSpacing) {
        setConfiguration(radius, requestedSpacing, mode);
    }

    public void setConfiguration(int requestedRadius, int requestedSpacing, VariableLightMode requestedMode) {
        int updatedRadius = VariableConstantLightSettings.clampRadius(requestedRadius);
        int updatedSpacing = isLegacy() ? VariableConstantLightSettings.clampSpacing(requestedSpacing) : 1;
        VariableLightMode updatedMode = requestedMode == null ? VariableLightMode.VANILLA_INVISIBLE : requestedMode;
        if (updatedRadius == radius && updatedSpacing == spacing && updatedMode == mode) return;
        int oldRadius = radius;
        int oldSpacing = spacing;
        VariableLightMode oldMode = mode;
        radius = updatedRadius;
        spacing = updatedSpacing;
        mode = updatedMode;
        if (level instanceof ServerLevel serverLevel
                && getBlockState().getValue(VariableConstantLightBlock.POWERED)) {
            ConstantLightTaskQueue.reconfigureVariable(
                    serverLevel, worldPosition,
                    oldRadius, oldSpacing, oldMode, updatedRadius, updatedSpacing, updatedMode);
        }
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(isLegacy() ? "block.light.copy_variable_constant_light" : "screen.quick_build.variable_light_title");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new VariableConstantLightMenu(id, inventory, menuData, this);
    }

    @Override
    protected void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        data.putInt("radius", radius);
        data.putInt("spacing", spacing);
        data.putInt("mode", mode.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput data) {
        super.loadAdditional(data);
        radius = VariableConstantLightSettings.clampRadius(
                data.getIntOr("radius", VariableConstantLightSettings.DEFAULT_RADIUS));
        spacing = VariableConstantLightSettings.clampSpacing(
                data.getIntOr("spacing", 4));
        if (!isLegacy()) spacing = 1;
        mode = VariableLightMode.fromOrdinal(data.getIntOr("mode", 0));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel
                && state.getValue(VariableConstantLightBlock.POWERED)) {
            ConstantLightTaskQueue.cleanupVariable(serverLevel, pos, radius, spacing, mode);
        }
        super.preRemoveSideEffects(pos, state);
    }
}
