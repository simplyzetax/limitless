package me.simplyzetax.limitless.client.features.damagenumbers.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages floating damage numbers for all entities
 */
public class DamageNumberManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("DamageNumbers");

    // Map of entity UUID to list of damage numbers
    private static final ConcurrentHashMap<UUID, List<DamageNumber>> entityDamageNumbers = new ConcurrentHashMap<>();

    /**
     * Add a damage number for an entity
     */
    public static void addDamageNumber(Entity entity, float damage, DamageNumber.DamageType damageType) {
        if (entity == null) {
            return;
        }

        UUID entityId = entity.getUuid();

        // Calculate position above entity's head
        Vec3d entityPos = entity.getPos();
        double entityHeight = entity.getHeight();
        Vec3d damagePos = entityPos.add(0, entityHeight + 0.5, 0);

        // Add some random offset to prevent numbers from overlapping
        double randomX = (Math.random() - 0.5) * 0.4;
        double randomZ = (Math.random() - 0.5) * 0.4;
        damagePos = damagePos.add(randomX, 0, randomZ);

        DamageNumber damageNumber = new DamageNumber(damage, damagePos, damageType);

        // Get or create damage number list for this entity
        entityDamageNumbers.computeIfAbsent(entityId, k -> new CopyOnWriteArrayList<>()).add(damageNumber);

        LOGGER.debug("Added damage number: {} {} at {}", damage, damageType, damagePos);
    }

    /**
     * Update all damage numbers and remove expired ones
     */
    public static void updateDamageNumbers() {
        entityDamageNumbers.entrySet().removeIf(entry -> {
            List<DamageNumber> damageNumbers = entry.getValue();

            // Update each damage number and remove expired ones
            damageNumbers.removeIf(damageNumber -> !damageNumber.update());

            // Remove the entity entry if it has no more damage numbers
            return damageNumbers.isEmpty();
        });
    }

    /**
     * Get all damage numbers for rendering
     */
    public static ConcurrentHashMap<UUID, List<DamageNumber>> getAllDamageNumbers() {
        return entityDamageNumbers;
    }

    /**
     * Get damage numbers for a specific entity
     */
    public static List<DamageNumber> getDamageNumbers(UUID entityId) {
        return entityDamageNumbers.getOrDefault(entityId, new CopyOnWriteArrayList<>());
    }

    /**
     * Clear all damage numbers (useful for cleanup)
     */
    public static void clearAllDamageNumbers() {
        entityDamageNumbers.clear();
        LOGGER.debug("Cleared all damage numbers");
    }

    /**
     * Remove damage numbers for a specific entity
     */
    public static void removeDamageNumbers(UUID entityId) {
        entityDamageNumbers.remove(entityId);
    }

    /**
     * Get total count of active damage numbers (for debugging)
     */
    public static int getTotalDamageNumberCount() {
        return entityDamageNumbers.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}
