from pathlib import Path
v=Path('versions/1.1.4-test10/mc26.2');root=v/'src/main/java/cn/quit5700/buildingwand'
p=root/'logic/StructureLayout.java';s=p.read_text(encoding='utf-8');s=s.replace(' private static int min(',''' public static java.math.BigInteger total(DeleteBox b,boolean vertical,int mode){
  int axis=axis(b,vertical,mode),a=(axis+1)%3,c=(axis+2)%3;long len=length(b,axis),pairs=len/2;
  java.math.BigInteger full=java.math.BigInteger.valueOf(pairs).multiply(java.math.BigInteger.TWO).multiply(java.math.BigInteger.valueOf(length(b,a))).multiply(java.math.BigInteger.valueOf(length(b,c)));
  if(mode!=2)return full;
  long interiorPairs=Math.max(0,pairs-(len%2==0?2:1));
  return full.subtract(java.math.BigInteger.valueOf(interiorPairs).multiply(java.math.BigInteger.TWO).multiply(java.math.BigInteger.valueOf(Math.max(0,length(b,a)-2))).multiply(java.math.BigInteger.valueOf(Math.max(0,length(b,c)-2))));
 }
 private static int min(''');p.write_text(s,encoding='utf-8')
p=root/'server/StructurePlacement.java';s=p.read_text(encoding='utf-8');s=s.replace(' public static boolean fits(', ' public static java.math.BigInteger total(Block b,DeleteBox box,int mode){return multiple(b)?StructureLayout.total(box,vertical(b),mode):BuildPlan.total(box,mode);}\n public static boolean fits(');p.write_text(s,encoding='utf-8')
p=root/'server/BuildTaskState.java';s=p.read_text(encoding='utf-8').replace('BuildPlan.total(j.box,j.mode)','StructurePlacement.total(j.block,j.box,j.mode)');p.write_text(s,encoding='utf-8')
p=v/'src/test/java/cn/quit5700/buildingwand/logic/StructureLayoutTest.java';s=p.read_text(encoding='utf-8');extra='''
 @Test void hollowTotalCountsActualThicknessAndExcludesAir(){var b=new DeleteBox(0,0,0,5,4,4);assertEquals(java.math.BigInteger.valueOf(132),StructureLayout.total(b,false,2));var odd=new DeleteBox(0,0,0,48,0,48);assertEquals(java.math.BigInteger.valueOf(2352),StructureLayout.total(odd,false,2));}
 @Test void totalMatchesEnumeratedPairsInEveryDirection(){for(int x=1;x<=7;x++)for(int y=1;y<=5;y++)for(int z=1;z<=7;z++)for(boolean vertical:new boolean[]{false,true})for(int mode:new int[]{1,2,3})for(int direction:new int[]{-1,1}){var b=new DeleteBox(0,0,0,x-1,y-1,z-1);int axis=StructureLayout.axis(b,vertical,mode),count=0;for(int xx=0;xx<x;xx++)for(int yy=0;yy<y;yy++)for(int zz=0;zz<z;zz++){var p=StructureLayout.pair(b,xx,yy,zz,axis,mode,direction);if(p.anchor()&&p.included()&&!p.remainder())count+=2;}assertEquals(java.math.BigInteger.valueOf(count),StructureLayout.total(b,vertical,mode),b+" mode="+mode+" vertical="+vertical+" direction="+direction);}}
''';s=s[:s.rfind('}')]+extra+'\n}\n';p.write_text(s,encoding='utf-8')
# Save the quantity semantics in world assertions as well.
p=v/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';s=p.read_text(encoding='utf-8');s=s.replace('"deep interior overwritten");','''"deep interior overwritten");var report=((net.minecraft.network.chat.contents.TranslatableContents)p.lastMessage.getContents()).getArgs();check(report[0].equals("132"),"hollow progress does not count actual structure thickness");''');p.write_text(s,encoding='utf-8')
print('进度总量改为实际多格占用数，增加尺寸和方向枚举回归')