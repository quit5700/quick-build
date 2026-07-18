package cn.quit5700.light.block;

import cn.quit5700.light.redstone.RedstoneEnergyDisplay;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import cn.quit5700.light.redstone.EnergyColor;
import cn.quit5700.light.redstone.EnergyNodeType;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import cn.quit5700.light.registry.LightBlockEntities;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class RedstoneEnergyEmitterBlock extends BaseEntityBlock {
    public static final MapCodec<RedstoneEnergyEmitterBlock> CODEC = simpleCodec(properties -> new RedstoneEnergyEmitterBlock(properties));
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
    public static final EnumProperty<EnergyColor> COLOR = EnumProperty.create("color", EnergyColor.class);

    public RedstoneEnergyEmitterBlock(ResourceKey<Block> key) {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_BLOCK)
                .setId(key)
                .strength(5.0F, 6.0F)
                .pushReaction(PushReaction.BLOCK));
    }

    private RedstoneEnergyEmitterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ENABLED, false).setValue(COLOR, EnergyColor.NONE));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneEnergyEmitterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide() ? null : createTickerHelper(type, LightBlockEntities.REDSTONE_ENERGY_EMITTER,
                (tickWorld, pos, tickState, blockEntity) -> RedstoneEnergyEmitterBlockEntity.tick((ServerLevel) tickWorld, pos, tickState, blockEntity));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (world instanceof ServerLevel serverWorld) {
            RedstoneEnergyState.get(serverWorld).register(serverWorld, pos, EnergyNodeType.EMITTER, EnergyColor.NONE, false);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getPower(world, pos);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getPower(world, pos);
    }

    private int getPower(BlockGetter world, BlockPos pos) {
        return world.getBlockState(pos).getValue(ENABLED) ? 15 : 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(world instanceof ServerLevel serverWorld)) {
            return InteractionResult.SUCCESS;
        }
        RedstoneEnergyDisplay.formatEmitterLines(RedstoneEnergyState.get(serverWorld).info(serverWorld, pos))
                .forEach(line -> player.displayClientMessage(Component.literal(line), false));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED, COLOR);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (!world.getBlockState(pos).is(this)) RedstoneEnergyState.get(world).remove(world, pos);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }
}
