from pathlib import Path
r=Path.cwd()
for v in ['1.20.4','1.20.1']:
 f=r/'versions/1.1.4'/('mc'+v)/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';s=f.read_text(encoding='utf-8').replace('net.minecraft.resources.new ResourceLocation(', 'new net.minecraft.resources.ResourceLocation(').replace('p.systemMessages.getLast()', 'p.systemMessages.get(p.systemMessages.size()-1)');f.write_text(s,encoding='utf-8')
print('Java17世界测试构造器及列表末项访问按相同语义修正。')
