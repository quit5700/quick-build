package cn.quit5700.buildingwand.server;

import cn.quit5700.buildingwand.blueprint.BlueprintReader;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.logic.DirectedDeleteCursor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

class GenerationBoxBlueprintTest {
    @TempDir Path folder;

    @BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test void cancelledPlanUnitsAreExcludedAndActiveStatesRetained() {
        ListTag plan = new ListTag();
        plan.add(unit(new BlockPos(-2, 70, 4), "minecraft:stone", true));
        plan.add(unit(new BlockPos(100, 70, 4), "minecraft:gold_block", false));
        plan.add(unit(new BlockPos(-1, 70, 4), "minecraft:glass", true));
        var root = GenerationBoxBlueprint.planStructure(plan);
        assertEquals(2, root.getListOrEmpty("blocks").size());
        assertEquals(2, root.getListOrEmpty("palette").size());
        assertTrue(root.getListOrEmpty("entities").isEmpty());
        assertEquals(3, plan.size());
    }

    @Test void exportedHollowGeometryRoundTripsWithItsInteriorEmpty() throws Exception {
        var box = new DeleteBox(-8, 60, -9, -6, 62, -7);
        byte[] bytes = GenerationBoxBlueprint.build(box, Blocks.STONE, 2, -1, 1, -1).join();
        var file = Files.write(folder.resolve("shell.nbt"), bytes);
        var root = BlueprintReader.read(file);
        assertEquals(26, root.getListOrEmpty("blocks").size());
        assertEquals(1, root.getListOrEmpty("palette").size());
    }

    @Test void replacementIsSampledOnTickAndRejectsAChangedTask() throws Exception {
        var box = new DeleteBox(3, 60, 4, 5, 60, 4);
        var id = UUID.randomUUID();
        var result = GenerationBoxBlueprint.replacement(id, box, Blocks.STONE,
            new DirectedDeleteCursor(box, 1, 1, 1), group -> group.containsKey(new BlockPos(4,60,4)), () -> true);
        assertFalse(result.isDone());
        try {
            for (int i = 0; i < 10; i++) GenerationBoxBlueprint.tick();
            var root = BlueprintReader.read(Files.write(folder.resolve("replace.nbt"), result.join()));
            assertEquals(1, root.getListOrEmpty("blocks").size());
            var stale = GenerationBoxBlueprint.replacement(id, box, Blocks.STONE,
                new DirectedDeleteCursor(box, 1, 1, 1), group -> { fail("Stale task was sampled"); return true; }, () -> false);
            GenerationBoxBlueprint.tick();
            assertThrows(CompletionException.class, stale::join);
        } finally {
            GenerationBoxBlueprint.cancel(id);
        }
    }

    @Test void oversizedExportFailsBeforeTraversingTheRange() {
        assertThrows(GenerationBoxBlueprint.TooLarge.class,
            () -> GenerationBoxBlueprint.build(new DeleteBox(0, 0, 0, 1024, 255, 1024), Blocks.STONE, 1, 1, 1, 1));
    }

    private static CompoundTag unit(BlockPos at, String id, boolean active) {
        var unit = new CompoundTag();
        var ids = new ListTag();
        if (active) ids.add(StringTag.valueOf("active"));
        unit.put("Ids", ids);
        var cell = new CompoundTag();
        var state = new CompoundTag();
        state.putString("id", id);
        cell.putLong("Pos", at.asLong());
        cell.put("State", state);
        var cells = new ListTag();
        cells.add(cell);
        unit.put("Cells", cells);
        return unit;
    }
}
