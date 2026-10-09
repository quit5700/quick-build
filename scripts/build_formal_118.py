from pathlib import Path
import shutil,subprocess,os,json
r=Path.cwd();b=r/'versions/1.1.8-test16';t=r/'versions/1.1.8';t.mkdir(exist_ok=False)
records=json.loads((b/'安装核验.json').read_text(encoding='utf-8'));(t/'安装基线.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
for v in records:
 source=b/('mc'+v);s=t/('mc'+v);shutil.copytree(source,s,ignore=shutil.ignore_patterns('run','dist','*.log'))
 p=s/'gradle.properties';p.write_text(p.read_text(encoding='utf-8').replace('mod_version=1.1.8-test16','mod_version=1.1.8'),encoding='utf-8')
 for n in ['README.md','VERSION_INFO.md']:
  p=s/n;p.write_text(p.read_text(encoding='utf-8').replace('1.1.8-test16测试版','1.1.8正式版')+'\n2026-10-09用户明确确认正式版；功能与test16一致，无新增玩法修改。\n',encoding='utf-8')
 env=os.environ.copy();env['JAVA_HOME']='C:/Program Files/Zulu/zulu-25' if v.startswith('26.') else 'C:/Program Files/Java/jdk-21';env['JAVA_TOOL_OPTIONS']='-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
 with (t/('构建_'+v+'.log')).open('w',encoding='utf-8') as log:
  result=subprocess.run([str(s/'gradlew.bat'),'-p',str(s),'test','build','--console=plain'],cwd=r,env=env,stdout=log,stderr=subprocess.STDOUT)
 if result.returncode:raise SystemExit('构建失败 '+v)
 print(v+' 正式版构建通过',flush=True)