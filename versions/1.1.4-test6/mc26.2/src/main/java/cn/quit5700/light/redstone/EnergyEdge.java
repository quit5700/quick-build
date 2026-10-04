package cn.quit5700.light.redstone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnergyEdge(String first, String second) {
    public static final Codec<EnergyEdge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("first").forGetter(EnergyEdge::first),
            Codec.STRING.fieldOf("second").forGetter(EnergyEdge::second)
    ).apply(instance, EnergyEdge::new));

    public EnergyEdge {
        if (first.compareTo(second) > 0) {
            String swap = first;
            first = second;
            second = swap;
        }
    }

    public boolean contains(String key) {
        return first.equals(key) || second.equals(key);
    }

    public String other(String key) {
        return first.equals(key) ? second : first;
    }
}
