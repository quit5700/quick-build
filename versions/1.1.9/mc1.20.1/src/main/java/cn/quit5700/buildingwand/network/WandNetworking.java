package cn.quit5700.buildingwand.network;

import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.server.WandActions;
import cn.quit5700.buildingwand.util.ShapeMode;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class WandNetworking {
    public static final ResourceLocation SELECT_BLOCK = BuildingWandMod.id("select_block");
    public static final ResourceLocation PLACE_SINGLE = BuildingWandMod.id("place_single");
    public static final ResourceLocation PLACE_SHAPE = BuildingWandMod.id("place_shape");
    public static final ResourceLocation CAPTURE_MOVE_SELECTION = BuildingWandMod.id("capture_move_selection");
    public static final ResourceLocation EXECUTE_MOVE = BuildingWandMod.id("execute_move");
    public static final ResourceLocation CANCEL_MOVE_SELECTION = BuildingWandMod.id("cancel_move_selection");
    public static final ResourceLocation MOVE_SELECTION_RESULT = BuildingWandMod.id("move_selection_result");
    public static final ResourceLocation MOVE_EXECUTION_RESULT = BuildingWandMod.id("move_execution_result");
    public static final ResourceLocation QUERY_MOVE_MEMORY = BuildingWandMod.id("query_move_memory");
    public static final ResourceLocation MOVE_MEMORY = BuildingWandMod.id("move_memory");
    public static final ResourceLocation OPEN_SETTINGS = BuildingWandMod.id("open_settings");
    public static final ResourceLocation UPDATE_SETTINGS = BuildingWandMod.id("update_settings");
    public static final ResourceLocation SETTINGS_SAVED = BuildingWandMod.id("settings_saved");

    private WandNetworking() {}

    public interface PacketData { void write(FriendlyByteBuf buf); }

    public static FriendlyByteBuf buffer(PacketData data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        data.write(buf);
        return buf;
    }

    public static void send(ServerPlayer player, ResourceLocation channel, PacketData data) {
        ServerPlayNetworking.send(player, channel, buffer(data));
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(SELECT_BLOCK, (server, player, handler, buf, response) -> {
            SelectBlockPayload payload = SelectBlockPayload.read(buf);
            server.execute(() -> WandActions.selectBlock(player, payload.pos()));
        });
        ServerPlayNetworking.registerGlobalReceiver(PLACE_SINGLE, (server, player, handler, buf, response) -> {
            PlaceSinglePayload payload = PlaceSinglePayload.read(buf);
            server.execute(() -> WandActions.placeSingle(player, payload.pos()));
        });
        ServerPlayNetworking.registerGlobalReceiver(PLACE_SHAPE, (server, player, handler, buf, response) -> {
            PlaceShapePayload payload = PlaceShapePayload.read(buf);
            server.execute(() -> WandActions.placeShape(player, payload.first(), payload.second(), payload.mode()));
        });
        ServerPlayNetworking.registerGlobalReceiver(CAPTURE_MOVE_SELECTION, (server, player, handler, buf, response) -> {
            CaptureMoveSelectionPayload payload = CaptureMoveSelectionPayload.read(buf);
            server.execute(() -> WandActions.captureMoveSelection(player, payload.first(), payload.second()));
        });
        ServerPlayNetworking.registerGlobalReceiver(EXECUTE_MOVE, (server, player, handler, buf, response) -> {
            ExecuteMovePayload payload = ExecuteMovePayload.read(buf);
            server.execute(() -> WandActions.executeMove(player, payload.targetFirst(), payload.rotation()));
        });
        ServerPlayNetworking.registerGlobalReceiver(CANCEL_MOVE_SELECTION, (server, player, handler, buf, response) ->
                server.execute(() -> WandActions.cancelMove(player)));
        ServerPlayNetworking.registerGlobalReceiver(QUERY_MOVE_MEMORY, (server, player, handler, buf, response) ->
                server.execute(() -> WandActions.sendMoveMemory(player)));
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_SETTINGS, (server, player, handler, buf, response) -> {
            UpdateSettingsPayload payload = UpdateSettingsPayload.read(buf);
            server.execute(() -> WandActions.updateSettings(player, payload.planeLimit(), payload.cubeLimit(), payload.moveLimit()));
        });
    }

    public record SelectBlockPayload(BlockPos pos) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBlockPos(pos); }
        public static SelectBlockPayload read(FriendlyByteBuf buf) { return new SelectBlockPayload(buf.readBlockPos()); }
    }
    public record PlaceSinglePayload(BlockPos pos) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBlockPos(pos); }
        public static PlaceSinglePayload read(FriendlyByteBuf buf) { return new PlaceSinglePayload(buf.readBlockPos()); }
    }
    public record PlaceShapePayload(BlockPos first, BlockPos second, ShapeMode mode) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBlockPos(first); buf.writeBlockPos(second); buf.writeVarInt(mode.ordinal()); }
        public static PlaceShapePayload read(FriendlyByteBuf buf) {
            BlockPos first = buf.readBlockPos();
            BlockPos second = buf.readBlockPos();
            int ordinal = Math.max(0, Math.min(ShapeMode.values().length - 1, buf.readVarInt()));
            return new PlaceShapePayload(first, second, ShapeMode.values()[ordinal]);
        }
    }
    public record CaptureMoveSelectionPayload(BlockPos first, BlockPos second) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBlockPos(first); buf.writeBlockPos(second); }
        public static CaptureMoveSelectionPayload read(FriendlyByteBuf buf) { return new CaptureMoveSelectionPayload(buf.readBlockPos(), buf.readBlockPos()); }
    }
    public record ExecuteMovePayload(BlockPos targetFirst, int rotation) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBlockPos(targetFirst); buf.writeVarInt(rotation & 3); }
        public static ExecuteMovePayload read(FriendlyByteBuf buf) { return new ExecuteMovePayload(buf.readBlockPos(), buf.readVarInt() & 3); }
    }
    public record CancelMoveSelectionPayload() implements PacketData { public void write(FriendlyByteBuf buf) {} }
    public record MoveSelectionResultPayload(boolean accepted) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBoolean(accepted); }
        public static MoveSelectionResultPayload read(FriendlyByteBuf buf) { return new MoveSelectionResultPayload(buf.readBoolean()); }
    }
    public record MoveExecutionResultPayload(boolean completed) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBoolean(completed); }
        public static MoveExecutionResultPayload read(FriendlyByteBuf buf) { return new MoveExecutionResultPayload(buf.readBoolean()); }
    }
    public record QueryMoveMemoryPayload() implements PacketData { public void write(FriendlyByteBuf buf) {} }
    public record MoveMemoryPayload(boolean hasMemory, BlockPos first, BlockPos second) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBoolean(hasMemory); if (hasMemory) { buf.writeBlockPos(first); buf.writeBlockPos(second); } }
        public static MoveMemoryPayload read(FriendlyByteBuf buf) {
            boolean hasMemory = buf.readBoolean();
            return hasMemory ? new MoveMemoryPayload(true, buf.readBlockPos(), buf.readBlockPos()) : new MoveMemoryPayload(false, BlockPos.ZERO, BlockPos.ZERO);
        }
    }
    public record OpenSettingsPayload(int planeLimit, int cubeLimit, int moveLimit, boolean editable) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeVarInt(planeLimit); buf.writeVarInt(cubeLimit); buf.writeVarInt(moveLimit); buf.writeBoolean(editable); }
        public static OpenSettingsPayload read(FriendlyByteBuf buf) { return new OpenSettingsPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean()); }
    }
    public record UpdateSettingsPayload(int planeLimit, int cubeLimit, int moveLimit) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeVarInt(planeLimit); buf.writeVarInt(cubeLimit); buf.writeVarInt(moveLimit); }
        public static UpdateSettingsPayload read(FriendlyByteBuf buf) { return new UpdateSettingsPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()); }
    }
    public record SettingsSavedPayload(boolean accepted, int planeLimit, int cubeLimit, int moveLimit) implements PacketData {
        public void write(FriendlyByteBuf buf) { buf.writeBoolean(accepted); buf.writeVarInt(planeLimit); buf.writeVarInt(cubeLimit); buf.writeVarInt(moveLimit); }
        public static SettingsSavedPayload read(FriendlyByteBuf buf) { return new SettingsSavedPayload(buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()); }
    }
}
