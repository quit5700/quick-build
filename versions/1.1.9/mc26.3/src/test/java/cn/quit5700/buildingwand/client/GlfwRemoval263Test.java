package cn.quit5700.buildingwand.client;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class GlfwRemoval263Test {
 private static void verify(String path)throws Exception{try(var in=GlfwRemoval263Test.class.getResourceAsStream(path)){assertNotNull(in);String code=new String(in.readAllBytes(),StandardCharsets.ISO_8859_1);assertFalse(code.contains("org/lwjgl/glfw/GLFW"));assertTrue(code.contains("com/mojang/blaze3d/platform/InputConstants"));}}
 @Test void wandInputHasNoRemovedRuntimeDependency()throws Exception{verify("/cn/quit5700/buildingwand/client/ClientWandController.class");}
 @Test void lightSelectionHasNoRemovedRuntimeDependency()throws Exception{verify("/cn/quit5700/light/client/AreaSelectionClient.class");}
}
