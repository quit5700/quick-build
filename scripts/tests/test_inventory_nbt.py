import importlib.util, pathlib, struct, unittest
path=pathlib.Path(__file__).resolve().parents[1]/'put_delete_wand_in_inventory.py'
spec=importlib.util.spec_from_file_location('inventory_edit',path);edit=importlib.util.module_from_spec(spec);spec.loader.exec_module(edit)

class InventoryNBTTest(unittest.TestCase):
    def fixture(self,slots):
        items=[edit.compound([edit.named(1,'Slot',struct.pack('>b',n)),edit.string_tag('id','minecraft:stone'),edit.int_tag('count',64)]) for n in slots]
        payload=b'\x0a'+struct.pack('>i',len(items))+b''.join(items)
        return b'\x0a\0\0'+edit.compound([edit.int_tag('UnrelatedData',123),edit.named(9,'Inventory',payload),edit.string_tag('CustomName','中文测试')])
    def test_adds_to_empty_slot_and_preserves_all_existing_tag_bytes(self):
        raw=self.fixture([0,2,8]);updated,slot=edit.insert(raw,'dfe29e19-6b20-4bc1-987c-d54e393e819a')
        self.assertEqual(slot,1);root=edit.Reader(updated).root();self.assertEqual(edit.value(root,'UnrelatedData'),123)
        self.assertEqual(edit.value(root,'CustomName'),'中文测试'.encode('utf-8'))
        tool=edit.inventory(root).value[1][-1];components=tool.value['components'];self.assertEqual(edit.value(components,'minecraft:damage'),511)
        self.assertEqual(edit.value(components.value['minecraft:custom_data'],'QuickBuildDeleteRemainder'),0)
    def test_full_inventory_refuses_to_overwrite_items(self):
        with self.assertRaises(ValueError):edit.insert(self.fixture(range(36)),'dfe29e19-6b20-4bc1-987c-d54e393e819a')
    def test_truncated_and_wrong_type_nbt_are_rejected(self):
        raw=self.fixture([0])
        with self.assertRaises(ValueError):edit.Reader(raw[:-1]).root()
        with self.assertRaises(ValueError):edit.Reader(b'\x01\0\0').root()
if __name__=='__main__':unittest.main()
