package cn.quit5700.quickbuild.api;
import java.util.*;
/** Deterministic registry independent of Minecraft mappings. */
public final class ItemPageRegistry {
 private record Key(String mod,String group){}
 private record Group(Key key,int order,List<String> items){}
 private final Map<Key,Group> groups=new HashMap<>();
 public synchronized void register(String mod,int order,String group,List<String> ids){
  requireSegment(mod);requireSegment(group);Objects.requireNonNull(ids,"items");
  var copy=new ArrayList<String>();
  for(String id:ids){Objects.requireNonNull(id,"item id");if(!id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+"))throw new IllegalArgumentException("Invalid item id: "+id);copy.add(id);}
  var key=new Key(mod,group);groups.put(key,new Group(key,order,List.copyOf(copy)));
 }
 public synchronized boolean unregister(String mod,String group){requireSegment(mod);requireSegment(group);return groups.remove(new Key(mod,group))!=null;}
 public synchronized List<String> orderedItems(Collection<String> builtins){
  var seen=new LinkedHashSet<String>(builtins);var result=new ArrayList<String>();
  groups.values().stream().sorted(Comparator.comparingInt(Group::order).thenComparing(g->g.key.mod).thenComparing(g->g.key.group)).forEach(g->{for(String id:g.items)if(seen.add(id))result.add(id);});return List.copyOf(result);
 }
 private static void requireSegment(String value){if(value==null||!value.matches("[a-z0-9_.-]+"))throw new IllegalArgumentException("Invalid group or mod id: "+value);}
}
