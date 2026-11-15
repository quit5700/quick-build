package cn.quit5700.quickbuild.api;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class HelpPageRegistryTest {
 private static void add(HelpPageRegistry r,String mod,int order,String id){r.register(mod,order,id,mod+":icon","help."+mod+".title",List.of(List.of("help."+mod+".one")));}
 @Test void priority(){var r=new HelpPageRegistry();add(r,"a",20,"x");add(r,"b",10,"x");assertEquals(List.of("b","a"),r.snapshot().stream().map(HelpPageRegistry.Entry::modId).toList());}
 @Test void deterministicTie(){for(int n=0;n<2;n++){var r=new HelpPageRegistry();var mods=new ArrayList<>(List.of("z","a"));if(n==1)Collections.reverse(mods);for(String m:mods)add(r,m,1,"x");assertEquals(List.of("a","z"),r.snapshot().stream().map(HelpPageRegistry.Entry::modId).toList());}}
 @Test void entryTie(){var r=new HelpPageRegistry();add(r,"a",1,"z");add(r,"a",1,"b");assertEquals(List.of("b","z"),r.snapshot().stream().map(HelpPageRegistry.Entry::entryId).toList());}
 @Test void update(){var r=new HelpPageRegistry();add(r,"a",1,"x");r.register("a",2,"x","a:other","help.a.changed",List.of(List.of("help.a.new")));assertEquals(1,r.snapshot().size());assertEquals("help.a.changed",r.snapshot().get(0).titleKey());assertEquals(2,r.snapshot().get(0).order());}
 @Test void separateOwners(){var r=new HelpPageRegistry();add(r,"a",1,"x");add(r,"b",1,"x");assertEquals(2,r.snapshot().size());}
 @Test void unregister(){var r=new HelpPageRegistry();add(r,"a",1,"x");assertTrue(r.unregister("a","x"));assertFalse(r.unregister("a","x"));assertTrue(r.snapshot().isEmpty());}
 @Test void deepCopy(){var r=new HelpPageRegistry();var lines=new ArrayList<>(List.of("help.a.one"));var pages=new ArrayList<List<String>>();pages.add(lines);r.register("a",1,"x","a:icon","help.a.title",pages);lines.clear();pages.clear();assertEquals(List.of(List.of("help.a.one")),r.snapshot().get(0).pages());}
 @Test void immutableSnapshot(){var r=new HelpPageRegistry();add(r,"a",1,"x");var snap=r.snapshot();assertThrows(UnsupportedOperationException.class,()->snap.clear());assertThrows(UnsupportedOperationException.class,()->snap.get(0).pages().clear());assertThrows(UnsupportedOperationException.class,()->snap.get(0).pages().get(0).clear());}
 @Test void invalidUpdateAtomic(){var r=new HelpPageRegistry();add(r,"a",1,"x");assertThrows(IllegalArgumentException.class,()->r.register("a",1,"x","INVALID","help.a.title",List.of(List.of("help.a.one"))));assertEquals("a:icon",r.snapshot().get(0).iconItemId());assertThrows(IllegalArgumentException.class,()->add(r,"INVALID",1,"x"));}
 @Test void pagesAndValidation(){var r=new HelpPageRegistry();assertThrows(IllegalArgumentException.class,()->r.register("a",1,"x","a:icon","help.a.title",List.of()));assertThrows(IllegalArgumentException.class,()->r.register("a",1,"x","a:icon","",List.of(List.of("help.a.one"))));r.register("a",1,"x","a:icon","help.a.title",List.of(List.of("help.a.one","help.a.two"),List.of("help.a.three")));assertEquals(List.of(List.of("help.a.one","help.a.two"),List.of("help.a.three")),r.snapshot().get(0).pages());}
}
