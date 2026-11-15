package cn.quit5700.light.logic;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class LightScreenColorTest {
    @Test void everyExplicitScreenTextColorIncludesOpaqueAlpha() throws Exception {
        String source=Files.readString(Path.of("src/main/java/cn/quit5700/light/client/VariableConstantLightScreen.java"),StandardCharsets.UTF_8);
        var colors=java.util.regex.Pattern.compile("0x([0-9A-Fa-f]{6,8})\\b").matcher(source);
        int count=0;
        while(colors.find()) {
            long color=Long.parseLong(colors.group(1),16);
            assertEquals(255,(color>>>24)&255,"Transparent screen color: "+colors.group());count++;
        }
        assertTrue(count>=5);
    }
}
