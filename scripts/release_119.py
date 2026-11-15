from pathlib import Path
import os,sys,shutil,re,json,hashlib,subprocess,xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
DELIVERY=Path('H:/Codex交接文档库/快捷建筑模组_两项修复接管_20261027/1.1.9_正式版')
VERIFY=DELIVERY/'验证'
VERSIONS=['1.20.1','1.20.4','1.21.1','1.21.4','1.21.8','1.21.10','1.21.11','26.1.2','26.2','26.3']
ICON_SOURCE=ROOT/'assets/confirmed/quick-build-approved.png'
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def project(v):return ROOT/f'versions/1.1.9/mc{v}'
def props(path):return dict(re.findall(r'^([^#\s=]+)=(.*)$',path.read_text(encoding='utf-8'),re.M))
def execute(v,world=False):
 dest=project(v);buildjava='C:/Program Files/Zulu/zulu-25' if v.startswith('26.') else 'C:/Program Files/Java/jdk-21'
 env=dict(os.environ,JAVA_HOME=buildjava,GRADLE_USER_HOME='H:/CodexWork/q5-gradle-cache',JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Djava.io.tmpdir=H:/CodexWork/q5-settings-runtime-tmp')
 if world:
  targets=json.loads((ROOT/'docs/INSTALLATION_TARGETS.json').read_text(encoding='utf-8'));target=next(t for t in targets if t['minecraft']==v)
  run=dest/'run';(run/'mods').mkdir(parents=True,exist_ok=True);api,=Path(target['mods']).glob('fabric-api-*.jar');shutil.copy2(api,run/'mods'/api.name)
  (run/'eula.txt').write_text('eula=true\n',encoding='utf-8');(run/'server.properties').write_text('online-mode=false\nserver-ip=127.0.0.1\nserver-port=0\nlevel-name=q5-release119-regression\nview-distance=2\nsimulation-distance=2\ngenerate-structures=false\nlevel-type=minecraft:flat\nspawn-protection=0\nmax-tick-time=-1\n',encoding='utf-8')
  java='C:/Program Files/Java/jdk-17' if v.startswith('1.20.') else buildjava
  (dest/'world-release119.gradle').write_text("allprojects { tasks.withType(JavaExec).configureEach { executable = '"+java+"/bin/java.exe' } }\n",encoding='utf-8')
  args='runServer -PwandSmoke -I world-release119.gradle';kind='world'
 else:args='test build';kind='build'
 log=VERIFY/f'{kind}-mc{v}.log'
 with log.open('wb') as out:subprocess.run(['cmd.exe','/d','/c','gradlew.bat '+args+' --offline --console=plain'],cwd=dest,env=env,stdout=out,stderr=subprocess.STDOUT,check=True,timeout=1500)
 if world:
  text=log.read_text(encoding='utf-8');assert 'Q5_MOVE_REGRESSION_PASS: 60 cases' in text and 'Q5_RECIPES_PASS: 59' in text and 'Q5_CASE_FAIL:' not in text
  result={'world_cases':60,'other_recipes':59}
 else:
  suites=[ET.parse(p).getroot().attrib for p in (dest/'build/test-results/test').glob('TEST-*.xml')];result={key:sum(int(s.get(key,0)) for s in suites) for key in ['tests','failures','errors','skipped']};assert result['tests']>300 and not any(result[k] for k in ['failures','errors','skipped']),result
  shutil.copytree(dest/'build/test-results/test',VERIFY/f'unit-mc{v}',dirs_exist_ok=True)
 (VERIFY/f'{kind}-mc{v}.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(kind,v,result,flush=True)
if __name__=='__main__':
 action=sys.argv[1]
 if action=='batch':
  import concurrent.futures,time
  deadline=time.monotonic()+1800
  while not all((VERIFY/f'world-mc{v}.json').exists() for v in ['1.20.1','1.20.4']):
   if time.monotonic()>deadline:raise TimeoutError('Initial world validation did not finish')
   time.sleep(3)
  def pipeline(v):
   print('START',v,flush=True);execute(v);execute(v,True);print('DONE',v,flush=True)
  with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
   futures={pool.submit(pipeline,v):v for v in VERSIONS[2:]}
   for future in concurrent.futures.as_completed(futures):future.result()
 else:execute(sys.argv[2],action=='world')
