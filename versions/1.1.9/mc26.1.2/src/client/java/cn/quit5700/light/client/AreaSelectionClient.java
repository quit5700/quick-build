package cn.quit5700.light.client;

import cn.quit5700.light.network.AreaSelectionNetworking;
import cn.quit5700.light.logic.LightBox;
import cn.quit5700.light.logic.AreaSelectionRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public final class AreaSelectionClient {
    private static boolean selecting,waiting,pinned;
    private static BlockPos first;
    private static LightBox preview;
    private static int timeout;
    private static boolean wasAttack,wasUse,dragging,confirmWaiting;
    private static final cn.quit5700.light.logic.WandAreaGesture<BlockPos> GESTURE=new cn.quit5700.light.logic.WandAreaGesture<>();
    private static final java.util.concurrent.atomic.AtomicReference<AreaSelectionNetworking.Preview> PENDING=new java.util.concurrent.atomic.AtomicReference<>();
    private AreaSelectionClient() { }
    public static boolean isSelecting(){return selecting;}
    public static void cancelForDeletionWand(){
        if(!selecting)return;
        selecting=false;waiting=false;confirmWaiting=false;dragging=false;first=null;preview=null;timeout=0;
        GESTURE.clear();cn.quit5700.buildingwand.client.WandPreviewState.clear();
        ClientPlayNetworking.send(new AreaSelectionNetworking.Pick(BlockPos.ZERO,true));
    }
    public static boolean rangeShown(){return pinned;}
    public static void hideRange(){pinned=false;preview=null;}
    public static void requestRange(){pinned=true;}
    private static BlockPos target(Minecraft client) {
        if(client.player==null)return null;
        return cn.quit5700.buildingwand.client.ClientWandController.buildingSelectionTarget(client);
    }
    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(AreaSelectionNetworking.Preview.ID,(payload,context)->PENDING.set(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->PENDING.set(new AreaSelectionNetworking.Preview(false,null,null)));
        ClientPreAttackCallback.EVENT.register((client,player,clicks)->selecting);
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player,world,hand,hit)->
                world.isClientSide() && selecting ? net.minecraft.world.InteractionResult.FAIL:net.minecraft.world.InteractionResult.PASS);
        net.fabricmc.fabric.api.event.player.UseItemCallback.EVENT.register((player,world,hand)->
                world.isClientSide() && selecting ? net.minecraft.world.InteractionResult.FAIL:net.minecraft.world.InteractionResult.PASS);
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            var payload=PENDING.getAndSet(null);
            if(payload!=null){
                boolean beginning=payload.selecting() && !selecting;
                if(beginning || !payload.selecting()) {GESTURE.clear();dragging=false;}
                else if(confirmWaiting)GESTURE.reject();
                confirmWaiting=false;
                selecting=payload.selecting();pinned=AreaSelectionRules.pinPreview(selecting,payload.pinned(),payload.first()!=null && payload.second()!=null);waiting=false;timeout=selecting?12000:0;
                first=selecting && payload.second()==null?payload.first():null;
                preview=payload.first()!=null && payload.second()!=null ? box(payload.first(),payload.second()) : first==null?null:box(first,first);
            }
            if(client.level==null){selecting=false;pinned=false;preview=null;GESTURE.clear();cn.quit5700.buildingwand.client.WandPreviewState.clear();return;}
            boolean attack=client.options.keyAttack.isDown(),use=client.options.keyUse.isDown();
            boolean pressed=attack && !wasAttack,released=!attack && wasAttack;
            boolean cancel=use && !wasUse && client.options.keyShift.isDown();
            wasAttack=attack;wasUse=use;
            if(timeout>0)timeout--;
            if(selecting && (cancel || org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().handle(),org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE)==org.lwjgl.glfw.GLFW.GLFW_PRESS || timeout==0)) {
                selecting=false;waiting=false;first=null;preview=null;
                GESTURE.clear();cn.quit5700.buildingwand.client.WandPreviewState.clear();
                ClientPlayNetworking.send(new AreaSelectionNetworking.Pick(BlockPos.ZERO,true));
            }
            if(!AreaSelectionRules.showPreview(selecting,pinned))preview=null;
            BlockPos point=target(client);
            if(selecting && point!=null) {
                cn.quit5700.buildingwand.client.WandPreviewState.set(java.util.List.of(point),cn.quit5700.buildingwand.client.WandPreviewState.PreviewColor.WHITE);
                if(pressed && !waiting) {
                    var action=GESTURE.press(point);dragging=action!=cn.quit5700.light.logic.WandAreaGesture.Action.CONFIRM;
                    if(action!=cn.quit5700.light.logic.WandAreaGesture.Action.NONE) {
                        waiting=true;
                        confirmWaiting=action==cn.quit5700.light.logic.WandAreaGesture.Action.CONFIRM;
                        ClientPlayNetworking.send(new AreaSelectionNetworking.Pick(action==cn.quit5700.light.logic.WandAreaGesture.Action.FIRST?point:GESTURE.pending(),false));
                    }
                }
                if(released && dragging){GESTURE.release(point);dragging=false;}
                BlockPos start=GESTURE.first();
                if(start!=null) {
                    BlockPos end=GESTURE.pending()!=null?GESTURE.pending():point;
                    try {preview=box(start,end);}catch(IllegalArgumentException ignored){preview=box(start,start);}
                }
            } else if(payload!=null) {
                cn.quit5700.buildingwand.client.WandPreviewState.clear();
            }
        });
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(context->{
            LightBox box=preview;
            var client=Minecraft.getInstance();
            if(box==null || context.poseStack()==null)return;
            Vec3 camera=camera(client);
            var pose=context.poseStack();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);
            context.submitNodeCollector().submitCustomGeometry(pose,RenderTypes.debugQuads(),(p,buffer)->{
                if(box==null)return;
                float a=box.minX(),b=box.minY(),c=box.minZ(),d=box.maxX()+1,e=box.maxY()+1,f=box.maxZ()+1,t=.025f;
                for(float y:new float[]{b,e})for(float z:new float[]{c,f})cuboid(buffer,p.pose(),a,y-t,z-t,d,y+t,z+t,0xDC1EF0FF);
                for(float x:new float[]{a,d})for(float z:new float[]{c,f})cuboid(buffer,p.pose(),x-t,b,z-t,x+t,e,z+t,0xDC1EF0FF);
                for(float x:new float[]{a,d})for(float y:new float[]{b,e})cuboid(buffer,p.pose(),x-t,y-t,c,x+t,y+t,f,0xDC1EF0FF);
            });pose.popPose();
        });
    }
    private static LightBox box(BlockPos a,BlockPos b){return LightBox.of(a.getX(),a.getY(),a.getZ(),b.getX(),b.getY(),b.getZ());}
    private static Vec3 camera(Minecraft client){
        try{return ((Camera)client.gameRenderer.getClass().getMethod("mainCamera").invoke(client.gameRenderer)).position();}
        catch(ReflectiveOperationException ignored){
            try{return ((Camera)client.gameRenderer.getClass().getMethod("getMainCamera").invoke(client.gameRenderer)).position();}
            catch(ReflectiveOperationException fallback){return client.getCameraEntity()==null?Vec3.ZERO:client.getCameraEntity().position();}
        }
    }
    private static void cuboid(VertexConsumer v,Matrix4f m,float a,float b,float c,float d,float e,float f,int color){
        quad(v,m,color,a,b,c,d,b,c,d,e,c,a,e,c);quad(v,m,color,d,b,f,a,b,f,a,e,f,d,e,f);
        quad(v,m,color,a,b,f,a,b,c,a,e,c,a,e,f);quad(v,m,color,d,b,c,d,b,f,d,e,f,d,e,c);
        quad(v,m,color,a,e,c,d,e,c,d,e,f,a,e,f);quad(v,m,color,a,b,f,d,b,f,d,b,c,a,b,c);
    }
    private static void quad(VertexConsumer v,Matrix4f m,int color,float... p){for(int i=0;i<12;i+=3)v.addVertex(m,p[i],p[i+1],p[i+2]).setColor(color);}
}
