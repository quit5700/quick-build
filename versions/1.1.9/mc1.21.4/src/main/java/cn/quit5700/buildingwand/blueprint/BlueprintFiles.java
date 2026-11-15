package cn.quit5700.buildingwand.blueprint;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;

/** Paths are relative to the running client's instance, never a launcher install. */
public final class BlueprintFiles {
    private BlueprintFiles() {}

    public static Path directory(Path gameDirectory) throws IOException {
        Path root = gameDirectory.toRealPath();
        Path result = root;
        for (String part : new String[]{"schematics", "q5"}) {
            result = result.resolve(part);
            try { Files.createDirectory(result); } catch (FileAlreadyExistsException ignored) { }
            if (Files.isSymbolicLink(result) || !Files.isDirectory(result, LinkOption.NOFOLLOW_LINKS))
                throw new IOException("Unsafe blueprint directory");
        }
        return result;
    }

    public static String fileName(String input) {
        if (input == null || input.isBlank() || !input.equals(input.strip()) || input.length() > 120
                || input.endsWith(".") || input.endsWith(" ") || input.matches(".*[\\x00-\\x1f<>:\"/\\\\|?*].*"))
            throw new IllegalArgumentException("Invalid Windows filename");
        String base = input.split("\\.", 2)[0].stripTrailing().toUpperCase(Locale.ROOT);
        if (base.matches("CON|PRN|AUX|NUL|COM[1-9¹²³]|LPT[1-9¹²³]") || base.isEmpty())
            throw new IllegalArgumentException("Reserved Windows filename");
        return input.toLowerCase(Locale.ROOT).endsWith(".nbt") ? input : input + ".nbt";
    }

    public static Path target(Path gameDirectory, String input) throws IOException {
        return directory(gameDirectory).resolve(fileName(input));
    }

    public static Path save(Path gameDirectory, String input, byte[] compressed, boolean overwrite) throws IOException {
        Path target = target(gameDirectory, input);
        if (Files.isSymbolicLink(target)) throw new IOException("Symbolic link target");
        Path temp = Files.createTempFile(target.getParent(), ".q5-", ".tmp");
        try {
            Files.write(temp, compressed);
            if (!overwrite) {
                // Same-directory non-replacing move refuses an existing target, including races.
                Files.move(temp, target);
            } else {
                try { Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                catch (AtomicMoveNotSupportedException ex) {
                    throw new IOException("Atomic replacement unsupported; original file retained", ex);
                }
            }
            return target;
        } finally { Files.deleteIfExists(temp); }
    }
}
