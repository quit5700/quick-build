from pathlib import Path
import shutil
r=Path.cwd();v=r/'versions/1.1.4-test11/mc26.2';assert not v.exists();shutil.copytree(r/'versions/1.1.4-test10/mc26.2',v,ignore=shutil.ignore_patterns('build','.gradle','run','dist','__pycache__'))
p=v/'gradle.properties';p.write_text(p.read_text(encoding='utf-8').replace('1.1.4-test10','1.1.4-test11'),encoding='utf-8')
base=v/'src/main/java/cn/quit5700/buildingwand'
p=base/'server/BuildTaskState.java';s=p.read_text(encoding='utf-8');s=s.replace(' public void release(){',' public int cancelAll(UUID owner){int count=0;for(var it=jobs.iterator();it.hasNext();){var job=it.next();if(job.owner.equals(owner)){job.lease.release();it.remove();count++;}}if(count>0)setDirty();return count;}\n public void release(){')
s=s.replace('      boolean blocked=false,protectedGroup=false,match=true,noop=true;','      boolean clearing=group.values().stream().allMatch(net.minecraft.world.level.block.state.BlockState::isAir);\n      boolean blocked=false,protectedGroup=false,match=true,noop=true;')
s=s.replace('(e.getValue().isAir()&&w.getBlockEntity(at)!=null)||','').replace('else if(!p.isCreative())blocked|=','else if(!p.isCreative()&&!clearing)blocked|=')
s=s.replace('       boolean clearing=group.values().stream().allMatch(net.minecraft.world.level.block.state.BlockState::isAir);\n','')
s=s.replace('if(!StructurePlacement.place(w,group)){', 'if(!(clearing&&j.mode!=3?StructurePlacement.clearRemainder(w,group):StructurePlacement.placeNatural(w,group))){')
s=s.replace('||!supported(w,pos)','').replace('StructurePlacement.place(w,StructurePlacement.single(j.block,pos))','StructurePlacement.placeNatural(w,StructurePlacement.single(j.block,pos))');p.write_text(s,encoding='utf-8')
p=base/'server/BuildWandServer.java';s=p.read_text(encoding='utf-8');s=s.replace(' static void pickMaterial(',''' public static int cancelAll(ServerPlayer p){int count=0;for(var w:p.getServer().getAllLevels())count+=BuildTaskState.get(w).cancelAll(p.getUUID());sessions.values().removeIf(s->s.owner.equals(p.getUUID()));return count;}
 static void pickMaterial(''');p.write_text(s,encoding='utf-8')
p=base/'server/WandActions.java';s=p.read_text(encoding='utf-8');old='''        if (!creative && !hasSupport(level, List.of(pos))) {
            fail(player, "text.quick_build.101");
            return;
        }
''';assert old in s;s=s.replace(old,'');s=s.replace('StructurePlacement.place(level,group)','StructurePlacement.placeNatural(level,group)');p.write_text(s,encoding='utf-8')
p=base/'server/StructurePlacement.java';s=p.read_text(encoding='utf-8');s=s.replace('return b instanceof BedBlock||b instanceof DoorBlock||b instanceof DoublePlantBlock;','return b.defaultBlockState().hasProperty(BedBlock.PART)||b.defaultBlockState().hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);');s=s.replace('return !(b instanceof BedBlock);','return !b.defaultBlockState().hasProperty(BedBlock.PART);');s=s.replace('if(b instanceof BedBlock){','if(s.hasProperty(BedBlock.PART)){');s=s.replace('s=s.setValue(BedBlock.FACING,facing).setValue(BedBlock.OCCUPIED,false);','if(s.hasProperty(BedBlock.FACING))s=s.setValue(BedBlock.FACING,facing);if(s.hasProperty(BedBlock.OCCUPIED))s=s.setValue(BedBlock.OCCUPIED,false);');s=s.replace('else if(b instanceof DoorBlock||b instanceof DoublePlantBlock){','else if(s.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)){');s=s.replace(' public static boolean place(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired){',''' public static boolean clearRemainder(ServerLevel w,LinkedHashMap<BlockPos,BlockState> cells){
  for(var e:cells.entrySet()){
   if(!e.getValue().isAir())throw new IllegalArgumentException("Remainder must be air");
   var entity=w.getBlockEntity(e.getKey());
   if(entity instanceof net.minecraft.world.Container container){net.minecraft.world.Containers.dropContents(w,e.getKey(),container);container.clearContent();}
   if(!w.getBlockState(e.getKey()).isAir()&&!w.setBlock(e.getKey(),Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL))return false;
   connect(w,e.getKey());for(var d:Direction.values())connect(w,e.getKey().relative(d));
  }
  return true;
 }
 public static boolean placeNatural(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired){return place(w,desired,true);}
 public static boolean place(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired){return place(w,desired,false);}
 private static boolean place(ServerLevel w,LinkedHashMap<BlockPos,BlockState> desired,boolean natural){''')
s=s.replace('for(var e:desired.entrySet())if(!e.getValue().isAir()&&!e.getValue().canSurvive(w,e.getKey()))','for(var e:desired.entrySet())if(!natural&&!e.getValue().isAir()&&!e.getValue().canSurvive(w,e.getKey()))')
s=s.replace('   w.updateNeighborsAt(e.getKey(),s.getBlock());connect(','''   if(natural){var updated=Block.updateFromNeighbourShapes(s,w,e.getKey());if(!s.canSurvive(w,e.getKey()))updated=Blocks.AIR.defaultBlockState();Block.updateOrDestroy(s,updated,w,e.getKey(),Block.UPDATE_ALL);s=w.getBlockState(e.getKey());}
   w.updateNeighborsAt(e.getKey(),s.getBlock());connect(''')
s=s.replace('for(var e:desired.entrySet())if(w.getBlockState(e.getKey()).getBlock()!=e.getValue().getBlock())','for(var e:desired.entrySet())if(!natural&&w.getBlockState(e.getKey()).getBlock()!=e.getValue().getBlock())');p.write_text(s,encoding='utf-8')
# Dedicated authenticated request contains no player ID, so it cannot cancel another player's work.
p=base/'network/BuildNetworking.java';s=p.read_text(encoding='utf-8');needle='BuildWandServer.register();';s=s.replace(needle,'''PayloadTypeRegistry.serverboundPlay().register(CancelAll.ID,CancelAll.CODEC);PayloadTypeRegistry.clientboundPlay().register(Cancelled.ID,Cancelled.CODEC);ServerPlayNetworking.registerGlobalReceiver(CancelAll.ID,(p,c)->c.server().execute(()->ServerPlayNetworking.send(c.player(),new Cancelled(BuildWandServer.cancelAll(c.player())))));BuildWandServer.register();''');s=s[:s.rfind('}')]+''' public record CancelAll() implements CustomPacketPayload{
  public static final Type<CancelAll> ID=new Type<>(BuildingWandMod.id("cancel_all_builds"));public static final StreamCodec<RegistryFriendlyByteBuf,CancelAll> CODEC=CustomPacketPayload.codec((p,b)->{},b->new CancelAll());public Type<? extends CustomPacketPayload> type(){return ID;}
 }
 public record Cancelled(int count) implements CustomPacketPayload{
  public static final Type<Cancelled> ID=new Type<>(BuildingWandMod.id("builds_cancelled"));public static final StreamCodec<RegistryFriendlyByteBuf,Cancelled> CODEC=CustomPacketPayload.codec((p,b)->b.writeVarInt(p.count),b->new Cancelled(b.readVarInt()));public Type<? extends CustomPacketPayload> type(){return ID;}
 }
}\n''';p.write_text(s,encoding='utf-8')
p=v/'src/client/java/cn/quit5700/buildingwand/client/BuildWandClient.java';s=p.read_text(encoding='utf-8');s=s.replace(' private static void clear(){',' public static void cancelAllState(){clear();}\n private static void clear(){');p.write_text(s,encoding='utf-8')
p=v/'src/client/java/cn/quit5700/buildingwand/BuildingWandClient.java';s=p.read_text(encoding='utf-8');s=s.replace('        ClientWandController.register();','''        ClientWandController.register();
        ClientPlayNetworking.registerGlobalReceiver(cn.quit5700.buildingwand.network.BuildNetworking.Cancelled.ID,(payload,context)->Minecraft.getInstance().execute(()->{cn.quit5700.buildingwand.client.BuildWandClient.cancelAllState();WandSettingsScreen.applyCancelledBuilds(payload.count());}));''');p.write_text(s,encoding='utf-8')
p=v/'src/client/java/cn/quit5700/buildingwand/client/WandSettingsScreen.java';s=p.read_text(encoding='utf-8');s=s.replace('    private Component status', '    private boolean cancellationPending;\n    private Component status');s=s.replace('        SettingsScreenLayout.Footer footer = SettingsScreenLayout.footer(layoutBounds());\n        addRenderableWidget(Button.builder', '''        SettingsScreenLayout.Footer footer = SettingsScreenLayout.footer(layoutBounds());
        SettingsScreenLayout.CancelArea cancelArea=SettingsScreenLayout.cancelArea(layoutBounds());
        Button cancel=Button.builder(Component.translatable("button.q5.cancel_all_builds"),button->{cancellationPending=true;status=Component.translatable("message.q5.cancel_all_pending");ClientPlayNetworking.send(new cn.quit5700.buildingwand.network.BuildNetworking.CancelAll());rebuildWidgets();}).bounds(cancelArea.x(),cancelArea.y(),cancelArea.width(),20).build();
        cancel.active=!cancellationPending;addRenderableWidget(cancel);
        addRenderableWidget(Button.builder''',1)
s=s.replace('    public static void applySavedSettings(','''    public static void applyCancelledBuilds(int count){if(activeScreen!=null){activeScreen.cancellationPending=false;activeScreen.status=Component.translatable("message.q5.cancel_all_done",count);activeScreen.rebuildWidgets();}}
    public static void applySavedSettings(''');s=s.replace('List.of("help.q5.structure_4","help.q5.structure_5","help.q5.structure_6")))','List.of("help.q5.structure_4","help.q5.structure_5","help.q5.structure_6"),\n                List.of("help.q5.structure_7","help.q5.structure_8","help.q5.structure_9")))');p.write_text(s,encoding='utf-8')
p=v/'src/client/java/cn/quit5700/buildingwand/client/SettingsScreenLayout.java';s=p.read_text(encoding='utf-8');s=s.replace('    static LimitArea limitArea(','''    static CancelArea cancelArea(Layout layout){Footer f=footer(layout);int x=layout.panelLeft+8;return new CancelArea(x,f.y,Math.max(1,Math.min(layout.listWidth-16,f.closeX-x-8)));}
    record CancelArea(int x,int y,int width){}
    static LimitArea limitArea(''');p.write_text(s,encoding='utf-8')
print('独立测试版11：取消任务、余位掉落和自然支撑规则已写入')