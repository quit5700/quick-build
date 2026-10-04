from pathlib import Path
v=Path('versions/1.1.4-test10/mc26.2')
p=v/'src/main/java/cn/quit5700/buildingwand/server/ReplacementRules.java';s=p.read_text(encoding='utf-8');old='boolean attached=adjacent.getBlock() instanceof TorchBlock||adjacent.is(BlockTags.CLIMBABLE)||protectedBlock(w,n,adjacent,true);';assert old in s;s=s.replace(old,'''boolean wallControl=false;
   if(adjacent.getBlock() instanceof FaceAttachedHorizontalDirectionalBlock&&adjacent.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE)){
    var face=adjacent.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE);
    wallControl=face==net.minecraft.world.level.block.state.properties.AttachFace.WALL&&adjacent.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)==d
     ||face==net.minecraft.world.level.block.state.properties.AttachFace.CEILING&&d==Direction.DOWN;
   }
   boolean attached=adjacent.getBlock() instanceof TorchBlock||adjacent.is(BlockTags.CLIMBABLE)||wallControl||(d==Direction.UP&&protectedBlock(w,n,adjacent,true));''');p.write_text(s,encoding='utf-8')
p=v/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';s=p.read_text(encoding='utf-8');s=s.replace('var p=new BlockPos(208,180,64);for(int x=0;x<3;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){var at=p.offset(x,y,z);reset(at);check(', 'var p=new BlockPos(208,180,64);for(int x=0;x<3;x++)for(int z=0;z<3;z++){world.setBlock(p.offset(x,-1,z),Blocks.STONE.defaultBlockState(),QUIET);for(int y=0;y<2;y++)world.setBlock(p.offset(x,y,z),Blocks.AIR.defaultBlockState(),QUIET);}\n  for(int x=0;x<3;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){var at=p.offset(x,y,z);check(')
# Add direct support checks so fixing false protection cannot weaken actual attachments.
s=s.replace('world.setBlock(first.east(),Blocks.DIRT.defaultBlockState(),QUIET);finish(build(p,bed()', '''world.setBlock(first.east(),Blocks.DIRT.defaultBlockState(),QUIET);finish(build(p,bed()''')
s=s.replace('private static void newChestProtection(net.minecraft.server.MinecraftServer server){', '''private static void newChestProtection(net.minecraft.server.MinecraftServer server){''')
s=s.replace('"new protected chest was replaced again");','''"new protected chest was replaced again");
  var support=first.offset(4,0,0);world.setBlock(support,Blocks.STONE.defaultBlockState(),QUIET);world.setBlock(support.above(),Blocks.REDSTONE_WIRE.defaultBlockState(),QUIET);check(ReplacementRules.protectedPosition(world,support,Blocks.CHEST.defaultBlockState(),true),"wire floor support protection weakened");world.setBlock(support.above(),Blocks.AIR.defaultBlockState(),QUIET);world.setBlock(support.east(),Blocks.STONE_BUTTON.defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE,net.minecraft.world.level.block.state.properties.AttachFace.WALL).setValue(BlockStateProperties.HORIZONTAL_FACING,net.minecraft.core.Direction.EAST),QUIET);check(ReplacementRules.protectedPosition(world,support,Blocks.CHEST.defaultBlockState(),true),"wall button support protection weakened");''')
p.write_text(s,encoding='utf-8')
print('修正水平相邻箱子误判，并保留墙面按钮和红石底座保护验证')