package cn.quit5700.light.block;

import cn.quit5700.light.registry.LightBlockEntities;
import cn.quit5700.light.logic.RedstoneDelayFacePolicy;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class RedstoneSignalDelayBlock extends BaseEntityBlock {
    public static final MapCodec<RedstoneSignalDelayBlock> CODEC = simpleCodec(RedstoneSignalDelayBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty OMNIDIRECTIONAL = BooleanProperty.create("omnidirectional");
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public RedstoneSignalDelayBlock(ResourceKey<Block> key) {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER).strength(0.5F));
    }

    private RedstoneSignalDelayBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(OMNIDIRECTIONAL, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RedstoneSignalDelayBlockEntity(pos, state); }
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? null : createTickerHelper(type, LightBlockEntities.REDSTONE_SIGNAL_DELAY,
                (tickWorld, pos, tickState, blockEntity) -> RedstoneSignalDelayBlockEntity.tick((ServerLevel) tickWorld, pos, tickState, blockEntity));
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected boolean isSignalSource(BlockState state) { return true; }
    @Override protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        if (!state.getValue(POWERED)) return 0;
        return RedstoneDelayFacePolicy.emitsForQueryDirection(
                state.getValue(OMNIDIRECTIONAL),
                direction,
                arrowTailSide(state),
                Direction::getOpposite) ? 15 : 0;
    }
    @Override protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) { return getSignal(state, world, pos, direction); }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public static Direction arrowTailSide(BlockState state) {
        // The unrotated model has its tail at north and its arrowhead at south.
        return state.getValue(FACING);
    }

    public static Direction arrowHeadSide(BlockState state) {
        return arrowTailSide(state).getOpposite();
    }

    public static int getArrowTailInputSignal(ServerLevel world, BlockPos pos, BlockState state) {
        Direction arrowTail = arrowTailSide(state);
        BlockPos inputPos = pos.relative(arrowTail);
        BlockState inputState = world.getBlockState(inputPos);
        int signal = world.getSignal(inputPos, arrowTail);
        if (inputState.getBlock() instanceof RedStoneWireBlock) {
            signal = Math.max(signal, inputState.getValue(RedStoneWireBlock.POWER));
        }
        return signal;
    }

    public static void updateOutputNeighbors(ServerLevel world, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            world.updateNeighborsAt(pos.relative(direction), state.getBlock());
        }
        world.updateNeighborsAt(pos, state.getBlock());

        if (state.getValue(OMNIDIRECTIONAL)) return;

        Direction arrowTail = arrowTailSide(state);
        BlockPos outputPos = pos.relative(arrowHeadSide(state));
        world.neighborChanged(outputPos, state.getBlock(), pos);
        world.updateNeighborsAtExceptFromFacing(outputPos, state.getBlock(), arrowTail);
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world instanceof ServerLevel serverWorld && world.getBlockEntity(pos) instanceof RedstoneSignalDelayBlockEntity delay) {
            delay.captureCurrentInput(serverWorld, state);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer && world.getBlockEntity(pos) instanceof RedstoneSignalDelayBlockEntity delay) {
            serverPlayer.openMenu(delay);
        }
        return InteractionResult.SUCCESS;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, OMNIDIRECTIONAL, FACING);
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
}
