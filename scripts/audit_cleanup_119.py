from pathlib import Path
import os,json,collections
ROOT=Path(__file__).resolve().parents[1]
OLD=Path('C:/Users/quit5700/Documents/mc虚拟线路模组/快捷建筑模组')
DELIVERY=Path('H:/Codex交接文档库/快捷建筑模组_两项修复接管_20261027/1.1.9_正式版')
def audit(base):
 totals=collections.Counter();worlds=[];blueprints=[];links=[]
 def walk(path,group):
  try:entries=list(os.scandir(path))
  except (PermissionError,FileNotFoundError):return
  for e in entries:
   p=Path(e.path);info=e.stat(follow_symlinks=False)
   if getattr(info,'st_file_attributes',0)&0x400:links.append(str(p));continue
   if e.is_dir(follow_symlinks=False):
    if p.name=='saves':worlds.extend(str(c) for c in p.iterdir() if c.is_dir())
    if p.name.lower() in ('blueprints','building_wand_blueprints','q5-blueprints'):blueprints.append(str(p))
    walk(p,group)
   else:totals[group]+=info.st_size
 for e in os.scandir(base):
  p=Path(e.path);info=e.stat(follow_symlinks=False)
  if getattr(info,'st_file_attributes',0)&0x400:links.append(str(p));continue
  if e.is_dir(follow_symlinks=False):walk(p,p.name)
  else:totals['根目录文件']+=info.st_size
 return {'root':str(base),'bytes':sum(totals.values()),'groups':dict(totals),'worlds':worlds,'blueprint_dirs':blueprints,'links':links}
result=audit(OLD)
(DELIVERY/'清理前C盘清单.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('C_GiB',round(result['bytes']/2**30,3),'worlds',len(result['worlds']),'blueprints',len(result['blueprint_dirs']),flush=True)
print('world_names',sorted(set(Path(p).name for p in result['worlds'])),flush=True)
print('blueprint_dirs',result['blueprint_dirs'][:12],flush=True)
