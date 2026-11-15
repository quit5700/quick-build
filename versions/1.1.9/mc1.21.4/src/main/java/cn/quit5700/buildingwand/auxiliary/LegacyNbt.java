package cn.quit5700.buildingwand.aux;
import net.minecraft.nbt.*;
import java.util.*;
import java.util.function.*;
/** Exact typed defaults and optional getters for pre-1.21.5 NBT; never copies mutable compound children. */
public final class LegacyNbt {
 private LegacyNbt(){}
 public static String stringOr(CompoundTag n,String k,String fallback){return n.contains(k,8)?n.getString(k):fallback;}
 public static int intOr(CompoundTag n,String k,int fallback){return n.contains(k,99)?n.getInt(k):fallback;}
 public static long longOr(CompoundTag n,String k,long fallback){return n.contains(k,99)?n.getLong(k):fallback;}
 public static byte byteOr(CompoundTag n,String k,byte fallback){return n.contains(k,99)?n.getByte(k):fallback;}
 public static boolean booleanOr(CompoundTag n,String k,boolean fallback){return n.contains(k,99)?n.getBoolean(k):fallback;}
 public static double doubleOr(CompoundTag n,String k,double fallback){return n.contains(k,99)?n.getDouble(k):fallback;}
 public static float floatOr(CompoundTag n,String k,float fallback){return n.contains(k,99)?n.getFloat(k):fallback;}
 public static CompoundTag compoundOrEmpty(CompoundTag n,String k){return n.get(k) instanceof CompoundTag c?c:new CompoundTag();}
 public static CompoundTag compoundOrEmpty(ListTag n,int i){return i>=0&&i<n.size()&&n.get(i) instanceof CompoundTag c?c:new CompoundTag();}
 public static ListTag listOrEmpty(CompoundTag n,String k){return n.get(k) instanceof ListTag l?l:new ListTag();}
 public static Optional<CompoundTag> compound(CompoundTag n,String k){return n.get(k) instanceof CompoundTag c?Optional.of(c):Optional.empty();}
 public static Optional<Integer> integer(CompoundTag n,String k){return n.contains(k,99)?Optional.of(n.getInt(k)):Optional.empty();}
 public static Optional<Long> longNumber(CompoundTag n,String k){return n.contains(k,99)?Optional.of(n.getLong(k)):Optional.empty();}
 public static Optional<int[]> intArray(CompoundTag n,String k){return n.contains(k,11)?Optional.of(n.getIntArray(k)):Optional.empty();}
 public static Optional<long[]> longArray(CompoundTag n,String k){return n.contains(k,12)?Optional.of(n.getLongArray(k)):Optional.empty();}
 public static Optional<byte[]> byteArray(CompoundTag n,String k){return n.contains(k,7)?Optional.of(n.getByteArray(k)):Optional.empty();}
 public static Set<String> keys(CompoundTag n){return n.getAllKeys();}
 public static <K,V> Set<K> keys(Map<K,V> map){return map.keySet();}
 public static void each(CompoundTag n,BiConsumer<String,Tag> consumer){for(String k:n.getAllKeys())consumer.accept(k,n.get(k));}
 public static <K,V> void each(Map<K,V> n,BiConsumer<? super K,? super V> consumer){n.forEach(consumer);}
 public static <T> void each(Iterable<T> n,Consumer<? super T> consumer){n.forEach(consumer);}
 public static int numberInt(NumericTag value){return value.getAsInt();}
 public static int numberInt(Number value){return value.intValue();}
 public static long numberLong(NumericTag value){return value.getAsLong();}
 public static long numberLong(Number value){return value.longValue();}
}
