package cn.quit5700.light.registry;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.item.RedstoneEnergyConnectorItem;
import cn.quit5700.light.redstone.EnergyColor;
import cn.quit5700.common.item.DeliberateBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import java.util.EnumMap;
import java.util.Map;

public final class LightItems {
    public static final BlockItem CONSTANT_LIGHT_BLOCK_ITEM = block("constant_light_block", LightBlocks.CONSTANT_LIGHT_BLOCK);
    public static final BlockItem VARIABLE_CONSTANT_LIGHT_ITEM = block("variable_constant_light", LightBlocks.VARIABLE_CONSTANT_LIGHT);
    public static final BlockItem REDSTONE_ENERGY_EMITTER_ITEM = block("redstone_energy_emitter", LightBlocks.REDSTONE_ENERGY_EMITTER);
    public static final Map<EnergyColor, BlockItem> REMOTE_SWITCH_ITEMS = new EnumMap<>(EnergyColor.class);
    public static final BlockItem REDSTONE_ENERGY_REMOTE_SWITCH_ITEM;
    public static final BlockItem REDSTONE_ENERGY_SENSOR_ITEM = block("redstone_energy_sensor", LightBlocks.REDSTONE_ENERGY_SENSOR);
    public static final BlockItem REDSTONE_SIGNAL_DELAY_ITEM = block("redstone_signal_delay", LightBlocks.REDSTONE_SIGNAL_DELAY);
    public static final Item REDSTONE_ENERGY_CONNECTOR_ITEM = item("redstone_energy_connector", RedstoneEnergyConnectorItem::new);

    static {
        for (Map.Entry<EnergyColor, cn.quit5700.light.block.RedstoneEnergyRemoteSwitchBlock> entry : LightBlocks.REMOTE_SWITCHES.entrySet()) {
            String id = entry.getKey() == EnergyColor.WHITE ? "redstone_energy_remote_switch" : "redstone_energy_remote_switch_" + entry.getKey().getSerializedName();
            REMOTE_SWITCH_ITEMS.put(entry.getKey(), block(id, entry.getValue()));
        }
        REDSTONE_ENERGY_REMOTE_SWITCH_ITEM = REMOTE_SWITCH_ITEMS.get(EnergyColor.WHITE);
    }

    private static BlockItem block(String path, Block block) {
        ResourceLocation id = LightMod.id(path);
        return Registry.register(BuiltInRegistries.ITEM, id, new DeliberateBlockItem(block,
                new Item.Properties()));
    }

    private static <T extends Item> T item(String path, java.util.function.Function<Item.Properties, T> factory) {
        ResourceLocation id = LightMod.id(path);
        return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(
                new Item.Properties().stacksTo(1)));
    }

    private LightItems() {}
    public static void initialize() {}
}
