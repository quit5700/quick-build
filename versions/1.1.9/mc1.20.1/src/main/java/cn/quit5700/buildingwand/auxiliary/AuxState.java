package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.BuildingWandMod;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.*;
import java.util.UUID;
public final class AuxState extends SavedData {
 public final CompoundTag root;
 private static final String SAVE_ID="building_wand_auxiliary";
 @Override public CompoundTag save(CompoundTag tag){tag.merge(root);return tag;}

 public AuxState(){this(new CompoundTag());}private AuxState(CompoundTag tag){root=tag;}
 public static AuxState get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(AuxState::new,AuxState::new,SAVE_ID);}
 public CompoundTag player(UUID owner){String id=owner.toString();if(!root.contains(id)){var n=new CompoundTag();n.put("Rows",new ListTag());n.put("Jobs",new ListTag());n.putBoolean("Shown",true);root.put(id,n);setDirty();}var data=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(root,id);for(String key:java.util.List.of("Rows","Jobs","Refunds","Updates"))if(!data.contains(key)){data.put(key,new ListTag());setDirty();}return data;}
}
