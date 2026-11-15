package cn.quit5700.quickbuild.api;
import java.util.List;
/** Public help API v1; safe to register from a common mod initializer. */
public final class Q5HelpPage {
 public static final int API_VERSION=1;
 private static final HelpPageRegistry REGISTRY=new HelpPageRegistry();
 private Q5HelpPage(){}
 public static void register(String modId,int order,String entryId,String iconItemId,String titleKey,List<List<String>> pages){REGISTRY.register(modId,order,entryId,iconItemId,titleKey,pages);}
 public static boolean unregister(String modId,String entryId){return REGISTRY.unregister(modId,entryId);}
 public static List<HelpPageRegistry.Entry> entries(){return REGISTRY.snapshot();}
}
