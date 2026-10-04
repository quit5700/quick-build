from pathlib import Path
r=Path.cwd();v=r/'versions/1.1.4-test8/mc26.2';main=v/'src/main/java/cn/quit5700/buildingwand'
(main/'server/ReplacementRules.java').write_text('''package cn.quit5700.buildingwand.server;
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
   ||s.is(BlockTags.BEDS)||s.is(BlockTags.DOORS)||s.is(BlockTags.TRAPDOORS)||s.is(BlockTags.RAILS)
   ||s.getBlock() instanceof DoublePlantBlock||crop(s)||SPECIAL.contains(id)||s.getMenuProvider(w,p)!=null
   ||(!creative&&s.getDestroySpeed(w,p)<0);
 }
 public static boolean protectedPosition(ServerLevel w,BlockPos p,BlockState replacement,boolean creative){
  BlockState current=w.getBlockState(p);if(protectedBlock(w,p,current,creative))return true;
  for(Direction d:Direction.values()){
   BlockPos n=p.relative(d);BlockState adjacent=w.getBlockState(n);
   if(d==Direction.UP&&crop(adjacent))return true;
   boolean attached=adjacent.getBlock() instanceof TorchBlock||adjacent.is(BlockTags.CLIMBABLE)||protectedBlock(w,n,adjacent,true);
   if(attached&&current.isFaceSturdy(w,p,d)&&!replacement.isFaceSturdy(w,p,d))return true;
  }
  var shape=replacement.getCollisionShape(w,p);AABB cell=new AABB(p);
  for(var entity:w.getEntities(null,cell.inflate(1.5,2.0,1.5),e->e.isAlive())){
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
''',encoding='utf-8')
p=main/'server/BuildTaskState.java';s=p.read_text(encoding='utf-8');s=s.replace('private record Data(DeleteTaskData progress,String block,int mode)', 'private record Data(DeleteTaskData progress,String block,int mode,String filter,String skipped)');s=s.replace('Codec.intRange(1,2).fieldOf("mode").forGetter(Data::mode)', 'Codec.intRange(1,3).fieldOf("mode").forGetter(Data::mode),Codec.STRING.optionalFieldOf("filter","").forGetter(Data::filter),Codec.STRING.optionalFieldOf("skipped","0").forGetter(Data::skipped)')
a=s.index(' public void add(');b=s.index(' public boolean cancel',a)
s=s[:a]+''' public void add(UUID p,String wand,Block block,DeleteSelection.Point a,DeleteSelection.Point b,int mode){add(p,wand,block,a,b,mode,Blocks.AIR);}
 public void add(UUID p,String wand,Block block,DeleteSelection.Point a,DeleteSelection.Point b,int mode,Block filter){var box=DeleteBox.between(a.x(),a.y(),a.z(),b.x(),b.y(),b.z());jobs.add(new Job(new Data(new DeleteTaskData(p.toString(),List.of(box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()),List.of(a.x(),a.y(),a.z()),"0","0",List.of(b.x()>=a.x()?1:-1,b.y()>=a.y()?1:-1,b.z()>=a.z()?1:-1),wand),BuiltInRegistries.BLOCK.getKey(block).toString(),mode,filter==Blocks.AIR?"":BuiltInRegistries.BLOCK.getKey(filter).toString(),"0")));setDirty();}
'''+s[b:]
s=s.replace('var state=w.getBlockState(pos);ItemStack material=ItemStack.EMPTY;', '''var state=w.getBlockState(pos);ItemStack material=ItemStack.EMPTY;
     if(j.mode==3){
      boolean match=j.filter==Blocks.AIR||state.is(j.filter);
      if(state.isAir()||!match||state.is(j.block)){c.advance();j.visited=j.visited.add(BigInteger.ONE);setDirty();continue;}
      if(ReplacementRules.protectedPosition(w,pos,j.block.defaultBlockState(),p.isCreative())){j.skipped=j.skipped.add(BigInteger.ONE);c.advance();j.visited=j.visited.add(BigInteger.ONE);setDirty();continue;}
     }''')
s=s.replace('||(!state.isAir()&&!state.canBeReplaced())||!supported(w,pos)', '||(j.mode!=3&&!state.isAir()&&!state.canBeReplaced())||!supported(w,pos)')
s=s.replace('BuildWandServer.message(p,"message.q5.build_done",j.placed.toString());','BuildWandServer.message(p,j.mode==3?"message.q5.replace_done":"message.q5.build_done",j.placed.toString(),j.skipped.toString());')
s=s.replace('j.paused.isEmpty()?"message.q5.build_progress":j.paused,j.placed.toString(),BuildPlan.total(j.box,j.mode).toString()', 'j.paused.isEmpty()?(j.mode==3?"message.q5.replace_progress":"message.q5.build_progress"):j.paused,j.placed.toString(),BuildPlan.total(j.box,j.mode).toString(),j.skipped.toString(),j.visited.toString()')
s=s.replace('final Block block;final int mode;', 'final Block block,filter;final int mode;').replace('BigInteger visited,placed;', 'BigInteger visited,placed,skipped;')
s=s.replace('mode=d.mode;visited=p.visitedCount();placed=p.removedCount();', 'mode=d.mode;filter=d.filter.isEmpty()?Blocks.AIR:BuiltInRegistries.BLOCK.getValue(Identifier.parse(d.filter));if(filter==null)throw new IllegalArgumentException("Invalid replacement filter");skipped=new BigInteger(d.skipped);if(skipped.signum()<0)throw new IllegalArgumentException("Invalid skipped count");visited=p.visitedCount();placed=p.removedCount();')
s=s.replace('BuiltInRegistries.BLOCK.getKey(block).toString(),mode);}', 'BuiltInRegistries.BLOCK.getKey(block).toString(),mode,filter==Blocks.AIR?"":BuiltInRegistries.BLOCK.getKey(filter).toString(),skipped.toString());}')
p.write_text(s,encoding='utf-8')
print('替换任务、保护策略及兼容旧任务的持久化已实现')