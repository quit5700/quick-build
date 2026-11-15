from pathlib import Path
import os,json,zipfile,hashlib,sys
ROOT=Path(__file__).resolve().parents[1]
VERIFY=Path('H:/Codex交接文档库/快捷建筑模组_两项修复接管_20261027/1.1.9_正式版/验证')
def class_hashes(jar):
 with zipfile.ZipFile(jar) as z:return {n:hashlib.sha256(z.read(n)).hexdigest() for n in z.namelist() if n.endswith('.class')}
def normalize(v):
 project=ROOT/f'versions/1.1.9/mc{v}';marker=VERIFY/f'windows-layout-mc{v}.json'
 if marker.exists():return
 jar=project/f'dist/quick-build-1.1.9-mc{v}-fabric.jar'
 if jar.exists():(VERIFY/f'binary-before-layout-mc{v}.json').write_text(json.dumps(class_hashes(jar),sort_keys=True),encoding='utf-8')
 moved=[]
 for old in sorted((project/'src').rglob('aux'),key=lambda p:len(p.parts),reverse=True):
  if not old.is_dir():continue
  target=old.with_name('auxiliary');assert old.parent.resolve().is_relative_to(ROOT.resolve()) and target.parent.resolve().is_relative_to(ROOT.resolve());assert not target.exists()
  os.rename('\\\\?\\'+str(old),'\\\\?\\'+str(target));moved.append(str(target.relative_to(ROOT)))
 gradle=project/'build.gradle'
 with gradle.open('a',encoding='utf-8') as f:f.write('''\n// Keep legacy binary package names while avoiding reserved Windows source directories.
tasks.named('sourcesJar') {
    includeEmptyDirs = false
    eachFile { details -> details.path = details.path.replace('/auxiliary/', '/aux/') }
}
''')
 marker.write_text(json.dumps({'directories':moved,'binary_package_changed':False},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print('LAYOUT_PASS',v,len(moved),flush=True)
if __name__=='__main__':
 for v in sys.argv[1:]:normalize(v)
