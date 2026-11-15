package cn.quit5700.buildingwand.util;

import net.minecraft.core.BlockPos;

import java.util.List;

public final class ShapeBuilder {
    private ShapeBuilder() {
    }

    public static List<BlockPos> between(BlockPos first, BlockPos second, ShapeMode mode) {
        return ShapeCoordinates.between(
                first.getX(), first.getY(), first.getZ(),
                second.getX(), second.getY(), second.getZ(),
                mode
        ).stream().map(pos -> new BlockPos(pos.x(), pos.y(), pos.z())).toList();
    }

    public static int dimensions(BlockPos first, BlockPos second) {
        return ShapeCoordinates.dimensions(
                first.getX(), first.getY(), first.getZ(),
                second.getX(), second.getY(), second.getZ()
        );
    }
}
