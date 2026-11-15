package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DelayedSignalStateTest {
    @Test
    void preservesBothEdgesOfAShortPulse() {
        DelayedSignalState state = new DelayedSignalState(6, false, false);

        state.capture(100, true);
        state.capture(103, false);

        assertFalse(state.advance(105));
        assertFalse(state.outputPowered());
        assertTrue(state.advance(106));
        assertTrue(state.outputPowered());
        assertTrue(state.advance(109));
        assertFalse(state.outputPowered());
    }

    @Test
    void zeroDelayAppliesOnTheSameTick() {
        DelayedSignalState state = new DelayedSignalState(0, false, false);

        state.capture(20, true);

        assertTrue(state.advance(20));
        assertTrue(state.outputPowered());
    }

    @Test
    void clampsDelayAndRestoresPendingChanges() {
        DelayedSignalState original = new DelayedSignalState(20, false, false);
        original.setDelayTicks(100_000);
        original.capture(50, true);

        DelayedSignalState restored = new DelayedSignalState(
                original.delayTicks(), original.inputPowered(), original.outputPowered(), original.pendingChanges());

        assertEquals(72_000, restored.delayTicks());
        assertFalse(restored.advance(72_049));
        assertTrue(restored.advance(72_050));
        assertTrue(restored.outputPowered());
    }

    @Test
    void changingDelayReschedulesTheCurrentInputFromNow() {
        DelayedSignalState state = new DelayedSignalState(20, false, false);
        state.capture(100, true);

        state.reconfigureDelay(105, 200);

        assertFalse(state.advance(299));
        assertTrue(state.advance(305));
        assertTrue(state.outputPowered());
    }

    @Test
    void changingDelayPreservesEveryEdgeAndTheLengthOfAShortPulse() {
        DelayedSignalState state = new DelayedSignalState(20, false, false);
        state.capture(100, true);
        state.capture(103, false);

        state.reconfigureDelay(105, 200);

        assertFalse(state.advance(304));
        assertTrue(state.advance(305));
        assertTrue(state.outputPowered());
        assertTrue(state.advance(308));
        assertFalse(state.outputPowered());
    }

    @Test
    void schedulingNearTheLongTimeLimitDoesNotOverflow() {
        DelayedSignalState state = new DelayedSignalState(20, false, false);

        state.capture(Long.MAX_VALUE - 10, true);

        assertFalse(state.advance(Long.MAX_VALUE - 1));
        assertTrue(state.advance(Long.MAX_VALUE));
        assertTrue(state.outputPowered());
    }

    @Test
    void reportsPendingEdgesUntilTheyAreApplied() {
        DelayedSignalState state = new DelayedSignalState(20, false, false);

        state.capture(100, true);
        assertTrue(state.hasPendingChanges());

        state.advance(120);
        assertFalse(state.hasPendingChanges());
    }
}
