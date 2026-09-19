from pathlib import Path
import zipfile,hashlib,json,shutil
R=Path(__file__).resolve().parents[1]
A=Path('H:/Codex交接文档库/快捷建筑模组_可调恒亮灯改版_01a0afdc/最终正式版_1.1.3');A.mkdir(parents=True,exist_ok=True)
for p in (R/'versions/1.1.3').glob('mc*'):
 d=A/p.name;d.mkdir(exist_ok=True)
 for f in (p/'dist').glob('*.jar'):shutil.copy2(f,d/f.name)
 for name in ['README.md','VERSION_INFO.md']:shutil.copy2(p/name,d/name)
for name in ['README.md','发布核验.json','安装核验.json']:shutil.copy2(R/'versions/1.1.3'/name,A/name)
archive=A/'完整八版本工程源码_1.1.3.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
 for folder in [R/'versions/1.1.3',R/'scripts',R/'docs']:
  for p in folder.rglob('*'):
   if p.is_file() and not any(x in p.relative_to(folder).parts for x in ['.gradle','build','dist','run']):z.write(p,p.relative_to(R).as_posix())
 for p in [R/'AGENTS.md',R/'README.md',R/'.gitignore']:z.write(p,p.name)
records=[{'file':p.relative_to(A).as_posix(),'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in A.rglob('*') if p.is_file() and p.name!='最终交付校验.json']
(A/'最终交付校验.json').write_text(json.dumps(records,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'files':len(records),'archive':str(archive),'bytes':archive.stat().st_size},ensure_ascii=False))
