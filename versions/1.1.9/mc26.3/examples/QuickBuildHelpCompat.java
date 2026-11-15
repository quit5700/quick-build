package example.integration;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/** Copy only this wrapper into the caller mod and adjust its package. */
public final class QuickBuildHelpCompat {
    private QuickBuildHelpCompat() {}
    public static boolean register(String modId, int order, String entryId,
            String iconItemId, String titleKey, List<List<String>> pages) {
        Class<?> api = api();
        if (api == null) return false;
        try {
            api.getMethod("register", String.class, int.class, String.class,
                    String.class, String.class, List.class)
                    .invoke(null, modId, order, entryId, iconItemId, titleKey, pages);
            return true;
        } catch (InvocationTargetException error) {
            throw new IllegalStateException("Quick Build help registration failed", error.getCause());
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Incompatible Quick Build help API", error);
        }
    }
    public static boolean unregister(String modId, String entryId) {
        Class<?> api = api();
        if (api == null) return false;
        try {
            return (boolean) api.getMethod("unregister", String.class, String.class)
                    .invoke(null, modId, entryId);
        } catch (InvocationTargetException error) {
            throw new IllegalStateException("Quick Build help removal failed", error.getCause());
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Incompatible Quick Build help API", error);
        }
    }
    private static Class<?> api() {
        if (!FabricLoader.getInstance().isModLoaded("quick_build")) return null;
        try {
            Class<?> api = Class.forName("cn.quit5700.quickbuild.api.Q5HelpPage");
            return api.getField("API_VERSION").getInt(null) == 1 ? api : null;
        } catch (ClassNotFoundException missingOldVersionApi) {
            return null;
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Incompatible Quick Build help API", error);
        }
    }
}