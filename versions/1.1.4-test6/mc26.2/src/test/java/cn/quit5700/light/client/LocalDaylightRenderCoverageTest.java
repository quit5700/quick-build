package cn.quit5700.light.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalDaylightRenderCoverageTest {
    @Test
    void localDaylightCoversBlocksSodiumAndEntities() throws IOException {
        String config = Files.readString(
                Path.of("src", "main", "resources", "quick_build.client.mixins.json"),
                StandardCharsets.UTF_8);

        assertTrue(config.contains("BlockModelLighterCacheMixin"), "Vanilla block lighting mixin is missing");
        assertTrue(config.contains("SodiumLightDataAccessMixin"), "Sodium block lighting mixin is missing");
        assertTrue(config.contains("EntityRendererMixin"), "Entity lighting mixin is missing");
        assertTrue(config.contains("BlockEntityRenderStateMixin"), "Block entity lighting mixin is missing");
        assertTrue(config.contains("ChestRendererMixin"), "Combined chest lighting mixin is missing");
        assertTrue(config.contains("BrushableBlockRendererMixin"), "Brushable block item lighting mixin is missing");
    }
}
