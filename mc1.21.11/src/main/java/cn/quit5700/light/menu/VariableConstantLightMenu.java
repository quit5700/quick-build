package cn.quit5700.light.menu;

import cn.quit5700.light.block.VariableConstantLightBlockEntity;
import cn.quit5700.light.logic.VariableConstantLightSettings;
import cn.quit5700.light.logic.VariableLightMode;
import cn.quit5700.light.registry.LightBlocks;
import cn.quit5700.light.registry.LightMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class VariableConstantLightMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final VariableConstantLightBlockEntity blockEntity;

    public VariableConstantLightMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainerData(4), null);
    }

    public VariableConstantLightMenu(int id, Inventory inventory, ContainerData data,
                                     VariableConstantLightBlockEntity blockEntity) {
        super(LightMenus.VARIABLE_CONSTANT_LIGHT, id);
        this.data = data;
        this.blockEntity = blockEntity;
        this.access = blockEntity == null || blockEntity.getLevel() == null
                ? ContainerLevelAccess.NULL
                : ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        checkContainerDataCount(data, 4);
        addDataSlots(data);
    }

    public int radius() {
        return data.get(0);
    }

    public boolean powered() {
        return data.get(1) != 0;
    }

    public int spacing() {
        return data.get(2);
    }

    public VariableLightMode mode() { return VariableLightMode.fromOrdinal(data.get(3)); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (VariableConstantLightSettings.isConfigurationButtonId(id)) {
            int radius = VariableConstantLightSettings.radiusFromConfigurationButtonId(id);
            int spacing = VariableConstantLightSettings.spacingFromConfigurationButtonId(id);
            VariableLightMode mode = VariableConstantLightSettings.modeFromConfigurationButtonId(id);
            if (blockEntity != null) {
                blockEntity.setConfiguration(radius, spacing, mode);
            } else {
                data.set(0, radius);
                data.set(2, spacing);
                data.set(3, mode.ordinal());
            }
            broadcastChanges();
            return true;
        }
        return false;
    }

    public static int configurationButtonId(int radius, int spacing, VariableLightMode mode) {
        return VariableConstantLightSettings.configurationButtonId(radius, spacing, mode);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, LightBlocks.VARIABLE_CONSTANT_LIGHT);
    }
}
