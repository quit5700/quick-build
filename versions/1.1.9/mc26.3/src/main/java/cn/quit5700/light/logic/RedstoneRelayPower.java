package cn.quit5700.light.logic;

public final class RedstoneRelayPower {
    private RedstoneRelayPower() {}

    public static int calculate(
            boolean entityDetected,
            int directSignal,
            int neighbouringWirePower,
            int currentOutput
    ) {
        if (entityDetected) return 15;
        int direct = clamp(directSignal);
        int wireInput = clamp(neighbouringWirePower);
        int wire = wireInput > clamp(currentOutput) ? wireInput - 1 : 0;
        return Math.max(direct, wire);
    }

    private static int clamp(int power) {
        return Math.max(0, Math.min(15, power));
    }
}
