from pathlib import Path
import json,hashlib,shutil,zipfile,sys,xml.etree.ElementTree as ET
r=Path(__file__).resolve().parents[1];root=r/'versions/1.1.6';selected=sys.argv[1:] or ['26.2','26.1.2','1.21.11','1.21.8','1.21.4','1.21.1','1.20.4','1.20.1'];h=Path('H:/Codex交接文档库/快捷建筑模组_可调恒亮灯改版_01a0afdc/正式版1.1.6_20261005');h.mkdir(parents=True,exist_ok=True);game=Path('G:/game/mcfabric/.minecraft/versions').resolve();locations={p['minecraft']:Path(p['installed']).parent for p in json.loads((r/'versions/1.1.3/安装核验.json').read_text(encoding='utf-8'))};sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();records=[]
existing=root/'安装核验.json'
if existing.exists():records=json.loads(existing.read_text(encoding='utf-8'))
for v in selected:
 for phase in ['prepare','verify','persist']:
  lightlog=root/f'灯加载回归_mc{v}_{phase}.log';lighttext=lightlog.read_text(encoding='utf-8',errors='replace');signal='Q5_LIGHT_LOAD_PREPARED:' if phase=='prepare' else 'Q5_LIGHT_LOAD_PASS: '+phase;assert signal in lighttext and 'Q5_LIGHT_LOAD_FAIL:' not in lighttext,(v,phase)
 d=root/('mc'+v);log=root/('正式构建_mc'+v+'.log');assert log.exists() and 'BUILD SUCCESSFUL' in log.read_text(encoding='utf-8',errors='replace'),v
 suites=[ET.parse(f).getroot() for f in (d/'build/test-results/test').glob('TEST-*.xml')];assert sum(int(s.attrib.get('tests',0)) for s in suites)>=193 and all(int(s.attrib.get(k,0))==0 for s in suites for k in ['failures','errors','skipped']),v
 assert any('Q5_MOVE_REGRESSION_PASS: 31 cases' in f.read_text(encoding='utf-8',errors='replace') and 'Q5_CASE_FAIL:' not in f.read_text(encoding='utf-8',errors='replace') for f in root.glob('世界回归_mc'+v+'*.log')),v
 src=d/'dist'/f'quick-build-1.1.6-mc{v}-fabric.jar';sources=src.with_name(src.stem+'-sources.jar');assert sources.exists()
 with zipfile.ZipFile(src) as z:
  mod=json.loads(z.read('fabric.mod.json'));assert mod['id']=='quick_build' and mod['version']=='1.1.6';assert not any('WandSmokeChecks' in name or 'LightLoadSmokeChecks' in name for name in z.namelist())
  for name in ['building_wand','move_wand']:
   path=f'assets/building_wand/textures/item/{name}.png';assert z.read(path)==(root/'mc26.2/src/main/resources'/path).read_bytes()
 with zipfile.ZipFile(src) as z:
  assert z.read('assets/pathfinding_beacon/models/item/id_sequence_reorderer.json')==(root/'mc26.2/src/main/resources/assets/pathfinding_beacon/models/item/id_sequence_reorderer.json').read_bytes()
  for renderer in ['BuildBoxRenderer','DeleteBoxRenderer','WandPreviewRenderer']:
   compiled=z.read('cn/quit5700/buildingwand/client/'+renderer+'.class');assert b'getMethod' not in compiled and b'getCameraEntity' not in compiled,(v,renderer)
 mods=locations[v].resolve();assert mods.is_relative_to(game) and mods.is_dir(),str(mods);target=mods/src.name
 for folder in [r/'dist/1.1.6'/('mc'+v),h/('mc'+v)]:
  folder.mkdir(parents=True,exist_ok=True)
  for jar in [src,sources]:shutil.copy2(jar,folder/jar.name);assert sha(jar)==sha(folder/jar.name)
 previous=[]
 for f in mods.glob('*.jar'):
  try:
   with zipfile.ZipFile(f) as z:
    data=json.loads(z.read('fabric.mod.json'))
    if data.get('id')=='quick_build':previous.append(f)
  except (zipfile.BadZipFile,KeyError,json.JSONDecodeError,UnicodeDecodeError):pass
 if not(len(previous)==1 and previous[0]==target and sha(target)==sha(src)):
  stage=target.with_suffix('.jar.staged');shutil.copy2(src,stage);assert sha(stage)==sha(src)
  backup=h/'安装前旧包'/('mc'+v);backup.mkdir(parents=True,exist_ok=True)
  for f in previous:
   assert f.resolve().parent==mods;dest=backup/f.name;shutil.copy2(f,dest);assert sha(dest)==sha(f)
  for f in previous:f.unlink()
  stage.replace(target)
 assert target.exists() and sha(target)==sha(src)
 active=[]
 for f in mods.glob('*.jar'):
  try:
   with zipfile.ZipFile(f) as z:
    if json.loads(z.read('fabric.mod.json')).get('id')=='quick_build':active.append(f.name)
  except (zipfile.BadZipFile,KeyError,json.JSONDecodeError,UnicodeDecodeError):pass
 assert active==[src.name],active
 record={'minecraft':v,'installed':str(target),'sha256':sha(target),'源码包':str(h/('mc'+v)/sources.name),'唯一运行包':True};records=[x for x in records if x['minecraft']!=v]+[record];existing.write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(record,ensure_ascii=False))
