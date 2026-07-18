package cn.quit5700.pathfindingbeacon.block;

import cn.quit5700.pathfindingbeacon.route.WorldRouteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public final class PathfindingBlock extends Block {
    private final int number;

    public PathfindingBlock(int number, ResourceKey<Block> key) {
        super(BlockBehaviour.Properties.ofLegacyCopy(Blocks.STONE)
                .lightLevel(state -> 15).strength(1.5F, 6.0F).requiresCorrectToolForDrops()
                .pushReaction(PushReaction.BLOCK));
        this.number = number;
    }

    public int number() { return number; }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getLevel() instanceof ServerLevel world
                && WorldRouteManager.hasNumberAtColumn(world, number, context.getClickedPos().getX(), context.getClickedPos().getZ())) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.literal("已有同号码方块，不得放置").withStyle(style -> style.withColor(0xFF5555)), true);
            }
            return null;
        }
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (world instanceof ServerLevel serverWorld && placer instanceof Player player) {
            WorldRouteManager.place(serverWorld, number, player.getUUID(), pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel world) WorldRouteManager.remove(world, pos);
        super.onRemove(state, level, pos, newState, moved);
    }
}
