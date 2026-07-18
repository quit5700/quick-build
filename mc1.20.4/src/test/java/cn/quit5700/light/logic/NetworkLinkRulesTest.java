package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class NetworkLinkRulesTest {
    @Test
    void allowsUnlimitedDevicesWithTheSameColor() {
        assertTrue(NetworkLinkRules.colorsCompatible(Set.of("red"), false));
    }

    @Test
    void rejectsDifferentColorsWithoutASensorEndpoint() {
        assertFalse(NetworkLinkRules.colorsCompatible(Set.of("red", "blue"), false));
    }

    @Test
    void sensorEndpointMayBridgeDifferentColors() {
        assertTrue(NetworkLinkRules.colorsCompatible(Set.of("red", "blue"), true));
    }
}
