package cn.quit5700.buildingwand.logic;

public final class MoveWandFlow {
    private MoveWandFlow() {}

    public static LeftAction leftAction(Stage stage, boolean hasServerMemory, boolean hasProjection) {
        if (hasServerMemory && hasProjection && stage == Stage.MOVE_PREVIEW) {
            return LeftAction.EXECUTE_MOVE;
        }
        return switch (stage) {
            case IDLE -> LeftAction.RECORD_FIRST;
            case FIRST_POINT -> LeftAction.CAPTURE_SELECTION;
            case SELECTION_ADJUSTING -> LeftAction.CAPTURE_SELECTION;
            default -> LeftAction.NONE;
        };
    }

    public enum Stage {
        IDLE,
        FIRST_POINT,
        SELECTION_ADJUSTING,
        CAPTURE_PENDING,
        RECORDED,
        MOVE_PREVIEW,
        EXECUTE_PENDING
    }

    public enum LeftAction {
        RECORD_FIRST,
        CAPTURE_SELECTION,
        EXECUTE_MOVE,
        NONE
    }
}
