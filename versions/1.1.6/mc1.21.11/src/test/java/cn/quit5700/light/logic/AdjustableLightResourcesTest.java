package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class AdjustableLightResourcesTest {
    @Test void copyLampHasNoRecipeOrCreativeEntryButRetainsRegistry() throws Exception {
        assertFalse(Files.exists(Path.of("src/main/resources/data/light/recipe/copy_variable_constant_light.json")));
        String creative = Files.readString(Path.of("src/main/java/cn/quit5700/pathfindingbeacon/registry/ModItemGroup.java"), StandardCharsets.UTF_8);
        assertFalse(creative.contains("entries.accept(LightItems.COPY_VARIABLE_CONSTANT_LIGHT_ITEM)"));
        String registry = Files.readString(Path.of("src/main/java/cn/quit5700/light/registry/LightBlocks.java"), StandardCharsets.UTF_8);
        assertTrue(registry.contains("COPY_VARIABLE_CONSTANT_LIGHT"));
    }
    @Test void simplifiedScreenOffersMissingSelectionMessage() throws Exception {
        String screen = Files.readString(Path.of("src/main/java/cn/quit5700/light/client/VariableConstantLightScreen.java"), StandardCharsets.UTF_8);
        assertTrue(screen.contains("AdjustableLightPolicy.modes(menu.isLegacy())"));
        assertTrue(screen.contains("screen.quick_build.area_selection_required"));
        assertFalse(screen.contains("30 + mode.ordinal() * 22"));
    }
    @Test void copyLampIsHiddenFromOptionalJeiWithoutBecomingRequired() throws Exception {
        var metadata = com.google.gson.JsonParser.parseString(Files.readString(Path.of("src/main/resources/fabric.mod.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        assertFalse(metadata.getAsJsonObject("depends").has("jei"));
        assertEquals("cn.quit5700.light.compat.jei.HiddenLegacyLightPlugin", metadata.getAsJsonObject("entrypoints").getAsJsonArray("jei_mod_plugin").get(0).getAsString());
    }
}
