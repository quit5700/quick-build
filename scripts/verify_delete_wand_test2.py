from pathlib import Path
import json,re,xml.etree.ElementTree as ET,hashlib
root=Path.cwd(); version=root/'versions/1.1.4-test2/mc26.2'
reports=list((version/'build').rglob('TEST-*.xml'))
print('reports',[(str(p),ET.parse(p).getroot().attrib.get('tests')) for p in reports][:4])
langs=version/'src/main/resources/assets/building_wand/lang'
data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in langs.glob('*.json')}
base=data['en_us']; assert len(data)==8
for name,d in data.items():
 assert set(d)==set(base),(name,'keys')
 for key,val in base.items():assert re.findall(r'%(?:\d+\$)?[sd]',val)==re.findall(r'%(?:\d+\$)?[sd]',d[key]),(name,key)
for p in (version/'src/main/resources').rglob('*.json'):json.loads(p.read_text(encoding='utf-8'))
unique={p.name:p for p in reports}; tests=failures=errors=0
for p in unique.values():
 a=ET.parse(p).getroot().attrib;tests+=int(a.get('tests',0));failures+=int(a.get('failures',0));errors+=int(a.get('errors',0))
result={'自动测试数量':tests,'失败':failures,'错误':errors,'语言数量':len(data),'语言键数量':len(base),'JSON校验':'通过'}
assert not failures and not errors
out=root/'outputs/20261003_删除法杖26.2测试版2';out.mkdir(parents=True,exist_ok=True)
(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result,ensure_ascii=False,indent=2))