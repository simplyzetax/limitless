package me.simplyzetax.limitless.client.util;

import net.minecraft.util.math.Vec3d;

public class ArrowInfo {
    public Vec3d position;
    public Vec3d velocity;
    public long spawnTime;

    public ArrowInfo(Vec3d pos, Vec3d vel, long time) {
        this.position = pos;
        this.velocity = vel;
        this.spawnTime = time;
    }
}
