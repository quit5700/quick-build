"""Edit only the Inventory list of the explicitly selected, unlocked test world.

Existing NBT tag payloads are copied byte-for-byte. No save backup is made.
The caller must hold session.lock exclusively for the entire write operation.
"""
from pathlib import Path
import argparse, gzip, hashlib, json, os, struct, sys, uuid

class Node:
    def __init__(self, kind, value, start, end):
        self.kind,self.value,self.start,self.end=kind,value,start,end

class Reader:
    def __init__(self, data):self.data=data;self.pos=0
    def take(self,n):
        if n<0 or self.pos+n>len(self.data):raise ValueError('Truncated NBT')
        result=self.data[self.pos:self.pos+n];self.pos+=n;return result
    def number(self,fmt):return struct.unpack('>'+fmt,self.take(struct.calcsize('>'+fmt)))[0]
    def text(self):return self.take(self.number('H'))
    def payload(self,kind):
        start=self.pos
        if kind in (1,2,3,4,5,6):value=self.number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[kind])
        elif kind==8:value=self.text()
        elif kind in (7,11,12):value=self.take(self.number('i')*{7:1,11:4,12:8}[kind])
        elif kind==9:
            subtype=self.number('B');count=self.number('i')
            if count<0 or count>len(self.data):raise ValueError('Invalid NBT list length')
            value=(subtype,[self.payload(subtype) for _ in range(count)])
        elif kind==10:
            value={}
            while True:
                subtype=self.number('B')
                if subtype==0:break
                name=self.text().decode('utf-8',errors='surrogateescape')
                if name in value:raise ValueError('Duplicate NBT key')
                value[name]=self.payload(subtype)
        else:raise ValueError('Unsupported NBT tag '+str(kind))
        return Node(kind,value,start,self.pos)
    def root(self):
        if self.number('B')!=10:raise ValueError('Root must be a compound')
        self.text();root=self.payload(10)
        if self.pos!=len(self.data):raise ValueError('Trailing NBT data')
        return root

def named(kind,name,payload):
    encoded=name.encode('utf-8');return bytes([kind])+struct.pack('>H',len(encoded))+encoded+payload
def string(value):
    encoded=value.encode('utf-8');return struct.pack('>H',len(encoded))+encoded
def int_tag(name,value):return named(3,name,struct.pack('>i',value))
def string_tag(name,value):return named(8,name,string(value))
def compound(fields):return b''.join(fields)+b'\0'
def inventory(root):
    if 'Inventory' not in root.value:raise ValueError('Player has no Inventory list')
    node=root.value['Inventory']
    if node.kind!=9 or node.value[0]!=10:raise ValueError('Inventory is not a compound list')
    return node
def value(node,name,default=None):return node.value[name].value if name in node.value else default
def make_wand(slot,wand_id):
    custom=compound([string_tag('QuickBuildDeleteWandId',wand_id),int_tag('QuickBuildDeleteRemainder',0)])
    components=compound([int_tag('minecraft:damage',511),named(10,'minecraft:custom_data',custom)])
    return compound([named(1,'Slot',struct.pack('>b',slot)),string_tag('id','building_wand:delete_wand'),int_tag('count',1),named(10,'components',components)])
def insert(raw,wand_id):
    root=Reader(raw).root();inv=inventory(root);items=inv.value[1]
    slots={value(item,'Slot') for item in items if value(item,'count',value(item,'Count',0))>0}
    free=next((n for n in range(36) if n not in slots),None)
    if free is None:raise ValueError('No empty inventory slot; existing items will not be overwritten')
    payload=bytes([10])+struct.pack('>i',len(items)+1)+b''.join(raw[n.start:n.end] for n in items)+make_wand(free,wand_id)
    updated=raw[:inv.start]+payload+raw[inv.end:]
    check=Reader(updated).root();new_items=inventory(check).value[1]
    assert len(new_items)==len(items)+1
    assert raw[:inv.start]==updated[:inv.start]
    assert raw[inv.end:]==updated[inventory(check).end:]
    for before,after in zip(items,new_items):assert raw[before.start:before.end]==updated[after.start:after.end]
    assert value(new_items[-1],'Slot')==free
    assert value(new_items[-1],'id')==b'building_wand:delete_wand'
    assert new_items[-1].value['components'].value['minecraft:damage'].value==511
    return updated,free

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    parser=argparse.ArgumentParser();parser.add_argument('--world',required=True);parser.add_argument('--inspect',action='store_true');parser.add_argument('--write',action='store_true');args=parser.parse_args()
    world=Path(args.world).resolve(strict=True)
    if world.name!='新的世界 (10)':raise ValueError('This task is authorized only for 新的世界 (10)')
    files=list((world/'players/data').glob('*.dat'))
    if len(files)!=1:raise ValueError('Player identity is ambiguous')
    player=files[0];packed=player.read_bytes();raw=gzip.decompress(packed);root=Reader(raw).root();items=inventory(root).value[1]
    old_hash=hashlib.sha256(packed).hexdigest()
    if args.inspect:
        print(json.dumps({'玩家文件':str(player),'背包': [{'槽位':value(n,'Slot'),'物品':value(n,'id',b'').decode('utf-8'),'数量':value(n,'count',value(n,'Count',0))} for n in items]},ensure_ascii=False,indent=2));return
    if not args.write:raise ValueError('Choose --inspect or --write')
    wand_id=str(uuid.uuid4());updated,slot=insert(raw,wand_id)
    if hashlib.sha256(player.read_bytes()).hexdigest()!=old_hash:raise ValueError('Player data changed during editing')
    temporary=player.with_name(player.name+'.codex-tmp')
    if temporary.exists():raise ValueError('Staging file already exists')
    try:
        with temporary.open('xb') as output:
            output.write(gzip.compress(updated,mtime=0));output.flush();os.fsync(output.fileno())
        assert gzip.decompress(temporary.read_bytes())==updated
        os.replace(temporary,player)
    finally:
        if temporary.exists():temporary.unlink()
    checked=Reader(gzip.decompress(player.read_bytes())).root();tool=inventory(checked).value[1][-1]
    assert value(tool,'Slot')==slot
    assert tool.value['components'].value['minecraft:custom_data'].value['QuickBuildDeleteRemainder'].value==0
    record={'存档':str(world),'玩家文件':str(player),'槽位':slot,'界面槽位序号':slot+1,'物品':'building_wand:delete_wand','剩余耐久':1,'损伤值':511,'累计余数':0,'法杖标识':wand_id,'存档备份':False,'原背包物品':len(items),'现背包物品':len(items)+1,'旧文件SHA256':old_hash,'新文件SHA256':hashlib.sha256(player.read_bytes()).hexdigest()}
    output=Path(__file__).resolve().parents[1]/'outputs/20261003_删除法杖26.2测试版2'
    output.mkdir(parents=True,exist_ok=True);(output/'测试杖放入记录.json').write_text(json.dumps(record,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(record,ensure_ascii=False,indent=2))

if __name__=='__main__':main()
