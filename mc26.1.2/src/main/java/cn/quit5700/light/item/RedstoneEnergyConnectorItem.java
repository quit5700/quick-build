package cn.quit5700.light.item;

import cn.quit5700.light.block.RedstoneEnergyEmitterBlock;
import cn.quit5700.light.block.RedstoneEnergyRemoteSwitchBlock;
import cn.quit5700.light.block.RedstoneEnergySensorBlock;
import cn.quit5700.light.block.RedstoneEnergySensorBlockEntity;
import cn.quit5700.light.redstone.EnergyNode;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerPlayer;
import cn.quit5700.light.redstone.EnergyColor;
import cn.quit5700.light.redstone.EnergyNodeType;
import cn.quit5700.light.logic.ConnectorInteractionPolicy;

import java.util.List;

public final class RedstoneEnergyConnectorItem extends Item {
    private static final int CLICK_COOLDOWN_TICKS = 6;
    private static final String SELECTED_WORLD = "light_selected_world";
    private static final String SELECTED_POS = "light_selected_pos";
    private static final CustomModelData SELECTED_MODEL = new CustomModelData(
            List.of(), List.of(true), List.of(), List.of());

    public RedstoneEnergyConnectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null
                || !ConnectorInteractionPolicy.shouldHandle(context.getPlayer().isShiftKeyDown())) {
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!(world instanceof ServerLevel serverWorld) || context.getPlayer() == null) return InteractionResult.PASS;
        Player player = context.getPlayer();
        ItemStack connector = context.getItemInHand();
        if (player.getCooldowns().isOnCooldown(connector)) return InteractionResult.SUCCESS;
        player.getCooldowns().addCooldown(connector, CLICK_COOLDOWN_TICKS);
        BlockPos pos = context.getClickedPos();
        if (!(world.getBlockState(pos).getBlock() instanceof RedstoneEnergyEmitterBlock)
                && !(world.getBlockState(pos).getBlock() instanceof RedstoneEnergyRemoteSwitchBlock)
                && !(world.getBlockState(pos).getBlock() instanceof RedstoneEnergySensorBlock)) {
            if (readSelection(context.getItemInHand()) != null) notify(player, "请选择红石能量设备，已保留第一个点。", false);
            return InteractionResult.SUCCESS;
        }

        RedstoneEnergyState state = RedstoneEnergyState.get(serverWorld);
        EnergyNode clicked = ensureRegistered(serverWorld, pos, state);
        if (clicked == null) {
            player.sendSystemMessage(Component.literal("该方块尚未完成网络注册，请重新放置。"));
            return InteractionResult.SUCCESS;
        }
        SelectedNode selected = readSelection(connector);
        if (selected == null) {
            writeSelection(connector, new SelectedNode(clicked.world(), clicked.pos()));
            notify(player, "已选择：" + displayName(clicked), true);
            return InteractionResult.SUCCESS;
        }
        EnergyNode first = state.getNode(serverWorld, selected.pos()).filter(node -> node.world().equals(selected.world())).orElse(null);
        if (first == null) {
            clear(connector);
            notify(player, "原选择已失效，请重新选择。", false);
            return InteractionResult.SUCCESS;
        }
        if (first.key().equals(clicked.key())) {
            clear(connector);
            notify(player, "已取消本次连接。", false);
            return InteractionResult.SUCCESS;
        }
        RedstoneEnergyState.LinkResult result = state.toggleLink(serverWorld, first, clicked);
        notify(player, message(result), result == RedstoneEnergyState.LinkResult.ADDED || result == RedstoneEnergyState.LinkResult.REMOVED);
        if (result == RedstoneEnergyState.LinkResult.ADDED || result == RedstoneEnergyState.LinkResult.REMOVED) {
            clear(connector);
            if (first.type() == EnergyNodeType.SENSOR || clicked.type() == EnergyNodeType.SENSOR) {
                RedstoneEnergySensorBlockEntity.refreshNearby(serverWorld, first.pos());
                RedstoneEnergySensorBlockEntity.refreshNearby(serverWorld, clicked.pos());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!ConnectorInteractionPolicy.shouldHandle(user.isShiftKeyDown())) {
            return InteractionResult.PASS;
        }
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide()) {
            if (user.getCooldowns().isOnCooldown(stack)) return InteractionResult.SUCCESS;
            user.getCooldowns().addCooldown(stack, CLICK_COOLDOWN_TICKS);
            if (readSelection(stack) != null) {
                clear(stack);
                notify(user, "已取消本次连接。", false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static EnergyNode ensureRegistered(ServerLevel world, BlockPos pos, RedstoneEnergyState state) {
        EnergyNode existing = state.getNode(world, pos).orElse(null);
        if (existing != null) return existing;
        var block = world.getBlockState(pos).getBlock();
        if (block instanceof RedstoneEnergyRemoteSwitchBlock remoteSwitch) {
            return state.register(world, pos, EnergyNodeType.SWITCH, remoteSwitch.color(),
                    world.getBlockState(pos).getValue(RedstoneEnergyRemoteSwitchBlock.POWERED));
        }
        if (block instanceof RedstoneEnergySensorBlock) {
            return state.register(world, pos, EnergyNodeType.SENSOR, EnergyColor.NONE,
                    world.getBlockState(pos).getValue(RedstoneEnergySensorBlock.POWERED));
        }
        if (block instanceof RedstoneEnergyEmitterBlock) {
            return state.register(world, pos, EnergyNodeType.EMITTER, EnergyColor.NONE, false);
        }
        return null;
    }

    private static void notify(Player player, String message, boolean success) {
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.sendOverlayMessage(Component.literal(message));
        if (player.level() instanceof ServerLevel world) {
            SoundEvent sound = success ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.NOTE_BLOCK_BASS.value();
            world.playSound(null, player.blockPosition(), sound,
                    SoundSource.PLAYERS, 0.45F, success ? 1.4F : 0.7F);
        }
    }

    private static void writeSelection(ItemStack stack, SelectedNode selected) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(SELECTED_WORLD, selected.world());
            tag.putLong(SELECTED_POS, selected.pos().asLong());
        });
        stack.set(DataComponents.CUSTOM_MODEL_DATA, SELECTED_MODEL);
    }

    private static SelectedNode readSelection(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        var tag = data.copyTag();
        String world = tag.getStringOr(SELECTED_WORLD, "");
        if (world.isEmpty() || !tag.contains(SELECTED_POS)) return null;
        return new SelectedNode(world, BlockPos.of(tag.getLongOr(SELECTED_POS, 0L)));
    }

    private static void clear(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
        stack.remove(DataComponents.CUSTOM_MODEL_DATA);
    }

    private static String displayName(EnergyNode node) {
        return switch (node.type()) {
            case SWITCH -> "红石能量远程开关";
            case SENSOR -> "红石能量感应器";
            case EMITTER -> "红石能量发射器";
        };
    }

    private static String message(RedstoneEnergyState.LinkResult result) {
        return switch (result) {
            case ADDED -> "连接成功。";
            case REMOVED -> "已断开重复连接。";
            case COLOR_CONFLICT -> "连接失败：网络中已有其他颜色的开关。";
            case DIFFERENT_DIMENSION -> "连接失败：不能跨维度连接。";
            case INVALID_PAIR -> "连接失败：这两个设备不能连接。";
            case INVALID_SENSOR_LINE -> "连接失败：两个感应器必须同轴，中间为1至10格无障碍空间。";
            case SAME_NODE -> "连接失败：不能连接同一个方块。";
        };
    }

    private record SelectedNode(String world, BlockPos pos) {}
}
