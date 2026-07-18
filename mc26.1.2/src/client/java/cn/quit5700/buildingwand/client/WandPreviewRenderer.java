package cn.quit5700.buildingwand.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.lang.reflect.Method;

public final class WandPreviewRenderer {
    private WandPreviewRenderer() {
    }

    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(WandPreviewRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        if (WandPreviewState.positions().isEmpty() || context.poseStack() == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        Vec3 camera = cameraPosition(client);
        PoseStack matrices = context.poseStack();
        WandPreviewState.PreviewColor color = WandPreviewState.color();

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        context.submitNodeCollector().submitCustomGeometry(matrices, RenderTypes.debugQuads(), (pose, buffer) -> {
            Matrix4f matrix = pose.pose();
            for (BlockPos pos : WandPreviewState.positions()) {
                box(buffer, matrix, pos, color);
            }
        });
        matrices.popPose();
    }

    private static void box(VertexConsumer buffer, Matrix4f matrix, BlockPos pos, WandPreviewState.PreviewColor color) {
        float x1 = pos.getX();
        float y1 = pos.getY();
        float z1 = pos.getZ();
        float x2 = x1 + 1.0F;
        float y2 = y1 + 1.0F;
        float z2 = z1 + 1.0F;
        quad(buffer, matrix, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, color);
        quad(buffer, matrix, x2, y1, z2, x1, y1, z2, x1, y2, z2, x2, y2, z2, color);
        quad(buffer, matrix, x1, y1, z2, x1, y1, z1, x1, y2, z1, x1, y2, z2, color);
        quad(buffer, matrix, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, color);
        quad(buffer, matrix, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, color);
        quad(buffer, matrix, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, color);
    }

    private static void quad(
            VertexConsumer buffer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            WandPreviewState.PreviewColor color
    ) {
        buffer.addVertex(matrix, x1, y1, z1).setColor(color.red, color.green, color.blue, color.alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(color.red, color.green, color.blue, color.alpha);
        buffer.addVertex(matrix, x3, y3, z3).setColor(color.red, color.green, color.blue, color.alpha);
        buffer.addVertex(matrix, x4, y4, z4).setColor(color.red, color.green, color.blue, color.alpha);
    }

    private static Vec3 cameraPosition(Minecraft client) {
        try {
            Method method = client.gameRenderer.getClass().getMethod("mainCamera");
            return ((Camera) method.invoke(client.gameRenderer)).position();
        } catch (ReflectiveOperationException ignored) {
            try {
                Method method = client.gameRenderer.getClass().getMethod("getMainCamera");
                return ((Camera) method.invoke(client.gameRenderer)).position();
            } catch (ReflectiveOperationException ignoredAgain) {
                return client.getCameraEntity() == null ? Vec3.ZERO : client.getCameraEntity().position();
            }
        }
    }
}
