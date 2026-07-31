package cn.quit5700.light.client;

import cn.quit5700.light.registry.LightMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class LightClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ClientDaylightState.initialize();
        MenuScreens.register(LightMenus.REDSTONE_DELAY, RedstoneDelayScreen::new);
        MenuScreens.register(LightMenus.VARIABLE_CONSTANT_LIGHT, VariableConstantLightScreen::new);
    }
}
