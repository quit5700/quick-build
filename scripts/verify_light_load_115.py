from pathlib import Path
import os, sys, re, subprocess, time

r = Path(__file__).resolve().parents[1]
root = r / 'versions/1.1.5'
versions = sys.argv[1:] or ['26.1.2']
for v in versions:
    d = root / ('mc' + v)
    env = os.environ.copy()
    env['JAVA_HOME'] = 'C:/Program Files/Zulu/zulu-25' if v.startswith('26.') else 'C:/Program Files/Java/jdk-21'
    env['PYTHONUTF8'] = '1'
    props = d / 'run/server.properties'
    previous = props.read_text(encoding='utf-8')
    props.write_text(re.sub(r'^level-name=.*$', 'level-name=q5-light-load-115-regression-world', previous, flags=re.M), encoding='utf-8')
    try:
        for phase in ['prepare', 'verify', 'persist']:
            log = root / f'灯加载回归_mc{v}_{phase}.log'
            args = [str(d/'gradlew.bat'), '-p', str(d), 'runServer', '-PlightLoadSmoke', '-PlightLoadPhase='+phase, '--console=plain']
            with log.open('w', encoding='utf-8') as f:
                process = subprocess.Popen(args, env=env, cwd=r, stdout=f, stderr=subprocess.STDOUT)
                try:
                    result = process.wait(timeout=240)
                except subprocess.TimeoutExpired:
                    # Only terminate this isolated test JVM/Gradle process tree, never the player's game.
                    subprocess.run(['taskkill', '/PID', str(process.pid), '/T', '/F'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                    raise RuntimeError(f'{v} isolated load regression timed out: {log}')
            text=log.read_text(encoding='utf-8', errors='replace')
            expected='Q5_LIGHT_LOAD_PREPARED:' if phase=='prepare' else 'Q5_LIGHT_LOAD_PASS: '+phase
            if result or expected not in text or 'Q5_LIGHT_LOAD_FAIL:' in text:
                print(text[-8500:], flush=True)
                raise RuntimeError(f'{v} {phase} failed: {log}')
            print(f'{v} {phase}: PASS', flush=True)
    finally:
        props.write_text(previous, encoding='utf-8')
