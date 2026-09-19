package cn.quit5700.light.block;

import cn.quit5700.light.redstone.EnergyColor;
import cn.quit5700.light.redstone.EnergyNodeType;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import cn.quit5700.light.registry.LightBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public final class RedstoneEnergySensorBlock extends BaseEntityBlock {
    public static final MapCodec<RedstoneEnergySensorBlock> CODEC = simpleCodec(RedstoneEnergySensorBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public RedstoneEnergySensorBlock(ResourceKey<Block> key) {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER).strength(2.0F));
    }

    private RedstoneEnergySensorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false)
                .setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RedstoneEnergySensorBlockEntity(pos, state); }
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? null : createTickerHelper(type, LightBlockEntities.REDSTONE_ENERGY_SENSOR,
                (tickWorld, pos, tickState, blockEntity) -> RedstoneEnergySensorBlockEntity.tick((ServerLevel) tickWorld, pos, tickState, blockEntity));
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected boolean isSignalSource(BlockState state) { return true; }
    @Override protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof RedstoneEnergySensorBlockEntity sensor) {
            return sensor.outputSignalStrength();
        }
        return state.getValue(POWERED) ? 15 : 0;
    }
    @Override protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getSignal(state, world, pos, direction);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (world instanceof ServerLevel serverWorld) {
            RedstoneEnergyState.get(serverWorld).register(serverWorld, pos, EnergyNodeType.SENSOR, EnergyColor.NONE, false);
            RedstoneEnergySensorBlockEntity.refreshNearby(serverWorld, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel world) {
            RedstoneEnergyState.get(world).remove(world, pos);
            RedstoneEnergySensorBlockEntity.refreshNearby(world, pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }
}
