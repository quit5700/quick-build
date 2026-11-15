package example.integration;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/** Copy into the caller mod; do not bundle Quick Build's implementation classes. */
public final class QuickBuildPageCompat {
    private QuickBuildPageCompat() {}
    public static boolean register(String modId, int order, String groupId, List<String> itemIds) {
        if (!FabricLoader.getInstance().isModLoaded("quick_build")) return false;
        final Class<?> api;
        try {
            api = Class.forName("cn.quit5700.quickbuild.api.Q5ItemPage");
        } catch (ClassNotFoundException missingApi) {
            return false; // Older Quick Build version: keep your own creative tab.
        }
        try {
            if (api.getField("API_VERSION").getInt(null) != 1) return false;
            api.getMethod("register", String.class, int.class, String.class, List.class)
                    .invoke(null, modId, order, groupId, itemIds);
            return true;
        } catch (InvocationTargetException failedRegistration) {
            throw new IllegalStateException("Quick Build item page registration failed", failedRegistration.getCause());
        } catch (ReflectiveOperationException unsupportedApi) {
            throw new IllegalStateException("Incompatible Quick Build item page API", unsupportedApi);
        }
    }
}