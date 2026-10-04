from pathlib import Path
r=Path.cwd();v=r/'versions/1.1.4-test8/mc26.2';p=v/'src/main/java/cn/quit5700/buildingwand/server/BuildWandServer.java';s=p.read_text(encoding='utf-8');old='''    WandStackData.setSelectedBlock(tool,Blocks.AIR);if(BuildWandData.mode(tool)==3)BuildWandData.replacementTarget(tool,Blocks.AIR);
    message(p,BuildWandData.mode(tool)==3?"message.q5.replace_clear":"message.q5.material_clear");''';assert old in s;s=s.replace(old,'    clearMaterials(p);');old='''    if(BuildWandData.mode(tool)==3&&WandStackData.selectedBlock(tool)!=Blocks.AIR){
     var block=w.getBlockState(r.pos()).getBlock();BuildWandData.replacementTarget(tool,block);
     message(p,"message.q5.replace_target",Component.translatable(block.getDescriptionId()));
    }else WandActions.selectBlock(p,r.pos());''';assert old in s;s=s.replace(old,'    pickMaterial(p,r.pos());');s=s.replace(' private static boolean reachable(',''' static void pickMaterial(ServerPlayer p,BlockPos pos){
  var tool=p.getMainHandItem();var state=p.level().getBlockState(pos);if(state.isAir())return;
  if(BuildWandData.mode(tool)==3&&WandStackData.selectedBlock(tool)!=Blocks.AIR){BuildWandData.replacementTarget(tool,state.getBlock());message(p,"message.q5.replace_target",Component.translatable(state.getBlock().getDescriptionId()));}
  else WandActions.selectBlock(p,pos);
 }
 static void clearMaterials(ServerPlayer p){var tool=p.getMainHandItem();WandStackData.setSelectedBlock(tool,Blocks.AIR);if(BuildWandData.mode(tool)==3)BuildWandData.replacementTarget(tool,Blocks.AIR);message(p,BuildWandData.mode(tool)==3?"message.q5.replace_clear":"message.q5.material_clear");}
 private static boolean reachable(''');p.write_text(s,encoding='utf-8')
p=v/'src/test/java/cn/quit5700/buildingwand/logic/ReplacementModeTest.java';p.write_text('''package cn.quit5700.buildingwand.logic;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;
class ReplacementModeTest {
 @Test void fourthModeDoesNotChangeDefaultOrOtherModes(){for(int m=0;m<=3;m++)assertEquals(m,BuildPlan.mode(m));assertEquals(0,BuildPlan.mode(4));assertEquals(0,BuildPlan.mode(-1));}
 @Test void replacementVisitsWholeAreaIncludingInterior(){var b=new DeleteBox(-2,-3,-4,2,3,4);assertTrue(BuildPlan.includes(b,0,0,0,3));assertFalse(BuildPlan.includes(b,3,0,0,3));assertEquals(b.volume(),BuildPlan.total(b,3));}
 @Test void replacementRetainsUnlimitedLongSafeDirectedTraversal(){var b=new DeleteBox(-30000000,-64,-30000000,30000000,319,30000000);assertTrue(BuildPlan.total(b,3).compareTo(BigInteger.valueOf(Long.MAX_VALUE))>0);}
}
''',encoding='utf-8')
# Existing world regression remains intact, adding actual replacement and persistence scenarios.
p=v/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';s=p.read_text(encoding='utf-8');s=s.replace('run("complete_powered_circuit",()->circuit());','''run("complete_powered_circuit",()->circuit());
  run("replace_material_selection",()->selection(server));run("replace_protection",()->protection());
  run("replace_entity_and_support",()->entityProtection());run("replace_job_persistence",()->persistence());
  run("replace_survival_pause_resume",()->survival(server));''');s=s.replace('Q5_MOVE_REGRESSION_PASS: 7 cases','Q5_MOVE_REGRESSION_PASS: 12 cases')
a=s.rindex('\n}')
s=s[:a]+'''
 private static final class TestPlayer extends net.minecraft.server.level.ServerPlayer {
  TestPlayer(net.minecraft.server.MinecraftServer server){super(server,world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"Q5ReplaceTest"),net.minecraft.server.level.ClientInformation.createDefault());}
  @Override public void sendOverlayMessage(net.minecraft.network.chat.Component message){}
 }
 private static void selection(net.minecraft.server.MinecraftServer server)throws Exception{
  var p=new TestPlayer(server);var wand=new ItemStack(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND);p.getInventory().setItem(0,wand);var data=cn.quit5700.buildingwand.util.BuildWandData.class;
  cn.quit5700.buildingwand.util.BuildWandData.mode(wand,3);var a=new BlockPos(128,180,64);var b=a.east();world.setBlock(a,Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(b,Blocks.DIRT.defaultBlockState(),QUIET);
  BuildWandServer.pickMaterial(p,a);BuildWandServer.pickMaterial(p,b);check(cn.quit5700.buildingwand.util.WandStackData.selectedBlock(wand)==Blocks.STONE,"global material overwritten");check(cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(wand)==Blocks.DIRT,"target not remembered");
  p.getInventory().setItem(0,ItemStack.EMPTY);p.getInventory().setItem(0,wand);check(cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(wand)==Blocks.DIRT,"item switch erased target");
  cn.quit5700.buildingwand.util.BuildWandData.mode(wand,1);BuildWandServer.pickMaterial(p,b);check(cn.quit5700.buildingwand.util.WandStackData.selectedBlock(wand)==Blocks.DIRT,"other mode material pick failed");
  BuildWandServer.clearMaterials(p);check(cn.quit5700.buildingwand.util.WandStackData.selectedBlock(wand)==Blocks.AIR,"clear material failed");check(cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(wand)==Blocks.DIRT,"other mode cleared exclusive target");
  cn.quit5700.buildingwand.util.BuildWandData.mode(wand,3);BuildWandServer.clearMaterials(p);check(cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(wand)==Blocks.AIR,"replacement did not clear both");
 }
 private static void protection(){var p=new BlockPos(132,180,64);for(Block block:List.of(Blocks.REDSTONE_WIRE,Blocks.PISTON,Blocks.WATER,Blocks.CHEST,Blocks.FURNACE,Blocks.CRAFTING_TABLE,Blocks.RED_BED,Blocks.WHEAT,Blocks.OAK_DOOR,Blocks.OAK_SIGN,Blocks.SPAWNER,Blocks.BEACON,Blocks.TORCH,Blocks.LADDER,Blocks.RAIL)){
  world.setBlock(p,block.defaultBlockState(),QUIET);check(ReplacementRules.protectedBlock(world,p,world.getBlockState(p),true),"protected block missed: "+block);
 }world.setBlock(p,Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED,true),QUIET);check(ReplacementRules.protectedBlock(world,p,world.getBlockState(p),true),"waterlogged block missed");world.setBlock(p,Blocks.STONE.defaultBlockState(),QUIET);check(!ReplacementRules.protectedBlock(world,p,world.getBlockState(p),false),"ordinary stone protected");world.setBlock(p,Blocks.BEDROCK.defaultBlockState(),QUIET);check(ReplacementRules.protectedBlock(world,p,world.getBlockState(p),false),"survival bedrock unprotected");check(!ReplacementRules.protectedBlock(world,p,world.getBlockState(p),true),"creative bedrock unexpectedly protected");}
 private static void entityProtection(){var p=new BlockPos(136,180,64);world.setBlock(p,Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(p.above(),Blocks.TORCH.defaultBlockState(),QUIET);check(ReplacementRules.protectedPosition(world,p,Blocks.AIR.defaultBlockState(),true),"torch support lost");world.setBlock(p.above(),Blocks.AIR.defaultBlockState(),QUIET);var stand=new net.minecraft.world.entity.decoration.ArmorStand(world,p.getX()+.5,p.getY()+.5,p.getZ()+.5);world.addFreshEntity(stand);check(ReplacementRules.protectedPosition(world,p,Blocks.DIRT.defaultBlockState(),true),"entity collision missed");stand.discard();}
 @SuppressWarnings("unchecked") private static void persistence()throws Exception{var tasks=new BuildTaskState();var owner=UUID.randomUUID();String wand=UUID.randomUUID().toString();tasks.add(owner,wand,Blocks.STONE,new cn.quit5700.buildingwand.logic.DeleteSelection.Point(0,0,0),new cn.quit5700.buildingwand.logic.DeleteSelection.Point(2,2,2),3,Blocks.DIRT);var f=BuildTaskState.class.getDeclaredField("CODEC");f.setAccessible(true);var codec=(com.mojang.serialization.Codec<BuildTaskState>)f.get(null);var json=codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,tasks).getOrThrow();check(json.toString().contains("minecraft:dirt"),"filter not persisted");var restored=codec.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow();check(restored.busy(owner)&&restored.cancel(owner,wand),"replacement did not restore");tasks=new BuildTaskState();tasks.add(owner,wand,Blocks.STONE,new cn.quit5700.buildingwand.logic.DeleteSelection.Point(0,0,0),new cn.quit5700.buildingwand.logic.DeleteSelection.Point(2,2,2),1);json=codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,tasks).getOrThrow();var job=json.getAsJsonObject().getAsJsonArray("jobs").get(0).getAsJsonObject();job.remove("filter");job.remove("skipped");check(codec.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow().busy(owner),"old task compatibility lost");}
 private static void survival(net.minecraft.server.MinecraftServer server)throws Exception{
  var p=new TestPlayer(server);server.getPlayerList().getPlayersByUUID().put(p.getUUID(),p);try{
   var wand=new ItemStack(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND);p.getInventory().setItem(0,wand);String id=cn.quit5700.buildingwand.util.BuildWandData.ensure(wand);var first=new BlockPos(144,180,64);world.getChunk(first);world.setBlock(first.below(),Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(first,Blocks.DIRT.defaultBlockState(),QUIET);world.setBlock(first.east().below(),Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(first.east(),Blocks.DIRT.defaultBlockState(),QUIET);var tasks=new BuildTaskState();var a=new cn.quit5700.buildingwand.logic.DeleteSelection.Point(first.getX(),first.getY(),first.getZ());var b=new cn.quit5700.buildingwand.logic.DeleteSelection.Point(first.getX()+1,first.getY(),first.getZ());tasks.add(p.getUUID(),id,Blocks.STONE,a,b,3,Blocks.DIRT);
   for(int n=0;n<20;n++)tasks.tick(world);check(tasks.busy(p.getUUID())&&world.getBlockState(first).is(Blocks.DIRT),"missing materials did not pause");p.getInventory().setItem(1,new ItemStack(Blocks.STONE,1));for(int n=0;n<20;n++)tasks.tick(world);check(world.getBlockState(first).is(Blocks.STONE)&&world.getBlockState(first.east()).is(Blocks.DIRT)&&tasks.busy(p.getUUID()),"survival material ratio wrong");p.getInventory().setItem(1,new ItemStack(Blocks.STONE,1));for(int n=0;n<20;n++)tasks.tick(world);check(!tasks.busy(p.getUUID())&&world.getBlockState(first.east()).is(Blocks.STONE)&&p.getInventory().getItem(1).isEmpty(),"refill did not resume");
  }finally{server.getPlayerList().getPlayersByUUID().remove(p.getUUID());}
 }
'''+s[a:];p.write_text(s,encoding='utf-8')
run=v/'run';(run/'mods').mkdir(parents=True,exist_ok=True);import shutil;shutil.copy2(Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods\fabric-api-0.154.0+26.2.jar'),run/'mods/fabric-api-0.154.0+26.2.jar');(run/'eula.txt').write_text('eula=true\n',encoding='utf-8');(run/'server.properties').write_text('online-mode=false\nserver-ip=127.0.0.1\nserver-port=25589\nlevel-name=q5-replacement-regression-world\nview-distance=2\nsimulation-distance=2\ngenerate-structures=false\nlevel-type=minecraft:flat\nspawn-protection=0\n',encoding='utf-8')
print('新增区域替换及旧任务兼容回归，保留原移动回归')