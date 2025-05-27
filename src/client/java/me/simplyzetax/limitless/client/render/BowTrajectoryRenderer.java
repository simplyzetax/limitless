package me.simplyzetax.limitless.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class BowTrajectoryRenderer implements WorldRenderEvents.Last {

    @Override
    public void onLast(WorldRenderContext context) {
        if (!BowTrajectoryData.shouldRenderTrajectory ||
                BowTrajectoryData.trajectoryPoints.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;

        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        // Get a VertexConsumerProvider.Immediate instance
        VertexConsumerProvider.Immediate vertexConsumers = client
                .getBufferBuilders()
                .getEntityVertexConsumers();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        // Draw White Trajectory Line
        if (BowTrajectoryData.trajectoryPoints.size() > 1) {
            VertexConsumer lineConsumer = vertexConsumers.getBuffer(
                    RenderLayer.getLines());

            for (int i = 0; i < BowTrajectoryData.trajectoryPoints.size() - 1; i++) {
                Vec3d p1 = BowTrajectoryData.trajectoryPoints.get(i);
                Vec3d p2 = BowTrajectoryData.trajectoryPoints.get(i + 1);

                lineConsumer
                        .vertex(positionMatrix, (float) p1.x, (float) p1.y, (float) p1.z)
                        .color(1.0f, 1.0f, 1.0f, 1.0f) // White
                        .normal(matrices.peek(), 0f, 1f, 0f);

                lineConsumer
                        .vertex(positionMatrix, (float) p2.x, (float) p2.y, (float) p2.z)
                        .color(1.0f, 1.0f, 1.0f, 1.0f) // White
                        .normal(matrices.peek(), 0f, 1f, 0f);
            }
        }

        // Draw Red Square (Box) at Impact Point
        if (BowTrajectoryData.impactPoint != null) {
            VertexConsumer boxConsumer = vertexConsumers.getBuffer(
                    RenderLayer.getLines());
            Vec3d hit = BowTrajectoryData.impactPoint;
            float boxSize = 0.25f;

            // Draw a wireframe box manually
            drawWireframeBox(matrices, boxConsumer, hit, boxSize);
        }

        matrices.pop();
        vertexConsumers.draw();
    }

    private void drawWireframeBox(
            MatrixStack matrices,
            VertexConsumer consumer,
            Vec3d center,
            float size) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        float halfSize = size / 2.0f;

        float minX = (float) (center.x - halfSize);
        float minY = (float) (center.y - halfSize);
        float minZ = (float) (center.z - halfSize);
        float maxX = (float) (center.x + halfSize);
        float maxY = (float) (center.y + halfSize);
        float maxZ = (float) (center.z + halfSize);

        // Bottom face
        drawLine(consumer, positionMatrix, matrices, minX, minY, minZ, maxX, minY, minZ);
        drawLine(consumer, positionMatrix, matrices, maxX, minY, minZ, maxX, minY, maxZ);
        drawLine(consumer, positionMatrix, matrices, maxX, minY, maxZ, minX, minY, maxZ);
        drawLine(consumer, positionMatrix, matrices, minX, minY, maxZ, minX, minY, minZ);

        // Top face
        drawLine(consumer, positionMatrix, matrices, minX, maxY, minZ, maxX, maxY, minZ);
        drawLine(consumer, positionMatrix, matrices, maxX, maxY, minZ, maxX, maxY, maxZ);
        drawLine(consumer, positionMatrix, matrices, maxX, maxY, maxZ, minX, maxY, maxZ);
        drawLine(consumer, positionMatrix, matrices, minX, maxY, maxZ, minX, maxY, minZ);

        // Vertical edges
        drawLine(consumer, positionMatrix, matrices, minX, minY, minZ, minX, maxY, minZ);
        drawLine(consumer, positionMatrix, matrices, maxX, minY, minZ, maxX, maxY, minZ);
        drawLine(consumer, positionMatrix, matrices, maxX, minY, maxZ, maxX, maxY, maxZ);
        drawLine(consumer, positionMatrix, matrices, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private void drawLine(
            VertexConsumer consumer,
            Matrix4f positionMatrix,
            MatrixStack matrices,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2) {
        consumer
                .vertex(positionMatrix, x1, y1, z1)
                .color(1.0f, 0.0f, 0.0f, 1.0f) // Red
                .normal(matrices.peek(), 0f, 1f, 0f);

        consumer
                .vertex(positionMatrix, x2, y2, z2)
                .color(1.0f, 0.0f, 0.0f, 1.0f) // Red
                .normal(matrices.peek(), 0f, 1f, 0f);
    }

    public static void register() {
        WorldRenderEvents.LAST.register(new BowTrajectoryRenderer());
    }
}
