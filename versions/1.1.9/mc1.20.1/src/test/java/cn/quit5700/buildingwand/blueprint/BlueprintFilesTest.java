package cn.quit5700.buildingwand.blueprint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class BlueprintFilesTest {
    @TempDir Path temp;
    @Test void chineseInstanceAndFilenameArePreserved() throws Exception {
        Path game=Files.createDirectories(temp.resolve("\u4e2d\u6587\u6e38\u620f \u5b9e\u4f8b\uff0826.3\uff09"));
        Path file=BlueprintFiles.save(game,"\u5927\u578b\u5efa\u7b51 \u6d4b\u8bd5",new byte[]{1,2,3},false);
        assertEquals(game.resolve("schematics/q5/\u5927\u578b\u5efa\u7b51 \u6d4b\u8bd5.nbt"),file);
        assertArrayEquals(new byte[]{1,2,3},Files.readAllBytes(file));
    }
    @Test void refusesTraversalAbsoluteAndReservedWindowsNames() {
        for(String value:new String[]{"../\u7bb1\u5b50","..\\\u7bb1\u5b50","C:\\test","\\\\server\\share","CON","NUL.nbt","aux.test","com1.nbt","lpt9","bad:","bad?","bad.","bad ",".nbt","../", "CON .nbt"})
            assertThrows(IllegalArgumentException.class,()->BlueprintFiles.fileName(value),value);
    }
    @Test void separateInstancesNeverShareBlueprintFolders() throws Exception {
        Path a=Files.createDirectory(temp.resolve("\u7248\u672c\u7532")), b=Files.createDirectory(temp.resolve("\u7248\u672c\u4e59"));
        BlueprintFiles.save(a,"\u7ed3\u6784",new byte[]{7},false);
        assertFalse(Files.exists(BlueprintFiles.target(b,"\u7ed3\u6784")));
    }
    @Test void refusesOverwriteUnlessExplicitlyConfirmed() throws Exception {
        Path path=BlueprintFiles.save(temp,"\u57ce\u5821",new byte[]{1},false);
        assertThrows(FileAlreadyExistsException.class,()->BlueprintFiles.save(temp,"\u57ce\u5821",new byte[]{2},false));
        assertArrayEquals(new byte[]{1},Files.readAllBytes(path));
        BlueprintFiles.save(temp,"\u57ce\u5821",new byte[]{3},true);
        assertArrayEquals(new byte[]{3},Files.readAllBytes(path));
        try(var paths=Files.list(path.getParent())){assertEquals(1,paths.count());}
    }
    @Test void concurrentSaveCannotSilentlyOverwrite() throws Exception {
        BlueprintFiles.directory(temp);
        var executor=Executors.newFixedThreadPool(2); try {
            var tasks=java.util.List.of((Callable<Boolean>)()->attempt((byte)1),()->attempt((byte)2));
            int successes=0;for(var future:executor.invokeAll(tasks))if(future.get())successes++;
            assertEquals(1,successes);assertEquals(1,Files.size(BlueprintFiles.target(temp,"\u540c\u65f6\u4fdd\u5b58")));
        } finally {executor.shutdown();assertTrue(executor.awaitTermination(30,TimeUnit.SECONDS));}
    }
    private boolean attempt(byte value)throws Exception {
        try{BlueprintFiles.save(temp,"\u540c\u65f6\u4fdd\u5b58",new byte[]{value},false);return true;}
        catch(FileAlreadyExistsException expected){return false;}
    }
}
