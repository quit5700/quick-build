package cn.quit5700.light.logic;

import java.util.ArrayDeque;
import java.util.List;

public final class DelayedSignalState {
    public static final int MAX_DELAY_TICKS = 72_000;

    private final ArrayDeque<PendingChange> pending = new ArrayDeque<>();
    private int delayTicks;
    private boolean inputPowered;
    private boolean outputPowered;

    public DelayedSignalState(int delayTicks, boolean inputPowered, boolean outputPowered) {
        this(delayTicks, inputPowered, outputPowered, List.of());
    }

    public DelayedSignalState(
            int delayTicks,
            boolean inputPowered,
            boolean outputPowered,
            List<PendingChange> pendingChanges
    ) {
        setDelayTicks(delayTicks);
        this.inputPowered = inputPowered;
        this.outputPowered = outputPowered;
        this.pending.addAll(pendingChanges);
    }

    public boolean capture(long gameTime, boolean input) {
        if (input == inputPowered) return false;
        inputPowered = input;
        pending.addLast(new PendingChange(saturatingAdd(gameTime, delayTicks), input));
        return true;
    }

    public boolean advance(long gameTime) {
        boolean changed = false;
        while (!pending.isEmpty() && pending.peekFirst().time() <= gameTime) {
            boolean next = pending.removeFirst().powered();
            if (outputPowered != next) {
                outputPowered = next;
                changed = true;
            }
        }
        return changed;
    }

    public void setDelayTicks(int ticks) {
        delayTicks = Math.max(0, Math.min(MAX_DELAY_TICKS, ticks));
    }

    public void reconfigureDelay(long gameTime, int ticks) {
        int previousDelay = delayTicks;
        setDelayTicks(ticks);
        if (delayTicks == previousDelay) return;

        if (pending.isEmpty()) {
            if (inputPowered != outputPowered) {
                pending.addLast(new PendingChange(saturatingAdd(gameTime, delayTicks), inputPowered));
            }
            return;
        }

        List<PendingChange> previous = List.copyOf(pending);
        pending.clear();
        long oldFirstTime = previous.getFirst().time();
        long newFirstTime = saturatingAdd(gameTime, delayTicks);
        long lastTime = newFirstTime;
        for (PendingChange change : previous) {
            long spacing = nonNegativeDistance(oldFirstTime, change.time());
            long newTime = Math.max(lastTime, saturatingAdd(newFirstTime, spacing));
            pending.addLast(new PendingChange(newTime, change.powered()));
            lastTime = newTime;
        }
    }

    private static long nonNegativeDistance(long start, long end) {
        if (end <= start) return 0;
        try {
            return Math.subtractExact(end, start);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private static long saturatingAdd(long value, long increment) {
        try {
            return Math.addExact(value, increment);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    public int delayTicks() { return delayTicks; }
    public boolean inputPowered() { return inputPowered; }
    public boolean outputPowered() { return outputPowered; }
    public boolean hasPendingChanges() { return !pending.isEmpty(); }
    public List<PendingChange> pendingChanges() { return List.copyOf(pending); }

    public record PendingChange(long time, boolean powered) {}
}
