package cn.quit5700.light.light;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableLightTickRegistrationTest {
    @Test
    void serverTickAdvancesPersistentVariableLightTasksInEveryWorld() throws Exception {
        String source = Files.readString(
                Path.of("src", "main", "java", "cn", "quit5700", "light", "LightMod.java"),
                StandardCharsets.UTF_8);
        assertTrue(source.contains("server.getAllLevels().forEach(world -> VariableLightTaskState.get(world).tick(world))"),
                "Server tick must advance persistent variable-light tasks in every world");
    }
}
