from pathlib import Path
import shutil
r=Path.cwd();v=r/'versions/1.1.4-test8/mc26.2';assert not v.exists();shutil.copytree(r/'versions/1.1.4-test7/mc26.2',v,ignore=shutil.ignore_patterns('build','.gradle','run','dist','__pycache__'))
p=v/'gradle.properties';p.write_text(p.read_text(encoding='utf-8').replace('1.1.4-test7','1.1.4-test8'),encoding='utf-8')
def edit(rel,old,new):
 p=v/rel;s=p.read_text(encoding='utf-8');assert old in s,rel;s=s.replace(old,new);p.write_text(s,encoding='utf-8')
main='src/main/java/cn/quit5700/buildingwand/';client='src/client/java/cn/quit5700/buildingwand/client/'
edit(main+'logic/BuildPlan.java','value<=2','value<=3')
edit(main+'util/BuildWandData.java','case 2->"mode.q5.2";','case 2->"mode.q5.2";case 3->"mode.q5.3";')
p=v/(main+'util/BuildWandData.java');s=p.read_text(encoding='utf-8');s=s.replace(' public static ItemStack find(',''' public static net.minecraft.world.level.block.Block replacementTarget(ItemStack s){
  String id=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr("Q5ReplaceTarget","");
  var key=net.minecraft.resources.Identifier.tryParse(id);if(key==null)return net.minecraft.world.level.block.Blocks.AIR;
  var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(key);return block==null?net.minecraft.world.level.block.Blocks.AIR:block;
 }
 public static void replacementTarget(ItemStack s,net.minecraft.world.level.block.Block block){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putString("Q5ReplaceTarget",net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString()));}
 public static ItemStack find(''');p.write_text(s,encoding='utf-8')
edit(main+'network/BuildNetworking.java','QUERY=3;','QUERY=3,CLEAR=4;')
edit(client+'BuildWandClient.java','if(right){if(!shift||hit!=null)send(shift?BuildNetworking.SELECT:BuildNetworking.RIGHT,shift?hit:target);}', 'if(right){if(shift&&hit==null)send(BuildNetworking.CLEAR,BlockPos.ZERO);else send(shift?BuildNetworking.SELECT:BuildNetworking.RIGHT,shift?hit:target);}')
p=v/(main+'server/BuildWandServer.java');s=p.read_text(encoding='utf-8').replace('(BuildWandData.mode(tool)+1)%3','(BuildWandData.mode(tool)+1)%4');s=s.replace('Component.translatable(BuildWandData.modeKey(BuildWandData.mode(tool)))));}', 'Component.translatable(BuildWandData.modeKey(BuildWandData.mode(tool)))));if(BuildWandData.mode(tool)==3)p.sendSystemMessage(Component.translatable("message.q5.replace_hint"));}')
s=s.replace('''}else if(r.action()==BuildNetworking.SELECT){if(reachable(p,w,r.pos()))WandActions.selectBlock(p,r.pos());}''','''}else if(r.action()==BuildNetworking.CLEAR){
   if(p.pick(4.0,0.0F,false).getType()==net.minecraft.world.phys.HitResult.Type.MISS){
    WandStackData.setSelectedBlock(tool,Blocks.AIR);if(BuildWandData.mode(tool)==3)BuildWandData.replacementTarget(tool,Blocks.AIR);
    message(p,BuildWandData.mode(tool)==3?"message.q5.replace_clear":"message.q5.material_clear");
   }
  }else if(r.action()==BuildNetworking.SELECT){
   if(reachable(p,w,r.pos())&&!w.getBlockState(r.pos()).isAir()){
    if(BuildWandData.mode(tool)==3&&WandStackData.selectedBlock(tool)!=Blocks.AIR){
     var block=w.getBlockState(r.pos()).getBlock();BuildWandData.replacementTarget(tool,block);
     message(p,"message.q5.replace_target",Component.translatable(block.getDescriptionId()));
    }else WandActions.selectBlock(p,r.pos());
   }
  }''')
s=s.replace('tasks.add(p.getUUID(),wand,WandStackData.selectedBlock(tool),a,b,mode);message(p,"message.q5.build_start");','tasks.add(p.getUUID(),wand,WandStackData.selectedBlock(tool),a,b,mode,mode==3?BuildWandData.replacementTarget(tool):Blocks.AIR);message(p,mode==3?"message.q5.replace_start":"message.q5.build_start");')
s=s.replace('message(p,sel.stage()==1?"message.q5.build_first":"message.q5.build_confirm");','message(p,mode==3?(sel.stage()==1?"message.q5.replace_first":"message.q5.replace_confirm"):(sel.stage()==1?"message.q5.build_first":"message.q5.build_confirm"));')
assert 'replace_target' in s and '%4' in s;p.write_text(s,encoding='utf-8')
edit(client+'WandSettingsScreen.java','List.of("help.q5.build_7","help.q5.build_8","help.q5.build_9")))','List.of("help.q5.build_7","help.q5.build_8","help.q5.build_9"),\n                List.of("help.q5.replace_1","help.q5.replace_2","help.q5.replace_3"),\n                List.of("help.q5.replace_4","help.q5.replace_5","help.q5.replace_6")))')
p=v/(main+'item/BuildingWandItem.java');s=p.read_text(encoding='utf-8');anchor='        Block selected = WandStackData.selectedBlock(stack);';s=s.replace(anchor,'''        if(cn.quit5700.buildingwand.util.BuildWandData.mode(stack)==3){
            Block target=cn.quit5700.buildingwand.util.BuildWandData.replacementTarget(stack);
            tooltip.accept(Component.translatable(target==Blocks.AIR?"message.q5.replace_all":"message.q5.replace_target",Component.translatable(target.getDescriptionId())));
        }
'''+anchor);p.write_text(s,encoding='utf-8')
print('第四模式、材料选择及说明入口已准备')