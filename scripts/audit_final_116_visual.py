from pathlib import Path
import json,hashlib
r=Path.cwd();root=r/'versions/1.1.6';baseline=root/'mc26.2/src/main/resources/assets';result=[]
for d in root.glob('mc*'):
 entries=[]
 for f in baseline.rglob('*'):
  if not f.is_file() or not (f.suffix=='.png' or 'remote_switch' in f.name and f.suffix=='.json' or f.name=='id_sequence_reorderer.json' and 'models' in f.parts):continue
  other=d/'src/main/resources/assets'/f.relative_to(baseline)
  entries.append({'文件':str(f.relative_to(baseline)),'一致':other.exists() and other.read_bytes()==f.read_bytes()})
 result.append({'版本':d.name,'图片及开关资源数':len(entries),'差异':[e for e in entries if not e['一致']]})
(root/'外观资源核查.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
for v in result:print(v['版本'],v['图片及开关资源数'],'差异',len(v['差异']))
