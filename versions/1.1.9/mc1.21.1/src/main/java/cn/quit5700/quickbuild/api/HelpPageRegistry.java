package cn.quit5700.quickbuild.api;
import java.util.*;
/** Immutable help entries; no Minecraft or client classes are loaded here. */
public final class HelpPageRegistry {
 public record Entry(String modId,int order,String entryId,String iconItemId,String titleKey,List<List<String>> pages){}
 private record Key(String modId,String entryId){}
 private final Map<Key,Entry> entries=new HashMap<>();
 public synchronized void register(String mod,int order,String id,String icon,String title,List<List<String>> pages){
  segment(mod);segment(id);Objects.requireNonNull(icon,"icon");if(!icon.matches("[a-z0-9_.-]+:[a-z0-9/._-]+"))throw new IllegalArgumentException("Invalid icon id: "+icon);
  key(title);Objects.requireNonNull(pages,"pages");if(pages.isEmpty())throw new IllegalArgumentException("At least one page is required");
  var copied=new ArrayList<List<String>>();for(var page:pages){Objects.requireNonNull(page,"page");if(page.isEmpty())throw new IllegalArgumentException("Empty help page");for(String line:page)key(line);copied.add(List.copyOf(page));}
  entries.put(new Key(mod,id),new Entry(mod,order,id,icon,title,List.copyOf(copied)));
 }
 public synchronized boolean unregister(String mod,String id){segment(mod);segment(id);return entries.remove(new Key(mod,id))!=null;}
 public synchronized List<Entry> snapshot(){return entries.values().stream().sorted(Comparator.comparingInt(Entry::order).thenComparing(Entry::modId).thenComparing(Entry::entryId)).toList();}
 private static void segment(String s){if(s==null||!s.matches("[a-z0-9_.-]+"))throw new IllegalArgumentException("Invalid mod or entry id: "+s);}
 private static void key(String s){if(s==null||!s.matches("[A-Za-z0-9_./:-]+"))throw new IllegalArgumentException("Invalid translation key: "+s);}
}
