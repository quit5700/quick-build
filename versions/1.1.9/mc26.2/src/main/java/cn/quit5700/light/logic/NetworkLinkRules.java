package cn.quit5700.light.logic;

import java.util.Set;

public final class NetworkLinkRules {
    private NetworkLinkRules() {}

    public static boolean colorsCompatible(Set<String> colors, boolean touchesSensor) {
        return colors.size() <= 1 || touchesSensor;
    }
}
