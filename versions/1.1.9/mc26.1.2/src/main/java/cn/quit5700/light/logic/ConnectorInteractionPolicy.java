package cn.quit5700.light.logic;

public final class ConnectorInteractionPolicy {
    private ConnectorInteractionPolicy() {}

    public static boolean shouldHandle(boolean shiftDown) {
        return shiftDown;
    }
}
