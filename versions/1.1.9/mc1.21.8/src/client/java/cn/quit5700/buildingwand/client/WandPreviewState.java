package cn.quit5700.buildingwand.client;

import net.minecraft.core.BlockPos;

import java.util.List;

public final class WandPreviewState {
    private static List<BlockPos> positions = List.of();
    private static PreviewColor color = PreviewColor.WHITE;

    private WandPreviewState() {
    }

    public static void set(List<BlockPos> newPositions, PreviewColor newColor) {
        positions = List.copyOf(newPositions);
        color = newColor;
    }

    public static void clear() {
        positions = List.of();
        color = PreviewColor.WHITE;
    }

    public static List<BlockPos> positions() {
        return positions;
    }

    public static PreviewColor color() {
        return color;
    }

    public enum PreviewColor {
        WHITE(255, 255, 255, 95),
        BLUE(80, 170, 255, 105),
        RED(255, 45, 45, 120);

        public final int red;
        public final int green;
        public final int blue;
        public final int alpha;

        PreviewColor(int red, int green, int blue, int alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }
}
