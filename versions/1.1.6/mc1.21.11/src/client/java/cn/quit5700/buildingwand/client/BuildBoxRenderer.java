package cn.quit5700.buildingwand.client;
import cn.quit5700.buildingwand.logic.DeleteBox;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BuildBoxRenderer {
    public static void register(){WorldRenderEvents.BEFORE_TRANSLUCENT.register(context->{
        DeleteBox box=BuildWandClient.box();Minecraft client=Minecraft.getInstance();
        if(box==null || client.player==null || !client.player.getMainHandItem().is(cn.quit5700.buildingwand.registry.ModItems.BUILDING_WAND) || context.matrices()==null)return;
        Vec3 camera=context.worldState().cameraRenderState.pos;
        float x1=(float)(box.minX()-camera.x),y1=(float)(box.minY()-camera.y),z1=(float)(box.minZ()-camera.z);
        float x2=(float)((long)box.maxX()+1-camera.x),y2=(float)((long)box.maxY()+1-camera.y),z2=(float)((long)box.maxZ()+1-camera.z);
        context.commandQueue().submitCustomGeometry(context.matrices(),RenderTypes.debugQuads(),(pose,buffer)->{
            Matrix4f matrix=pose.pose();float t=.025F;
            for(float y:new float[]{y1,y2})for(float z:new float[]{z1,z2})beam(buffer,matrix,x1,y-t,z-t,x2,y+t,z+t);
            for(float x:new float[]{x1,x2})for(float z:new float[]{z1,z2})beam(buffer,matrix,x-t,y1,z-t,x+t,y2,z+t);
            for(float x:new float[]{x1,x2})for(float y:new float[]{y1,y2})beam(buffer,matrix,x-t,y-t,z1,x+t,y+t,z2);
        });
    });}
    private static void beam(VertexConsumer b,Matrix4f m,float x1,float y1,float z1,float x2,float y2,float z2){
        quad(b,m,x1,y1,z1,x2,y1,z1,x2,y2,z1,x1,y2,z1);
        quad(b,m,x2,y1,z2,x1,y1,z2,x1,y2,z2,x2,y2,z2);
        quad(b,m,x1,y1,z2,x1,y1,z1,x1,y2,z1,x1,y2,z2);
        quad(b,m,x2,y1,z1,x2,y1,z2,x2,y2,z2,x2,y2,z1);
        quad(b,m,x1,y2,z1,x2,y2,z1,x2,y2,z2,x1,y2,z2);
        quad(b,m,x1,y1,z2,x2,y1,z2,x2,y1,z1,x1,y1,z1);
    }
    private static void quad(VertexConsumer b,Matrix4f m,float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3,float x4,float y4,float z4){
        b.addVertex(m,x1,y1,z1).setColor(255,255,255,210);b.addVertex(m,x2,y2,z2).setColor(255,255,255,210);
        b.addVertex(m,x3,y3,z3).setColor(255,255,255,210);b.addVertex(m,x4,y4,z4).setColor(255,255,255,210);
    }
}
