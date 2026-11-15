package cn.quit5700.buildingwand;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class GenerationBoxAccessTest {
    @Test void generationBoxHasNoCraftingRecipe() {
        for (String folder : new String[]{"recipe", "recipes"})
            assertNull(getClass().getResource("/data/building_wand/" + folder + "/generation_box.json"));
    }

    @Test void generationBoxHasNoItemLoot() throws Exception {
        var stream=getClass().getResourceAsStream("/data/building_wand/loot_table/blocks/generation_box.json");
        assertNotNull(stream);
        try (var reader=new InputStreamReader(stream,StandardCharsets.UTF_8)) {
            assertEquals(0,JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("pools").size());
        }
    }

    @Test void oldGenerationBoxItemsRejectManualPlacement() throws Exception {
        try(var stream=getClass().getResourceAsStream("/cn/quit5700/buildingwand/item/GenerationBoxItem.class")) {
            assertNotNull(stream);
            var node=new ClassNode();
            new ClassReader(stream).accept(node,0);
            var use=node.methods.stream().filter(method->method.name.equals("useOn")).findFirst().orElseThrow();
            boolean fails=false;
            for(var instruction:use.instructions) {
                if(instruction instanceof FieldInsnNode field && field.name.equals("FAIL"))fails=true;
                assertFalse(instruction instanceof MethodInsnNode call && call.name.equals("useOn"),
                        "Retained legacy item must not invoke block placement.");
            }
            assertTrue(fails);
        }
    }

    @Test void generationBoxIsNotListedInCreativeTab() throws Exception {
        try(var stream=getClass().getResourceAsStream("/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.class")) {
            assertNotNull(stream);
            var node=new ClassNode();
            new ClassReader(stream).accept(node,0);
            for(var method:node.methods)for(var instruction:method.instructions)
                assertFalse(instruction instanceof FieldInsnNode field && field.name.equals("GENERATION_BOX_ITEM"));
        }
    }
}