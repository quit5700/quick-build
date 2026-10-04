from pathlib import Path
import shutil
v=Path('versions/1.1.4-test10/mc26.2');p=v/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';s=p.read_text(encoding='utf-8')
s=s.replace('System.out.println(failures==0?"Q5_MOVE_REGRESSION_PASS: 13 cases"', '''run("build_connected_fences_walls_panes",()->connections());run("build_independent_chests",()->chests());
  run("build_complete_beds_doors_plants",()->completeStructures());run("build_odd_49x49_and_reverse",()->oddBeds(server));
  run("build_hollow_inward_and_interior",()->hollowBeds(server));run("build_multiblock_pause_refill",()->pairSurvival(server));
  run("replace_pair_protection_and_warning",()->pairProtection(server));run("replace_new_chest_is_protected",()->newChestProtection(server));
  System.out.println(failures==0?"Q5_MOVE_REGRESSION_PASS: 21 cases"''')
s=s.replace('net.minecraft.network.chat.Component lastMessage;', '''boolean creative;
  java.util.List<net.minecraft.network.chat.Component> systemMessages=new java.util.ArrayList<>();
  @Override public boolean isCreative(){return creative;}
  @Override public void sendSystemMessage(net.minecraft.network.chat.Component message){systemMessages.add(message);}
  net.minecraft.network.chat.Component lastMessage;''')
extra='''
 private static Block bed(){return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("minecraft:red_bed"));}
 private static void connections(){
  var p=new BlockPos(192,180,64);
  for(Block b:List.of(Blocks.OAK_FENCE,Blocks.COBBLESTONE_WALL,Blocks.GLASS_PANE)){
   reset(p);reset(p.east());reset(p.east(2));
   check(StructurePlacement.place(world,StructurePlacement.single(b,p)),"first connected block failed");
   check(StructurePlacement.place(world,StructurePlacement.single(b,p.east())),"second connected block failed");
   check(StructurePlacement.place(world,StructurePlacement.single(Blocks.STONE,p.east(2))),"boundary support failed");
   for(var at:List.of(p,p.east()))check(world.getBlockState(at).equals(Block.updateFromNeighbourShapes(world.getBlockState(at),world,at)),"connection did not match complete neighbourhood");
   if(b instanceof WallBlock)check(world.getBlockState(p).getValue(BlockStateProperties.EAST_WALL)!=net.minecraft.world.level.block.state.properties.WallSide.NONE,"wall connection missing");
   else check(world.getBlockState(p).getValue(BlockStateProperties.EAST),"fence/pane connection missing");
  }
 }
 private static void chests(){
  var p=new BlockPos(208,180,64);for(int x=0;x<3;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){var at=p.offset(x,y,z);reset(at);check(StructurePlacement.place(world,StructurePlacement.single(Blocks.CHEST,at)),"chest failed");}
  for(int x=0;x<3;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){var at=p.offset(x,y,z);world.updateNeighborsAt(at,Blocks.CHEST);check(world.getBlockState(at).getValue(ChestBlock.TYPE)==net.minecraft.world.level.block.state.properties.ChestType.SINGLE,"chest merged");check(world.getBlockEntity(at) instanceof Container,"chest entity missing");check(((Container)world.getBlockEntity(at)).getContainerSize()==27&&((Container)world.getBlockEntity(at)).isEmpty(),"new chest not empty single inventory");}
 }
 private static void completeStructures(){
  var p=new BlockPos(224,180,64);reset(p);reset(p.east());check(StructurePlacement.place(world,StructurePlacement.single(bed(),p)),"complete bed failed");check(world.getBlockState(p).getValue(BedBlock.PART)==net.minecraft.world.level.block.state.properties.BedPart.FOOT,"bed foot missing");check(world.getBlockState(p.east()).getValue(BedBlock.PART)==net.minecraft.world.level.block.state.properties.BedPart.HEAD,"bed head missing");
  p=p.offset(4,0,0);reset(p);world.setBlock(p.above(),Blocks.AIR.defaultBlockState(),QUIET);check(StructurePlacement.place(world,StructurePlacement.single(Blocks.OAK_DOOR,p)),"door failed");check(world.getBlockState(p).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER&&world.getBlockState(p.above()).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER,"door incomplete");
  p=p.offset(4,0,0);world.setBlock(p.below(),Blocks.DIRT.defaultBlockState(),QUIET);world.setBlock(p,Blocks.AIR.defaultBlockState(),QUIET);world.setBlock(p.above(),Blocks.AIR.defaultBlockState(),QUIET);check(StructurePlacement.place(world,StructurePlacement.single(Blocks.SUNFLOWER,p)),"plant failed");check(world.getBlockState(p.above()).is(Blocks.SUNFLOWER),"plant upper missing");
  p=p.offset(4,0,0);world.setBlock(p.below(),Blocks.AIR.defaultBlockState(),QUIET);world.setBlock(p,Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(p.above(),Blocks.DIRT.defaultBlockState(),QUIET);check(!StructurePlacement.place(world,StructurePlacement.single(Blocks.OAK_DOOR,p)),"unsupported door accepted");check(world.getBlockState(p).is(Blocks.STONE)&&world.getBlockState(p.above()).is(Blocks.DIRT),"failed pair did not roll back");
 }
 private static TestPlayer testPlayer(net.minecraft.server.MinecraftServer server,boolean creative){var p=new TestPlayer(server);p.creative=creative;p.getAbilities().instabuild=creative;server.getPlayerList().getPlayersByUUID().put(p.getUUID(),p);var wand=new ItemStack(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND);p.getInventory().setItem(0,wand);cn.quit5700.buildingwand.util.BuildWandData.ensure(wand);return p;}
 private static BuildTaskState build(TestPlayer p,Block material,BlockPos a,BlockPos b,int mode){var tasks=new BuildTaskState();tasks.add(p.getUUID(),cn.quit5700.buildingwand.util.BuildWandData.ensure(p.getMainHandItem()),material,new cn.quit5700.buildingwand.logic.DeleteSelection.Point(a.getX(),a.getY(),a.getZ()),new cn.quit5700.buildingwand.logic.DeleteSelection.Point(b.getX(),b.getY(),b.getZ()),mode);return tasks;}
 private static void finish(BuildTaskState tasks,TestPlayer p){for(int n=0;n<2000&&tasks.busy(p.getUUID());n++)tasks.tick(world);check(!tasks.busy(p.getUUID()),"task did not finish");}
 private static void oddBeds(net.minecraft.server.MinecraftServer server){var p=testPlayer(server,true);try{
  var first=new BlockPos(256,180,64);var end=first.offset(48,0,48);for(int x=0;x<49;x++)for(int z=0;z<49;z++){world.setBlock(first.offset(x,-1,z),Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(first.offset(x,0,z),Blocks.DIRT.defaultBlockState(),QUIET);}
  finish(build(p,bed(),first,end,1),p);for(int x=0;x<49;x++)for(int z=0;z<49;z++)check(x==48?world.getBlockState(first.offset(x,0,z)).isAir():world.getBlockState(first.offset(x,0,z)).is(bed()),"49x49 solid placement mismatch");
  var reverse=new BlockPos(352,180,64);for(int x=-4;x<=0;x++){reset(reverse.offset(x,0,0));world.setBlock(reverse.offset(x,0,0),Blocks.DIRT.defaultBlockState(),QUIET);}finish(build(p,bed(),reverse,reverse.offset(-4,0,0),1),p);check(world.getBlockState(reverse.offset(-4,0,0)).isAir()&&world.getBlockState(reverse).getValue(BedBlock.FACING)==net.minecraft.core.Direction.WEST,"reverse first corner lost");
 }finally{server.getPlayerList().getPlayersByUUID().remove(p.getUUID());}}
 private static void hollowBeds(net.minecraft.server.MinecraftServer server){var p=testPlayer(server,true);try{
  var first=new BlockPos(368,180,64);for(int x=0;x<6;x++)for(int y=0;y<5;y++)for(int z=0;z<5;z++)world.setBlock(first.offset(x,y,z),Blocks.DIRT.defaultBlockState(),QUIET);
  finish(build(p,bed(),first,first.offset(5,4,4),2),p);check(world.getBlockState(first.offset(0,2,2)).is(bed())&&world.getBlockState(first.offset(1,2,2)).is(bed()),"near shell not aligned inward");check(world.getBlockState(first.offset(4,2,2)).is(bed())&&world.getBlockState(first.offset(5,2,2)).is(bed()),"far shell dimension lost");check(world.getBlockState(first.offset(2,2,2)).is(Blocks.DIRT)&&world.getBlockState(first.offset(3,2,2)).is(Blocks.DIRT),"deep interior overwritten");
  first=new BlockPos(400,180,64);for(int x=0;x<49;x++)for(int z=0;z<49;z++)world.setBlock(first.offset(x,0,z),Blocks.DIRT.defaultBlockState(),QUIET);finish(build(p,bed(),first,first.offset(48,0,48),2),p);for(int z=0;z<49;z++)check(world.getBlockState(first.offset(48,0,z)).isAir()&&world.getBlockState(first.offset(47,0,z)).is(bed()),"odd hollow must build and leave far end empty");
 }finally{server.getPlayerList().getPlayersByUUID().remove(p.getUUID());}}
 private static void pairSurvival(net.minecraft.server.MinecraftServer server){var p=testPlayer(server,false);try{
  var first=new BlockPos(464,180,64);for(int x=0;x<5;x++)reset(first.offset(x,0,0));var tasks=build(p,bed(),first,first.offset(4,0,0),1);for(int n=0;n<20;n++)tasks.tick(world);check(tasks.busy(p.getUUID())&&world.getBlockState(first).isAir()&&world.getBlockState(first.east()).isAir(),"missing bed material left half");
  p.getInventory().setItem(1,new ItemStack(bed(),1));for(int n=0;n<20;n++)tasks.tick(world);check(world.getBlockState(first).is(bed())&&world.getBlockState(first.east()).is(bed())&&world.getBlockState(first.east(2)).isAir()&&tasks.busy(p.getUUID()),"one bed did not consume one item as a pair");p.getInventory().setItem(1,new ItemStack(bed(),1));finish(tasks,p);check(world.getBlockState(first.east(2)).is(bed())&&world.getBlockState(first.east(3)).is(bed())&&world.getBlockState(first.east(4)).isAir()&&p.getInventory().getItem(1).isEmpty(),"bed refill or odd remainder failed");
 }finally{server.getPlayerList().getPlayersByUUID().remove(p.getUUID());}}
 private static void pairProtection(net.minecraft.server.MinecraftServer server){var p=testPlayer(server,true);try{
  var first=new BlockPos(480,180,64);reset(first);reset(first.east());world.setBlock(first,Blocks.DIRT.defaultBlockState(),QUIET);world.setBlock(first.east(),Blocks.CHEST.defaultBlockState(),QUIET);((Container)world.getBlockEntity(first.east())).setItem(0,new ItemStack(Items.DIAMOND,9));finish(build(p,bed(),first,first.east(),3),p);check(world.getBlockState(first).is(Blocks.DIRT)&&((Container)world.getBlockEntity(first.east())).getItem(0).getCount()==9,"protected pair partially overwritten");
  cn.quit5700.buildingwand.util.BuildWandData.mode(p.getMainHandItem(),3);cn.quit5700.buildingwand.util.WandStackData.setSelectedBlock(p.getMainHandItem(),bed());BuildWandServer.warnMultiple(p);check(!p.systemMessages.isEmpty()&&((net.minecraft.network.chat.contents.TranslatableContents)p.systemMessages.getLast().getContents()).getKey().equals("message.q5.multiblock_warning"),"multi-block warning missing");
  world.setBlock(first.east(),Blocks.DIRT.defaultBlockState(),QUIET);finish(build(p,bed(),first,first.east(),3),p);check(world.getBlockState(first).is(bed())&&world.getBlockState(first.east()).is(bed()),"warning incorrectly prohibited replacement");
 }finally{server.getPlayerList().getPlayersByUUID().remove(p.getUUID());}}
 private static void newChestProtection(net.minecraft.server.MinecraftServer server){var p=testPlayer(server,true);try{
  var first=new BlockPos(496,180,64);reset(first);reset(first.east());world.setBlock(first,Blocks.DIRT.defaultBlockState(),QUIET);world.setBlock(first.east(),Blocks.DIRT.defaultBlockState(),QUIET);finish(build(p,Blocks.CHEST,first,first.east(),3),p);check(world.getBlockState(first).is(Blocks.CHEST)&&world.getBlockState(first.east()).is(Blocks.CHEST),"replacement did not build chest");((Container)world.getBlockEntity(first)).setItem(0,new ItemStack(Items.DIAMOND,11));finish(build(p,Blocks.STONE,first,first.east(),3),p);check(world.getBlockState(first).is(Blocks.CHEST)&&world.getBlockState(first.east()).is(Blocks.CHEST)&&((Container)world.getBlockEntity(first)).getItem(0).getCount()==11,"new protected chest was replaced again");
 }finally{server.getPlayerList().getPlayersByUUID().remove(p.getUUID());}}
'''
s=s[:s.rfind('}')]+extra+'\n}\n';p.write_text(s,encoding='utf-8')
run=v/'run';(run/'mods').mkdir(parents=True,exist_ok=True);shutil.copy2(Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods\fabric-api-0.154.0+26.2.jar'),run/'mods/fabric-api-0.154.0+26.2.jar');(run/'eula.txt').write_text('eula=true\n',encoding='utf-8');(run/'server.properties').write_text('online-mode=false\nserver-ip=127.0.0.1\nserver-port=25589\nlevel-name=q5-structures-regression-world\nview-distance=2\nsimulation-distance=2\ngenerate-structures=false\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\nspawn-protection=0\n',encoding='utf-8')
print('独立世界回归扩展至21场景，未读取玩家存档')