package cn.quit5700.buildingwand.network;

import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.server.WandActions;
import cn.quit5700.buildingwand.util.ShapeMode;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public final class WandNetworking {
    private static final ResourceLocation SELECT_BLOCK = BuildingWandMod.id("select_block");
    private static final ResourceLocation PLACE_SINGLE = BuildingWandMod.id("place_single");
    private static final ResourceLocation PLACE_SHAPE = BuildingWandMod.id("place_shape");
    private static final ResourceLocation CAPTURE_MOVE_SELECTION = BuildingWandMod.id("capture_move_selection");
    private static final ResourceLocation EXECUTE_MOVE = BuildingWandMod.id("execute_move");
    private static final ResourceLocation CANCEL_MOVE_SELECTION = BuildingWandMod.id("cancel_move_selection");
    private static final ResourceLocation MOVE_SELECTION_RESULT = BuildingWandMod.id("move_selection_result");
    private static final ResourceLocation MOVE_EXECUTION_RESULT = BuildingWandMod.id("move_execution_result");
    private static final ResourceLocation QUERY_MOVE_MEMORY = BuildingWandMod.id("query_move_memory");
    private static final ResourceLocation MOVE_MEMORY = BuildingWandMod.id("move_memory");
    private static final ResourceLocation OPEN_SETTINGS = BuildingWandMod.id("open_settings");
    private static final ResourceLocation UPDATE_SETTINGS = BuildingWandMod.id("update_settings");
    private static final ResourceLocation SETTINGS_SAVED = BuildingWandMod.id("settings_saved");

    private WandNetworking() {
    }

    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(SelectBlockPayload.ID, SelectBlockPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PlaceSinglePayload.ID, PlaceSinglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PlaceShapePayload.ID, PlaceShapePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CaptureMoveSelectionPayload.ID, CaptureMoveSelectionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ExecuteMovePayload.ID, ExecuteMovePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CancelMoveSelectionPayload.ID, CancelMoveSelectionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MoveSelectionResultPayload.ID, MoveSelectionResultPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MoveExecutionResultPayload.ID, MoveExecutionResultPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(QueryMoveMemoryPayload.ID, QueryMoveMemoryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MoveMemoryPayload.ID, MoveMemoryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenSettingsPayload.ID, OpenSettingsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateSettingsPayload.ID, UpdateSettingsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SettingsSavedPayload.ID, SettingsSavedPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SelectBlockPayload.ID,
                (payload, context) -> context.server().execute(() -> WandActions.selectBlock(context.player(), payload.pos())));
        ServerPlayNetworking.registerGlobalReceiver(PlaceSinglePayload.ID,
                (payload, context) -> context.server().execute(() -> WandActions.placeSingle(context.player(), payload.pos())));
        ServerPlayNetworking.registerGlobalReceiver(PlaceShapePayload.ID,
                (payload, context) -> context.server().execute(() -> WandActions.placeShape(
                        context.player(), payload.first(), payload.second(), payload.mode())));
        ServerPlayNetworking.registerGlobalReceiver(CaptureMoveSelectionPayload.ID,
                (payload, context) -> context.server().execute(() -> WandActions.captureMoveSelection(
                        context.player(), payload.first(), payload.second())));
        ServerPlayNetworking.registerGlobalReceiver(ExecuteMovePayload.ID,
                (payload, context) -> context.server().execute(() -> WandActions.executeMove(
                        context.player(), payload.targetFirst(), payload.rotation())));
        ServerPlayNetworking.registerGlobalReceiver(CancelMoveSelectionPayload.ID,
                (payload, context) -> context.server().execute(() -> WandActions.cancelMove(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(QueryMoveMemoryPayload.ID,
                (payload, context) -> context.server().execute(() -> WandActions.sendMoveMemory(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(UpdateSettingsPayload.ID,
                (payload, context) -> context.server().execute(() -> WandActions.updateSettings(
                        context.player(), payload.planeLimit(), payload.cubeLimit(), payload.moveLimit())));
    }

    public record SelectBlockPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<SelectBlockPayload> ID = new Type<>(SELECT_BLOCK);
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectBlockPayload> CODEC = CustomPacketPayload.codec(
                SelectBlockPayload::write, SelectBlockPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
        }

        private static SelectBlockPayload read(RegistryFriendlyByteBuf buf) {
            return new SelectBlockPayload(buf.readBlockPos());
        }
    }

    public record PlaceSinglePayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<PlaceSinglePayload> ID = new Type<>(PLACE_SINGLE);
        public static final StreamCodec<RegistryFriendlyByteBuf, PlaceSinglePayload> CODEC = CustomPacketPayload.codec(
                PlaceSinglePayload::write, PlaceSinglePayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
        }

        private static PlaceSinglePayload read(RegistryFriendlyByteBuf buf) {
            return new PlaceSinglePayload(buf.readBlockPos());
        }
    }

    public record PlaceShapePayload(BlockPos first, BlockPos second, ShapeMode mode) implements CustomPacketPayload {
        public static final Type<PlaceShapePayload> ID = new Type<>(PLACE_SHAPE);
        public static final StreamCodec<RegistryFriendlyByteBuf, PlaceShapePayload> CODEC = CustomPacketPayload.codec(
                PlaceShapePayload::write, PlaceShapePayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBlockPos(first);
            buf.writeBlockPos(second);
            buf.writeVarInt(mode.ordinal());
        }

        private static PlaceShapePayload read(RegistryFriendlyByteBuf buf) {
            BlockPos first = buf.readBlockPos();
            BlockPos second = buf.readBlockPos();
            int ordinal = Math.max(0, Math.min(ShapeMode.values().length - 1, buf.readVarInt()));
            return new PlaceShapePayload(first, second, ShapeMode.values()[ordinal]);
        }
    }

    public record CaptureMoveSelectionPayload(BlockPos first, BlockPos second) implements CustomPacketPayload {
        public static final Type<CaptureMoveSelectionPayload> ID = new Type<>(CAPTURE_MOVE_SELECTION);
        public static final StreamCodec<RegistryFriendlyByteBuf, CaptureMoveSelectionPayload> CODEC = CustomPacketPayload.codec(
                CaptureMoveSelectionPayload::write, CaptureMoveSelectionPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBlockPos(first);
            buf.writeBlockPos(second);
        }

        private static CaptureMoveSelectionPayload read(RegistryFriendlyByteBuf buf) {
            return new CaptureMoveSelectionPayload(buf.readBlockPos(), buf.readBlockPos());
        }
    }

    public record ExecuteMovePayload(BlockPos targetFirst, int rotation) implements CustomPacketPayload {
        public static final Type<ExecuteMovePayload> ID = new Type<>(EXECUTE_MOVE);
        public static final StreamCodec<RegistryFriendlyByteBuf, ExecuteMovePayload> CODEC = CustomPacketPayload.codec(
                ExecuteMovePayload::write, ExecuteMovePayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBlockPos(targetFirst);
            buf.writeVarInt(rotation & 3);
        }

        private static ExecuteMovePayload read(RegistryFriendlyByteBuf buf) {
            return new ExecuteMovePayload(buf.readBlockPos(), buf.readVarInt() & 3);
        }
    }

    public record CancelMoveSelectionPayload() implements CustomPacketPayload {
        public static final Type<CancelMoveSelectionPayload> ID = new Type<>(CANCEL_MOVE_SELECTION);
        public static final StreamCodec<RegistryFriendlyByteBuf, CancelMoveSelectionPayload> CODEC = CustomPacketPayload.codec(
                CancelMoveSelectionPayload::write, CancelMoveSelectionPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
        }

        private static CancelMoveSelectionPayload read(RegistryFriendlyByteBuf buf) {
            return new CancelMoveSelectionPayload();
        }
    }

    public record MoveSelectionResultPayload(boolean accepted) implements CustomPacketPayload {
        public static final Type<MoveSelectionResultPayload> ID = new Type<>(MOVE_SELECTION_RESULT);
        public static final StreamCodec<RegistryFriendlyByteBuf, MoveSelectionResultPayload> CODEC = CustomPacketPayload.codec(
                MoveSelectionResultPayload::write, MoveSelectionResultPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(accepted);
        }

        private static MoveSelectionResultPayload read(RegistryFriendlyByteBuf buf) {
            return new MoveSelectionResultPayload(buf.readBoolean());
        }
    }

    public record MoveExecutionResultPayload(boolean completed) implements CustomPacketPayload {
        public static final Type<MoveExecutionResultPayload> ID = new Type<>(MOVE_EXECUTION_RESULT);
        public static final StreamCodec<RegistryFriendlyByteBuf, MoveExecutionResultPayload> CODEC = CustomPacketPayload.codec(
                MoveExecutionResultPayload::write, MoveExecutionResultPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(completed);
        }

        private static MoveExecutionResultPayload read(RegistryFriendlyByteBuf buf) {
            return new MoveExecutionResultPayload(buf.readBoolean());
        }
    }

    public record QueryMoveMemoryPayload() implements CustomPacketPayload {
        public static final Type<QueryMoveMemoryPayload> ID = new Type<>(QUERY_MOVE_MEMORY);
        public static final StreamCodec<RegistryFriendlyByteBuf, QueryMoveMemoryPayload> CODEC = CustomPacketPayload.codec(
                QueryMoveMemoryPayload::write, QueryMoveMemoryPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
        }

        private static QueryMoveMemoryPayload read(RegistryFriendlyByteBuf buf) {
            return new QueryMoveMemoryPayload();
        }
    }

    public record MoveMemoryPayload(boolean hasMemory, BlockPos first, BlockPos second) implements CustomPacketPayload {
        public static final Type<MoveMemoryPayload> ID = new Type<>(MOVE_MEMORY);
        public static final StreamCodec<RegistryFriendlyByteBuf, MoveMemoryPayload> CODEC = CustomPacketPayload.codec(
                MoveMemoryPayload::write, MoveMemoryPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(hasMemory);
            if (hasMemory) {
                buf.writeBlockPos(first);
                buf.writeBlockPos(second);
            }
        }

        private static MoveMemoryPayload read(RegistryFriendlyByteBuf buf) {
            boolean hasMemory = buf.readBoolean();
            return hasMemory
                    ? new MoveMemoryPayload(true, buf.readBlockPos(), buf.readBlockPos())
                    : new MoveMemoryPayload(false, BlockPos.ZERO, BlockPos.ZERO);
        }
    }

    public record OpenSettingsPayload(int planeLimit, int cubeLimit, int moveLimit, boolean editable)
            implements CustomPacketPayload {
        public static final Type<OpenSettingsPayload> ID = new Type<>(OPEN_SETTINGS);
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenSettingsPayload> CODEC = CustomPacketPayload.codec(
                OpenSettingsPayload::write, OpenSettingsPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeVarInt(planeLimit);
            buf.writeVarInt(cubeLimit);
            buf.writeVarInt(moveLimit);
            buf.writeBoolean(editable);
        }

        private static OpenSettingsPayload read(RegistryFriendlyByteBuf buf) {
            return new OpenSettingsPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
        }
    }

    public record UpdateSettingsPayload(int planeLimit, int cubeLimit, int moveLimit) implements CustomPacketPayload {
        public static final Type<UpdateSettingsPayload> ID = new Type<>(UPDATE_SETTINGS);
        public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSettingsPayload> CODEC = CustomPacketPayload.codec(
                UpdateSettingsPayload::write, UpdateSettingsPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeVarInt(planeLimit);
            buf.writeVarInt(cubeLimit);
            buf.writeVarInt(moveLimit);
        }

        private static UpdateSettingsPayload read(RegistryFriendlyByteBuf buf) {
            return new UpdateSettingsPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }
    }

    public record SettingsSavedPayload(
            boolean accepted, int planeLimit, int cubeLimit, int moveLimit) implements CustomPacketPayload {
        public static final Type<SettingsSavedPayload> ID = new Type<>(SETTINGS_SAVED);
        public static final StreamCodec<RegistryFriendlyByteBuf, SettingsSavedPayload> CODEC = CustomPacketPayload.codec(
                SettingsSavedPayload::write, SettingsSavedPayload::read);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(accepted);
            buf.writeVarInt(planeLimit);
            buf.writeVarInt(cubeLimit);
            buf.writeVarInt(moveLimit);
        }

        private static SettingsSavedPayload read(RegistryFriendlyByteBuf buf) {
            return new SettingsSavedPayload(
                    buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }
    }
}
