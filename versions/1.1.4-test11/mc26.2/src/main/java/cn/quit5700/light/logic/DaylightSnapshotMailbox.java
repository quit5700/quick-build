package cn.quit5700.light.logic;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class DaylightSnapshotMailbox<T> {
    private final AtomicReference<List<T>> pending = new AtomicReference<>();

    public void offer(List<T> snapshot) {
        pending.set(List.copyOf(snapshot));
    }

    public List<T> drain() {
        return pending.getAndSet(null);
    }
}
