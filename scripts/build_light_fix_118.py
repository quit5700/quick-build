from pathlib import Path
import os, sys, subprocess, xml.etree.ElementTree as ET

r=Path(__file__).resolve().parents[1]
root=r/'versions/1.1.8-test1'
for v in sys.argv[1:]:
    d=root/('mc'+v)
    env=os.environ.copy()
    env['JAVA_HOME']='C:/Program Files/Zulu/zulu-25' if v.startswith('26.') else 'C:/Program Files/Java/jdk-21'
    for step,args,signal in [
        ('世界回归', ['runServer','-PwandSmoke'], 'Q5_MOVE_REGRESSION_PASS: 31 cases'),
        ('正式构建', ['clean','test','build'], 'BUILD SUCCESSFUL')]:
        log=root/(step+'_mc'+v+'.log')
        with log.open('w',encoding='utf-8') as f:
            proc=subprocess.Popen([str(d/'gradlew.bat'),'-p',str(d),*args,'--console=plain'],cwd=r,env=env,stdout=f,stderr=subprocess.STDOUT)
            try: code=proc.wait(timeout=300)
            except subprocess.TimeoutExpired:
                subprocess.run(['taskkill','/PID',str(proc.pid),'/T','/F'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
                raise RuntimeError(f'{v} isolated {step} timed out')
        t=log.read_text(encoding='utf-8',errors='replace')
        if code or signal not in t or 'Q5_CASE_FAIL:' in t:
            print(t[-8500:],flush=True)
            raise RuntimeError(f'{v} {step} failed')
        print(v+' '+step+': PASS',flush=True)
    suites=[ET.parse(p).getroot() for p in (d/'build/test-results/test').glob('TEST-*.xml')]
    assert sum(int(s.attrib['tests']) for s in suites)>=193
    assert all(int(s.attrib.get(k,0))==0 for s in suites for k in ['failures','errors','skipped'])
    print(v+' final verification complete',flush=True)
