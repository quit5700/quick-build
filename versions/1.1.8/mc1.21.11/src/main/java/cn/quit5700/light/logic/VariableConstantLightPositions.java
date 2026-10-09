package cn.quit5700.light.logic;

import java.util.AbstractList;
import java.util.List;
import java.util.TreeSet;

public final class VariableConstantLightPositions {
    private VariableConstantLightPositions() {
    }

    public static List<Offset> relativeOffsets(int requestedRadius, int requestedSpacing) {
        int radius = VariableConstantLightSettings.clampRadius(requestedRadius);
        int spacing = VariableConstantLightSettings.clampSpacing(requestedSpacing);
        return createOffsets(radius, spacing);
    }

    public static boolean containsOffset(int requestedRadius, int requestedSpacing,
                                         int dx, int dy, int dz) {
        int radius = VariableConstantLightSettings.clampRadius(requestedRadius);
        int spacing = VariableConstantLightSettings.clampSpacing(requestedSpacing);
        if (dx == 0 && dy == 0 && dz == 0) return false;
        if (Math.abs(dx) > radius || Math.abs(dy) > radius || Math.abs(dz) > radius) return false;
        List<Integer> axes = axisCoordinates(radius, spacing);
        return axes.contains(dx) && axes.contains(dy) && axes.contains(dz);
    }

    private static List<Offset> createOffsets(int radius, int spacing) {
        List<Integer> axes = axisCoordinates(radius, spacing);
        int axisSize = axes.size();
        int centerIndex = axes.indexOf(0);
        int omittedIndex = (centerIndex * axisSize + centerIndex) * axisSize + centerIndex;
        int fullSize = axisSize * axisSize * axisSize;
        return new AbstractList<>() {
            @Override
            public Offset get(int index) {
                if (index < 0 || index >= size()) throw new IndexOutOfBoundsException(index);
                int fullIndex = index >= omittedIndex ? index + 1 : index;
                int dxIndex = fullIndex / (axisSize * axisSize);
                int remainder = fullIndex % (axisSize * axisSize);
                int dyIndex = remainder / axisSize;
                int dzIndex = remainder % axisSize;
                return new Offset(axes.get(dxIndex), axes.get(dyIndex), axes.get(dzIndex));
            }

            @Override
            public int size() {
                return fullSize - 1;
            }
        };
    }

    private static List<Integer> axisCoordinates(int radius, int spacing) {
        TreeSet<Integer> coordinates = new TreeSet<>();
        coordinates.add(-radius);
        coordinates.add(0);
        coordinates.add(radius);
        for (int coordinate = -radius; coordinate <= radius; coordinate += spacing) {
            coordinates.add(coordinate);
        }
        return List.copyOf(coordinates);
    }

    public record Offset(int dx, int dy, int dz) {
    }
}
