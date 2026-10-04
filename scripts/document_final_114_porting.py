from pathlib import Path
r=Path.cwd();f=r/'versions/1.1.4/mc26.2/src/main/resources/data/building_wand/loot_tables/blocks/wand_settings_station.json';native=f.parents[2]/'loot_table/blocks/wand_settings_station.json'
if native.exists() and native.read_bytes()==f.read_bytes():assert f.resolve().is_relative_to((r/'versions/1.1.4').resolve());f.unlink()
root=r/'docs/bugs';f=root/'BUG_INDEX.md'
s=f.read_text(encoding='utf-8');entry='\n- [BUG-20261004-FORMAL-114-PORTING](BUG-20261004-FORMAL-114-PORTING.md)：正式1.1.4八版本接口适配、历史法杖纹理遗漏、旧版静默装配及独立世界实体观测验证；交付正在最终核验。\n'
if 'BUG-20261004-FORMAL-114-PORTING' not in s:f.write_text(s+entry,encoding='utf-8')
f=r/'docs/PORTING_NOTES.md'
with f.open('a',encoding='utf-8') as stream:stream.write('\n## 正式1.1.4八版本适配\n优先参考 docs/bugs/BUG-20261004-FORMAL-114-PORTING.md 与 scripts/port_final_114*.py、scripts/fix_final_114*.py。历史纹理遗漏须比较各目标与验收基线，不能只比较26.2版本差量。26.2/26.1.2采用Mojang官方命名；旧版网络、SavedData、静默写入、Java17列表与DataResult接口须分别适配。独立世界用实际实体分区核查未激活区块掉落，断言保持原精确数量。所有测试源码仅-PwandSmoke启用，运行包排除测试类。1.20.1固定FabricAPI0.92.9要求Loader>=0.16.10，构建使用0.16.14。\n')
for d in (r/'versions/1.1.4').glob('mc*'):
 f=d/'docs/PORTING_NOTES.md';f.parent.mkdir(exist_ok=True)
 with f.open('a',encoding='utf-8') as stream:stream.write('\n正式1.1.4跨版本适配：参考仓库docs/bugs/BUG-20261004-FORMAL-114-PORTING.md；禁止直接删除静默装配副作用保护或回滚验证；旧版方块实体清理必须保留，物品闪烁仅作用物品栏。\n')
print('正式适配记录、旧方案失败原因和各版本移植说明已同步。')
