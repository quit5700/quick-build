package cn.quit5700.buildingwand.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SettingsSaveFlowTest {
    @Test
    void anyValueSendsImmediatelyThenWaitsForTheServer() {
        SettingsSaveFlow flow = new SettingsSaveFlow();

        assertEquals(SettingsSaveFlow.Action.SEND, flow.request());
        assertEquals(SettingsSaveFlow.Action.NONE, flow.request());

        flow.complete();
        assertEquals(SettingsSaveFlow.Action.SEND, flow.request());
    }

    @Test
    void defaultValuesSendImmediately() {
        SettingsSaveFlow flow = new SettingsSaveFlow();
        assertEquals(SettingsSaveFlow.Action.SEND, flow.request());
    }
}
