package cn.quit5700.light.logic;

import java.util.Objects;
import java.util.function.UnaryOperator;

public final class RedstoneDelayFacePolicy {
    private RedstoneDelayFacePolicy() {}

    public static <T> boolean acceptsInputFrom(T physicalSide, T arrowTailSide) {
        return Objects.equals(physicalSide, arrowTailSide);
    }

    public static <T> boolean emitsToPhysicalSide(
            boolean fiveFaceMode,
            T physicalSide,
            T arrowTailSide,
            UnaryOperator<T> opposite
    ) {
        if (fiveFaceMode) {
            return !Objects.equals(physicalSide, arrowTailSide);
        }
        return Objects.equals(physicalSide, opposite.apply(arrowTailSide));
    }

    public static <T> boolean emitsForQueryDirection(
            boolean fiveFaceMode,
            T queriedDirection,
            T arrowTailSide,
            UnaryOperator<T> opposite
    ) {
        T physicalReceiverSide = opposite.apply(queriedDirection);
        return emitsToPhysicalSide(fiveFaceMode, physicalReceiverSide, arrowTailSide, opposite);
    }
}
