package cn.quit5700.buildingwand.block;

import cn.quit5700.buildingwand.config.WandSettingsState;
import cn.quit5700.buildingwand.network.WandNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WandSettingsStationBlock extends Block {
    public WandSettingsStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return handleUse(level, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = handleUse(level, player);
        return result == InteractionResult.PASS
                ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    private static InteractionResult handleUse(Level level, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        MinecraftServer server = serverPlayer.level().getServer();
        if (server == null) {
            return InteractionResult.SUCCESS;
        }
        WandSettingsState settings = WandSettingsState.get(server);
        ServerPlayNetworking.send(serverPlayer, new WandNetworking.OpenSettingsPayload(
                settings.planeLimit(),
                settings.cubeLimit(),
                settings.moveLimit(),
                canEdit(serverPlayer)
        ));
        return InteractionResult.SUCCESS;
    }

    public static boolean canEdit(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        return server != null && (server.isSingleplayer() || server.getPlayerList().isOp(player.getGameProfile()));
    }
}
