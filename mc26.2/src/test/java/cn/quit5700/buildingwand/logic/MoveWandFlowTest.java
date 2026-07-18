package cn.quit5700.buildingwand.logic;

import org.junit.jupiter.api.Test;

import static cn.quit5700.buildingwand.logic.MoveWandFlow.LeftAction;
import static cn.quit5700.buildingwand.logic.MoveWandFlow.Stage;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveWandFlowTest {
    @Test
    void secondPointImmediatelyCapturesTheOriginalRegion() {
        assertEquals(LeftAction.RECORD_FIRST, MoveWandFlow.leftAction(Stage.IDLE, false, false));
        assertEquals(LeftAction.CAPTURE_SELECTION,
                MoveWandFlow.leftAction(Stage.FIRST_POINT, false, false));
    }

    @Test
    void failedCaptureCanBeRetriedFromFrozenSelection() {
        assertEquals(LeftAction.CAPTURE_SELECTION,
                MoveWandFlow.leftAction(Stage.SELECTION_ADJUSTING, false, false));
    }

    @Test
    void projectionLeftClickCanOnlyExecuteMove() {
        assertEquals(LeftAction.EXECUTE_MOVE,
                MoveWandFlow.leftAction(Stage.MOVE_PREVIEW, true, true));
    }

    @Test
    void waitingAndRecordedStagesIgnoreLeftClick() {
        assertEquals(LeftAction.NONE, MoveWandFlow.leftAction(Stage.CAPTURE_PENDING, false, false));
        assertEquals(LeftAction.NONE, MoveWandFlow.leftAction(Stage.RECORDED, true, false));
        assertEquals(LeftAction.NONE, MoveWandFlow.leftAction(Stage.EXECUTE_PENDING, true, true));
    }
}
