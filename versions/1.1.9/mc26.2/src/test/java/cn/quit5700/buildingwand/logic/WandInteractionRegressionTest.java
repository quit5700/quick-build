package cn.quit5700.buildingwand.logic;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WandInteractionRegressionTest {
 @Test void reordererHasTheApprovedSolidModel() throws Exception {
  try(var input=getClass().getClassLoader().getResourceAsStream("assets/pathfinding_beacon/models/item/id_sequence_reorderer.json")) {
   assertNotNull(input);assertTrue(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).contains("\"elements\""));
  }
 }
 @Test void rangeAndCursorRenderersNeverUseReflectionOrPlayerFeet() throws Exception {
  for(String name:java.util.List.of("BuildBoxRenderer","DeleteBoxRenderer","WandPreviewRenderer")) {
   String source=Files.readString(Path.of("src/client/java/cn/quit5700/buildingwand/client/"+name+".java"),java.nio.charset.StandardCharsets.UTF_8);
   assertFalse(source.contains("getMethod("));assertFalse(source.contains("getCameraEntity()"));
  }
 }
}
