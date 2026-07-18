package cn.quit5700.light.menu;

import cn.quit5700.light.block.RedstoneSignalDelayBlockEntity;
import cn.quit5700.light.registry.LightBlocks;
import cn.quit5700.light.registry.LightMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class RedstoneDelayMenu extends AbstractContainerMenu {
    public static final int TOGGLE_MODE_BUTTON_ID = 72_001;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public RedstoneDelayMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainerData(4), null);
    }

    public RedstoneDelayMenu(int id, Inventory inventory, ContainerData data, RedstoneSignalDelayBlockEntity blockEntity) {
        super(LightMenus.REDSTONE_DELAY, id);
        this.data = data;
        this.access = blockEntity == null || blockEntity.getLevel() == null
                ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        checkContainerDataCount(data, 4);
        addDataSlots(data);
    }

    public int delayTicks() { return data.get(0); }
    public boolean inputPowered() { return data.get(1) != 0; }
    public boolean outputPowered() { return data.get(2) != 0; }
    public boolean omnidirectional() { return data.get(3) != 0; }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (id == TOGGLE_MODE_BUTTON_ID) {
            data.set(3, omnidirectional() ? 0 : 1);
            broadcastChanges();
            return true;
        }
        if (id < 0 || id > 72000) return false;
        data.set(0, id);
        broadcastChanges();
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return stillValid(access, player, LightBlocks.REDSTONE_SIGNAL_DELAY); }
}
