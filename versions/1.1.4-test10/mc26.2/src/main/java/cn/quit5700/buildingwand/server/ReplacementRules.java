package cn.quit5700.buildingwand.server;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.util.Set;
public final class ReplacementRules {
 private static final Set<String> SPECIAL=Set.of("crafting_table","fletching_table","composter","lodestone","respawn_anchor","bell","tnt","moving_piston","piston","sticky_piston","piston_head","redstone_lamp","tripwire","tripwire_hook","daylight_detector","lectern","enchanting_table","anvil","chipped_anvil","damaged_anvil","grindstone","stonecutter","loom","smithing_table","cartography_table","spawner","trial_spawner","vault","nether_portal","end_portal","end_portal_frame","end_gateway","command_block","repeating_command_block","chain_command_block","structure_block","structure_void","jigsaw","barrier");
 private static boolean crop(BlockState s){String id=BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();return s.is(BlockTags.CROPS)||s.getBlock() instanceof CropBlock||s.getBlock() instanceof StemBlock||s.getBlock() instanceof AttachedStemBlock||Set.of("cocoa","sweet_berry_bush","sugar_cane","bamboo","bamboo_sapling","cactus","nether_wart","kelp","kelp_plant","chorus_plant","chorus_flower","pitcher_crop").contains(id);}
 public static boolean protectedBlock(ServerLevel w,BlockPos p,BlockState s,boolean creative){
  String id=BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
  return s.hasBlockEntity()||w.getBlockEntity(p)!=null||!s.getFluidState().isEmpty()
   ||s.isSignalSource()||s.is(Blocks.REDSTONE_WIRE)||id.contains("redstone")||id.contains("pressure_plate")||id.endsWith("_button")||id.endsWith("_detector")
   ||s.getBlock() instanceof TorchBlock||s.is(BlockTags.CLIMBABLE)||s.is(Blocks.END_ROD)
   ||s.is(BlockTags.BEDS)||s.is(BlockTags.DOORS)||s.is(BlockTags.TRAPDOORS)||s.is(BlockTags.RAILS)
   ||s.getBlock() instanceof DoublePlantBlock||crop(s)||SPECIAL.contains(id)||s.getMenuProvider(w,p)!=null
   ||(!creative&&s.getDestroySpeed(w,p)<0);
 }
 public static boolean protectedPosition(ServerLevel w,BlockPos p,BlockState replacement,boolean creative){
  BlockState current=w.getBlockState(p);if(protectedBlock(w,p,current,creative))return true;
  for(Direction d:Direction.values()){
   BlockPos n=p.relative(d);BlockState adjacent=w.getBlockState(n);
   if(d==Direction.UP&&crop(adjacent))return true;
   boolean wallControl=false;
   if(adjacent.getBlock() instanceof FaceAttachedHorizontalDirectionalBlock&&adjacent.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE)){
    var face=adjacent.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE);
    wallControl=face==net.minecraft.world.level.block.state.properties.AttachFace.WALL&&adjacent.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)==d
     ||face==net.minecraft.world.level.block.state.properties.AttachFace.CEILING&&d==Direction.DOWN;
   }
   boolean attached=adjacent.getBlock() instanceof TorchBlock||adjacent.is(BlockTags.CLIMBABLE)||wallControl||(d==Direction.UP&&protectedBlock(w,n,adjacent,true));
   if(attached&&current.isFaceSturdy(w,p,d)&&!replacement.isFaceSturdy(w,p,d))return true;
  }
  var shape=replacement.getCollisionShape(w,p);AABB cell=new AABB(p);
  for(var entity:w.getEntities((net.minecraft.world.entity.Entity)null,cell.inflate(1.5,2.0,1.5),e->e.isAlive())){
   AABB body=entity.getBoundingBox();
   if(entity instanceof HangingEntity hanging&&body.move(hanging.getDirection().getOpposite().getStepX()*.5,hanging.getDirection().getOpposite().getStepY()*.5,hanging.getDirection().getOpposite().getStepZ()*.5).intersects(cell))return true;
   for(AABB box:shape.toAabbs())if(box.move(p).intersects(body))return true;
   if(entity.onGround()&&body.minY>=p.getY()&&body.minY<=p.getY()+1.001&&body.maxX>p.getX()&&body.minX<p.getX()+1&&body.maxZ>p.getZ()&&body.minZ<p.getZ()+1){
    boolean floor=false;for(AABB box:shape.toAabbs())if(box.maxY+p.getY()>=body.minY-.001&&box.move(p).maxX>body.minX&&box.move(p).minX<body.maxX&&box.move(p).maxZ>body.minZ&&box.move(p).minZ<body.maxZ){floor=true;break;}
    if(!floor)return true;
   }
  }
  return false;
 }
}
