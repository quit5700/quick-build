package cn.quit5700.buildingwand.server;

import cn.quit5700.buildingwand.aux.AuxServer;
import cn.quit5700.buildingwand.blueprint.BlueprintNbt;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.logic.DirectedDeleteCursor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/** Read-only export of a generation task. World-dependent replacement is sampled on the server thread. */
public final class GenerationBoxBlueprint {
    public static final int MAX_CELLS = 262144;
    private static final Map<UUID, Replacement> replacements = new LinkedHashMap<>();

    private GenerationBoxBlueprint() {}

    public static CompletableFuture<byte[]> plan(ListTag plan) {
        requireSize(plan.size());
        var snapshot = plan.copy();
        return CompletableFuture.supplyAsync(() -> encode(planStructure(snapshot)));
    }

    static CompoundTag planStructure(ListTag plan) {
        var cells = new LinkedHashMap<BlockPos, CompoundTag>();
        for (var raw : plan) {
            var unit = (CompoundTag) raw;
            if (cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(unit,"Ids").isEmpty()) continue;
            for (var entry : cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(unit,"Cells")) {
                var cell = (CompoundTag) entry;
                var at = BlockPos.of(cn.quit5700.buildingwand.aux.LegacyNbt.longOr(cell,"Pos", 0));
                cells.put(at, cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(cell,"State"));
                requireSize(cells.size());
            }
        }
        return structure(cells);
    }

    public static CompletableFuture<byte[]> build(DeleteBox box, Block block, int mode, int dx, int dy, int dz) {
        requireVolume(box);
        return CompletableFuture.supplyAsync(() -> {
            var capture = new Capture(box, block, mode, new DirectedDeleteCursor(box, dx, dy, dz));
            while (!capture.cursor.done()) capture.step(group -> true);
            return encode(structure(capture.cells));
        });
    }

    public static CompletableFuture<byte[]> replacement(UUID player, DeleteBox box, Block block,
            DirectedDeleteCursor cursor, Predicate<Map<BlockPos, BlockState>> matches, BooleanSupplier unchanged) {
        requireVolume(box);
        var result = new CompletableFuture<byte[]>();
        var capture = new Capture(box, block, 3, cursor);
        replacements.put(player, new Replacement(capture, matches, unchanged, result));
        return result;
    }

    public static void tick() {
        long deadline = System.nanoTime() + 2_000_000;
        int budget = 512;
        var iterator = replacements.entrySet().iterator();
        while (iterator.hasNext() && budget > 0 && System.nanoTime() < deadline) {
            var replacement = iterator.next().getValue();
            try {
                if (!replacement.unchanged.getAsBoolean()) throw new IllegalStateException("Replacement changed during export");
                while (!replacement.capture.cursor.done() && budget > 0 && System.nanoTime() < deadline) {
                    replacement.capture.step(replacement.matches);
                    budget--;
                }
                if (replacement.capture.cursor.done()) {
                    iterator.remove();
                    CompletableFuture.supplyAsync(() -> encode(structure(replacement.capture.cells)))
                        .whenComplete((bytes, error) -> {
                            if (error == null) replacement.result.complete(bytes);
                            else replacement.result.completeExceptionally(error);
                        });
                }
            } catch (RuntimeException error) {
                iterator.remove();
                replacement.result.completeExceptionally(error);
            }
        }
    }

    public static void cancel(UUID player) {
        var replacement = replacements.remove(player);
        if (replacement != null) replacement.result.cancel(false);
    }

    public static void clear() {
        for (UUID id : List.copyOf(cn.quit5700.buildingwand.aux.LegacyNbt.keys(replacements))) cancel(id);
    }

    private record Replacement(Capture capture, Predicate<Map<BlockPos, BlockState>> matches,
                               BooleanSupplier unchanged, CompletableFuture<byte[]> result) {}

    private static final class Capture {
        final DeleteBox box;
        final Block block;
        final int mode;
        final DirectedDeleteCursor cursor;
        final Map<BlockPos, CompoundTag> cells = new LinkedHashMap<>();
        final Map<BlockState, CompoundTag> states = new HashMap<>();

        Capture(DeleteBox box, Block block, int mode, DirectedDeleteCursor cursor) {
            this.box = box;
            this.block = block;
            this.mode = mode;
            this.cursor = cursor;
        }

        void step(Predicate<Map<BlockPos, BlockState>> matches) {
            var at = new BlockPos(cursor.x(), cursor.y(), cursor.z());
            var group = StructurePlacement.plan(block, box, at, mode, cursor.dx(), cursor.dy(), cursor.dz());
            if (!group.isEmpty() && matches.test(group)) {
                for (var entry : group.entrySet()) {
                    cells.put(entry.getKey(), states.computeIfAbsent(entry.getValue(), NbtUtils::writeBlockState));
                }
            }
            cursor.advance();
        }
    }

    private static CompoundTag structure(Map<BlockPos, CompoundTag> cells) {
        if (cells.isEmpty()) throw new IllegalArgumentException("No exportable blocks");
        int minX = Integer.MAX_VALUE, minY = minX, minZ = minX;
        int maxX = Integer.MIN_VALUE, maxY = maxX, maxZ = maxX;
        for (BlockPos at : cn.quit5700.buildingwand.aux.LegacyNbt.keys(cells)) {
            minX = Math.min(minX, at.getX()); maxX = Math.max(maxX, at.getX());
            minY = Math.min(minY, at.getY()); maxY = Math.max(maxY, at.getY());
            minZ = Math.min(minZ, at.getZ()); maxZ = Math.max(maxZ, at.getZ());
        }
        var output = new ArrayList<BlueprintNbt.Cell>(cells.size());
        for (var entry : cells.entrySet()) {
            var at = entry.getKey();
            output.add(new BlueprintNbt.Cell(at.getX()-minX, at.getY()-minY, at.getZ()-minZ, entry.getValue()));
        }
        return BlueprintNbt.structure(maxX-minX+1, maxY-minY+1, maxZ-minZ+1, output, null);
    }

    private static byte[] encode(CompoundTag tag) {
        try { return BlueprintNbt.compress(tag); }
        catch (IOException error) { throw new UncheckedIOException(error); }
    }

    private static void requireVolume(DeleteBox box) {
        if (box.volume().compareTo(BigInteger.valueOf(MAX_CELLS)) > 0) throw new TooLarge();
    }

    private static void requireSize(int size) {
        if (size > MAX_CELLS) throw new TooLarge();
    }

    public static final class TooLarge extends IllegalArgumentException {
        public TooLarge() { super("Generation box export exceeds the cell limit"); }
    }
}
