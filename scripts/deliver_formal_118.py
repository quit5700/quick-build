from pathlib import Path
import json,zipfile,shutil,hashlib,xml.etree.ElementTree as E
r=Path.cwd();t=r/'versions/1.1.8';b=r/'versions/1.1.8-test16';h=Path('H:/Codex交接文档库/快捷建筑模组_可调恒亮灯改版_01a0afdc/正式版_1.1.8');h.mkdir(parents=True,exist_ok=True);records=json.loads((t/'安装基线.json').read_text(encoding='utf-8'));out={};deps={};assets=[]
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for v,m in records.items():
 s=t/('mc'+v);base=b/('mc'+v)
 for folder in ['src','libs']:
  for p in (base/folder).rglob('*'):
   if p.is_file():assert p.read_bytes()==(s/p.relative_to(base)).read_bytes(),str(p)
 suites=[E.parse(p).getroot() for p in (s/'build/test-results/test').glob('TEST-*.xml')];assert suites and all(int(x.attrib.get(k,0))==0 for x in suites for k in ['errors','failures','skipped']);count=sum(int(x.attrib['tests']) for x in suites)
 j=s/f'dist/quick-build-1.1.8-mc{v}-fabric.jar';sources=j.with_name(j.stem+'-sources.jar');assert sources.exists()
 with zipfile.ZipFile(j) as z,zipfile.ZipFile(base/f'dist/quick-build-1.1.8-test16-mc{v}-fabric.jar') as oldz:
  assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.8'
  assert set(z.namelist())==set(oldz.namelist())
  for n in z.namelist():
   if n!='fabric.mod.json':assert z.read(n)==oldz.read(n),(v,n)
 full=s/f'dist/quick-build-1.1.8-mc{v}-完整源码.zip'
 with zipfile.ZipFile(full,'w',zipfile.ZIP_DEFLATED) as z:
  for p in s.rglob('*'):
   if p.is_file() and not set(p.relative_to(s).parts)&{'build','run','dist','.gradle','.git'}:z.write(p,p.relative_to(s).as_posix())
 d=Path(m['目录']);old=list(d.glob('quick-build*.jar'));assert len(old)==1;old=old[0];backup=h/('mc'+v)/'旧测试运行包';backup.mkdir(parents=True,exist_ok=True);shutil.copy2(old,backup/old.name)
 tmp=d/(j.name+'.tmp');shutil.copy2(j,tmp);assert sha(tmp)==sha(j);status='已安装'
 try:
  old.unlink()
  try:tmp.replace(d/j.name)
  except Exception:shutil.copy2(backup/old.name,old);raise
 except PermissionError:status='游戏占用，待退出后安装'
 finally:
  if tmp.exists():tmp.unlink()
 if status=='已安装':assert sha(d/j.name)==sha(j)
 out[v]={'状态':status,'目录':str(d),'文件':j.name,'SHA256':sha(j),'单元测试':count,'功能与test16逐字节一致':True}
 props=dict(x.split('=',1) for x in (s/'gradle.properties').read_text(encoding='utf-8').splitlines() if '=' in x);deps[v]={k:props[k] for k in ['minecraft_version','java_version','loader_version','fabric_version','loom_version','yarn_mappings']}
 for dest in [h/('mc'+v),r/'dist/1.1.8'/('mc'+v)]:
  dest.mkdir(parents=True,exist_ok=True)
  for p in [j,sources,full,s/'VERSION_INFO.md']:shutil.copy2(p,dest/p.name)
 assets.extend([str(j),str(sources),str(full)]);print(v+' '+status,flush=True)
summary='快捷建筑1.1.8正式版，2026-10-09用户明确确认。八版沿用test16功能，重新构建包内正式版本；功能与资源除fabric.mod.json版本号外逐字节一致。建筑/删除法杖、生存配方与耐久、区域替换保护和支撑、材料选择与任务、旧版资源和兼容修复均保留。1597项完整单元测试通过。未加入26.3，未修改存档。列车模拟模组造成的卡顿未作为本模组修复内容。'
(t/'README.md').write_text(summary+'\n\n依赖与安装信息见依赖版本.json、安装核验.json，各版本源码位于mc对应目录。',encoding='utf-8')
for name,data in [('安装核验.json',out),('依赖版本.json',deps),('发布文件.json',assets)]:
 (t/name).write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8');shutil.copy2(t/name,h/name)
(t/'发行说明.md').write_text(summary+'\n\n提供8个运行包、8个源码JAR和8个完整源码ZIP。仅将与游戏版本对应的运行包安装到mods（模组）文件夹，源码包不安装。\n',encoding='utf-8')
p=r/'README.md';p.write_text('当前正式版：**1.1.8**（2026-10-09用户确认）。八版源码、依赖和安装记录见 [versions/1.1.8](versions/1.1.8/README.md)。\n\n'+p.read_text(encoding='utf-8'),encoding='utf-8')
p=r/'docs/VERSION_BEHAVIOR_NOTES.md';p.write_text(p.read_text(encoding='utf-8')+'\n'+summary+'\n',encoding='utf-8')
for p in [t/'README.md',t/'发行说明.md']:shutil.copy2(p,h/p.name)