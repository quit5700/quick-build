from pathlib import Path
import json,re,hashlib,zipfile,shutil,xml.etree.ElementTree as ET
r=Path(__file__).resolve().parents[1];root=r/'versions/1.1.4';versions=['26.2','26.1.2','1.21.11','1.21.8','1.21.4','1.21.1','1.20.4','1.20.1']
archive=Path('H:/Codex交接文档库/快捷建筑模组_可调恒亮灯改版_01a0afdc/正式版1.1.4_20261005');archive.mkdir(parents=True,exist_ok=True)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
results=[]
for v in versions:
 d=root/('mc'+v);log=root/('正式构建_mc'+v+'.log');assert log.exists() and 'BUILD SUCCESSFUL' in log.read_text(encoding='utf-8',errors='replace'),f'正式构建未完成 {v}'
 suites=[ET.parse(f).getroot() for f in (d/'build/test-results/test').glob('TEST-*.xml')];counts={key:sum(int(x.attrib.get(key,0)) for x in suites) for key in ['tests','failures','errors','skipped']};assert counts['tests']>=180 and counts['failures']==counts['errors']==counts['skipped']==0,(v,counts)
 worldlog=[]
 for f in root.glob('世界回归_mc'+v+'*.log'):
  text=f.read_text(encoding='utf-8',errors='replace')
  if 'Q5_MOVE_REGRESSION_PASS: 27 cases' in text and 'Q5_CASE_FAIL:' not in text:worldlog.append(f)
 assert worldlog,'独立世界回归未通过 '+v
 chosen=max(worldlog,key=lambda p:p.stat().st_mtime)
 lang={f.stem:json.loads(f.read_text(encoding='utf-8')) for f in (d/'src/main/resources/assets/building_wand/lang').glob('*.json')};assert len(lang)==8
 keys=set(lang['zh_cn']);assert all(set(data)==keys for data in lang.values());assert all(sorted(re.findall(r'%(?:\d+\$)?[a-zA-Z]',data[key]))==sorted(re.findall(r'%(?:\d+\$)?[a-zA-Z]',lang['zh_cn'][key])) for data in lang.values() for key in keys)
 jars={kind:d/'dist'/f'quick-build-1.1.4-mc{v}-fabric{suffix}.jar' for kind,suffix in [('运行包',''),('源码包','-sources')]}
 for kind,jar in jars.items():assert jar.is_file(),str(jar)
 with zipfile.ZipFile(jars['运行包']) as z:
  names=set(z.namelist());mod=json.loads(z.read('fabric.mod.json'));assert mod['id']=='quick_build' and mod['version']=='1.1.4';assert not any('WandSmokeChecks' in s for s in names),'运行包混入测试类'
  for f in (root/'mc26.2/src/main/resources/assets').rglob('*.png'):
   key='assets/'+f.relative_to(root/'mc26.2/src/main/resources/assets').as_posix();assert key in names and hashlib.sha256(z.read(key)).hexdigest()==sha(f),(v,key)
  path='data/building_wand/'+('recipes' if v.startswith('1.20.') else 'recipe')+'/delete_wand.json';recipe=json.loads(z.read(path));assert recipe['result'].get('item',recipe['result'].get('id'))=='building_wand:delete_wand'
  for f in names:
   if f.endswith('.json'):json.loads(z.read(f).decode('utf-8'))
 for kind,jar in jars.items():
  for folder in [r/'dist/1.1.4'/('mc'+v),archive/('mc'+v)]:folder.mkdir(parents=True,exist_ok=True);shutil.copy2(jar,folder/jar.name);assert sha(folder/jar.name)==sha(jar)
 for name in ['README.md','VERSION_INFO.md']:shutil.copy2(d/name,archive/('mc'+v)/name)
 shutil.copy2(log,archive/('mc'+v)/log.name);shutil.copy2(chosen,archive/('mc'+v)/'独立世界回归_27项通过.log')
 results.append({'minecraft':v,'正式构建':True,'自动测试':counts,'独立世界回归':27,'世界日志':chosen.name,'八语言键及参数一致':True,'运行包排除测试类':True,'纹理与验收基线一致':True,'包':{kind:{'文件':str(jar),'sha256':sha(jar),'字节':jar.stat().st_size} for kind,jar in jars.items()}})
(root/'发布核验.json').write_text(json.dumps(results,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({'验证版本':len(results),'自动测试总数':sum(x['自动测试']['tests'] for x in results),'世界回归总数':len(results)*27,'交付目录':str(archive)},ensure_ascii=False))
