package cn.quit5700.buildingwand.util;
public final class WandTagData {public static void update(net.minecraft.world.item.ItemStack stack,java.util.function.Consumer<net.minecraft.nbt.CompoundTag> action){action.accept(stack.getOrCreateTag());}}
