package cn.quit5700.light.item;

import cn.quit5700.light.block.RedstoneEnergyEmitterBlock;
import cn.quit5700.light.block.RedstoneEnergyRemoteSwitchBlock;
import cn.quit5700.light.block.RedstoneEnergySensorBlock;
import cn.quit5700.light.block.RedstoneEnergySensorBlockEntity;
import cn.quit5700.light.redstone.EnergyNode;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
        if (player.getCooldowns().isOnCooldown(connector.getItem())) return InteractionResult.SUCCESS;
        player.getCooldowns().addCooldown(connector.getItem(), CLICK_COOLDOWN_TICKS);
        BlockPos pos = context.getClickedPos();
        if (!(world.getBlockState(pos).getBlock() instanceof RedstoneEnergyEmitterBlock)
                && !(world.getBlockState(pos).getBlock() instanceof RedstoneEnergyRemoteSwitchBlock)
                && !(world.getBlockState(pos).getBlock() instanceof RedstoneEnergySensorBlock)) {
            if (readSelection(context.getItemInHand()) != null) notify(player, "text.quick_build.110", false);
            return InteractionResult.SUCCESS;
        }

        RedstoneEnergyState state = RedstoneEnergyState.get(serverWorld);
        EnergyNode clicked = ensureRegistered(serverWorld, pos, state);
        if (clicked == null) {
            player.displayClientMessage(Component.translatable("text.quick_build.052"), false);
            return InteractionResult.SUCCESS;
        }
        SelectedNode selected = readSelection(connector);
        if (selected == null) {
            writeSelection(connector, new SelectedNode(clicked.world(), clicked.pos()));
            notify(player, "message.quick_build.connector_selected", true, Component.translatable(displayName(clicked)));
            return InteractionResult.SUCCESS;
        }
        EnergyNode first = state.getNode(serverWorld, selected.pos()).filter(node -> node.world().equals(selected.world())).orElse(null);
        if (first == null) {
            clear(connector);
            notify(player, "text.quick_build.183", false);
            return InteractionResult.SUCCESS;
        }
        if (first.key().equals(clicked.key())) {
            clear(connector);
            notify(player, "text.quick_build.170", false);
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
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!ConnectorInteractionPolicy.shouldHandle(user.isShiftKeyDown())) {
            return InteractionResultHolder.pass(stack);
        }
        if (!world.isClientSide()) {
            if (user.getCooldowns().isOnCooldown(stack.getItem())) return InteractionResultHolder.success(stack);
            user.getCooldowns().addCooldown(stack.getItem(), CLICK_COOLDOWN_TICKS);
            if (readSelection(stack) != null) {
                clear(stack);
                notify(user, "text.quick_build.170", false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
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

    private static void notify(Player player, String message, boolean success, Object... args) {
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.displayClientMessage(Component.translatable(message, args), true);
        if (player.level() instanceof ServerLevel world) {
            SoundEvent sound = success ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.NOTE_BLOCK_BASS.value();
            world.playSound(null, player.blockPosition(), sound,
                    SoundSource.PLAYERS, 0.45F, success ? 1.4F : 0.7F);
        }
    }

    private static void writeSelection(ItemStack stack, SelectedNode selected) {
        var tag = stack.getOrCreateTag();
        tag.putString(SELECTED_WORLD, selected.world());
        tag.putLong(SELECTED_POS, selected.pos().asLong());
        tag.putInt("CustomModelData", 1);
    }

    private static SelectedNode readSelection(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return null;
        String world = tag.getString(SELECTED_WORLD);
        if (world.isEmpty() || !tag.contains(SELECTED_POS)) return null;
        return new SelectedNode(world, BlockPos.of(tag.getLong(SELECTED_POS)));
    }

    private static void clear(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return;
        tag.remove(SELECTED_WORLD);
        tag.remove(SELECTED_POS);
        tag.remove("CustomModelData");
    }

    private static String displayName(EnergyNode node) {
        return switch (node.type()) {
            case SWITCH -> "text.quick_build.064";
            case SENSOR -> "text.quick_build.063";
            case EMITTER -> "text.quick_build.062";
        };
    }

    private static String message(RedstoneEnergyState.LinkResult result) {
        return switch (result) {
            case ADDED -> "text.quick_build.083";
            case REMOVED -> "text.quick_build.167";
            case COLOR_CONFLICT -> "text.quick_build.087";
            case DIFFERENT_DIMENSION -> "text.quick_build.084";
            case INVALID_PAIR -> "text.quick_build.088";
            case INVALID_SENSOR_LINE -> "text.quick_build.086";
            case SAME_NODE -> "text.quick_build.085";
        };
    }

    private record SelectedNode(String world, BlockPos pos) {}
}
