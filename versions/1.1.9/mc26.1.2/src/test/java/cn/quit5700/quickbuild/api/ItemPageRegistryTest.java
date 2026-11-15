package cn.quit5700.quickbuild.api;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ItemPageRegistryTest {
 @Test void priority(){var r=new ItemPageRegistry();r.register("b",20,"tools",List.of("b:one"));r.register("a",10,"tools",List.of("a:one"));assertEquals(List.of("a:one","b:one"),r.orderedItems(List.of()));}
 @Test void deterministicTies(){for(int n=0;n<2;n++){var r=new ItemPageRegistry();var mods=new ArrayList<>(List.of("z","a"));if(n==1)Collections.reverse(mods);for(var m:mods)r.register(m,1,"x",List.of(m+":one"));assertEquals(List.of("a:one","z:one"),r.orderedItems(List.of()));}}
 @Test void groupTie(){var r=new ItemPageRegistry();r.register("a",1,"z",List.of("a:z"));r.register("a",1,"b",List.of("a:b"));assertEquals(List.of("a:b","a:z"),r.orderedItems(List.of()));}
 @Test void itemOrderAndDedup(){var r=new ItemPageRegistry();r.register("a",1,"x",List.of("a:z","a:b","a:z"));assertEquals(List.of("a:z","a:b"),r.orderedItems(List.of()));}
 @Test void builtinDedup(){var r=new ItemPageRegistry();r.register("a",1,"x",List.of("building_wand:building_wand","a:b"));assertEquals(List.of("a:b"),r.orderedItems(List.of("building_wand:building_wand")));}
 @Test void replaceGroup(){var r=new ItemPageRegistry();r.register("a",1,"x",List.of("a:old"));r.register("a",1,"x",List.of("a:new"));assertEquals(List.of("a:new"),r.orderedItems(List.of()));}
 @Test void immutableInputAndSnapshot(){var r=new ItemPageRegistry();var in=new ArrayList<>(List.of("a:b"));r.register("a",1,"x",in);in.clear();var out=r.orderedItems(List.of());assertEquals(List.of("a:b"),out);assertThrows(UnsupportedOperationException.class,()->out.add("a:c"));}
 @Test void unregister(){var r=new ItemPageRegistry();r.register("a",1,"x",List.of("a:b"));assertTrue(r.unregister("a","x"));assertFalse(r.unregister("a","x"));assertTrue(r.orderedItems(List.of()).isEmpty());}
 @Test void invalidRegistrationAtomic(){var r=new ItemPageRegistry();r.register("a",1,"x",List.of("a:old"));assertThrows(IllegalArgumentException.class,()->r.register("a",1,"x",List.of("a:ok","INVALID")));assertEquals(List.of("a:old"),r.orderedItems(List.of()));assertThrows(IllegalArgumentException.class,()->r.register("INVALID",1,"x",List.of()));}
 @Test void emptyAndIndependentInstances(){var r=new ItemPageRegistry();r.register("a",1,"x",List.of());assertTrue(r.orderedItems(List.of()).isEmpty());assertTrue(new ItemPageRegistry().orderedItems(List.of()).isEmpty());}
}
