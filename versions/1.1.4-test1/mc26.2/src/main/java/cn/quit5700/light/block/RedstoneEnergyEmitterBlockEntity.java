package cn.quit5700.light.block;

import cn.quit5700.light.redstone.*;
import cn.quit5700.light.registry.LightBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class RedstoneEnergyEmitterBlockEntity extends BlockEntity {
    private boolean pendingSync = true;
    public RedstoneEnergyEmitterBlockEntity(BlockPos pos, BlockState state) { super(LightBlockEntities.REDSTONE_ENERGY_EMITTER, pos, state); }
    @Override public void setLevel(Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel) {
            RedstoneEnergyState state = RedstoneEnergyState.get(serverLevel);
            if (state.getNode(serverLevel, worldPosition).isEmpty()) state.register(serverLevel, worldPosition, EnergyNodeType.EMITTER, EnergyColor.NONE, false);
            RedstoneEnergyNotifier.register(serverLevel, worldPosition);
        }
    }
    public static void tick(ServerLevel world, BlockPos pos, BlockState state, RedstoneEnergyEmitterBlockEntity blockEntity) {
        if (!blockEntity.pendingSync) return;
        blockEntity.pendingSync = false;
        RedstoneEnergyNotifier.syncEmitter(world, pos);
    }
    @Override public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) RedstoneEnergyNotifier.unregister(serverLevel, worldPosition);
        super.setRemoved();
    }
}
