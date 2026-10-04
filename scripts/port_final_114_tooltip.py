from pathlib import Path
import re
r=Path.cwd();root=r/'versions/1.1.4'
for v in ['1.21.4','1.21.1','1.20.4','1.20.1']:
 d=root/('mc'+v)
 for name in ['BuildingWandItem','DeletionWandItem']:
  f=d/f'src/main/java/cn/quit5700/buildingwand/item/{name}.java';s=f.read_text(encoding='utf-8').replace('import net.minecraft.world.item.component.TooltipDisplay;','').replace('import java.util.function.Consumer;','import java.util.List;').replace('TooltipDisplay display,','').replace('Consumer<Component> tooltip','List<Component> tooltip').replace('tooltip.accept(', 'tooltip.add(')
  if v.startswith('1.20.'):s=s.replace('TooltipContext context','net.minecraft.world.level.Level context')
  f.write_text(s,encoding='utf-8')
print('物品说明保留同等功能，改为目标版本列表接口。')
