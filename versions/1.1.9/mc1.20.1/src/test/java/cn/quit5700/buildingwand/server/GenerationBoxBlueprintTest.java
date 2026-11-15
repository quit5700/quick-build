package cn.quit5700.buildingwand.server;

import cn.quit5700.buildingwand.blueprint.BlueprintReader;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.logic.DirectedDeleteCursor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
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
    }

    @Test void cancelledPlanUnitsAreExcludedAndActiveStatesRetained() {
        ListTag plan = new ListTag();
        plan.add(unit(new BlockPos(-2, 70, 4), "minecraft:stone", true));
        plan.add(unit(new BlockPos(100, 70, 4), "minecraft:gold_block", false));
        plan.add(unit(new BlockPos(-1, 70, 4), "minecraft:glass", true));
        var root = GenerationBoxBlueprint.planStructure(plan);
        assertEquals(2, cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"blocks").size());
        assertEquals(2, cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"palette").size());
        assertTrue(cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"entities").isEmpty());
        assertEquals(3, plan.size());
    }

    @Test void mixedMaterialsRoundTripInTheExistingStructureFileFormat() throws Exception {
        var plan = new ListTag();
        plan.add(unit(new BlockPos(-6,60,-5), "minecraft:stone", true));
        plan.add(unit(new BlockPos(-5,61,-4), "minecraft:glass", true));
        var root = BlueprintReader.read(Files.write(folder.resolve("mixed.nbt"), GenerationBoxBlueprint.plan(plan).join()));
        assertEquals(2, cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"blocks").size());
        assertEquals(2, cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"palette").size());
    }

    @Test void changedReplacementTaskIsRejectedBeforeSampling() {
        var box = new DeleteBox(3,60,4,5,60,4);
        var id = UUID.randomUUID();
        var result = GenerationBoxBlueprint.replacement(id, box, null,
            new DirectedDeleteCursor(box,1,1,1), group -> { fail("Stale task was sampled"); return true; }, () -> false);
        assertFalse(result.isDone());
        try {
            for (int i=0;i<10&&!result.isDone();i++) GenerationBoxBlueprint.tick();
            assertTrue(result.isDone());
            assertThrows(CompletionException.class, result::join);
        } finally {
            GenerationBoxBlueprint.cancel(id);
        }
    }

    @Test void oversizedExportFailsBeforeTraversingTheRange() {
        assertThrows(GenerationBoxBlueprint.TooLarge.class,
            () -> GenerationBoxBlueprint.build(new DeleteBox(0, 0, 0, 1024, 255, 1024), null, 1, 1, 1, 1));
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
