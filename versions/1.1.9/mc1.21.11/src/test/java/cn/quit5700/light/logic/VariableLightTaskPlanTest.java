package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableLightTaskPlanTest {
    @Test
    void latestConfigurationReplacesAllOlderInstallWork() {
        VariableLightTaskPlan.Configuration old = new VariableLightTaskPlan.Configuration(64, 7);
        VariableLightTaskPlan.Configuration intermediate = new VariableLightTaskPlan.Configuration(32, 4);
        VariableLightTaskPlan.Configuration desired = new VariableLightTaskPlan.Configuration(8, 1);

        VariableLightTaskPlan.Plan plan = VariableLightTaskPlan.reconfigure(
                List.of(old, intermediate, old), desired);

        assertEquals(List.of(old, intermediate), plan.cleanup());
        assertEquals(desired, plan.install());
    }

    @Test
    void removalCancelsInstallAndCleansEveryHistoricalConfiguration() {
        VariableLightTaskPlan.Configuration first = new VariableLightTaskPlan.Configuration(14, 4);
        VariableLightTaskPlan.Configuration second = new VariableLightTaskPlan.Configuration(64, 1);

        VariableLightTaskPlan.Plan plan = VariableLightTaskPlan.remove(List.of(first, second, first));

        assertEquals(List.of(first, second), plan.cleanup());
        assertEquals(null, plan.install());
    }
}
