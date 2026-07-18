package cn.quit5700.pathfindingbeacon.event;

import cn.quit5700.pathfindingbeacon.PathfindingBeaconMod;
import cn.quit5700.pathfindingbeacon.network.RouteNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class PlayerEvents {
    private static int ticks;

    private PlayerEvents() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            handler.player.displayClientMessage(Component.literal("寻路器取消指令 /pfcancel <1-30>"), false);
            RouteNetworking.syncPlayer(handler.player);
        });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                RouteNetworking.syncPlayer(player));
        ServerTickEvents.END_SERVER_TICK.register(PlayerEvents::unlockRecipesForPickaxeOwners);
    }

    private static void unlockRecipesForPickaxeOwners(MinecraftServer server) {
        if (++ticks % 20 != 0) {
            return;
        }
        List<net.minecraft.resources.ResourceLocation> ids = new ArrayList<>();
        ids.add(PathfindingBeaconMod.id("route_block_1"));
        for (int i = 2; i <= 30; i++) {
            ids.add(PathfindingBeaconMod.id("route_block_" + i));
        }
        ids.add(PathfindingBeaconMod.id("id_sequence_reorderer"));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean hasPickaxe = player.getInventory().items.stream().anyMatch(PlayerEvents::isPickaxe);
            if (hasPickaxe) {
                player.awardRecipesByKey(ids.toArray(net.minecraft.resources.ResourceLocation[]::new));
            }
        }
    }

    private static boolean isPickaxe(ItemStack stack) {
        return stack.is(holder -> holder.is(ItemTags.PICKAXES));
    }

}
