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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;

public final class VariableConstantLightBlockEntity extends BlockEntity implements MenuProvider {
    private cn.quit5700.light.logic.LightBox area;
    public cn.quit5700.light.logic.LightBox area() { return area; }
    public void selectArea(cn.quit5700.light.logic.LightBox selected,VariableLightMode areaMode) {
        if (isLegacy()) return;
        if(!areaMode.isAreaMode() || (areaMode==VariableLightMode.AREA_REAL_LIGHT && !cn.quit5700.light.logic.RealAreaPolicy.accepts(selected)))return;
        area = selected;
        setConfiguration(radius,spacing,areaMode);
        setChanged();
        if(level instanceof ServerLevel server) {
            cn.quit5700.light.light.AreaLightState.get(server).update(server,worldPosition);
            cn.quit5700.light.light.RealAreaLightState.get(server).update(this);
            cn.quit5700.light.network.VariableLightNetworking.syncWorld(server);
        }
    }
    private int radius = VariableConstantLightSettings.DEFAULT_RADIUS;
    private int spacing = VariableConstantLightSettings.DEFAULT_SPACING;
    private VariableLightMode mode = VariableLightMode.AREA_FULL_BRIGHT;
    private VariableLightMode retiredMode;
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> radius;
                case 1 -> getBlockState().getValue(VariableConstantLightBlock.POWERED) ? 1 : 0;
                case 2 -> spacing;
                case 3 -> mode.ordinal();
                case 4 -> isLegacy() ? 1 : 0;
                case 5 -> area != null ? 1 : 0;
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
            return 6;
        }
    };

    public VariableConstantLightBlockEntity(BlockPos pos, BlockState state) {
        super(LightBlockEntities.VARIABLE_CONSTANT_LIGHT, pos, state);
        if (isLegacy()) mode = VariableLightMode.VANILLA_INVISIBLE;
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
            if (retiredMode != null) {
                taskState.reconfigure(worldPosition, radius, spacing, retiredMode, radius, spacing, mode);
                cn.quit5700.light.light.DaylightOcclusion.remove(serverLevel, worldPosition);
                retiredMode = null;
                setChanged();
            } else taskState.registerExisting(worldPosition, radius, spacing, mode);
            taskState.setLegacy(worldPosition, isLegacy());
            cn.quit5700.light.light.AreaLightState.get(serverLevel).update(this);
            cn.quit5700.light.light.RealAreaLightState.get(serverLevel).update(this);
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
        VariableLightMode updatedMode = cn.quit5700.light.logic.AdjustableLightPolicy.savedMode(isLegacy(), requestedMode == null
                ? VariableLightMode.AREA_FULL_BRIGHT : requestedMode);
        if (isLegacy() && updatedMode.isAreaMode()) return;
        if(updatedMode==VariableLightMode.AREA_REAL_LIGHT && area!=null && !cn.quit5700.light.logic.RealAreaPolicy.accepts(area))return;
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
    protected void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putInt("radius", radius);
        data.putInt("spacing", spacing);
        data.putInt("mode", mode.ordinal());
        if (area != null) {
            data.putInt("area_defined",1);
            data.putInt("area_min_x",area.minX()); data.putInt("area_min_y",area.minY()); data.putInt("area_min_z",area.minZ());
            data.putInt("area_max_x",area.maxX()); data.putInt("area_max_y",area.maxY()); data.putInt("area_max_z",area.maxZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.loadAdditional(data, registries);
        radius = VariableConstantLightSettings.clampRadius(
                (data.contains("radius") ? data.getInt("radius") : VariableConstantLightSettings.DEFAULT_RADIUS));
        spacing = VariableConstantLightSettings.clampSpacing(
                (data.contains("spacing") ? data.getInt("spacing") : 4));
        if (!isLegacy()) spacing = 1;
        VariableLightMode savedMode = VariableLightMode.fromOrdinal((data.contains("mode") ? data.getInt("mode") : 0));
        mode = cn.quit5700.light.logic.AdjustableLightPolicy.savedMode(isLegacy(), savedMode);
        retiredMode = mode != savedMode ? savedMode : null;
        area = null;
        if (!isLegacy() && (data.contains("area_defined") ? data.getInt("area_defined") : 0)==1) {
            try {
                area = cn.quit5700.light.logic.LightBox.of((data.contains("area_min_x") ? data.getInt("area_min_x") : 0),(data.contains("area_min_y") ? data.getInt("area_min_y") : 0),
                        (data.contains("area_min_z") ? data.getInt("area_min_z") : 0),(data.contains("area_max_x") ? data.getInt("area_max_x") : 0),(data.contains("area_max_y") ? data.getInt("area_max_y") : 0),(data.contains("area_max_z") ? data.getInt("area_max_z") : 0));
            } catch (IllegalArgumentException invalid) { area = null; }
        }
        if (isLegacy() && mode.isAreaMode()) mode=VariableLightMode.VANILLA_INVISIBLE;
    }

}
