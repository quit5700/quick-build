package cn.quit5700.buildingwand.client;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.registry.ModItems;
/** Independent warning row below vanilla's material/selection overlay. */
public final class BuildWarningHud {
 private static String key="";private static long expires;
 public static void show(String next){if(next.isEmpty())return;key=next;expires=System.nanoTime()+5_000_000_000L;}
 public static void clear(){key="";expires=0;}
 public static void register(){HudRenderCallback.EVENT.register((graphics,delta)->{
  var mc=Minecraft.getInstance();if(key.isEmpty()||System.nanoTime()>expires||mc.player==null||!mc.mouseHandler.isMouseGrabbed()||!mc.player.getMainHandItem().is(ModItems.BUILDING_WAND))return;
  var lines=mc.font.split(Component.translatable(key),Math.max(80,graphics.guiWidth()-16));
  int page=(int)((System.nanoTime()/2_500_000_000L)%Math.max(1,(lines.size()+1)/2));
  int first=page*2;for(int n=first;n<Math.min(lines.size(),first+2);n++){var line=lines.get(n);graphics.drawString(mc.font,line,(graphics.guiWidth()-mc.font.width(line))/2,graphics.guiHeight()-56+(n-first)*10,0xFFFF5555);}
 });}
}
