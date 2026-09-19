package cn.quit5700.light.logic;

import java.util.LinkedHashSet;
import java.util.List;

public final class VariableLightTaskPlan {
    private VariableLightTaskPlan() {
    }

    public static Plan reconfigure(List<Configuration> historical, Configuration desired) {
        return new Plan(distinct(historical), desired);
    }

    public static Plan remove(List<Configuration> historical) {
        return new Plan(distinct(historical), null);
    }

    private static List<Configuration> distinct(List<Configuration> configurations) {
        return List.copyOf(new LinkedHashSet<>(configurations));
    }

    public record Configuration(int radius, int spacing) {
        public Configuration {
            radius = VariableConstantLightSettings.clampRadius(radius);
            spacing = VariableConstantLightSettings.clampSpacing(spacing);
        }
    }

    public record Plan(List<Configuration> cleanup, Configuration install) {
        public Plan {
            cleanup = List.copyOf(cleanup);
        }
    }
}
