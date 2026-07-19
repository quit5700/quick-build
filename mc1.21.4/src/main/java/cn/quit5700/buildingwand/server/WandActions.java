package cn.quit5700.buildingwand.server;

import cn.quit5700.buildingwand.block.WandSettingsStationBlock;
import cn.quit5700.buildingwand.config.WandSettingsState;
import cn.quit5700.buildingwand.network.WandNetworking;
import cn.quit5700.buildingwand.registry.ModItems;
import cn.quit5700.buildingwand.util.ShapeBuilder;
import cn.quit5700.buildingwand.util.ShapeMode;
import cn.quit5700.buildingwand.util.WandStackData;
import cn.quit5700.pathfindingbeacon.block.PathfindingBlock;
import cn.quit5700.pathfindingbeacon.route.RouteMoveStatus;
import cn.quit5700.pathfindingbeacon.route.RouteSnapshot;
import cn.quit5700.pathfindingbeacon.route.WorldRouteManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WandActions {
    private static final Map<UUID, MoveSession> MOVE_SESSIONS = new HashMap<>();
    private static final Set<Block> FORBIDDEN_MOVE_BLOCKS = Set.of(
            Blocks.BEDROCK,
            Blocks.COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.BARRIER,
            Blocks.STRUCTURE_BLOCK,
            Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW
    );

    private WandActions() {
    }

    public static void selectBlock(ServerPlayer player, BlockPos pos) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.BUILDING_WAND)) {
            return;
        }
        BlockState state = player.level().getBlockState(pos);
        if (state.isAir()) {
            fail(player, "text.quick_build.036");
            return;
        }
        WandStackData.setSelectedBlock(stack, state.getBlock());
        player.displayClientMessage(Component.translatable("text.quick_build.172")
                .append(Component.translatable(state.getBlock().getDescriptionId()))
                .withStyle(ChatFormatting.AQUA), true);
    }

    public static void placeShape(ServerPlayer player, BlockPos first, BlockPos second, ShapeMode mode) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.BUILDING_WAND)) {
            return;
        }
        Block block = WandStackData.selectedBlock(stack);
        if (block == Blocks.AIR) {
            fail(player, "text.quick_build.109");
            return;
        }

        List<BlockPos> positions = ShapeBuilder.between(first, second, mode);
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        WandSettingsState settings = WandSettingsState.get(server);
        int dimensions = mode == ShapeMode.PLANE ? 2 : ShapeBuilder.dimensions(first, second);
        int limit = dimensions <= 2 ? settings.planeLimit() : settings.cubeLimit();
        if (positions.size() > limit) {
            fail(player, "message.quick_build.structure_too_large", limit);
            return;
        }

        ServerLevel level = player.serverLevel();
        boolean creative = player.getAbilities().instabuild;
        if (!creative && !targetsReplaceable(level, positions)) {
            fail(player, "text.quick_build.100");
            return;
        }
        if (!creative && !hasSupport(level, positions)) {
            fail(player, "text.quick_build.184");
            return;
        }
        if (!creative && countInventoryBlocks(player, block) < positions.size()) {
            fail(player, "message.quick_build.materials_insufficient", positions.size());
            return;
        }

        for (BlockPos pos : positions) {
            level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (!creative) {
            consumeInventoryBlocks(player, block, positions.size());
        }
    }

    public static void placeSingle(ServerPlayer player, BlockPos pos) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.BUILDING_WAND)) {
            return;
        }
        Block block = WandStackData.selectedBlock(stack);
        if (block == Blocks.AIR) {
            fail(player, "text.quick_build.109");
            return;
        }

        ServerLevel level = player.serverLevel();
        boolean creative = player.getAbilities().instabuild;
        if (!creative && !isReplaceable(level.getBlockState(pos))) {
            fail(player, "text.quick_build.102");
            return;
        }
        if (!creative && !hasSupport(level, List.of(pos))) {
            fail(player, "text.quick_build.101");
            return;
        }
        if (!creative && countInventoryBlocks(player, block) < 1) {
            fail(player, "text.quick_build.033");
            return;
        }

        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
        if (!creative) {
            consumeInventoryBlocks(player, block, 1);
        }
    }

    public static void captureMoveSelection(ServerPlayer player, BlockPos first, BlockPos second) {
        if (!player.getMainHandItem().is(ModItems.MOVE_WAND)) {
            sendSelectionResult(player, false);
            return;
        }

        List<BlockPos> positions = ShapeBuilder.between(first, second, ShapeMode.SOLID);
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            sendSelectionResult(player, false);
            return;
        }
        int limit = WandSettingsState.get(server).moveLimit();
        if (positions.size() > limit) {
            fail(player, "message.quick_build.selection_too_large", limit);
            sendSelectionResult(player, false);
            return;
        }

        ServerLevel level = player.serverLevel();
        List<MoveBlock> blocks = new ArrayList<>(positions.size());
        int movableBlockCount = 0;
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (FORBIDDEN_MOVE_BLOCKS.contains(state.getBlock())) {
                fail(player, "message.quick_build.forbidden_move_block", BuiltInRegistries.BLOCK.getKey(state.getBlock()));
                sendSelectionResult(player, false);
                return;
            }
            BlockEntity entity = level.getBlockEntity(pos);
            CompoundTag tag = entity == null ? null : entity.saveWithFullMetadata(level.registryAccess());
            blocks.add(new MoveBlock(pos.immutable(), state, tag));
            if (!state.isAir()) {
                movableBlockCount++;
            }
        }
        if (movableBlockCount == 0) {
            fail(player, "text.quick_build.149");
            sendSelectionResult(player, false);
            return;
        }

        MOVE_SESSIONS.put(player.getUUID(), new MoveSession(
                first.immutable(), second.immutable(), level.dimension(), List.copyOf(blocks), centerX(blocks), centerZ(blocks)));
        player.displayClientMessage(Component.translatable("message.quick_build.move_saved", movableBlockCount)
                .withStyle(ChatFormatting.AQUA), true);
        sendSelectionResult(player, true);
    }

    public static void executeMove(ServerPlayer player, BlockPos targetFirst, int rotation) {
        if (!player.getMainHandItem().is(ModItems.MOVE_WAND)) {
            sendExecutionResult(player, false);
            return;
        }
        MoveSession session = MOVE_SESSIONS.get(player.getUUID());
        if (session == null) {
            fail(player, "text.quick_build.094");
            sendExecutionResult(player, false);
            return;
        }
        if (player.level().dimension() != session.dimension) {
            fail(player, "text.quick_build.035");
            sendExecutionResult(player, false);
            return;
        }

        ServerLevel level = player.serverLevel();
        Map<BlockPos, MoveBlock> targetBlocks = transformedBlocks(session, targetFirst, rotation & 3);
        Set<BlockPos> sourcePositions = new HashSet<>();
        for (MoveBlock block : session.blocks) {
            sourcePositions.add(block.source);
        }
        if (targetBlocks.keySet().equals(sourcePositions) && (rotation & 3) == 0) {
            fail(player, "text.quick_build.133");
            sendExecutionResult(player, false);
            return;
        }

        boolean creative = player.getAbilities().instabuild;
        for (BlockPos target : targetBlocks.keySet()) {
            if (!level.isInWorldBounds(target)) {
                fail(player, "text.quick_build.099");
                sendExecutionResult(player, false);
                return;
            }
            if (sourcePositions.contains(target)) {
                continue;
            }
            BlockState existing = level.getBlockState(target);
            if (FORBIDDEN_MOVE_BLOCKS.contains(existing.getBlock())) {
                fail(player, "text.quick_build.098");
                sendExecutionResult(player, false);
                return;
            }
            if (!creative && !isReplaceable(existing)) {
                fail(player, "text.quick_build.117");
                sendExecutionResult(player, false);
                return;
            }
        }

        Set<BlockPos> updatePositions = new LinkedHashSet<>();
        updatePositions.addAll(sourcePositions);
        updatePositions.addAll(targetBlocks.keySet());
        RouteSnapshot routeSnapshot = WorldRouteManager.snapshot(level);
        Set<BlockPos> trackedRoutePositions = new HashSet<>();
        routeSnapshot.nodes().forEach(node ->
                trackedRoutePositions.add(WorldRouteManager.toBlockPos(node.position())));
        Map<BlockPos, BlockPos> routeMovements = new LinkedHashMap<>();
        targetBlocks.forEach((target, block) -> {
            if (block.state.getBlock() instanceof PathfindingBlock
                    && trackedRoutePositions.contains(block.source)) {
                routeMovements.put(block.source, target);
            }
        });
        Set<BlockPos> affectedRoutePositions = new LinkedHashSet<>();
        for (BlockPos routePosition : trackedRoutePositions) {
            if (updatePositions.contains(routePosition)) {
                affectedRoutePositions.add(routePosition);
            }
        }
        boolean updatesRoutes = !routeMovements.isEmpty() || !affectedRoutePositions.isEmpty();
        if (updatesRoutes) {
            RouteMoveStatus routeStatus = WorldRouteManager.validateMove(
                    routeSnapshot, routeMovements, affectedRoutePositions);
            if (routeStatus == RouteMoveStatus.DUPLICATE_XZ) {
                fail(player, "text.quick_build.161");
                sendExecutionResult(player, false);
                return;
            }
            if (routeStatus != RouteMoveStatus.SUCCESS) {
                fail(player, "text.quick_build.153");
                sendExecutionResult(player, false);
                return;
            }
        }
        Map<BlockPos, MoveBlock> rollback = snapshot(level, updatePositions);
        try {
            clearPositions(level, sourcePositions);
            placeSnapshot(level, targetBlocks);
            verifySnapshot(level, targetBlocks);
            if (updatesRoutes) {
                WorldRouteManager.applyMove(
                        level, routeSnapshot, routeMovements, affectedRoutePositions);
            }
        } catch (RuntimeException exception) {
            restoreSnapshot(level, updatePositions, rollback);
            if (updatesRoutes) {
                WorldRouteManager.restoreSnapshot(level, routeSnapshot);
            }
            fail(player, "text.quick_build.097");
            sendExecutionResult(player, false);
            return;
        }
        for (BlockPos pos : updatePositions) {
            level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        }

        MOVE_SESSIONS.remove(player.getUUID());
        player.displayClientMessage(Component.translatable("text.quick_build.165").withStyle(ChatFormatting.GREEN), true);
        sendExecutionResult(player, true);
    }

    public static void cancelMove(ServerPlayer player) {
        MOVE_SESSIONS.remove(player.getUUID());
        player.displayClientMessage(Component.translatable("text.quick_build.163")
                .withStyle(ChatFormatting.YELLOW), true);
        sendMoveMemory(player);
    }

    public static void sendMoveMemory(ServerPlayer player) {
        MoveSession session = MOVE_SESSIONS.get(player.getUUID());
        boolean available = session != null && player.level().dimension() == session.dimension;
        ServerPlayNetworking.send(player, available
                ? new WandNetworking.MoveMemoryPayload(true, session.first, session.second)
                : new WandNetworking.MoveMemoryPayload(false, BlockPos.ZERO, BlockPos.ZERO));
    }

    public static void clearMoveSessions() {
        MOVE_SESSIONS.clear();
    }

    public static void updateSettings(ServerPlayer player, int planeLimit, int cubeLimit, int moveLimit) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        WandSettingsState settings = WandSettingsState.get(server);
        if (!WandSettingsStationBlock.canEdit(player)) {
            fail(player, "text.quick_build.187");
            sendSettingsSaved(player, false, settings);
            return;
        }
        settings.setLimits(planeLimit, cubeLimit, moveLimit);
        sendSettingsSaved(player, true, settings);
        player.displayClientMessage(Component.translatable("message.quick_build.settings_saved", settings.planeLimit(), settings.cubeLimit(), settings.moveLimit())
                .withStyle(ChatFormatting.GREEN), true);
    }

    private static void sendSettingsSaved(
            ServerPlayer player, boolean accepted, WandSettingsState settings) {
        ServerPlayNetworking.send(player, new WandNetworking.SettingsSavedPayload(
                accepted, settings.planeLimit(), settings.cubeLimit(), settings.moveLimit()));
    }

    private static Map<BlockPos, MoveBlock> transformedBlocks(
            MoveSession session, BlockPos targetFirst, int rotation) {
        BlockPos rotatedFirst = rotateAroundCenter(session.first, session.centerX, session.centerZ, rotation);
        int offsetX = targetFirst.getX() - rotatedFirst.getX();
        int offsetY = targetFirst.getY() - rotatedFirst.getY();
        int offsetZ = targetFirst.getZ() - rotatedFirst.getZ();
        Map<BlockPos, MoveBlock> result = new LinkedHashMap<>();
        Rotation blockRotation = switch (rotation & 3) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
        for (MoveBlock block : session.blocks) {
            BlockPos rotated = rotateAroundCenter(block.source, session.centerX, session.centerZ, rotation);
            result.put(rotated.offset(offsetX, offsetY, offsetZ),
                    new MoveBlock(block.source, block.state.rotate(blockRotation), block.blockEntityTag));
        }
        return result;
    }

    private static Map<BlockPos, MoveBlock> snapshot(ServerLevel level, Set<BlockPos> positions) {
        Map<BlockPos, MoveBlock> result = new LinkedHashMap<>();
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            BlockEntity entity = level.getBlockEntity(pos);
            CompoundTag tag = entity == null ? null : entity.saveWithFullMetadata(level.registryAccess());
            result.put(pos.immutable(), new MoveBlock(pos.immutable(), state, tag));
        }
        return result;
    }

    private static void clearPositions(ServerLevel level, Set<BlockPos> positions) {
        for (BlockPos pos : positions) {
            level.removeBlockEntity(pos);
            if (!level.getBlockState(pos).isAir()
                    && !level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)) {
                throw new IllegalStateException("Cannot clear move source at " + pos);
            }
        }
    }

    private static void placeSnapshot(ServerLevel level, Map<BlockPos, MoveBlock> blocks) {
        for (Map.Entry<BlockPos, MoveBlock> entry : blocks.entrySet()) {
            level.removeBlockEntity(entry.getKey());
            if (!level.getBlockState(entry.getKey()).equals(entry.getValue().state)
                    && !level.setBlock(entry.getKey(), entry.getValue().state, Block.UPDATE_CLIENTS)) {
                throw new IllegalStateException("Cannot place move target at " + entry.getKey());
            }
        }
        for (Map.Entry<BlockPos, MoveBlock> entry : blocks.entrySet()) {
            MoveBlock block = entry.getValue();
            if (block.blockEntityTag == null) {
                continue;
            }
            BlockEntity entity = BlockEntity.loadStatic(
                    entry.getKey(), block.state, block.blockEntityTag, level.registryAccess());
            if (entity == null) {
                throw new IllegalStateException("Cannot restore block entity at " + entry.getKey());
            }
            level.setBlockEntity(entity);
        }
    }

    private static void verifySnapshot(ServerLevel level, Map<BlockPos, MoveBlock> blocks) {
        for (Map.Entry<BlockPos, MoveBlock> entry : blocks.entrySet()) {
            if (!level.getBlockState(entry.getKey()).equals(entry.getValue().state)) {
                throw new IllegalStateException("Move verification failed at " + entry.getKey());
            }
        }
    }

    private static void restoreSnapshot(
            ServerLevel level, Set<BlockPos> affectedPositions, Map<BlockPos, MoveBlock> rollback) {
        for (BlockPos pos : affectedPositions) {
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        placeSnapshot(level, rollback);
        for (BlockPos pos : affectedPositions) {
            level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        }
    }

    private static BlockPos rotateAroundCenter(BlockPos pos, double centerX, double centerZ, int rotation) {
        double dx = pos.getX() - centerX;
        double dz = pos.getZ() - centerZ;
        double rotatedX = dx;
        double rotatedZ = dz;
        for (int i = 0; i < rotation; i++) {
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

    private static double centerX(List<MoveBlock> blocks) {
        return blocks.stream().mapToInt(block -> block.source.getX()).average().orElse(0.0D);
    }

    private static double centerZ(List<MoveBlock> blocks) {
        return blocks.stream().mapToInt(block -> block.source.getZ()).average().orElse(0.0D);
    }

    private static boolean targetsReplaceable(Level level, List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            if (!isReplaceable(level.getBlockState(pos))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.canBeReplaced();
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

    private static int countInventoryBlocks(ServerPlayer player, Block block) {
        Inventory inventory = player.getInventory();
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeInventoryBlocks(ServerPlayer player, Block block, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
    }

    private static void sendSelectionResult(ServerPlayer player, boolean accepted) {
        ServerPlayNetworking.send(player, new WandNetworking.MoveSelectionResultPayload(accepted));
    }

    private static void sendExecutionResult(ServerPlayer player, boolean completed) {
        ServerPlayNetworking.send(player, new WandNetworking.MoveExecutionResultPayload(completed));
    }

    private static void fail(ServerPlayer player, String message, Object... args) {
        player.displayClientMessage(Component.translatable(message, args).withStyle(ChatFormatting.RED), true);
    }

    private record MoveSession(
            BlockPos first,
            BlockPos second,
            net.minecraft.resources.ResourceKey<Level> dimension,
            List<MoveBlock> blocks,
            double centerX,
            double centerZ
    ) {
    }

    private record MoveBlock(BlockPos source, BlockState state, CompoundTag blockEntityTag) {
    }
}
