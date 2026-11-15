package cn.quit5700.persistence;

public final class SavedDataFileName {
    private static final String WINDOWS_FORBIDDEN_CHARACTERS = "<>:\"/\\|?*";

    private SavedDataFileName() {
    }

    public static String requireSafe(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Saved data file name must not be blank");
        }
        for (int index = 0; index < id.length(); index++) {
            char character = id.charAt(index);
            if (character < 32 || WINDOWS_FORBIDDEN_CHARACTERS.indexOf(character) >= 0) {
                throw new IllegalArgumentException("Saved data file name contains a Windows-forbidden character");
            }
        }
        if (id.endsWith(".") || id.endsWith(" ")) {
            throw new IllegalArgumentException("Saved data file name must not end with a dot or space");
        }
        return id;
    }
}
