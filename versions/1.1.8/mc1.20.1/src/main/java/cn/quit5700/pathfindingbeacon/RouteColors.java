package cn.quit5700.pathfindingbeacon;

import java.util.List;

public final class RouteColors {
    private static final List<Integer> RGB = List.of(
            0xFFFFFF, 0xE53935, 0x1E88E5, 0x43A047, 0xFDD835,
            0x8E24AA, 0x00ACC1, 0xFB8C00, 0x6D4C41, 0x546E7A,
            0xD81B60, 0x3949AB, 0x00897B, 0x7CB342, 0xF4511E,
            0x5E35B1, 0x039BE5, 0xC0CA33, 0xFFB300, 0x757575,
            0xAD1457, 0x283593, 0x00695C, 0x558B2F, 0xEF6C00,
            0x4527A0, 0x0277BD, 0x9E9D24, 0xFF8F00, 0x3E2723
    );

    private RouteColors() {
    }

    public static int size() {
        return RGB.size();
    }

    public static int rgb(int number) {
        if (number < 1 || number > RGB.size()) {
            throw new IllegalArgumentException("Route number must be between 1 and 30");
        }
        return RGB.get(number - 1);
    }
}

