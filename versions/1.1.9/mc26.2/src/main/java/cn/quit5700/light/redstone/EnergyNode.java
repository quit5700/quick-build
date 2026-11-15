package cn.quit5700.light.redstone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record EnergyNode(String world, BlockPos pos, EnergyNodeType type, EnergyColor color, boolean powered) {
    public static final Codec<EnergyNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("world").forGetter(EnergyNode::world),
            BlockPos.CODEC.fieldOf("pos").forGetter(EnergyNode::pos),
            Codec.STRING.fieldOf("type").forGetter(node -> node.type.name()),
            Codec.STRING.optionalFieldOf("color", "none").forGetter(node -> node.color.getSerializedName()),
            Codec.BOOL.optionalFieldOf("powered", false).forGetter(EnergyNode::powered)
    ).apply(instance, (world, pos, type, color, powered) -> new EnergyNode(
            world, pos.immutable(), EnergyNodeType.valueOf(type), parseColor(color), powered)));

    private static EnergyColor parseColor(String value) {
        for (EnergyColor color : EnergyColor.values()) {
            if (color.getSerializedName().equals(value)) return color;
        }
        return EnergyColor.NONE;
    }

    public String key() {
        return key(world, pos);
    }

    public static String key(String world, BlockPos pos) {
        return world + "|" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public EnergyNode withPowered(boolean value) {
        return new EnergyNode(world, pos, type, color, value);
    }
}
