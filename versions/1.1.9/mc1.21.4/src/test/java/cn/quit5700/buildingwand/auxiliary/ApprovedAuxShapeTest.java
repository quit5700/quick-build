package cn.quit5700.buildingwand.aux;
import com.google.gson.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/** Golden masks come from the exact planar preview explicitly approved by the user. */
class ApprovedAuxShapeTest {
 private static JsonObject masks;
 @BeforeAll static void load()throws Exception{SharedConstants.tryDetectVersion();try(var stream=ApprovedAuxShapeTest.class.getResourceAsStream("/approved-aux-shapes.json")){masks=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();}}
 private static void matches(int shape,String key,boolean prism){
  var tag=AuxRow.create(BlockPos.ZERO,prism?new BlockPos(30,30,5):new BlockPos(30,0,30));tag.putInt("Shape",shape);var row=new AuxRow(tag);
  var expected=masks.getAsJsonArray(key);
  for(int y=0;y<31;y++)for(int x=0;x<31;x++)for(int depth=0;depth<(prism?6:1);depth++){
   boolean value=expected.get(y).getAsString().charAt(x)=='#';
   boolean actual=prism?row.contains(x,30-y,depth):row.contains(x,0,y);
   assertEquals(value,actual,"approved "+key+" shape="+shape+" x="+x+" imageRow="+y+" depth="+depth);
  }
 }
 @Test void planarStarMatchesTheApprovedPreview(){matches(9,"star",false);}
 @Test void starPrismHasTheApprovedUprightCrossSectionAtEveryDepth(){matches(8,"star",true);}
 @Test void planarCrescentMatchesTheApprovedPreview(){matches(11,"moon",false);}
 @Test void crescentPrismMatchesTheSameCrossSectionAtEveryDepth(){matches(10,"moon",true);}
 @Test void starTipPointsUpRatherThanDown(){var tag=AuxRow.create(BlockPos.ZERO,new BlockPos(30,30,5));tag.putInt("Shape",8);var row=new AuxRow(tag);assertTrue(row.contains(15,30,0));assertFalse(row.contains(15,0,0));for(int i=0;i<4;i++)row.rotate(2,BlockPos.ZERO,1);assertTrue(row.contains(15,30,0));}
}
