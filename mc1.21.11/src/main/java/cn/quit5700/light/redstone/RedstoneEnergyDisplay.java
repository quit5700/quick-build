package cn.quit5700.light.redstone;

import java.util.List;

public final class RedstoneEnergyDisplay {
    private RedstoneEnergyDisplay() {}

    public static List<String> formatEmitterLines(RedstoneEnergyState.NetworkInfo info) {
        return List.of(
                "网络编号：" + info.networkId(),
                "网络节点：" + info.nodes(),
                "开关：" + info.switches() + "，感应器：" + info.sensors() + "，发射器：" + info.emitters(),
                "网络状态：" + (info.powered() ? "有信号" : "无信号"),
                "输出信号：" + (info.powered() ? 15 : 0),
                "网络颜色：" + colorName(info.color())
        );
    }

    public static String colorName(EnergyColor color) {
        return switch (color) {
            case NONE -> "未连接色"; case WHITE -> "白色"; case ORANGE -> "橙色"; case MAGENTA -> "品红色";
            case LIGHT_BLUE -> "淡蓝色"; case YELLOW -> "黄色"; case LIME -> "黄绿色"; case PINK -> "粉红色";
            case GRAY -> "灰色"; case LIGHT_GRAY -> "淡灰色"; case CYAN -> "青色"; case PURPLE -> "紫色";
            case BLUE -> "蓝色"; case BROWN -> "棕色"; case GREEN -> "绿色"; case RED -> "红色"; case BLACK -> "黑色";
        };
    }
}
