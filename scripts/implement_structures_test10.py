from pathlib import Path
r=Path.cwd();v=r/'versions/1.1.4-test10/mc26.2';base=v/'src/main/java/cn/quit5700/buildingwand'
(base/'logic/StructureLayout.java').write_text('''package cn.quit5700.buildingwand.logic;
/** Two-cell tiling without storing a region-sized plan. Axis: 0=X, 1=Y, 2=Z. */
public final class StructureLayout {
 public record Pair(int x,int y,int z,int mx,int my,int mz,boolean anchor,boolean included,boolean remainder){}
 public static long length(DeleteBox b,int axis){return (long)max(b,axis)-min(b,axis)+1;}
 public static int axis(DeleteBox b,boolean vertical,int mode){
  if(vertical)return 1;
  if(mode==2){if(length(b,0)%2==0)return 0;if(length(b,2)%2==0)return 2;return -1;}
  return length(b,0)>=2?0:2;
 }
 public static boolean fits(DeleteBox b,boolean vertical,int mode){int a=axis(b,vertical,mode);return a>=0&&length(b,a)>=2&&(mode!=2||length(b,a)%2==0);}
 public static Pair pair(DeleteBox b,int x,int y,int z,int axis,int mode){
  int at=axis==0?x:axis==1?y:z,lo=min(b,axis),hi=max(b,axis);long offset=(long)at-lo;
  boolean rest=(length(b,axis)%2!=0&&at==hi);int first=rest?at:(int)((long)at-(offset%2));
  int ax=axis==0?first:x,ay=axis==1?first:y,az=axis==2?first:z;
  int mx=axis==0&&!rest?first+1:ax,my=axis==1&&!rest?first+1:ay,mz=axis==2&&!rest?first+1:az;
  boolean included=mode!=2||BuildPlan.includes(b,ax,ay,az,2)||BuildPlan.includes(b,mx,my,mz,2);
  return new Pair(ax,ay,az,mx,my,mz,rest||at==first,included,rest);
 }
 private static int min(DeleteBox b,int a){return a==0?b.minX():a==1?b.minY():b.minZ();}
 private static int max(DeleteBox b,int a){return a==0?b.maxX():a==1?b.maxY():b.maxZ();}
}
''',encoding='utf-8')
(base/'server/StructurePlacement.java').write_text('''package cn.quit5700.buildingwand.server;
import cn.quit5700.buildingwand.logic.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import java.util.*;
/** Complete structures are assembled before notifying neighbours. No source inventory is copied. */
public final class StructurePlacement {
 private static final int QUIET=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SKIP_ALL_SIDEEFFECTS;
 public static boolean multiple(Block b){return b instanceof BedBlock||b instanceof DoorBlock||b instanceof DoublePlantBlock;}
 public static boolean vertical(Block b){return !(b instanceof BedBlock);}
 public static boolean fits(Block b,DeleteBox box,int mode){return !multiple(b)||StructureLayout.fits(box,vertical(b),mode);}
 public static LinkedHashMap<BlockPos,BlockState> single(Block b,BlockPos at){return states(b,at,at.relative(vertical(b)?Direction.UP:Direction.EAST));}
 public static LinkedHashMap<BlockPos,BlockState> plan(Block b,DeleteBox box,BlockPos at,int mode){
  if(!multiple(b)){var m=new LinkedHashMap<BlockPos,BlockState>();if(BuildPlan.includes(box,at.getX(),at.getY(),at.getZ(),mode))m.put(at,b.defaultBlockState());return m;}
  int axis=StructureLayout.axis(box,vertical(b),mode);if(axis<0)return new LinkedHashMap<>();
  var p=StructureLayout.pair(box,at.getX(),at.getY(),at.getZ(),axis,mode);if(!p.anchor()||!p.included())return new LinkedHashMap<>();
  var a=new BlockPos(p.x(),p.y(),p.z());var m=new BlockPos(p.mx(),p.my(),p.mz());
  if(p.remainder()){var result=new LinkedHashMap<BlockPos,BlockState>();result.put(a,Blocks.AIR.defaultBlockState());return result;}
  return states(b,a,m);
 }
 private static LinkedHashMap<BlockPos,BlockState> states(Block b,BlockPos a,BlockPos mate){
  var result=new LinkedHashMap<BlockPos,BlockState>();var s=b.defaultBlockState();
  if(b instanceof BedBlock){Direction facing=mate.getX()!=a.getX()?Direction.EAST:Direction.SOUTH;s=s.setValue(BedBlock.FACING,facing).setValue(BedBlock.OCCUPIED,false);result.put(a,s.setValue(BedBlock.PART,BedPart.FOOT));result.put(mate,s.setValue(BedBlock.PART,BedPart.HEAD));}
  else if(b instanceof DoorBlock||b instanceof DoublePlantBlock){result.put(a,s.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.LOWER));result.put(mate,s.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.UPPER));}
  else result.put(a,s);
  return result;
 }
 public static boolean place(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired){
  var old=new LinkedHashMap<BlockPos,BlockState>();
  for(var e:desired.entrySet()){if(w.getBlockEntity(e.getKey())!=null&&!w.getBlockState(e.getKey()).equals(e.getValue()))return false;old.put(e.getKey(),w.getBlockState(e.getKey()));}
  for(var e:desired.entrySet())if(!w.getBlockState(e.getKey()).equals(e.getValue())&&!w.setBlock(e.getKey(),e.getValue(),QUIET)){restore(w,old);return false;}
  for(var e:desired.entrySet())if(!e.getValue().isAir()&&!e.getValue().canSurvive(w,e.getKey())){restore(w,old);return false;}
  for(var e:desired.entrySet()){
   var s=w.getBlockState(e.getKey());if(s.getBlock() instanceof ChestBlock&&s.getValue(ChestBlock.TYPE)!=ChestType.SINGLE)w.setBlock(e.getKey(),s.setValue(ChestBlock.TYPE,ChestType.SINGLE),QUIET);
   w.updateNeighborsAt(e.getKey(),s.getBlock());connect(w,e.getKey());for(var d:Direction.values())connect(w,e.getKey().relative(d));
  }
  for(var e:desired.entrySet())if(w.getBlockState(e.getKey()).getBlock()!=e.getValue().getBlock()){restore(w,old);return false;}
  return true;
 }
 private static void restore(ServerLevel w,Map<BlockPos,BlockState> old){for(var e:old.entrySet())w.setBlock(e.getKey(),e.getValue(),QUIET);}
 private static void connect(ServerLevel w,BlockPos p){var s=w.getBlockState(p);var b=s.getBlock();if(b instanceof FenceBlock||b instanceof WallBlock||b instanceof IronBarsBlock){var updated=Block.updateFromNeighbourShapes(s,w,p);if(!updated.equals(s))w.setBlock(p,updated,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}}
}
''',encoding='utf-8')
# Add group-aware execution before the old ordinary-block branch; ordinary semantics remain intact.
p=base/'server/BuildTaskState.java';s=p.read_text(encoding='utf-8');needle='budget--;BlockPos pos=new BlockPos(c.x(),c.y(),c.z());boolean include='
assert needle in s;s=s.replace(needle,'''budget--;BlockPos pos=new BlockPos(c.x(),c.y(),c.z());
    if(StructurePlacement.multiple(j.block)){
     var group=StructurePlacement.plan(j.block,j.box,pos,j.mode);
     if(!group.isEmpty()){
      boolean blocked=false,protectedGroup=false,match=true,noop=true;
      for(var e:group.entrySet()){
       var at=e.getKey();var old=w.getBlockState(at);
       blocked|=!w.isInWorldBounds(at)||!w.getWorldBorder().isWithinBounds(at)||!w.mayInteract(p,at);
       if(j.mode==3){match&=!old.isAir()&&(j.filter==Blocks.AIR||old.is(j.filter));protectedGroup|=ReplacementRules.protectedPosition(w,at,e.getValue(),p.isCreative());}
       else if(!p.isCreative())blocked|=!old.isAir()&&!old.canBeReplaced();
       noop&=old.equals(e.getValue());
      }
      if(j.mode==3&&(!match||protectedGroup)){if(protectedGroup)j.skipped=j.skipped.add(BigInteger.valueOf(group.size()));}
      else if(!noop){
       if(blocked){pause(j,p,"message.q5.build_blocked");break;}
       boolean clearing=group.values().stream().allMatch(net.minecraft.world.level.block.state.BlockState::isAir);
       ItemStack material=ItemStack.EMPTY;
       if(!p.isCreative()&&!clearing){for(int n=0;n<p.getInventory().getContainerSize();n++){var st=p.getInventory().getItem(n);if(st.getItem() instanceof BlockItem bi&&bi.getBlock()==j.block&&!st.isEmpty()){material=st;break;}}if(material.isEmpty()){pause(j,p,"message.q5.build_materials");break;}}
       if(!StructurePlacement.place(w,group)){pause(j,p,"message.q5.build_blocked");break;}
       if(!p.isCreative()&&!clearing)material.shrink(1);
       if(!clearing)j.placed=j.placed.add(BigInteger.valueOf(group.size()));j.paused="";
       for(var at:group.keySet()){min=Math.min(min,at.getY()>>4);max=Math.max(max,at.getY()>>4);}
      }
     }
     c.advance();j.visited=j.visited.add(BigInteger.ONE);setDirty();continue;
    }
    boolean include=''')
s=s.replace('boolean success=state.equals(j.block.defaultBlockState())||w.setBlock(pos,j.block.defaultBlockState(),Block.UPDATE_ALL);','boolean success=state.equals(j.block.defaultBlockState())||StructurePlacement.place(w,StructurePlacement.single(j.block,pos));')
# Group partners can cross chunk boundaries. Load the maximum of two participating chunks before committing.
s=s.replace('var at=e.getKey();var old=w.getBlockState(at);','var at=e.getKey();w.getChunk(at);var old=w.getBlockState(at);')
p.write_text(s,encoding='utf-8')
p=base/'server/BuildWandServer.java';s=p.read_text(encoding='utf-8');s=s.replace('if(valid(w,box)){sel.confirm', 'if(!StructurePlacement.fits(WandStackData.selectedBlock(tool),box,mode)){message(p,"message.q5.multiblock_dimensions");}\n    else if(valid(w,box)){sel.confirm')
# Show warning at mode entry, material selection and the existing two-point confirmation. No extra confirmation button.
s=s.replace('if(BuildWandData.mode(tool)==3)p.sendSystemMessage(Component.translatable("message.q5.replace_hint"));','if(BuildWandData.mode(tool)==3){p.sendSystemMessage(Component.translatable("message.q5.replace_hint"));warnMultiple(p);}')
s=s.replace('  send(p,s,r.sequence());', '  if(r.action()==BuildNetworking.LEFT&&sel.stage()==2)warnMultiple(p);\n  send(p,s,r.sequence());')
s=s.replace('else WandActions.selectBlock(p,pos);','else WandActions.selectBlock(p,pos);\n  warnMultiple(p);')
s=s.replace(' static void clearMaterials', ' static void warnMultiple(ServerPlayer p){if(BuildWandData.mode(p.getMainHandItem())==3&&StructurePlacement.multiple(WandStackData.selectedBlock(p.getMainHandItem())))p.sendSystemMessage(Component.translatable("message.q5.multiblock_warning").withStyle(net.minecraft.ChatFormatting.RED));}\n static void clearMaterials')
p.write_text(s,encoding='utf-8')
p=base/'server/WandActions.java';s=p.read_text(encoding='utf-8');s=s.replace('        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);','''        var group=StructurePlacement.single(block,pos);
        for(var at:group.keySet())if(!level.isInWorldBounds(at)||!level.getWorldBorder().isWithinBounds(at)||!level.mayInteract(player,at)||(!creative&&!isReplaceable(level.getBlockState(at)))){fail(player,"text.quick_build.102");return;}
        if(!StructurePlacement.place(level,group)){fail(player,"message.q5.build_blocked");return;}''');p.write_text(s,encoding='utf-8')
# Keep the accepted layout and append pages, three lines per page.
p=v/'src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java';s=p.read_text(encoding='utf-8');s=s.replace('List.of("help.q5.replace_4","help.q5.replace_5","help.q5.replace_6")))','List.of("help.q5.replace_4","help.q5.replace_5","help.q5.replace_6"),\n                List.of("help.q5.structure_1","help.q5.structure_2","help.q5.structure_3"),\n                List.of("help.q5.structure_4","help.q5.structure_5","help.q5.structure_6")))');p.write_text(s,encoding='utf-8')
print('结构放置与任务处理已写入独立测试版10')