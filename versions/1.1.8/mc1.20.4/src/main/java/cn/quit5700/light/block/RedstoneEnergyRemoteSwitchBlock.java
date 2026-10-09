package cn.quit5700.light.block;

import cn.quit5700.light.redstone.EnergyColor;
import cn.quit5700.light.redstone.EnergyNodeType;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RedstoneEnergyRemoteSwitchBlock extends Block {
    private static final int USE_COOLDOWN_TICKS = 4;
    private static final Map<UUID, Long> LAST_USE_TICKS = new HashMap<>();
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private final EnergyColor color;

    public RedstoneEnergyRemoteSwitchBlock(ResourceKey<Block> key, EnergyColor color) {
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER).strength(0.5F).pushReaction(PushReaction.BLOCK));
        this.color = color;
        registerDefaultState(stateDefinition.any().setValue(POWERED, false)
                .setValue(FACE, AttachFace.FLOOR).setValue(FACING, Direction.NORTH));
    }

    public EnergyColor color() { return color; }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        AttachFace face = side == Direction.UP ? AttachFace.FLOOR
                : side == Direction.DOWN ? AttachFace.CEILING : AttachFace.WALL;
        Direction facing = face == AttachFace.WALL ? side : context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACE, face).setValue(FACING, facing).setValue(POWERED, false);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (world instanceof ServerLevel serverWorld) {
            RedstoneEnergyState.get(serverWorld).register(serverWorld, pos, EnergyNodeType.SWITCH, color, state.getValue(POWERED));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!(world instanceof ServerLevel serverWorld)) return InteractionResult.SUCCESS;
        long time = serverWorld.getGameTime();
        long last = LAST_USE_TICKS.getOrDefault(player.getUUID(), time - USE_COOLDOWN_TICKS);
        if (time - last < USE_COOLDOWN_TICKS) return InteractionResult.SUCCESS;
        LAST_USE_TICKS.put(player.getUUID(), time);
        boolean powered = !state.getValue(POWERED);
        RedstoneEnergyState energy = RedstoneEnergyState.get(serverWorld);
        if (energy.getNode(serverWorld, pos).isEmpty()) energy.register(serverWorld, pos, EnergyNodeType.SWITCH, color, powered);
        energy.setSwitchNetworkPowered(serverWorld, pos, powered);
        world.setBlock(pos, state.setValue(POWERED, powered), UPDATE_ALL);
        world.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, powered ? 0.6F : 0.5F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel world) RedstoneEnergyState.get(world).remove(world, pos);
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(POWERED, FACE, FACING); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
}
