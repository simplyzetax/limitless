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

        VertexConsumerProvider.Immediate vertexConsumers = client
                .getBufferBuilders()
                .getEntityVertexConsumers();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        // Draw trajectory line (white)
        if (BowTrajectoryData.trajectoryPoints.size() > 1) {
            VertexConsumer lineConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

            for (int i = 0; i < BowTrajectoryData.trajectoryPoints.size() - 1; i++) {
                Vec3d p1 = BowTrajectoryData.trajectoryPoints.get(i);
                Vec3d p2 = BowTrajectoryData.trajectoryPoints.get(i + 1);

                // Add line segment
                lineConsumer.vertex(positionMatrix, (float) p1.x, (float) p1.y, (float) p1.z)
                        .color(255, 255, 255, 255)
                        .normal(matrices.peek(), 0f, 1f, 0f);

                lineConsumer.vertex(positionMatrix, (float) p2.x, (float) p2.y, (float) p2.z)
                        .color(255, 255, 255, 255)
                        .normal(matrices.peek(), 0f, 1f, 0f);
            }
        }

        // Draw impact point box (red)
        if (BowTrajectoryData.impactPoint != null) {
            VertexConsumer boxConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
            drawImpactBox(matrices, boxConsumer, BowTrajectoryData.impactPoint, 0.25f);
        }

        matrices.pop();
        vertexConsumers.draw();
    }

    private void drawImpactBox(MatrixStack matrices, VertexConsumer consumer, Vec3d center, float size) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float half = size / 2.0f;

        float minX = (float) (center.x - half);
        float minY = (float) (center.y - half);
        float minZ = (float) (center.z - half);
        float maxX = (float) (center.x + half);
        float maxY = (float) (center.y + half);
        float maxZ = (float) (center.z + half);

        // Draw 12 edges of the cube
        // Bottom face (4 edges)
        addLine(consumer, matrix, matrices, minX, minY, minZ, maxX, minY, minZ);
        addLine(consumer, matrix, matrices, maxX, minY, minZ, maxX, minY, maxZ);
        addLine(consumer, matrix, matrices, maxX, minY, maxZ, minX, minY, maxZ);
        addLine(consumer, matrix, matrices, minX, minY, maxZ, minX, minY, minZ);

        // Top face (4 edges)
        addLine(consumer, matrix, matrices, minX, maxY, minZ, maxX, maxY, minZ);
        addLine(consumer, matrix, matrices, maxX, maxY, minZ, maxX, maxY, maxZ);
        addLine(consumer, matrix, matrices, maxX, maxY, maxZ, minX, maxY, maxZ);
        addLine(consumer, matrix, matrices, minX, maxY, maxZ, minX, maxY, minZ);

        // Vertical edges (4 edges)
        addLine(consumer, matrix, matrices, minX, minY, minZ, minX, maxY, minZ);
        addLine(consumer, matrix, matrices, maxX, minY, minZ, maxX, maxY, minZ);
        addLine(consumer, matrix, matrices, maxX, minY, maxZ, maxX, maxY, maxZ);
        addLine(consumer, matrix, matrices, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private void addLine(VertexConsumer consumer, Matrix4f matrix, MatrixStack matrices,
            float x1, float y1, float z1, float x2, float y2, float z2) {
        consumer.vertex(matrix, x1, y1, z1)
                .color(255, 0, 0, 255) // Red
                .normal(matrices.peek(), 0f, 1f, 0f);

        consumer.vertex(matrix, x2, y2, z2)
                .color(255, 0, 0, 255) // Red
                .normal(matrices.peek(), 0f, 1f, 0f);
    }

    public static void register() {
        WorldRenderEvents.LAST.register(new BowTrajectoryRenderer());
    }
}
