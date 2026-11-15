package cn.quit5700.buildingwand.aux;
import org.junit.jupiter.api.Test;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import java.lang.reflect.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AuxRendererDepthTest {
 @Test void actualOverlayEdgesStayInFrontOfFillOnAllSixFaces()throws Exception{
  var face=AuxRenderer.class.getDeclaredMethod("face",VertexConsumer.class,Matrix4f.class,BlockPos.class,Vec3.class,Direction.class,int.class,int.class,int.class);face.setAccessible(true);
  for(var direction:Direction.values())for(var camera:List.of(new Vec3(0,0,0),new Vec3(150.125,190.25,220.375))){
   var vertices=new ArrayList<Vec3>();var consumer=(VertexConsumer)Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),new Class<?>[]{VertexConsumer.class},(proxy,method,args)->{if(method.getName().equals("addVertex")&&args.length==4&&args[0] instanceof Matrix4f)vertices.add(new Vec3(((Number)args[1]).doubleValue(),((Number)args[2]).doubleValue(),((Number)args[3]).doubleValue()));return proxy;});
   face.invoke(null,consumer,new Matrix4f(),new BlockPos(100,150,200),camera,direction,0xffa000,62,15);assertEquals(20,vertices.size());
   double fill=depth(vertices.getFirst(),direction);double modelSurface=depth(Vec3.atCenterOf(new BlockPos(100,150,200)).subtract(camera),direction)+.5;assertTrue(fill-modelSurface>.001,"Overlay conflicts with material model on "+direction);for(int i=4;i<20;i++)assertTrue(depth(vertices.get(i),direction)-fill>.001,"Coplanar fill/edge depth conflict on "+direction);
  }
 }
 private static double depth(Vec3 p,Direction d){return p.x*d.getStepX()+p.y*d.getStepY()+p.z*d.getStepZ();}
}
