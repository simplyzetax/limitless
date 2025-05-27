package me.simplyzetax.limitless.client.render;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class BowTrajectoryData {
    public static boolean shouldRenderTrajectory = false;
    public static List<Vec3d> trajectoryPoints = new ArrayList<>();
    public static Vec3d impactPoint = null;

    public static void clearTrajectory() {
        trajectoryPoints.clear();
        impactPoint = null;
        shouldRenderTrajectory = false;
    }

    public static void addTrajectoryPoint(Vec3d point) {
        trajectoryPoints.add(point);
    }

    public static void setImpactPoint(Vec3d point) {
        impactPoint = point;
    }

    public static void setShouldRender(boolean shouldRender) {
        shouldRenderTrajectory = shouldRender;
    }
}
