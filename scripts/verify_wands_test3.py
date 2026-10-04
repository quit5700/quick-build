from pathlib import Path
import json,re,zipfile,hashlib,xml.etree.ElementTree as ET
root=Path.cwd();v=root/'versions/1.1.4-test3/mc26.2';out=root/'outputs/20261004_建筑移动法杖26.2测试版3';out.mkdir(parents=True,exist_ok=True)
reports=list((v/'build/test-results/test').glob('TEST-*.xml'));count=failures=errors=0
for p in reports:
 a=ET.parse(p).getroot().attrib;count+=int(a.get('tests',0));failures+=int(a.get('failures',0));errors+=int(a.get('errors',0))
assert count==170 and failures==errors==0,(count,failures,errors)
for ns in ['building_wand','light','pathfinding_beacon']:
 folder=v/f'src/main/resources/assets/{ns}/lang';data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in folder.glob('*.json')};assert len(data)==8,ns
 base=data['en_us']
 for lang,d in data.items():
  assert set(d)==set(base),(ns,lang)
  for k,val in base.items():assert re.findall(r'%(?:\d+\$)?[sd]',val)==re.findall(r'%(?:\d+\$)?[sd]',d[k]),(ns,lang,k)
for p in (v/'src/main/resources').rglob('*.json'):json.loads(p.read_text(encoding='utf-8'))
files=list((v/'dist').glob('*.jar'));assert len(files)==2
runtime=next(p for p in files if 'sources' not in p.name)
with zipfile.ZipFile(runtime) as z:
 names=z.namelist();assert not any('WandSmokeChecks' in x for x in names)
 for name in ['BuildTaskState','BuildWandServer','MoveUpdates']:assert f'cn/quit5700/buildingwand/server/{name}.class' in names
 assert 'cn/quit5700/light/mixin/MoveBlockEventsAccessor.class' in names
 meta=json.loads(z.read('fabric.mod.json'));assert meta['version']=='1.1.4-test3'
 langs=json.loads(z.read('assets/building_wand/lang/zh_cn.json'));assert langs['block.building_wand.wand_settings_station']=='Q5模组方块说明台'
log=(out/'独立世界通过日志.log').read_text(encoding='utf-8');assert 'Q5_WAND_SMOKE_PASS' in log and 'Q5_WAND_SMOKE_FAIL' not in log
result={'版本':'1.1.4-test3','Minecraft':'26.2','自动测试':count,'失败':failures,'错误':errors,'八语言键及参数':'通过','资源JSON':'通过','独立世界实际测试':'通过','生产包排除测试类':'通过','文件':{p.name:{'字节':p.stat().st_size,'SHA256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in files}}
(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False,indent=2))