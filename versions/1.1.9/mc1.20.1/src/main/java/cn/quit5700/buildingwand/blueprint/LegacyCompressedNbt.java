package cn.quit5700.buildingwand.blueprint;
import java.io.*;
import java.nio.file.*;
import java.util.zip.GZIPInputStream;
import net.minecraft.nbt.*;
/** Bounded decompression for the native 1.20.1 API. */
public final class LegacyCompressedNbt {
 public static CompoundTag read(Path path,NbtAccounter limit)throws IOException{try(var input=Files.newInputStream(path)){return read(input,limit);}}
 public static CompoundTag read(InputStream input,NbtAccounter limit)throws IOException{try(var data=new DataInputStream(new BufferedInputStream(new GZIPInputStream(input)))){return NbtIo.read(data,limit);}}
}
