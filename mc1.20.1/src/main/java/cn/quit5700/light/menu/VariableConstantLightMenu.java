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
    private int pendingRadius;
    private int pendingSpacing;
    private VariableLightMode pendingMode;

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
        pendingRadius = VariableConstantLightSettings.clampRadius(data.get(0));
        pendingSpacing = VariableConstantLightSettings.clampSpacing(data.get(2));
        pendingMode = VariableLightMode.fromOrdinal(data.get(3));
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
        if (VariableConstantLightSettings.isRadiusButtonId(id)) {
            pendingRadius = VariableConstantLightSettings.radiusFromButtonId(id);
            return true;
        }
        if (VariableConstantLightSettings.isSpacingButtonId(id)) {
            pendingSpacing = VariableConstantLightSettings.spacingFromButtonId(id);
            return true;
        }
        if (VariableConstantLightSettings.isModeButtonId(id)) {
            pendingMode = VariableConstantLightSettings.modeFromButtonId(id);
            return true;
        }
        if (id == VariableConstantLightSettings.APPLY_BUTTON_ID) {
            if (blockEntity != null) {
                blockEntity.setConfiguration(pendingRadius, pendingSpacing, pendingMode);
            } else {
                data.set(0, pendingRadius);
                data.set(2, pendingSpacing);
                data.set(3, pendingMode.ordinal());
            }
            broadcastChanges();
            return true;
        }
        return false;
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
