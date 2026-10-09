package cn.quit5700.light.client;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertTrue;
class LocalDaylightRenderCoverageTest {
    @Test void coversEveryRenderCategory() throws Exception {
        String config = Files.readString(Path.of("src", "main", "resources", "quick_build.client.mixins.json"), StandardCharsets.UTF_8);
        assertTrue(config.contains("BlockModelLighterCacheMixin"));
        assertTrue(config.contains("SodiumLightDataAccessMixin"));
        assertTrue(config.contains("EntityRendererMixin"));
        assertTrue(config.contains("BlockEntityRenderStateMixin"));
        assertTrue(config.contains("ChestRendererMixin"));
        assertTrue(config.contains("BrushableBlockRendererMixin"));
    }
}
