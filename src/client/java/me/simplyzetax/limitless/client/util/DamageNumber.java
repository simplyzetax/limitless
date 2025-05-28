package me.simplyzetax.limitless.client.util;

import net.minecraft.util.math.Vec3d;

/**
 * Represents a floating damage number that appears above entities
 */
public class DamageNumber {
    public final float damage;
    public final Vec3d position;
    public final long spawnTime;
    public final DamageType damageType;

    // Animation properties
    public Vec3d currentPosition;
    public float alpha;
    public float scale;

    // Duration in milliseconds
    public static final long DISPLAY_DURATION = 2000; // 2 seconds

    public enum DamageType {
        NORMAL(0xFFFF5555), // Bright Red
        CRITICAL(0xFFFFAA00), // Bright Orange
        HEALING(0xFF55FF55), // Bright Green
        TRUE_DAMAGE(0xFFAA00AA), // Purple
        ENVIRONMENTAL(0xFF888888); // Gray

        public final int color;

        DamageType(int color) {
            this.color = color;
        }
    }

    public DamageNumber(float damage, Vec3d position, DamageType damageType) {
        this.damage = damage;
        this.position = position;
        this.currentPosition = position;
        this.spawnTime = System.currentTimeMillis();
        this.damageType = damageType;
        this.alpha = 1.0f;
        this.scale = 1.0f;
    }

    /**
     * Update the damage number animation
     * 
     * @return true if the damage number should continue to exist, false if it
     *         should be removed
     */
    public boolean update() {
        long age = System.currentTimeMillis() - spawnTime;

        if (age >= DISPLAY_DURATION) {
            return false; // Remove this damage number
        }

        // Calculate animation progress (0.0 to 1.0)
        float progress = (float) age / DISPLAY_DURATION;

        // Animate position - float upward
        double yOffset = progress * 2.0; // Float up 2 blocks over lifetime
        currentPosition = position.add(0, yOffset, 0);

        // Animate alpha - fade out in the last 50% of lifetime
        if (progress > 0.5f) {
            alpha = 1.0f - ((progress - 0.5f) * 2.0f);
        } else {
            alpha = 1.0f;
        }

        // Animate scale - start big, shrink to normal, then grow again at end
        if (progress < 0.1f) {
            // Pop in effect
            scale = 1.0f + (1.0f - (progress / 0.1f)) * 0.5f;
        } else if (progress > 0.9f) {
            // Pop out effect
            scale = 1.0f + ((progress - 0.9f) / 0.1f) * 0.3f;
        } else {
            scale = 1.0f;
        }

        return true;
    }

    /**
     * Get the formatted damage string
     */
    public String getDamageText() {
        if (damageType == DamageType.HEALING) {
            return "+" + String.format("%.1f", damage);
        } else {
            return String.format("%.1f", damage);
        }
    }

    /**
     * Check if this damage number has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime >= DISPLAY_DURATION;
    }
}
