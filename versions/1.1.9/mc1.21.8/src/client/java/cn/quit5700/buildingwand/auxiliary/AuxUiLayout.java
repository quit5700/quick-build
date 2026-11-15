package cn.quit5700.buildingwand.aux;

/** Keep text on whole framebuffer pixels regardless of the game's global GUI scale. */
public final class AuxUiLayout {
 private AuxUiLayout(){}
 public static int pixelScale(int width,int height){return width>=720&&height>=480?2:1;}
 public static float renderScale(int pixels,int guiScale){return pixels/(float)Math.max(1,guiScale);}
 public static int panelWidth(int available,int preferred){return Math.max(180,Math.min(available-16,preferred));}
 public static int panelHeight(int available,int preferred){return Math.max(140,Math.min(available-16,preferred));}
 public static int rows(int height,int top,int bottom,int step){return Math.max(1,(height-top-bottom)/step);}
 public static int page(int page,int count,int per){return Math.max(0,Math.min(page,Math.max(0,(count-1)/per)));}
 public static int mainWidth(int available,int height){return panelWidth(available,Math.min(620,(int)(height*1.6)));}
 public static int inventorySlot(int display){if(display<0||display>=36)throw new IllegalArgumentException("Invalid slot");return display<27?display+9:display-27;}
 public static boolean compact(int width){return width<600;}
}
