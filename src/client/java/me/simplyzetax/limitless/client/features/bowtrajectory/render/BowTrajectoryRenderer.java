package me.simplyzetax.limitless.client.features.bowtrajectory.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class BowTrajectoryRenderer implements WorldRenderEvents.Last {

    // Simplified rendering - removed aggressive frame limiting that was causing
    // flashing
    private static final double MAX_RENDER_DISTANCE = 150.0; // Don't render if further than 150 blocks

    @Override
    public void onLast(WorldRenderContext context) {
        if (!BowTrajectoryData.shouldRenderTrajectory ||
                BowTrajectoryData.trajectoryPoints.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null)
            return;

        // Simple distance culling only - removed complex adaptive rendering
        Vec3d playerPos = client.player.getPos();
        if (!BowTrajectoryData.trajectoryPoints.isEmpty()) {
            Vec3d firstPoint = BowTrajectoryData.trajectoryPoints.get(0);
            double distance = playerPos.distanceTo(firstPoint);
            if (distance > MAX_RENDER_DISTANCE) {
                return;
            }
        }

        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        VertexConsumerProvider.Immediate vertexConsumers = client
                .getBufferBuilders()
                .getEntityVertexConsumers();

        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Draw trajectory line (white) with better smoothing and stability
        if (BowTrajectoryData.trajectoryPoints.size() > 1) {
            VertexConsumer lineConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
            Matrix4f posMatrix = matrices.peek().getPositionMatrix();

            // Enhanced trajectory rendering with better line connectivity
            int pointCount = BowTrajectoryData.trajectoryPoints.size();
            for (int i = 0; i < pointCount - 1; i++) {
                Vec3d p1 = BowTrajectoryData.trajectoryPoints.get(i);
                Vec3d p2 = BowTrajectoryData.trajectoryPoints.get(i + 1);

                // Draw line segment with consistent rendering and brighter color
                lineConsumer.vertex(posMatrix, (float) p1.x, (float) p1.y, (float) p1.z)
                        .color(255, 255, 255, 255) // Bright white
                        .normal(matrices.peek(), 0f, 1f, 0f);

                lineConsumer.vertex(posMatrix, (float) p2.x, (float) p2.y, (float) p2.z)
                        .color(255, 255, 255, 255) // Bright white
                        .normal(matrices.peek(), 0f, 1f, 0f);
            } // Add a small connecting line from bow position to first trajectory point
            if (pointCount > 0) {
                Vec3d firstPoint = BowTrajectoryData.trajectoryPoints.get(0);

                // Calculate approximate bow position (similar to mixin calculation)
                Vec3d playerEyePos = client.player.getEyePos();
                float yaw = client.player.getYaw();
                Vec3d lookDirection = client.player.getRotationVec(1.0f);

                Vec3d sideVector = new Vec3d(
                        -Math.sin(Math.toRadians(yaw + 90)),
                        0,
                        Math.cos(Math.toRadians(yaw + 90)));

                Vec3d bowPosition = playerEyePos
                        .add(lookDirection.multiply(0.4))
                        .add(sideVector.multiply(0.3))
                        .add(0, -0.2, 0);

                // Only draw connection if bow and first point are reasonably close
                if (firstPoint.distanceTo(bowPosition) < 1.0) {
                    // Draw connection line from bow position to first trajectory point
                    lineConsumer
                            .vertex(posMatrix, (float) bowPosition.x, (float) bowPosition.y, (float) bowPosition.z)
                            .color(255, 255, 255, 128) // Semi-transparent
                            .normal(matrices.peek(), 0f, 1f, 0f);

                    lineConsumer.vertex(posMatrix, (float) firstPoint.x, (float) firstPoint.y, (float) firstPoint.z)
                            .color(255, 255, 255, 255) // Full opacity
                            .normal(matrices.peek(), 0f, 1f, 0f);
                }
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

        // Performance optimization: Pre-calculate all coordinates
        float minX = (float) (center.x - half);
        float minY = (float) (center.y - half);
        float minZ = (float) (center.z - half);
        float maxX = (float) (center.x + half);
        float maxY = (float) (center.y + half);
        float maxZ = (float) (center.z + half);

        // Performance optimization: Batch all cube edges in one go
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
        // Performance optimization: Reduce redundant normal calculations
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
