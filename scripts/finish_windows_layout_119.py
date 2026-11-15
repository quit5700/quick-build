from pathlib import Path
import time,json,shutil,concurrent.futures
from release_119 import VERSIONS,VERIFY,execute,project
from normalize_windows_sources_119 import normalize,class_hashes
deadline=time.monotonic()+3600
while not all((VERIFY/f'world-mc{v}.json').exists() for v in VERSIONS):
 if time.monotonic()>deadline:raise TimeoutError('World validation remains incomplete')
 time.sleep(3)
pending=[v for v in VERSIONS if not (VERIFY/f'windows-layout-mc{v}.json').exists()]
for v in pending:
 for kind in ['build','world']:
  for suffix in ['log','json']:
   p=VERIFY/f'{kind}-mc{v}.{suffix}';shutil.copy2(p,VERIFY/f'pre-layout-{kind}-mc{v}.{suffix}')
 normalize(v)
def validate(v):
 execute(v)
 before=json.loads((VERIFY/f'binary-before-layout-mc{v}.json').read_text(encoding='utf-8'))
 after=class_hashes(project(v)/f'dist/quick-build-1.1.9-mc{v}-fabric.jar')
 assert before==after,('Production bytecode changed unexpectedly',v)
 (VERIFY/f'layout-bytecode-mc{v}.json').write_text(json.dumps({'passed':True,'production_classes':len(after),'all_class_bytes_identical':True},indent=2)+'\n',encoding='utf-8')
 print('LAYOUT_FINAL_PASS',v,flush=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
 for result in pool.map(validate,pending):pass
(VERIFY/'ALL_FINAL_CHECKS_PASSED.json').write_text(json.dumps({'versions':VERSIONS,'layout_rebuilt':pending,'all_worlds_passed':True},indent=2)+'\n',encoding='utf-8')
print('ALL_FINAL_CHECKS_PASSED',flush=True)
