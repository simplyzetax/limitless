package me.simplyzetax.limitless.client.features.arrowdodge.util;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared data structure for tracking arrows that pose a threat and should glow
 * red
 */
public class ThreatArrowData {
    private static final Set<UUID> threateningArrows = ConcurrentHashMap.newKeySet();

    /**
     * Mark an arrow as threatening (will glow red)
     * 
     * @param arrowUuid UUID of the threatening arrow
     */
    public static void markArrowAsThreatening(UUID arrowUuid) {
        threateningArrows.add(arrowUuid);
    }

    /**
     * Remove an arrow from the threatening list
     * 
     * @param arrowUuid UUID of the arrow to remove
     */
    public static void removeThreateningArrow(UUID arrowUuid) {
        threateningArrows.remove(arrowUuid);
    }

    /**
     * Check if an arrow is marked as threatening
     * 
     * @param arrowUuid UUID of the arrow to check
     * @return true if the arrow should glow red
     */
    public static boolean isArrowThreatening(UUID arrowUuid) {
        return threateningArrows.contains(arrowUuid);
    }

    /**
     * Clear all threatening arrows (useful for cleanup)
     */
    public static void clearAllThreateningArrows() {
        threateningArrows.clear();
    }

    /**
     * Get the number of currently threatening arrows (for debugging)
     * 
     * @return number of arrows marked as threatening
     */
    public static int getThreateningArrowCount() {
        return threateningArrows.size();
    }
}
