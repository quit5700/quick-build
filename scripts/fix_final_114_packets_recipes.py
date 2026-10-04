from pathlib import Path
import json,shutil
r=Path.cwd();root=r/'versions/1.1.4'
for v in ['1.20.4','1.20.1']:
 d=root/('mc'+v)
 f=d/'src/main/java/cn/quit5700/buildingwand/network/BuildNetworking.java';s=f.read_text(encoding='utf-8');a=s.index(' public record Cancelled(');s=s[:a]+''' public record Cancelled(int count) implements LegacyWandPayload {
  public static final net.minecraft.resources.ResourceLocation ID=BuildingWandMod.id("builds_cancelled");
  public net.minecraft.resources.ResourceLocation id(){return ID;}
  public void write(FriendlyByteBuf b){b.writeVarInt(count);}
  public static Cancelled read(FriendlyByteBuf b){return new Cancelled(b.readVarInt());}
 }
}
''';f.write_text(s,encoding='utf-8')
 for f in (d/'src/main/java/cn/quit5700/buildingwand/server').rglob('*.java'):
  s=f.read_text(encoding='utf-8').replace('net.fabricmc.fabric.api.networking.v1.cn.quit5700.buildingwand.network.LegacyWandPackets','cn.quit5700.buildingwand.network.LegacyWandPackets').replace('.saveWithFullMetadata(level.registryAccess())','.saveWithFullMetadata()').replace('block.state, block.blockEntityTag, level.registryAccess()', 'block.state, block.blockEntityTag');f.write_text(s,encoding='utf-8')
for d in root.glob('mc*'):
 v=d.name[2:];data=d/'src/main/resources/data/building_wand';base=root/'mc26.2/src/main/resources/data/building_wand'
 for f in (base/'recipe').glob('*.json'):
  recipe=json.loads(f.read_text(encoding='utf-8'))
  if v in ['1.21.1','1.20.4','1.20.1']:recipe['key']={key:{'item':value} if isinstance(value,str) else value for key,value in recipe['key'].items()}
  folder='recipes' if v.startswith('1.20.') else 'recipe'
  if v.startswith('1.20.'):recipe['result']['item']=recipe['result'].pop('id')
  target=data/folder/f.name;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(json.dumps(recipe,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
  unused=data/('recipe' if folder=='recipes' else 'recipes')/f.name
  if unused.exists():assert unused.resolve().is_relative_to(data.resolve());unused.unlink()
 source=base/'loot_tables/blocks/wand_settings_station.json';folder='loot_tables' if v.startswith('1.20.') else 'loot_table';target=data/folder/'blocks/wand_settings_station.json';target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,target)
 # Keep source baseline available until each native target is written.
 if folder!='loot_tables' and d.name!='mc26.2':
  old=data/'loot_tables/blocks/wand_settings_station.json'
  if old.exists():old.unlink()
 if v in ['1.21.1','1.20.4','1.20.1']:
  f=d/'src/main/java/cn/quit5700/buildingwand/item/DeletionWandItem.java';s=f.read_text(encoding='utf-8').replace('    public DeletionWandItem(Properties properties){super(properties);}', '    public DeletionWandItem(Properties properties){super(properties);}\n    @Override public int getEnchantmentValue(){return 1;}');f.write_text(s,encoding='utf-8')
print('旧版协议和容器存档接口修正；四种法杖/说明台配方按原生目录、格式补齐，删除法杖保持可经验修补。')
