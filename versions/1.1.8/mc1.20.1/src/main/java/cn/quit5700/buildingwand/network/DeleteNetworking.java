package cn.quit5700.buildingwand.network;
import cn.quit5700.buildingwand.BuildingWandMod;
import cn.quit5700.buildingwand.server.DeleteWandServer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;



public final class DeleteNetworking {
    public static final int PICK=0, CONFIRM=1, CANCEL=2, QUERY=3;
    public static void register() {
        
        
        
        LegacyWandPackets.serverReceiver(Request.ID,Request::read,(p,c)->c.server().execute(()->DeleteWandServer.handle(c.player(),p)));
        DeleteWandServer.register();
    }
    public record Request(int action,BlockPos pos,long revision,long sequence) implements LegacyWandPayload {
        public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("delete_request");
        
        public net.minecraft.resources.ResourceLocation id(){return ID;}
        public void write(FriendlyByteBuf b){b.writeVarInt(action);b.writeBlockPos(pos);b.writeLong(revision);b.writeLong(sequence);}
        public static Request read(FriendlyByteBuf b){return new Request(b.readVarInt(),b.readBlockPos(),b.readLong(),b.readLong());}
    }
    public record State(String dimension,int stage,BlockPos first,BlockPos second,long revision,long sequence,boolean checking,boolean insufficient,long capacity) implements LegacyWandPayload {
        public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("delete_state");
        
        public net.minecraft.resources.ResourceLocation id(){return ID;}
        public void write(FriendlyByteBuf b){b.writeUtf(dimension,128);b.writeVarInt(stage);b.writeBlockPos(first);b.writeBlockPos(second);b.writeLong(revision);b.writeLong(sequence);b.writeBoolean(checking);b.writeBoolean(insufficient);b.writeLong(capacity);}
        public static State read(FriendlyByteBuf b){return new State(b.readUtf(128),b.readVarInt(),b.readBlockPos(),b.readBlockPos(),b.readLong(),b.readLong(),b.readBoolean(),b.readBoolean(),b.readLong());}
    }
    public record Refresh(String dimension,int x,int z,int minY,int maxY) implements LegacyWandPayload {
        public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("delete_refresh");
        
        public net.minecraft.resources.ResourceLocation id(){return ID;}
        public void write(FriendlyByteBuf b){b.writeUtf(dimension,128);b.writeVarInt(x);b.writeVarInt(z);b.writeVarInt(minY);b.writeVarInt(maxY);}
        public static Refresh read(FriendlyByteBuf b){return new Refresh(b.readUtf(128),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt());}
    }
}
