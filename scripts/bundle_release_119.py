from pathlib import Path
import json,zipfile,hashlib,shutil,struct,re
ROOT=Path(__file__).resolve().parents[1]
DELIVERY=Path('H:/Codex交接文档库/快捷建筑模组_两项修复接管_20261027/1.1.9_正式版')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def bundle():
 baselines=json.loads((ROOT/'docs/RELEASE_1.1.9_BASELINES.json').read_text(encoding='utf-8'))
 targets=json.loads((ROOT/'docs/INSTALLATION_TARGETS.json').read_text(encoding='utf-8'))
 icon=ROOT/'assets/confirmed/quick-build-approved.png';ih=sha(icon)
 reports={};attachments=DELIVERY/'运行包与源码包';attachments.mkdir(exist_ok=True)
 for v,baseline in baselines.items():
  project=ROOT/f'versions/1.1.9/mc{v}';verify=DELIVERY/'验证'
  tests=json.loads((verify/f'build-mc{v}.json').read_text(encoding='utf-8'));world=json.loads((verify/f'world-mc{v}.json').read_text(encoding='utf-8'))
  paths=[project/f'dist/quick-build-1.1.9-mc{v}-fabric.jar',project/f'dist/quick-build-1.1.9-mc{v}-fabric-sources.jar']
  for p in paths:
   with zipfile.ZipFile(p) as z:
    meta=json.loads(z.read('fabric.mod.json'));assert meta['version']=='1.1.9',meta
    assert hashlib.sha256(z.read(meta['icon'])).hexdigest()==ih
    png=z.read(meta['icon']);assert struct.unpack('>II',png[16:24])==(1254,1254)
    assert not any(any(word in n for word in ['MouseProbe','DragProbe','BeaconProbe','WandSmokeChecks','PortProbe']) for n in z.namelist())
    assert not any('/recipe' in n and 'generation_box' in n for n in z.namelist())
   shutil.copy2(p,attachments/p.name)
  reports[v]={'version':'1.1.9','baseline':baseline['baseline'],'tests':tests,'world':world,'runtime':str(attachments/paths[0].name),'runtime_sha256':sha(paths[0]),'sources':str(attachments/paths[1].name),'sources_sha256':sha(paths[1]),'icon_sha256':ih,'java':baseline['java'],'loaderMinimum':baseline['loader'],'fabricApiCompile':baseline['api'],'loom':baseline['loom'],'gradle':baseline['gradle'],'installations':[]}
 for target in targets:
  v=target['minecraft'];mods=Path(target['mods']);profile=Path(target['profile']);data=json.loads(profile.read_text(encoding='utf-8'))
  with zipfile.ZipFile(profile.with_suffix('.jar')) as z:assert json.loads(z.read('version.json'))['id']==v
  loader=[a['name'] for a in data['libraries'] if 'fabric-loader:' in a['name']]
  apis=list(mods.glob('fabric-api-*.jar'));assert len(apis)==1
  old=list(mods.glob('quick-build-*.jar'));assert len(old)==1,old
  installation={'minecraft':v,'instance':target['instance'],'profile':str(profile),'mods':str(mods),'actualLoader':loader,'actualApi':apis[0].name,'previousActive':str(old[0]),'runtime':reports[v]['runtime'],'runtime_sha256':reports[v]['runtime_sha256']}
  reports[v]['installations'].append(installation)
 (DELIVERY/'正式版交付核验.json').write_text(json.dumps(reports,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print('BUNDLE_PASS',len(reports),'versions',len(list(attachments.glob('*.jar'))),'artifacts',flush=True)
def install():
 report=json.loads((DELIVERY/'正式版交付核验.json').read_text(encoding='utf-8'));updated=[];backup=DELIVERY/'安装过渡备份';backup.mkdir(exist_ok=True)
 for v,data in report.items():
  for item in data['installations']:
   mods=Path(item['mods']);runtime=Path(item['runtime']);old=Path(item['previousActive'])
   others={p.name:sha(p) for p in mods.glob('*.jar') if not p.name.startswith('quick-build-')}
   if old.exists():
    before=backup/item['instance'];before.mkdir(exist_ok=True);shutil.copy2(old,before/old.name)
    disabled=old.with_name(old.name+'.disabled.pre-1.1.9-formal');assert not disabled.exists();old.rename(disabled)
   installed=mods/runtime.name;shutil.copy2(runtime,installed);assert sha(installed)==data['runtime_sha256']
   assert others=={p.name:sha(p) for p in mods.glob('*.jar') if not p.name.startswith('quick-build-')}
   assert len(list(mods.glob('quick-build-*.jar')))==1
   item.update(installed=str(installed),installed_sha256=sha(installed),unrelatedModsUnchanged=True)
   updated.append({'minecraft':v,'instance':item['instance'],'profile':item['profile'],'mods':str(mods),'loader':item['actualLoader'][0].split(':')[-1],'fabricApi':item['actualApi'],'active':str(installed),'activeVersion':'1.1.9','activeSHA256':sha(installed),'status':'正式版已安装并核验'})
 (DELIVERY/'正式版交付核验.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 (ROOT/'docs/INSTALLATION_TARGETS.json').write_text(json.dumps(updated,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 (ROOT/'docs/INSTALLATION_TARGETS.md').write_text('# 1.1.9正式版安装目标\n\n2026-11-16逐实例核实游戏版本与加载器，运行包与分发包SHA-256一致；源码包不安装。\n\n|游戏版本|实例|模组目录|\n|---|---|---|\n'+''.join(f"|{x['minecraft']}|{x['instance']}|{x['mods']}|\n" for x in updated),encoding='utf-8')
 print('INSTALL_PASS',len(updated),'instances',flush=True)
if __name__=='__main__':
 import sys
 if sys.argv[1]=='bundle':bundle()
 else:install()
