package cn.quit5700.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SavedDataFileNameTest {
    @Test
    void acceptsCurrentFlatSavedDataIds() {
        assertEquals("light_redstone_networks",
                SavedDataFileName.requireSafe("light_redstone_networks"));
        assertEquals("pathfinding_beacon_routes",
                SavedDataFileName.requireSafe("pathfinding_beacon_routes"));
    }

    @Test
    void rejectsNamespacedIdsAndOtherWindowsForbiddenCharacters() {
        for (String id : new String[] {
                "light:light_redstone_networks",
                "pathfinding/beacon",
                "pathfinding\\beacon",
                "route?data",
                "route*data",
                "route\"data",
                "route<data",
                "route>data",
                "route|data"
        }) {
            assertThrows(IllegalArgumentException.class, () -> SavedDataFileName.requireSafe(id), id);
        }
    }
}
