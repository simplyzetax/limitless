package me.simplyzetax.limitless.client.util;

import net.minecraft.util.math.Vec3d;

public class DodgeCalculation {
    public boolean willHit;
    public int timeToImpact;
    public Vec3d impactPoint;

    public DodgeCalculation(boolean hit, int time, Vec3d point) {
        this.willHit = hit;
        this.timeToImpact = time;
        this.impactPoint = point;
    }
}
