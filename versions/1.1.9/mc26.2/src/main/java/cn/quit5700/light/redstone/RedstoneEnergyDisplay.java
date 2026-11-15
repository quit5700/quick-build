package cn.quit5700.light.redstone;

import java.util.List;
import net.minecraft.network.chat.Component;

public final class RedstoneEnergyDisplay {
    private RedstoneEnergyDisplay() {}

    public static List<Component> formatEmitterLines(RedstoneEnergyState.NetworkInfo info) {
        return List.of(
                Component.translatable("display.quick_build.network_id", info.networkId()),
                Component.translatable("display.quick_build.network_nodes", info.nodes()),
                Component.translatable("display.quick_build.network_counts", info.switches(), info.sensors(), info.emitters()),
                Component.translatable("display.quick_build.network_status",
                        Component.translatable(info.powered() ? "text.quick_build.177" : "text.quick_build.142")),
                Component.translatable("display.quick_build.output_signal", info.powered() ? 15 : 0),
                Component.translatable("display.quick_build.network_color", colorName(info.color()))
        );
    }

    public static Component colorName(EnergyColor color) {
        return Component.translatable(switch (color) {
            case NONE -> "text.quick_build.141"; case WHITE -> "text.quick_build.030"; case ORANGE -> "text.quick_build.039"; case MAGENTA -> "text.quick_build.103";
            case LIGHT_BLUE -> "text.quick_build.042"; case YELLOW -> "text.quick_build.068"; case LIME -> "text.quick_build.067"; case PINK -> "text.quick_build.051";
            case GRAY -> "text.quick_build.069"; case LIGHT_GRAY -> "text.quick_build.041"; case CYAN -> "text.quick_build.108"; case PURPLE -> "text.quick_build.189";
            case BLUE -> "text.quick_build.081"; case BROWN -> "text.quick_build.190"; case GREEN -> "text.quick_build.093"; case RED -> "text.quick_build.061"; case BLACK -> "text.quick_build.060";
        });
    }
}
