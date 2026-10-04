package cn.quit5700.light.registry;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.menu.RedstoneDelayMenu;
import cn.quit5700.light.menu.VariableConstantLightMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class LightMenus {
    public static final MenuType<RedstoneDelayMenu> REDSTONE_DELAY = Registry.register(
            BuiltInRegistries.MENU, LightMod.id("redstone_delay"),
            new MenuType<>(RedstoneDelayMenu::new, FeatureFlags.VANILLA_SET));
    public static final MenuType<VariableConstantLightMenu> VARIABLE_CONSTANT_LIGHT = Registry.register(
            BuiltInRegistries.MENU, LightMod.id("variable_constant_light"),
            new MenuType<>(VariableConstantLightMenu::new, FeatureFlags.VANILLA_SET));

    private LightMenus() {}
    public static void initialize() {}
}
