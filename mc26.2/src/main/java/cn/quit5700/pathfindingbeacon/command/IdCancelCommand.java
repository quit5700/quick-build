package cn.quit5700.pathfindingbeacon.command;

import cn.quit5700.pathfindingbeacon.route.WorldRouteManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class IdCancelCommand {
    private static final String COMMAND = "pfcancel";

    private IdCancelCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal(COMMAND)
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .then(argument("number", IntegerArgumentType.integer(1, 30))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int number = IntegerArgumentType.getInteger(context, "number");
                                    int removed = WorldRouteManager.clearColor(player.level(), number);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("已删除本维度" + number + "号寻路方块：" + removed + "个"),
                                            false
                                    );
                                    return removed;
                                }))));
    }
}
