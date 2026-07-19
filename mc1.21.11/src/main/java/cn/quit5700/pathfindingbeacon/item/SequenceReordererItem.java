package cn.quit5700.pathfindingbeacon.item;

import cn.quit5700.pathfindingbeacon.block.PathfindingBlock;
import cn.quit5700.pathfindingbeacon.route.ReorderStatus;
import cn.quit5700.pathfindingbeacon.route.RoutePosition;
import cn.quit5700.pathfindingbeacon.route.WorldRouteManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SequenceReordererItem extends Item {
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();

    public SequenceReordererItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity entity) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel world) || context.getPlayer() == null) {
            return InteractionResult.SUCCESS;
        }
        if (!(world.getBlockState(context.getClickedPos()).getBlock() instanceof PathfindingBlock block)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        UUID playerId = player.getUUID();
        RoutePosition position = WorldRouteManager.toRoutePosition(context.getClickedPos());
        if (!WorldRouteManager.isActive(world, position)) {
            player.displayClientMessage(Component.translatable("text.quick_build.053").withStyle(style -> style.withColor(0xFF5555)), true);
            return InteractionResult.FAIL;
        }

        Selection first = SELECTIONS.get(playerId);
        Identifier worldKey = world.dimension().identifier();
        if (first == null || !first.worldKey().equals(worldKey) || first.number() != block.number()) {
            SELECTIONS.put(playerId, new Selection(worldKey, block.number(), position));
            player.displayClientMessage(Component.translatable("message.quick_build.route_first_selected", block.number()), true);
            return InteractionResult.SUCCESS;
        }

        SELECTIONS.remove(playerId);
        ReorderStatus status = WorldRouteManager.reorder(
                world,
                block.number(),
                playerId,
                first.position(),
                position,
                player.isCreative()
        );
        Component message = switch (status) {
            case RECONNECTED -> Component.translatable("text.quick_build.091");
            case REORDERED -> Component.translatable("text.quick_build.147");
            case DENIED -> Component.translatable("text.quick_build.106").withStyle(style -> style.withColor(0xFF5555));
            case INVALID -> Component.translatable("text.quick_build.111").withStyle(style -> style.withColor(0xFF5555));
        };
        player.displayClientMessage(message, true);
        return status == ReorderStatus.DENIED || status == ReorderStatus.INVALID
                ? InteractionResult.FAIL
                : InteractionResult.SUCCESS;
    }

    private record Selection(Identifier worldKey, int number, RoutePosition position) {
    }
}
