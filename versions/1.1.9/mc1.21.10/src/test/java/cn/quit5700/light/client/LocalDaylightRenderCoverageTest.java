package cn.quit5700.light.client;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertTrue;
class LocalDaylightRenderCoverageTest {
    @Test void coversEveryRenderCategory() throws Exception {
        Path path = Path.of("src", "main", "resources", "quick_build.client.mixins.json");
        byte[] bytes = Files.readAllBytes(path);
        assertTrue(bytes.length > 0 && bytes[0] == '{', "Mixin config must be UTF-8 without BOM and start with an object");
        String config = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(config.contains("BlockModelLighterCacheMixin")); assertTrue(config.contains("SodiumLightDataAccessMixin"));
        assertTrue(config.contains("EntityRendererMixin")); assertTrue(config.contains("BlockEntityRenderStateMixin"));
        assertTrue(config.contains("ChestRendererMixin")); assertTrue(config.contains("BrushableBlockRendererMixin"));
    }
}
