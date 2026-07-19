package cn.quit5700.buildingwand.client;

import cn.quit5700.buildingwand.network.WandNetworking;
import cn.quit5700.buildingwand.logic.MoveWandFlow;
import cn.quit5700.buildingwand.logic.MoveWandFlow.Stage;
import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.util.ShapeBuilder;
import cn.quit5700.buildingwand.util.ShapeMode;
import cn.quit5700.buildingwand.util.WandStackData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientWandController {
    private static final double MAX_DISTANCE = 4.0D;
    private static final double AIR_PREVIEW_DISTANCE = 4.0D;

    private static boolean wasAttacking;
    private static boolean wasUsing;
    private static boolean wasUp;
    private static boolean wasDown;
    private static boolean wasLeft;
    private static boolean wasRight;

    private static BuildingDrawing buildingDrawing;
    private static BlockPos buildingPointFirst;
    private static ShapeMode buildingPointMode;
    private static PendingShape pendingBuilding;

    private static Stage moveStage = Stage.IDLE;
    private static BlockPos moveFirst;
    private static BlockPos moveSecond;
    private static List<BlockPos> moveSource = List.of();
    private static List<BlockPos> moveVisibleSource = List.of();
    private static PendingMove pendingMove;
    private static BlockPos queuedMovePreviewTarget;
    private static boolean hasMoveMemory;
    private static boolean wasHoldingMoveWand;
    private static boolean moveMemoryQueryPending;

    private ClientWandController() {
    }

    public static void register() {
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
            ItemStack stack = player.getMainHandItem();
            return stack.is(ModItems.BUILDING_WAND) || stack.is(ModItems.MOVE_WAND);
        });
        ClientTickEvents.END_CLIENT_TICK.register(ClientWandController::tick);
    }

    public static void onMoveSelectionResult(boolean accepted) {
        if (moveStage != Stage.CAPTURE_PENDING) {
            return;
        }
        if (!accepted) {
            hasMoveMemory = false;
            moveStage = Stage.SELECTION_ADJUSTING;
            refreshMoveSelectionPreview();
            return;
        }
        rebuildMoveSource();
        hasMoveMemory = true;
        startMovePreview(moveFirst);
        if (queuedMovePreviewTarget != null) {
            startMovePreview(queuedMovePreviewTarget);
            queuedMovePreviewTarget = null;
        }
    }

    public static void onMoveExecutionResult(boolean completed) {
        if (moveStage != Stage.EXECUTE_PENDING) {
            return;
        }
        if (completed) {
            resetMoveClientState();
            return;
        }
        moveStage = Stage.MOVE_PREVIEW;
        refreshMovePreview();
    }

    public static void onMoveMemory(boolean available, BlockPos first, BlockPos second) {
        Minecraft client = Minecraft.getInstance();
        moveMemoryQueryPending = false;
        if (moveStage != Stage.IDLE && moveStage != Stage.RECORDED) {
            return;
        }
        if (!available) {
            resetMoveClientState();
            return;
        }
        moveFirst = first.immutable();
        moveSecond = second.immutable();
        rebuildMoveSource();
        hasMoveMemory = true;
        pendingMove = null;
        queuedMovePreviewTarget = null;
        moveStage = Stage.RECORDED;
        WandPreviewState.clear();
        if (client.player != null && client.player.getMainHandItem().is(ModItems.MOVE_WAND)) {
            client.player.displayClientMessage(Component.translatable("text.quick_build.029"), true);
        }
    }

    private static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            WandPreviewState.clear();
            wasAttacking = false;
            wasUsing = false;
            wasHoldingMoveWand = false;
            return;
        }

        ItemStack stack = client.player.getMainHandItem();
        boolean buildingWand = stack.is(ModItems.BUILDING_WAND);
        boolean moveWand = stack.is(ModItems.MOVE_WAND);
        if (moveWand && !wasHoldingMoveWand) {
            moveMemoryQueryPending = true;
            ClientPlayNetworking.send(new WandNetworking.QueryMoveMemoryPayload());
        }
        boolean attacking = client.options.keyAttack.isDown();
        boolean attackPressed = attacking && !wasAttacking;
        boolean attackReleased = !attacking && wasAttacking;
        boolean using = client.options.keyUse.isDown();
        boolean usePressed = using && !wasUsing;
        boolean shift = client.options.keyShift.isDown();
        boolean ctrl = controlDown(client);
        boolean alt = altDown(client);

        HitResult hit = client.player.pick(MAX_DISTANCE, 0.0F, false);
        BlockPos selectionTarget = selectionTarget(client, hit);
        BlockPos buildingTarget = buildingTarget(client, hit);

        if (buildingWand) {
            tickBuildingWand(client, attackPressed, attackReleased, attacking, usePressed,
                    shift, ctrl, alt, hit, buildingTarget);
        } else if (moveWand) {
            tickMoveWand(client, attackPressed, usePressed, shift, ctrl, selectionTarget);
        } else {
            WandPreviewState.clear();
            buildingDrawing = null;
        }

        if (buildingWand || moveWand) {
            tickAdjustmentKeys(client, buildingWand, moveWand, shift, ctrl, alt);
        } else {
            resetArrowEdges(client);
        }

        wasAttacking = attacking;
        wasUsing = using;
        wasHoldingMoveWand = moveWand;
    }

    private static void tickBuildingWand(
            Minecraft client,
            boolean attackPressed,
            boolean attackReleased,
            boolean attacking,
            boolean usePressed,
            boolean shift,
            boolean ctrl,
            boolean alt,
            HitResult hit,
            BlockPos target
    ) {
        if (usePressed && shift) {
            cancelBuildingSelection();
            return;
        }
        if (pendingBuilding != null) {
            refreshBuildingPreview();
            if (attackPressed && !alt) {
                ClientPlayNetworking.send(new WandNetworking.PlaceShapePayload(
                        pendingBuilding.first(), pendingBuilding.second(), pendingBuilding.mode()));
                pendingBuilding = null;
                WandPreviewState.clear();
            }
            return;
        }

        if (attackPressed && alt) {
            if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos selectedPos = blockHit.getBlockPos();
                BlockState state = client.level.getBlockState(selectedPos);
                if (!state.isAir()) {
                    WandStackData.setSelectedBlock(client.player.getMainHandItem(), state.getBlock());
                    ClientPlayNetworking.send(new WandNetworking.SelectBlockPayload(selectedPos));
                }
            }
            buildingDrawing = null;
            return;
        }

        if (attackPressed) {
            ShapeMode dragMode = ctrl ? ShapeMode.SOLID : shift ? ShapeMode.PLANE : ShapeMode.SOLID;
            buildingDrawing = new BuildingDrawing(
                    target, target, dragMode, buildingPointFirst, buildingPointMode);
        }

        if (attacking && buildingDrawing != null) {
            buildingDrawing = buildingDrawing.withCurrent(target);
            BlockPos previewFirst = buildingDrawing.pointFirst() == null
                    ? buildingDrawing.start() : buildingDrawing.pointFirst();
            ShapeMode previewMode = buildingDrawing.pointMode() == null
                    ? buildingDrawing.dragMode() : buildingDrawing.pointMode();
            List<BlockPos> positions = ShapeBuilder.between(previewFirst, buildingDrawing.current(), previewMode);
            WandPreviewState.set(positions, buildingPreviewColor(positions));
            return;
        }

        if (attackReleased && buildingDrawing != null) {
            finishBuildingGesture(client, shift, ctrl);
            return;
        }

        if (buildingPointFirst != null && buildingPointMode != null) {
            List<BlockPos> positions = ShapeBuilder.between(buildingPointFirst, target, buildingPointMode);
            WandPreviewState.set(positions, buildingPreviewColor(positions));
            return;
        }

        Block selected = WandStackData.selectedBlock(client.player.getMainHandItem());
        if (selected == Blocks.AIR) {
            WandPreviewState.clear();
            return;
        }
        List<BlockPos> single = List.of(target);
        WandPreviewState.set(single, buildingPreviewColor(single));
    }

    private static void finishBuildingGesture(Minecraft client, boolean shift, boolean ctrl) {
        BuildingDrawing drawing = buildingDrawing;
        buildingDrawing = null;
        boolean dragged = !drawing.start().equals(drawing.current());

        if (drawing.pointFirst() != null && drawing.pointMode() != null) {
            pendingBuilding = new PendingShape(drawing.pointFirst(), drawing.current(), drawing.pointMode());
            buildingPointFirst = null;
            buildingPointMode = null;
            refreshBuildingPreview();
            return;
        }

        if (dragged) {
            pendingBuilding = new PendingShape(drawing.start(), drawing.current(), drawing.dragMode());
            buildingPointFirst = null;
            buildingPointMode = null;
            refreshBuildingPreview();
            return;
        }

        if (ctrl) {
            buildingPointFirst = drawing.current();
            buildingPointMode = ShapeMode.HOLLOW;
            refreshBuildingPointPreview();
            return;
        }
        if (shift) {
            buildingPointFirst = drawing.current();
            buildingPointMode = ShapeMode.SOLID;
            refreshBuildingPointPreview();
            return;
        }

        ClientPlayNetworking.send(new WandNetworking.PlaceSinglePayload(drawing.current()));
    }

    private static void tickMoveWand(
            Minecraft client,
            boolean attackPressed,
            boolean usePressed,
            boolean shift,
            boolean ctrl,
            BlockPos target
    ) {
        if (usePressed && shift) {
            ClientPlayNetworking.send(new WandNetworking.CancelMoveSelectionPayload());
            resetMoveClientState();
            return;
        }
        if (moveMemoryQueryPending) {
            WandPreviewState.set(List.of(target), WandPreviewState.PreviewColor.WHITE);
            return;
        }
        if (usePressed && ctrl) {
            if (hasMoveMemory && moveFirst != null && moveSecond != null
                    && moveStage != Stage.EXECUTE_PENDING) {
                startMovePreview(target);
                return;
            }
            if (moveStage == Stage.CAPTURE_PENDING) {
                queuedMovePreviewTarget = target.immutable();
                return;
            }
        }

        if (moveStage == Stage.SELECTION_ADJUSTING || moveStage == Stage.CAPTURE_PENDING) {
            refreshMoveSelectionPreview();
        } else if (moveStage == Stage.MOVE_PREVIEW || moveStage == Stage.EXECUTE_PENDING) {
            refreshMovePreview();
        }

        if (!attackPressed) {
            if (moveStage == Stage.FIRST_POINT) {
                WandPreviewState.set(List.of(moveFirst, target), WandPreviewState.PreviewColor.BLUE);
            } else if (moveStage == Stage.IDLE || moveStage == Stage.RECORDED) {
                WandPreviewState.set(List.of(target), WandPreviewState.PreviewColor.WHITE);
            }
            return;
        }

        switch (MoveWandFlow.leftAction(moveStage, hasMoveMemory, pendingMove != null)) {
            case RECORD_FIRST -> {
                moveFirst = target;
                moveSecond = null;
                moveStage = Stage.FIRST_POINT;
                WandPreviewState.set(List.of(moveFirst), WandPreviewState.PreviewColor.BLUE);
                client.player.displayClientMessage(Component.translatable("text.quick_build.169"), true);
                client.player.displayClientMessage(Component.literal(
                        "text.quick_build.164"), false);
            }
            case CAPTURE_SELECTION -> {
                if (moveStage == Stage.FIRST_POINT) {
                    moveSecond = target;
                    refreshMoveSelectionPreview();
                    client.player.displayClientMessage(Component.translatable("text.quick_build.168"), true);
                }
                moveStage = Stage.CAPTURE_PENDING;
                ClientPlayNetworking.send(new WandNetworking.CaptureMoveSelectionPayload(moveFirst, moveSecond));
            }
            case EXECUTE_MOVE -> {
                moveStage = Stage.EXECUTE_PENDING;
                ClientPlayNetworking.send(new WandNetworking.ExecuteMovePayload(
                        pendingMove.targetFirst(), pendingMove.rotation()));
            }
            default -> {
            }
        }
    }

    private static void tickAdjustmentKeys(
            Minecraft client, boolean buildingWand, boolean moveWand, boolean shift, boolean ctrl, boolean alt) {
        boolean up = keyDown(client, GLFW.GLFW_KEY_UP);
        boolean down = keyDown(client, GLFW.GLFW_KEY_DOWN);
        boolean left = keyDown(client, GLFW.GLFW_KEY_LEFT);
        boolean right = keyDown(client, GLFW.GLFW_KEY_RIGHT);

        if (up && !wasUp) {
            adjust(client, buildingWand, moveWand, AdjustmentKey.UP, shift, ctrl, alt);
        }
        if (down && !wasDown) {
            adjust(client, buildingWand, moveWand, AdjustmentKey.DOWN, shift, ctrl, alt);
        }
        if (left && !wasLeft) {
            adjust(client, buildingWand, moveWand, AdjustmentKey.LEFT, shift, ctrl, alt);
        }
        if (right && !wasRight) {
            adjust(client, buildingWand, moveWand, AdjustmentKey.RIGHT, shift, ctrl, alt);
        }

        wasUp = up;
        wasDown = down;
        wasLeft = left;
        wasRight = right;
    }

    private static void adjust(
            Minecraft client,
            boolean buildingWand,
            boolean moveWand,
            AdjustmentKey key,
            boolean shift,
            boolean ctrl,
            boolean alt
    ) {
        if (buildingWand && pendingBuilding != null) {
            pendingBuilding = adjustShape(client, pendingBuilding, key, shift, ctrl);
            refreshBuildingPreview();
            return;
        }

        if (!moveWand) {
            return;
        }
        if (moveStage == Stage.SELECTION_ADJUSTING) {
            PendingShape selection = new PendingShape(moveFirst, moveSecond, ShapeMode.SOLID);
            selection = adjustShape(client, selection, key, shift, ctrl);
            moveFirst = selection.first();
            moveSecond = selection.second();
            refreshMoveSelectionPreview();
            return;
        }
        if (moveStage != Stage.MOVE_PREVIEW || pendingMove == null) {
            return;
        }

        if (alt && (key == AdjustmentKey.LEFT || key == AdjustmentKey.RIGHT)) {
            int delta = key == AdjustmentKey.RIGHT ? 1 : -1;
            pendingMove = pendingMove.rotate(delta);
            refreshMovePreview();
            return;
        }
        if (ctrl && (key == AdjustmentKey.UP || key == AdjustmentKey.DOWN)) {
            int dy = key == AdjustmentKey.UP ? 1 : -1;
            pendingMove = pendingMove.move(0, dy, 0);
            refreshMovePreview();
            return;
        }
        Direction direction = horizontalDirection(client, key);
        pendingMove = pendingMove.move(direction.getStepX(), 0, direction.getStepZ());
        refreshMovePreview();
    }

    private static PendingShape adjustShape(
            Minecraft client, PendingShape shape, AdjustmentKey key, boolean shift, boolean ctrl) {
        if (ctrl && (key == AdjustmentKey.UP || key == AdjustmentKey.DOWN)) {
            int dy = key == AdjustmentKey.UP ? 1 : -1;
            return shift ? shape.resizeSecond(0, dy, 0) : shape.move(0, dy, 0);
        }
        Direction direction = horizontalDirection(client, key);
        return shift
                ? shape.resizeSecond(direction.getStepX(), 0, direction.getStepZ())
                : shape.move(direction.getStepX(), 0, direction.getStepZ());
    }

    private static Direction horizontalDirection(Minecraft client, AdjustmentKey key) {
        Direction facing = client.player.getDirection();
        return switch (key) {
            case UP -> facing;
            case DOWN -> facing.getOpposite();
            case LEFT -> facing.getCounterClockWise();
            case RIGHT -> facing.getClockWise();
        };
    }

    private static void refreshBuildingPreview() {
        if (pendingBuilding == null) {
            return;
        }
        List<BlockPos> positions = pendingBuilding.positions();
        WandPreviewState.set(positions, buildingPreviewColor(positions));
    }

    private static void refreshBuildingPointPreview() {
        if (buildingPointFirst == null) {
            return;
        }
        WandPreviewState.set(List.of(buildingPointFirst), buildingPreviewColor(List.of(buildingPointFirst)));
    }

    private static void refreshMoveSelectionPreview() {
        if (moveFirst == null || moveSecond == null) {
            return;
        }
        List<BlockPos> positions = ShapeBuilder.between(moveFirst, moveSecond, ShapeMode.SOLID);
        WandPreviewState.set(positions, moveSelectionColor(positions));
    }

    private static void refreshMovePreview() {
        if (pendingMove == null || moveSource.isEmpty()) {
            return;
        }
        double centerX = moveSource.stream().mapToInt(BlockPos::getX).average().orElse(moveFirst.getX());
        double centerZ = moveSource.stream().mapToInt(BlockPos::getZ).average().orElse(moveFirst.getZ());
        List<BlockPos> allMoved = transform(
                moveSource, moveFirst, pendingMove.targetFirst(), pendingMove.rotation(), centerX, centerZ);
        List<BlockPos> visible = moveVisibleSource.isEmpty() ? allMoved
                : transform(moveVisibleSource, moveFirst, pendingMove.targetFirst(), pendingMove.rotation(), centerX, centerZ);
        WandPreviewState.set(visible, movePreviewColor(moveSource, allMoved));
    }

    private static void startMovePreview(BlockPos target) {
        pendingMove = new PendingMove(target.immutable(), 0);
        moveStage = Stage.MOVE_PREVIEW;
        refreshMovePreview();
    }

    private static void rebuildMoveSource() {
        if (moveFirst == null || moveSecond == null) {
            moveSource = List.of();
            moveVisibleSource = List.of();
            return;
        }
        moveSource = ShapeBuilder.between(moveFirst, moveSecond, ShapeMode.SOLID);
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            moveVisibleSource = moveSource;
        } else {
            moveVisibleSource = moveSource.stream()
                    .filter(pos -> !client.level.getBlockState(pos).isAir())
                    .toList();
        }
    }

    private static WandPreviewState.PreviewColor buildingPreviewColor(List<BlockPos> positions) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return WandPreviewState.PreviewColor.RED;
        }
        Block selected = WandStackData.selectedBlock(client.player.getMainHandItem());
        if (selected == Blocks.AIR) {
            return WandPreviewState.PreviewColor.RED;
        }
        if (client.player.getAbilities().instabuild) {
            return WandPreviewState.PreviewColor.WHITE;
        }
        if (!targetsReplaceable(client.level, positions) || !hasSupport(client.level, positions)) {
            return WandPreviewState.PreviewColor.RED;
        }
        return countInventoryBlocks(selected) >= positions.size()
                ? WandPreviewState.PreviewColor.WHITE
                : WandPreviewState.PreviewColor.RED;
    }

    private static WandPreviewState.PreviewColor moveSelectionColor(List<BlockPos> positions) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return WandPreviewState.PreviewColor.RED;
        }
        for (BlockPos pos : positions) {
            if (isForbiddenMoveBlock(client.level.getBlockState(pos))) {
                return WandPreviewState.PreviewColor.RED;
            }
        }
        return WandPreviewState.PreviewColor.BLUE;
    }

    private static WandPreviewState.PreviewColor movePreviewColor(
            List<BlockPos> source, List<BlockPos> moved) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return WandPreviewState.PreviewColor.RED;
        }
        Set<BlockPos> sourceSet = new HashSet<>(source);
        boolean creative = client.player.getAbilities().instabuild;
        for (BlockPos pos : moved) {
            BlockState state = client.level.getBlockState(pos);
            if (isForbiddenMoveBlock(state)) {
                return WandPreviewState.PreviewColor.RED;
            }
            if (!creative && !sourceSet.contains(pos) && !isReplaceable(state)) {
                return WandPreviewState.PreviewColor.RED;
            }
        }
        return WandPreviewState.PreviewColor.BLUE;
    }

    private static boolean targetsReplaceable(Level level, List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            if (!isReplaceable(level.getBlockState(pos))) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasSupport(Level level, List<BlockPos> positions) {
        Set<BlockPos> targets = new HashSet<>(positions);
        for (BlockPos pos : positions) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (!targets.contains(neighbor) && !isReplaceable(level.getBlockState(neighbor))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static boolean isForbiddenMoveBlock(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.BEDROCK
                || block == Blocks.COMMAND_BLOCK
                || block == Blocks.REPEATING_COMMAND_BLOCK
                || block == Blocks.CHAIN_COMMAND_BLOCK
                || block == Blocks.BARRIER
                || block == Blocks.STRUCTURE_BLOCK
                || block == Blocks.STRUCTURE_VOID
                || block == Blocks.JIGSAW;
    }

    private static int countInventoryBlocks(Block block) {
        Minecraft client = Minecraft.getInstance();
        int count = 0;
        for (int slot = 0; slot < client.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = client.player.getInventory().getItem(slot);
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static BlockPos selectionTarget(Minecraft client, HitResult hit) {
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getBlockPos();
        }
        return airPoint(client);
    }

    private static BlockPos buildingTarget(Minecraft client, HitResult hit) {
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = blockHit.getBlockPos();
            if (client.player.getAbilities().instabuild || isReplaceable(client.level.getBlockState(hitPos))) {
                return hitPos;
            }
            return hitPos.relative(blockHit.getDirection());
        }
        return airPoint(client);
    }

    private static BlockPos airPoint(Minecraft client) {
        Vec3 eye = client.player.getEyePosition(0.0F);
        Vec3 view = client.player.getViewVector(0.0F);
        return BlockPos.containing(eye.add(view.scale(AIR_PREVIEW_DISTANCE)));
    }

    private static List<BlockPos> transform(
            List<BlockPos> source, BlockPos sourceFirst, BlockPos targetFirst, int rotation) {
        double centerX = source.stream().mapToInt(BlockPos::getX).average().orElse(sourceFirst.getX());
        double centerZ = source.stream().mapToInt(BlockPos::getZ).average().orElse(sourceFirst.getZ());
        return transform(source, sourceFirst, targetFirst, rotation, centerX, centerZ);
    }

    private static List<BlockPos> transform(
            List<BlockPos> source,
            BlockPos sourceFirst,
            BlockPos targetFirst,
            int rotation,
            double centerX,
            double centerZ
    ) {
        BlockPos rotatedFirst = rotateAroundCenter(sourceFirst, centerX, centerZ, rotation);
        int dx = targetFirst.getX() - rotatedFirst.getX();
        int dy = targetFirst.getY() - rotatedFirst.getY();
        int dz = targetFirst.getZ() - rotatedFirst.getZ();
        return source.stream()
                .map(pos -> rotateAroundCenter(pos, centerX, centerZ, rotation).offset(dx, dy, dz))
                .toList();
    }

    private static BlockPos rotateAroundCenter(BlockPos pos, double centerX, double centerZ, int rotation) {
        double dx = pos.getX() - centerX;
        double dz = pos.getZ() - centerZ;
        double rotatedX = dx;
        double rotatedZ = dz;
        for (int i = 0; i < (rotation & 3); i++) {
            double nextX = -rotatedZ;
            double nextZ = rotatedX;
            rotatedX = nextX;
            rotatedZ = nextZ;
        }
        return new BlockPos(
                (int) Math.round(centerX + rotatedX),
                pos.getY(),
                (int) Math.round(centerZ + rotatedZ));
    }

    private static void cancelBuildingSelection() {
        buildingDrawing = null;
        buildingPointFirst = null;
        buildingPointMode = null;
        pendingBuilding = null;
        WandPreviewState.clear();
    }

    private static void resetMoveClientState() {
        moveStage = Stage.IDLE;
        moveFirst = null;
        moveSecond = null;
        moveSource = List.of();
        moveVisibleSource = List.of();
        pendingMove = null;
        queuedMovePreviewTarget = null;
        hasMoveMemory = false;
        moveMemoryQueryPending = false;
        WandPreviewState.clear();
    }

    private static boolean controlDown(Minecraft client) {
        return keyDown(client, GLFW.GLFW_KEY_LEFT_CONTROL)
                || keyDown(client, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static boolean altDown(Minecraft client) {
        return keyDown(client, GLFW.GLFW_KEY_LEFT_ALT)
                || keyDown(client, GLFW.GLFW_KEY_RIGHT_ALT);
    }

    private static boolean keyDown(Minecraft client, int key) {
        return GLFW.glfwGetKey(client.getWindow().getWindow(), key) == GLFW.GLFW_PRESS;
    }

    private static void resetArrowEdges(Minecraft client) {
        wasUp = keyDown(client, GLFW.GLFW_KEY_UP);
        wasDown = keyDown(client, GLFW.GLFW_KEY_DOWN);
        wasLeft = keyDown(client, GLFW.GLFW_KEY_LEFT);
        wasRight = keyDown(client, GLFW.GLFW_KEY_RIGHT);
    }

    private enum AdjustmentKey {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private record BuildingDrawing(
            BlockPos start,
            BlockPos current,
            ShapeMode dragMode,
            BlockPos pointFirst,
            ShapeMode pointMode
    ) {
        private BuildingDrawing withCurrent(BlockPos newCurrent) {
            return new BuildingDrawing(start, newCurrent, dragMode, pointFirst, pointMode);
        }
    }

    private record PendingShape(BlockPos first, BlockPos second, ShapeMode mode) {
        private List<BlockPos> positions() {
            return ShapeBuilder.between(first, second, mode);
        }

        private PendingShape move(int dx, int dy, int dz) {
            return new PendingShape(first.offset(dx, dy, dz), second.offset(dx, dy, dz), mode);
        }

        private PendingShape resizeSecond(int dx, int dy, int dz) {
            return new PendingShape(first, second.offset(dx, dy, dz), mode);
        }
    }

    private record PendingMove(BlockPos targetFirst, int rotation) {
        private PendingMove move(int dx, int dy, int dz) {
            return new PendingMove(targetFirst.offset(dx, dy, dz), rotation);
        }

        private PendingMove rotate(int delta) {
            return new PendingMove(targetFirst, (rotation + delta) & 3);
        }
    }
}
