package cn.quit5700.light.compat.jei;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.registry.LightItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Loaded only through JEI's optional plugin entrypoint, never by the server. */
public final class HiddenLegacyLightPlugin implements IModPlugin {
    @Override public Identifier getPluginUid() {
        return LightMod.id("hidden_legacy_light");
    }
    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                List.of(new ItemStack(LightItems.COPY_VARIABLE_CONSTANT_LIGHT_ITEM)));
    }
}
