package cn.quit5700.buildingwand.logic;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RangeSubmitPhaseRegressionTest {
 @Test void bothConfirmedBoxesSubmitBeforeFeatureDispatchLikeTheCursor() throws Exception {
  String cursor=Files.readString(Path.of("src/client/java/cn/quit5700/buildingwand/client/WandPreviewRenderer.java"),StandardCharsets.UTF_8);
  assertTrue(cursor.contains("LevelRenderEvents.COLLECT_SUBMITS.register"));
  for(String name:java.util.List.of("BuildBoxRenderer","DeleteBoxRenderer")) {
   String source=Files.readString(Path.of("src/client/java/cn/quit5700/buildingwand/client/"+name+".java"),StandardCharsets.UTF_8);
   assertTrue(source.contains("LevelRenderEvents.COLLECT_SUBMITS.register"),name+" must submit geometry before feature dispatch");
   assertFalse(source.contains("LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register"));
   assertTrue(source.contains("submitCustomGeometry"));
  }
 }
}
