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
            player.sendOverlayMessage(Component.literal("该方块未参与线路，无法重排").withStyle(style -> style.withColor(0xFF5555)));
            return InteractionResult.FAIL;
        }

        Selection first = SELECTIONS.get(playerId);
        Identifier worldKey = world.dimension().identifier();
        if (first == null || !first.worldKey().equals(worldKey) || first.number() != block.number()) {
            SELECTIONS.put(playerId, new Selection(worldKey, block.number(), position));
            player.sendOverlayMessage(Component.literal("已选择第一个" + block.number() + "号寻路方块"));
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
            case RECONNECTED -> Component.literal("两段线路已连接");
            case REORDERED -> Component.literal("线路顺序已重排");
            case DENIED -> Component.literal("其他玩家已使用这个颜色，该操作没有作用").withStyle(style -> style.withColor(0xFF5555));
            case INVALID -> Component.literal("请选择两个不同的同号有效方块").withStyle(style -> style.withColor(0xFF5555));
        };
        player.sendOverlayMessage(message);
        return status == ReorderStatus.DENIED || status == ReorderStatus.INVALID
                ? InteractionResult.FAIL
                : InteractionResult.SUCCESS;
    }

    private record Selection(Identifier worldKey, int number, RoutePosition position) {
    }
}
