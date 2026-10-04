from pathlib import Path
import json,xml.etree.ElementTree as ET,re,zipfile,hashlib
r=Path.cwd();root=r/'versions/1.1.4'
for d in sorted(root.glob('mc*')):
 suites=[ET.parse(f).getroot() for f in (d/'build/test-results/test').glob('TEST-*.xml')];counts={k:sum(int(x.attrib.get(k,0)) for x in suites) for k in ['tests','failures','errors','skipped']}
 worlds=[]
 for f in root.glob('世界回归_'+d.name+'*.log'):
  s=f.read_text(encoding='utf-8',errors='replace');worlds.append({'log':f.name,'pass':bool(re.search(r'Q5_MOVE_REGRESSION_PASS: 27 cases',s)) and 'Q5_CASE_FAIL:' not in s})
 langs=list((d/'src/main/resources/assets/building_wand/lang').glob('*.json'));data={f.stem:json.loads(f.read_text(encoding='utf-8')) for f in langs};keys=set(data['zh_cn']);langok=len(data)==8 and all(set(a)==keys for a in data.values());placeholderok=all(sorted(re.findall(r'%(?:\d+\$)?[a-zA-Z]',a[key]))==sorted(re.findall(r'%(?:\d+\$)?[a-zA-Z]',data['zh_cn'][key])) for a in data.values() for key in keys)
 print(d.name,counts,'lang',langok,placeholderok,'world',any(w['pass'] for w in worlds))
