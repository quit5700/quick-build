from pathlib import Path
v=Path('versions/1.1.4-test11/mc26.2');p=v/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';s=p.read_text(encoding='utf-8')
for literal in ['lost-one','lost-two','other-player','lost-nether','lost','kept']:s=s.replace('"'+literal+'"','UUID.randomUUID().toString()')
s=s.replace('"mod pair spare or material charge incorrect");','"mod pair spare or material charge incorrect");check(ReplacementRules.protectedBlock(world,first,world.getBlockState(first),true),"mod pair not protected like a door");')
p.write_text(s,encoding='utf-8')
p=v/'src/main/java/cn/quit5700/buildingwand/server/ReplacementRules.java';s=p.read_text(encoding='utf-8');s=s.replace('||s.getBlock() instanceof DoublePlantBlock||crop(s)', '||StructurePlacement.multiple(s.getBlock())||s.getBlock() instanceof DoublePlantBlock||crop(s)');p.write_text(s,encoding='utf-8')
p=Path('docs/bugs/BUG-20261004-TASK-CANCEL-NATURAL-DROPS.md');p.write_text(p.read_text(encoding='utf-8')+'\n首轮世界测试中两个取消场景因测试使用lost等非法法杖标识失败；DeleteTaskData要求合法UUID（通用唯一标识），改用真实UUID准备任务，不放宽原校验。自然掉落、余位内容精确掉落和模组属性双格放置场景已通过。模组属性双格也同步纳入原有区域替换保护，避免破坏已有结构。\n',encoding='utf-8')
print('修正取消测试的法杖标识；模组双格同步受保护')