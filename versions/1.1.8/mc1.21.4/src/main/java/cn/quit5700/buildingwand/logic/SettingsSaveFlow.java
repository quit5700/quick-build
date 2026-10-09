package cn.quit5700.buildingwand.logic;

public final class SettingsSaveFlow {
    private boolean pending;

    public Action request() {
        if (pending) {
            return Action.NONE;
        }
        pending = true;
        return Action.SEND;
    }

    public void complete() {
        pending = false;
    }

    public boolean pending() {
        return pending;
    }

    public enum Action {
        SEND,
        NONE
    }
}
