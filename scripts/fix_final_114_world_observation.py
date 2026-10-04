from pathlib import Path
r=Path.cwd();root=r/'versions/1.1.4'
helper=''' @SuppressWarnings("unchecked") private static java.util.List<net.minecraft.world.entity.item.ItemEntity> allItems(BlockPos at){
  try{var f=ServerLevel.class.getDeclaredField("entityManager");f.setAccessible(true);Object manager=f.get(world);var sf=manager.getClass().getDeclaredField("sectionStorage");sf.setAccessible(true);var sections=(net.minecraft.world.level.entity.EntitySectionStorage<net.minecraft.world.entity.Entity>)sf.get(manager);var box=new net.minecraft.world.phys.AABB(at).inflate(7);var found=new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();for(long chunk:sections.getAllChunksWithExistingSections())sections.getExistingSectionsInChunk(chunk).flatMap(net.minecraft.world.level.entity.EntitySection::getEntities).filter(e->!e.isRemoved()&&e instanceof net.minecraft.world.entity.item.ItemEntity&&box.intersects(e.getBoundingBox())).forEach(e->found.add((net.minecraft.world.entity.item.ItemEntity)e));return found;}
  catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
 }
'''
for v in ['1.21.4','1.21.1','1.20.4','1.20.1']:
 f=root/('mc'+v)/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';s=f.read_text(encoding='utf-8');s=s.replace(' private static int items(',helper+' private static int items(').replace('world.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(7))','allItems(at)');f.write_text(s,encoding='utf-8')
f=root/'mc1.20.1/gradle.properties';s=f.read_text(encoding='utf-8').replace('loader_version=0.15.11','loader_version=0.16.14');f.write_text(s,encoding='utf-8')
f=r/'docs/bugs/BUG-20261004-FORMAL-114-PORTING.md'
with f.open('a',encoding='utf-8') as stream:stream.write('\n测试环境问题：1.21.4及以下在无真实玩家的SERVER_STARTED阶段，远端区块生成的掉落实体可能处于HIDDEN实体分区，getEntitiesOfClass不返回它们。为严格核查内容是否丢失/重复，独立测试读取实体分区完整实际实体列表（同一包围盒、排除已删除实体），保持13钻石/7金锭和正常掉落数量原断言不变；正式游戏代码不因测试观测接口调整。另1.20.1固定FabricAPI0.92.9要求Loader>=0.16.10，旧构建Loader0.15.11未进行加载验证，已失败；升构建最低Loader为0.16.14，实际玩家目录已为0.19.3，无需修改启动器。\n')
print('独立测试计数改为核查实际实体分区，严格数量预期保持；1.20.1构建加载器依赖修正。')
