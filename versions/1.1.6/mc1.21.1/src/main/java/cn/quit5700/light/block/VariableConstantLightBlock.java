package cn.quit5700.light.block;

import cn.quit5700.light.light.ConstantLightTaskQueue;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class VariableConstantLightBlock extends BaseEntityBlock {
    public static final MapCodec<VariableConstantLightBlock> CODEC = simpleCodec(VariableConstantLightBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public VariableConstantLightBlock(ResourceKey<Block> key) {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_LAMP)
                .lightLevel(state -> state.getValue(POWERED) ? 15 : 0)
                .strength(1.5F, 6.0F)
                .pushReaction(PushReaction.BLOCK));
    }

    private VariableConstantLightBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VariableConstantLightBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(POWERED,
                context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (world instanceof ServerLevel serverWorld
                && state.getValue(POWERED)
                && world.getBlockEntity(pos) instanceof VariableConstantLightBlockEntity light) {
            ConstantLightTaskQueue.installVariable(serverWorld, pos, light.radius(), light.spacing(), light.mode());
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        boolean powered = world.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) {
            return;
        }
        world.setBlock(pos, state.setValue(POWERED, powered), UPDATE_ALL);
        if (world instanceof ServerLevel serverWorld
                && world.getBlockEntity(pos) instanceof VariableConstantLightBlockEntity light) {
            if (powered) {
                ConstantLightTaskQueue.installVariable(serverWorld, pos, light.radius(), light.spacing(), light.mode());
            } else {
                ConstantLightTaskQueue.cleanupVariable(serverWorld, pos, light.radius(), light.spacing(), light.mode());
            }
            light.setChanged();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer
                && world.getBlockEntity(pos) instanceof VariableConstantLightBlockEntity light) {
            serverPlayer.openMenu(light);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && world instanceof ServerLevel serverWorld
                && state.getValue(POWERED)
                && world.getBlockEntity(pos) instanceof VariableConstantLightBlockEntity light) {
            ConstantLightTaskQueue.cleanupVariable(serverWorld, pos, light.radius(), light.spacing(), light.mode());
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
