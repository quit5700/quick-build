package cn.quit5700.light.light;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public final class ConstantLightTaskQueue {
    private static final int MAX_OPERATIONS_PER_TICK = 256;
    private static final int UPDATE_FLAGS = 3;
    private static final Queue<Task> TASKS = new ArrayDeque<>();
    private static final Map<TrackedLight, Integer> LIGHT_COUNTS = new HashMap<>();

    private ConstantLightTaskQueue() {
    }

    public static void install(ServerLevel world, BlockPos sourcePos) {
        TASKS.add(new Task(world, sourcePos.immutable(), true));
    }

    public static void cleanup(ServerLevel world, BlockPos sourcePos) {
        TASKS.add(new Task(world, sourcePos.immutable(), false));
    }

    public static void tick() {
        int operations = 0;
        while (operations < MAX_OPERATIONS_PER_TICK && !TASKS.isEmpty()) {
            Task task = TASKS.peek();
            operations += task.process(MAX_OPERATIONS_PER_TICK - operations);
            if (task.isFinished()) {
                TASKS.remove();
            }
        }
    }

    private static void installLight(ServerLevel world, BlockPos pos) {
        TrackedLight trackedLight = new TrackedLight(world, pos.immutable());
        LIGHT_COUNTS.merge(trackedLight, 1, Integer::sum);
        BlockState currentState = world.getBlockState(pos);
        if (currentState.isAir() || currentState.is(Blocks.LIGHT)) {
            world.setBlock(pos, lightStateFor(currentState), UPDATE_FLAGS);
        }
    }

    private static void cleanupLight(ServerLevel world, BlockPos pos) {
        TrackedLight trackedLight = new TrackedLight(world, pos.immutable());
        Integer count = LIGHT_COUNTS.get(trackedLight);
        if (count != null && count > 1) {
            LIGHT_COUNTS.put(trackedLight, count - 1);
            return;
        }
        LIGHT_COUNTS.remove(trackedLight);
        BlockState currentState = world.getBlockState(pos);
        if (currentState.is(Blocks.LIGHT)) {
            world.setBlock(pos, currentState.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), UPDATE_FLAGS);
        }
    }

    private static BlockState lightStateFor(BlockState replacedState) {
        BlockState lightState = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15);
        if (lightState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            lightState = lightState.setValue(BlockStateProperties.WATERLOGGED, replacedState.getFluidState().is(Fluids.WATER));
        }
        return lightState;
    }

    private static final class Task {
        private final ServerLevel world;
        private final BlockPos sourcePos;
        private final boolean install;
        private int nextIndex;

        private Task(ServerLevel world, BlockPos sourcePos, boolean install) {
            this.world = world;
            this.sourcePos = sourcePos;
            this.install = install;
        }

        private int process(int maxOperations) {
            int operations = 0;
            while (operations < maxOperations && nextIndex < ConstantLightPositions.relativeOffsets().size()) {
                ConstantLightPositions.Offset offset = ConstantLightPositions.relativeOffsets().get(nextIndex++);
                BlockPos lightPos = sourcePos.offset(offset.dx(), offset.dy(), offset.dz());
                if (!world.isOutsideBuildHeight(lightPos) && world.isLoaded(lightPos)) {
                    if (install) {
                        installLight(world, lightPos);
                    } else {
                        cleanupLight(world, lightPos);
                    }
                    operations++;
                }
            }
            return operations;
        }

        private boolean isFinished() {
            if (nextIndex < ConstantLightPositions.relativeOffsets().size()) {
                return false;
            }
            nextIndex = 0;
            return true;
        }
    }

    private record TrackedLight(ServerLevel world, BlockPos pos) {
    }
}
