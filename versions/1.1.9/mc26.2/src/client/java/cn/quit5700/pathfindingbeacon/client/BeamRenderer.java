package cn.quit5700.pathfindingbeacon.client;

import cn.quit5700.pathfindingbeacon.RouteColors;
import cn.quit5700.pathfindingbeacon.route.BeamColumnResolver;
import cn.quit5700.pathfindingbeacon.route.Column;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.lang.reflect.Method;

public final class BeamRenderer {
    private static final float HALF_WIDTH = 0.16F;
    private static final int ALPHA = 190;

    private BeamRenderer() {
    }

    public static void register() {
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(BeamRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || context.poseStack() == null) {
            return;
        }
        BeamColumnResolver resolver = ClientRouteState.resolver();
        if (resolver.columns().isEmpty()) {
            return;
        }

        Vec3 camera = cameraPosition(client);
        int renderDistance = client.options.getEffectiveRenderDistance() * 16 + 16;
        double maxDistanceSquared = (double) renderDistance * renderDistance;
        long second = System.currentTimeMillis() / 1000L;
        float bottom = client.level.dimensionType().minY();
        float top = bottom + client.level.dimensionType().height();

        PoseStack matrices = context.poseStack();
        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        context.submitNodeCollector().submitCustomGeometry(matrices, RenderTypes.debugQuads(), (pose, buffer) -> {
            Matrix4f matrix = pose.pose();
            for (Column column : resolver.columns()) {
                double dx = column.x() + 0.5D - camera.x;
                double dz = column.z() + 0.5D - camera.z;
                if (dx * dx + dz * dz > maxDistanceSquared) {
                    continue;
                }
                int number = resolver.colorAt(column, second);
                if (number == 0) {
                    continue;
                }
                int rgb = RouteColors.rgb(number);
                int red = rgb >> 16 & 0xFF;
                int green = rgb >> 8 & 0xFF;
                int blue = rgb & 0xFF;
                addBeam(buffer, matrix, column.x() + 0.5F, bottom, top, column.z() + 0.5F, red, green, blue);
            }
        });
        matrices.popPose();
    }

    private static void addBeam(
            VertexConsumer buffer,
            Matrix4f matrix,
            float x,
            float bottom,
            float top,
            float z,
            int red,
            int green,
            int blue
    ) {
        float minX = x - HALF_WIDTH;
        float maxX = x + HALF_WIDTH;
        float minZ = z - HALF_WIDTH;
        float maxZ = z + HALF_WIDTH;

        quad(buffer, matrix, minX, bottom, minZ, minX, top, minZ, maxX, top, minZ, maxX, bottom, minZ, red, green, blue);
        quad(buffer, matrix, maxX, bottom, maxZ, maxX, top, maxZ, minX, top, maxZ, minX, bottom, maxZ, red, green, blue);
        quad(buffer, matrix, minX, bottom, maxZ, minX, top, maxZ, minX, top, minZ, minX, bottom, minZ, red, green, blue);
        quad(buffer, matrix, maxX, bottom, minZ, maxX, top, minZ, maxX, top, maxZ, maxX, bottom, maxZ, red, green, blue);
    }

    private static void quad(
            VertexConsumer buffer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            int red, int green, int blue
    ) {
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, ALPHA);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, ALPHA);
        buffer.addVertex(matrix, x3, y3, z3).setColor(red, green, blue, ALPHA);
        buffer.addVertex(matrix, x4, y4, z4).setColor(red, green, blue, ALPHA);
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
