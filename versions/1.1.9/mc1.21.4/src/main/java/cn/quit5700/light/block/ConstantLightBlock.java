package cn.quit5700.light.block;

import cn.quit5700.light.light.ConstantLightTaskQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public final class ConstantLightBlock extends Block {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ConstantLightBlock(ResourceKey<Block> key) {
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_LAMP)
                .lightLevel(state -> state.getValue(POWERED) ? 15 : 0)
                .strength(1.5F, 6.0F)
                .pushReaction(PushReaction.BLOCK)
                .setId(key));
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (world instanceof ServerLevel serverWorld && state.getValue(POWERED)) {
            ConstantLightTaskQueue.install(serverWorld, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        boolean powered = world.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) {
            return;
        }
        world.setBlock(pos, state.setValue(POWERED, powered), UPDATE_ALL);
        if (world instanceof ServerLevel serverWorld) {
            if (powered) {
                ConstantLightTaskQueue.install(serverWorld, pos);
            } else {
                ConstantLightTaskQueue.cleanup(serverWorld, pos);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel world && state.getValue(POWERED)) {
            ConstantLightTaskQueue.cleanup(world, pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
