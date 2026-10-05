package cn.quit5700.light.light;
import cn.quit5700.light.logic.ConnectedLightVolume;
import net.minecraft.core.BlockPos;
import java.lang.reflect.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DaylightReentrantSmokeChecks {
 public static void register() {
  ServerLifecycleEvents.SERVER_STARTED.register(server -> {
   int passed=0;
   var test=new DaylightReentrantSmokeChecks();
   for(int i=0;i<4;i++)try{
    switch(i){case 0->test.invalidationInsidePassageDoesNotCrashOrPublishStaleCompletion();case 1->test.replacementDuringPassageCannotPublishTheOldResult();case 2->test.normalCompletionIsPublishedOnlyOnce();case 3->test.partialCalculationRetainsItsBudgetAndVolume();}
    passed++;System.out.println("Q5_REENTRANT_CASE_PASS: "+i);
   }catch(Throwable failure){System.out.println("Q5_REENTRANT_CASE_FAIL: "+i);failure.printStackTrace();}
   System.out.println("Q5_REENTRANT_RESULT: "+passed+"/4");server.halt(false);
  });
 }
 static void assertEquals(long a,long b){if(a!=b)throw new AssertionError(a+" != "+b);}
 static void assertNull(Object v){if(v!=null)throw new AssertionError("Expected null");}
 static void assertNotNull(Object v){if(v==null)throw new AssertionError("Expected value");}
 static void assertSame(Object a,Object b){if(a!=b)throw new AssertionError("Different references");}
 static void assertTrue(boolean v){if(!v)throw new AssertionError("Expected true");}
 static void assertFalse(boolean v){if(v)throw new AssertionError("Expected false");}

 static class Fixture {
  final Class<?> wc,sc; final Object world,source; final Field volume,revision; final Method advance;
  Fixture() throws Exception {
   wc=Class.forName("cn.quit5700.light.light.DaylightOcclusion$WorldCache");
   sc=Class.forName("cn.quit5700.light.light.DaylightOcclusion$SourceCache");
   var w=wc.getDeclaredConstructor();w.setAccessible(true);world=w.newInstance();
   var s=sc.getDeclaredConstructor(BlockPos.class,int.class);s.setAccessible(true);source=s.newInstance(BlockPos.ZERO,1);
   volume=sc.getDeclaredField("volume");volume.setAccessible(true);
   revision=wc.getDeclaredField("revision");revision.setAccessible(true);
   advance=DaylightOcclusion.class.getDeclaredMethod("advanceSource",wc,sc,int.class,ConnectedLightVolume.Passage.class);advance.setAccessible(true);
  }
  int run(int budget,ConnectedLightVolume.Passage passage) throws Exception {
   try{return (Integer)advance.invoke(null,world,source,budget,passage);}catch(InvocationTargetException e){throw (Exception)e.getCause();}
  }
 }
 void invalidationInsidePassageDoesNotCrashOrPublishStaleCompletion() throws Exception {
  var f=new Fixture();boolean[] first={true};
  assertEquals(1,f.run(8192,(x,y,z,nx,ny,nz)->{if(first[0]){first[0]=false;try{f.volume.set(f.source,null);f.revision.setLong(f.world,1);}catch(Exception e){throw new RuntimeException(e);}}return false;}));
  assertNull(f.volume.get(f.source));assertEquals(1,f.revision.getLong(f.world));
  assertEquals(1,f.run(8192,(x,y,z,nx,ny,nz)->false));
  assertNotNull(f.volume.get(f.source));assertEquals(2,f.revision.getLong(f.world));
 }
 void replacementDuringPassageCannotPublishTheOldResult() throws Exception {
  var f=new Fixture();var replacement=new ConnectedLightVolume(1);
  f.run(8192,(x,y,z,nx,ny,nz)->{try{f.volume.set(f.source,replacement);}catch(Exception e){throw new RuntimeException(e);}return false;});
  assertSame(replacement,f.volume.get(f.source));assertEquals(0,f.revision.getLong(f.world));
 }
 void normalCompletionIsPublishedOnlyOnce() throws Exception {
  var f=new Fixture();assertEquals(1,f.run(8192,(x,y,z,nx,ny,nz)->false));assertEquals(1,f.revision.getLong(f.world));
  assertEquals(0,f.run(8192,(x,y,z,nx,ny,nz)->false));assertEquals(1,f.revision.getLong(f.world));
 }
 void partialCalculationRetainsItsBudgetAndVolume() throws Exception {
  var f=new Fixture();assertEquals(1,f.run(1,(x,y,z,nx,ny,nz)->true));var v=f.volume.get(f.source);
  assertFalse(((ConnectedLightVolume)v).complete());assertEquals(0,f.revision.getLong(f.world));
  assertTrue(f.run(8192,(x,y,z,nx,ny,nz)->true)>0);assertSame(v,f.volume.get(f.source));assertEquals(1,f.revision.getLong(f.world));
 }
}
