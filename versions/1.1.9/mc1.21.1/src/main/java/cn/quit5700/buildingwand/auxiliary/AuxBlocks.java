package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.BuildingWandMod;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
public final class AuxBlocks {
 public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("generation_box");
 public static final Block GENERATION_BOX=Registry.register(BuiltInRegistries.BLOCK,ID,new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.5F,3600000F)));
 public static void register(){UseBlockCallback.EVENT.register((p,w,hand,hit)->{if(!w.getBlockState(hit.getBlockPos()).is(GENERATION_BOX))return InteractionResult.PASS;if(p instanceof ServerPlayer server)AuxServer.openBox(server,hit.getBlockPos());return InteractionResult.SUCCESS;});AttackBlockCallback.EVENT.register((p,w,hand,pos,face)->{if(!w.getBlockState(pos).is(GENERATION_BOX)||!(p instanceof ServerPlayer server))return InteractionResult.PASS;var saved=AuxState.get(((net.minecraft.server.level.ServerLevel)server.level()).getServer());for(String id:cn.quit5700.buildingwand.aux.LegacyNbt.keys(saved.root))for(var raw:cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(saved.player(java.util.UUID.fromString(id)),"Jobs")){var job=(net.minecraft.nbt.CompoundTag)raw;if(cn.quit5700.buildingwand.aux.LegacyNbt.longOr(job,"Box",0)==pos.asLong()&&cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(job,"Dim","").equals(w.dimension().location().toString()))return InteractionResult.FAIL;}return InteractionResult.PASS;});}
}
