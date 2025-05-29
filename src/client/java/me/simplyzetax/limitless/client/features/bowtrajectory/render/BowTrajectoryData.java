package me.simplyzetax.limitless.client.features.bowtrajectory.render;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class BowTrajectoryData {
    public static boolean shouldRenderTrajectory = false;
    public static List<Vec3d> trajectoryPoints = new ArrayList<>();
    public static Vec3d impactPoint = null;

    // Performance optimization: Limit trajectory points to prevent lag
    private static final int MAX_TRAJECTORY_POINTS = 200;

    public static void clearTrajectory() {
        trajectoryPoints.clear();
        impactPoint = null;
        shouldRenderTrajectory = false;
    }

    public static void addTrajectoryPoint(Vec3d point) {
        // Performance optimization: Limit trajectory points
        if (trajectoryPoints.size() >= MAX_TRAJECTORY_POINTS) {
            trajectoryPoints.remove(0); // Remove oldest point
        }

        // Skip points that are too close to the last point to reduce rendering load
        if (!trajectoryPoints.isEmpty()) {
            Vec3d lastPoint = trajectoryPoints.get(trajectoryPoints.size() - 1);
            if (lastPoint.distanceTo(point) < 0.02) { // Reduced from 0.05 to 0.02 for even smoother trajectory
                return; // Skip this point
            }
        }

        trajectoryPoints.add(point);
    }

    public static void setImpactPoint(Vec3d point) {
        impactPoint = point;
    }

    public static void setShouldRender(boolean shouldRender) {
        shouldRenderTrajectory = shouldRender;
    }
}
