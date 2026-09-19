from pathlib import Path
import hashlib,json,re,zipfile,xml.etree.ElementTree as ET,shutil
R=Path(__file__).resolve().parents[1];release=R/'versions/1.1.3'
records=[]
for p in sorted(release.glob('mc*')):
 props=dict(line.split('=',1) for line in (p/'gradle.properties').read_text(encoding='utf-8').splitlines() if '=' in line and not line.startswith('#'))
 v=props['minecraft_version'];java=int(props['java_version'])
 totals={x:0 for x in ['tests','failures','errors','skipped']}
 reports=list((p/'build/test-results/test').glob('TEST-*.xml'));assert reports,v
 for f in reports:
  test=ET.parse(f).getroot()
  for key in totals:totals[key]+=int(test.attrib[key])
 expected={'26.1.2':123,'26.2':125,'1.21.11':126,'1.21.8':126,'1.21.4':124,'1.21.1':124,'1.20.4':124,'1.20.1':124}
 assert totals['tests']==expected[v] and totals['failures']==totals['errors']==totals['skipped']==0,(v,totals)
 paths=[p/'dist'/f'quick-build-1.1.3-mc{v}-fabric.jar',p/'dist'/f'quick-build-1.1.3-mc{v}-fabric-sources.jar']
 jar=paths[0]
 with zipfile.ZipFile(jar) as z:
  names=set(z.namelist());manifest=json.loads(z.read('fabric.mod.json'));assert manifest['version']=='1.1.3',v
  assert manifest['depends']['minecraft']=='~'+v,v
  assert 'jei' not in manifest['depends'] and 'jei_mod_plugin' in manifest['entrypoints'],v
  assert json.loads(z.read('quick_build.mixins.json'))['compatibilityLevel']=='JAVA_'+str(java),v
  for config in manifest['mixins']:
   config=config['config'] if isinstance(config,dict) else config
   d=json.loads(z.read(config));assert int(d['compatibilityLevel'].split('_')[1])<=java,(v,config)
   for c in d.get('mixins',[])+d.get('client',[]):assert (d['package']+'.'+c).replace('.','/')+'.class' in names,(v,c)
  languages={loc:json.loads(z.read('assets/light/lang/'+loc+'.json')) for loc in ['en_us','zh_cn','zh_tw','ja_jp','ko_kr','de_de','fr_fr','es_es']}
  for loc,data in languages.items():
   assert data.keys()==languages['en_us'].keys(),(v,loc)
   for key,value in data.items():assert re.findall(r'%(?:\d+\$)?[sdif]',value)==re.findall(r'%(?:\d+\$)?[sdif]',languages['en_us'][key]),(v,loc,key)
  assert languages['zh_cn']['block.light.variable_constant_light']=='可调恒亮灯'
  assert not any(n.endswith('/copy_variable_constant_light.json') and ('/recipe/' in n or '/recipes/' in n) for n in names),v
  icons=[n for n in names if n.startswith('assets/light/textures/item/redstone_energy_remote_switch') and n.endswith('_inventory.png')];assert len(icons)==16,(v,len(icons))
  if v in ['1.21.1','1.20.4','1.20.1']:assert 'cn/quit5700/light/mixin/client/SwitchIconMixin.class' in names,v
  for n in names:
   if n.startswith('cn/quit5700/') and n.endswith('.class'):assert int.from_bytes(z.read(n)[6:8],'big')<=java+44,(v,n)
  assert not any(n.startswith('snownee/jade/') for n in names),v
 artifacts=[]
 for f in paths:
  assert f.is_file(),f
  dest=R/'dist/1.1.3'/p.name/f.name;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(f,dest)
  artifacts.append({'file':f.name,'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
 records.append({'minecraft':v,'java':java,'loader':props['loader_version'],'fabric_api':props['fabric_version'],'loom':props['loom_version'],'yarn':'not-used; Mojang official mappings','tests':totals,'artifacts':artifacts})
out=release/'发布核验.json';out.write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'versions':len(records),'tests':sum(r['tests']['tests'] for r in records),'failures':0,'artifacts':sum(len(r['artifacts']) for r in records)},ensure_ascii=False))
