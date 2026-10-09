package cn.quit5700.light.light;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VariableLightRecoveryTest {
    @Test
    void currentInstallPrecedesCleanupAndMissingPoweredSourceIsRecovered() throws Exception {
        String source = Files.readString(Path.of("src", "main", "java", "cn", "quit5700", "light", "light",
                "VariableLightTaskState.java"), StandardCharsets.UTF_8);
        int append = source.indexOf("private void appendPlan");
        int normalize = source.indexOf("private static ArrayList<TaskData> normalize");
        assertTrue(source.contains("if (hasSource(source, radius, spacing, mode)) return"));
        assertTrue(source.contains("reconcile(source, radius, spacing, mode, true)"));
        assertTrue(source.indexOf("plan.install()", append) < source.indexOf("plan.cleanup()", append));
        assertTrue(source.indexOf("plan.install()", normalize) < source.indexOf("plan.cleanup()", normalize));
    }
}
