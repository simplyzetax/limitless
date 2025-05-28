package me.simplyzetax.limitless.client.util;

import net.minecraft.util.math.Vec3d;

public class BowChargeInfo {
    public Vec3d shooterPos;
    public float yaw, pitch;
    public int chargeTime;
    public long timestamp;

    public BowChargeInfo(Vec3d pos, float y, float p, int charge, long time) {
        this.shooterPos = pos;
        this.yaw = y;
        this.pitch = p;
        this.chargeTime = charge;
        this.timestamp = time;
    }
}
