package cn.quit5700.light.compat.jade;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.block.RedstoneEnergyEmitterBlock;
import cn.quit5700.light.block.RedstoneEnergyEmitterBlockEntity;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class LightJadePlugin implements IWailaPlugin {
    private static final Identifier UID = LightMod.id("redstone_energy_network");

    @Override public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(DataProvider.INSTANCE, RedstoneEnergyEmitterBlockEntity.class);
    }

    @Override public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(UID, true);
        registration.registerBlockComponent(ComponentProvider.INSTANCE, RedstoneEnergyEmitterBlock.class);
        registration.markAsClientFeature(UID);
        registration.markAsServerFeature(UID);
    }

    private enum DataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;
        @Override public Identifier getUid() { return UID; }
        @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getLevel() instanceof ServerLevel world)) return;
            RedstoneEnergyState.NetworkInfo info = RedstoneEnergyState.get(world).info(world, accessor.getPosition());
            data.putString("networkId", info.networkId());
            data.putInt("nodes", info.nodes());
            data.putInt("switches", info.switches());
            data.putInt("sensors", info.sensors());
            data.putInt("emitters", info.emitters());
            data.putBoolean("powered", info.powered());
            data.putString("color", info.color().getSerializedName());
        }
    }

    private enum ComponentProvider implements IBlockComponentProvider {
        INSTANCE;
        @Override public Identifier getUid() { return UID; }
        @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            tooltip.add(Component.translatable("display.quick_build.network_id", data.getString("networkId").orElse("-")));
            tooltip.add(Component.translatable("display.quick_build.network_nodes", data.getInt("nodes").orElse(0)));
            tooltip.add(Component.translatable("display.quick_build.network_counts", data.getInt("switches").orElse(0),
                    data.getInt("sensors").orElse(0), data.getInt("emitters").orElse(0)));
            boolean powered = data.getBoolean("powered").orElse(false);
            tooltip.add(Component.translatable("display.quick_build.network_status",
                    Component.translatable(powered ? "text.quick_build.177" : "text.quick_build.142")));
            tooltip.add(Component.translatable("display.quick_build.output_signal", powered ? 15 : 0));
        }
    }
}
