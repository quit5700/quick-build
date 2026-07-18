package cn.quit5700.light.block;

import cn.quit5700.light.registry.LightBlockEntities;
import cn.quit5700.light.logic.DelayedSignalState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import cn.quit5700.light.menu.RedstoneDelayMenu;

public final class RedstoneSignalDelayBlockEntity extends BlockEntity implements MenuProvider {
    private DelayedSignalState signal;
    private final ContainerData menuData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) { case 0 -> signal.delayTicks(); case 1 -> signal.inputPowered() ? 1 : 0;
                case 2 -> signal.outputPowered() ? 1 : 0;
                case 3 -> getBlockState().getValue(RedstoneSignalDelayBlock.OMNIDIRECTIONAL) ? 1 : 0;
                default -> 0; };
        }
        @Override public void set(int index, int value) {
            if (index == 0) setDelayTicks(value);
            if (index == 3) setOmnidirectional(value != 0);
        }
        @Override public int getCount() { return 4; }
    };

    public RedstoneSignalDelayBlockEntity(BlockPos pos, BlockState state) {
        super(LightBlockEntities.REDSTONE_SIGNAL_DELAY, pos, state);
        signal = new DelayedSignalState(20, false, state.getValue(RedstoneSignalDelayBlock.POWERED));
    }

    private void captureInput(ServerLevel world, boolean input) {
        if (signal.capture(world.getGameTime(), input)) setChanged();
    }

    public void captureCurrentInput(ServerLevel world, BlockState state) {
        captureInput(world, RedstoneSignalDelayBlock.getArrowTailInputSignal(
                world, worldPosition, state) > 0);
    }

    public static void tick(ServerLevel world, BlockPos pos, BlockState state, RedstoneSignalDelayBlockEntity blockEntity) {
        blockEntity.captureCurrentInput(world, state);
        boolean outputChanged = blockEntity.signal.advance(world.getGameTime());
        if (outputChanged) {
            boolean output = blockEntity.signal.outputPowered();
            if (state.getValue(RedstoneSignalDelayBlock.POWERED) != output) {
                state = state.setValue(RedstoneSignalDelayBlock.POWERED, output);
                world.setBlock(pos, state, 3);
                RedstoneSignalDelayBlock.updateOutputNeighbors(world, pos, state);
            }
        }
        if (outputChanged) blockEntity.setChanged();
    }

    public void setDelayTicks(int ticks) {
        if (level instanceof ServerLevel serverLevel) {
            signal.reconfigureDelay(serverLevel.getGameTime(), ticks);
        } else {
            signal.setDelayTicks(ticks);
        }
        setChanged();
    }

    public void setOmnidirectional(boolean enabled) {
        if (getBlockState().getValue(RedstoneSignalDelayBlock.OMNIDIRECTIONAL) == enabled) return;
        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlockState().setValue(RedstoneSignalDelayBlock.OMNIDIRECTIONAL, enabled);
            serverLevel.setBlock(worldPosition, state, 3);
            RedstoneSignalDelayBlock.updateOutputNeighbors(serverLevel, worldPosition, state);
        }
        setChanged();
    }

    @Override public Component getDisplayName() { return Component.literal("红石信号延时开关"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RedstoneDelayMenu(id, inventory, menuData, this);
    }

    @Override
    protected void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        data.putInt("delayTicks", signal.delayTicks());
        data.putBoolean("lastInput", signal.inputPowered());
        data.store("pending", PendingChangeData.CODEC.listOf(), signal.pendingChanges().stream()
                .map(change -> new PendingChangeData(change.time(), change.powered())).toList());
    }

    @Override
    protected void loadAdditional(ValueInput data) {
        super.loadAdditional(data);
        int delayTicks = data.getIntOr("delayTicks", 20);
        boolean lastInput = data.getBooleanOr("lastInput", false);
        var pending = data.read("pending", PendingChangeData.CODEC.listOf()).orElse(java.util.List.of()).stream()
                .map(change -> new DelayedSignalState.PendingChange(change.time(), change.powered())).toList();
        signal = new DelayedSignalState(delayTicks, lastInput,
                getBlockState().getValue(RedstoneSignalDelayBlock.POWERED), pending);
    }

    private record PendingChangeData(long time, boolean powered) {
        private static final Codec<PendingChangeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("time").forGetter(PendingChangeData::time),
                Codec.BOOL.fieldOf("powered").forGetter(PendingChangeData::powered)
        ).apply(instance, PendingChangeData::new));
    }
}
