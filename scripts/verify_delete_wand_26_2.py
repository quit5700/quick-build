from pathlib import Path
from PIL import Image
import json, re, hashlib, zipfile, xml.etree.ElementTree as ET, sys

sys.stdout.reconfigure(encoding='utf-8')
root=Path(__file__).resolve().parents[1]
project=root/'versions/1.1.4-test1/mc26.2'
assets=project/'src/main/resources/assets/building_wand'
languages=['en_us','zh_cn','zh_tw','ja_jp','ko_kr','de_de','fr_fr','es_es']
lang={name:json.loads((assets/f'lang/{name}.json').read_text(encoding='utf-8')) for name in languages}
expected=set(lang['zh_cn'])
for name,data in lang.items():
    assert set(data)==expected,(name,'keys')
    for key in expected:
        assert re.findall(r'%(?:\d+\$)?[sd]',data[key])==re.findall(r'%(?:\d+\$)?[sd]',lang['zh_cn'][key]),(name,key,'placeholders')
model=json.loads((assets/'models/item/delete_wand.json').read_text(encoding='utf-8'))
for element in model['elements']:
    assert all(-16<=n<=32 for n in element['from']+element['to'])
    if 'rotation' in element:assert element['rotation']['angle'] in [-45,-22.5,0,22.5,45]
for texture in model['textures'].values():
    if texture.startswith('#'):continue
    namespace,location=texture.split(':')
    assert (project/f'src/main/resources/assets/{namespace}/textures/{location}.png').is_file()
for name in ['items/delete_wand.json','models/item/delete_wand_inventory.json']:
    json.loads((assets/name).read_text(encoding='utf-8'))
with Image.open(assets/'textures/item/delete_wand_inventory.png') as icon:
    assert icon.size==(64,64) and icon.mode=='RGBA'
    assert icon.getchannel('A').getextrema()==(0,255)
recipe=json.loads((project/'src/main/resources/data/building_wand/recipe/delete_wand.json').read_text(encoding='utf-8'))
assert recipe['result']=={'id':'building_wand:delete_wand','count':1}
assert recipe['pattern']==['R','S','D']
assert recipe['key']=={'R':'minecraft:redstone_block','S':'minecraft:stick','D':'minecraft:diamond'}
totals={key:0 for key in ['tests','failures','errors','skipped']}
for path in (project/'build/test-results/test').glob('TEST-*.xml'):
    doc=ET.parse(path).getroot()
    for key in totals:totals[key]+=int(doc.attrib.get(key,0))
assert totals['tests']==141,totals
assert totals['failures']==totals['errors']==totals['skipped']==0,totals
files=[]
for path in sorted((project/'dist').glob('*.jar')):
    with zipfile.ZipFile(path) as jar:
        assert jar.testzip() is None
        if not path.name.endswith('-sources.jar'):
            metadata=json.loads(jar.read('fabric.mod.json'))
            assert metadata['version']=='1.1.4-test1'
            for name in ['cn/quit5700/buildingwand/server/DeleteTaskState.class',
                         'cn/quit5700/buildingwand/logic/DeleteTaskData.class',
                         'cn/quit5700/buildingwand/client/DeleteBoxRenderer.class',
                         'assets/building_wand/items/delete_wand.json',
                         'assets/building_wand/models/item/delete_wand.json',
                         'assets/building_wand/textures/item/delete_wand_inventory.png',
                         'data/building_wand/recipe/delete_wand.json']:
                assert name in jar.namelist(),name
            assert metadata['depends']['minecraft']=='~26.2'
            assert metadata['suggests']['jei']=='*'
        else:
            assert 'cn/quit5700/buildingwand/server/DeleteTaskState.java' in jar.namelist()
    files.append({'file':str(path),'bytes':path.stat().st_size,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
assert len(files)==2
result={'version':'1.1.4-test1','minecraft':'26.2','tests':totals,'languages':languages,
        'model_elements':len(model['elements']),'recipe':'redstone_block/stick/diamond',
        'ingame_test':'pending user verification','dedicated_server_check':'dependency loading and EULA check only; no world entered',
        'files':files}
output=root/'outputs/20261003_删除法杖26.2测试版'
output.mkdir(parents=True,exist_ok=True)
(output/'交付校验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result,ensure_ascii=False,indent=2))
