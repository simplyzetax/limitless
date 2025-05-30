package me.simplyzetax.limitless.client.features.zoom.util;

import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages zoom functionality similar to OptiFine.
 * Handles zoom state, levels, and smooth transitions.
 */
public class ZoomManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("ZoomManager");

    private static boolean isZooming = false;
    private static float currentZoomLevel = ClientConfig.ZoomLevel;
    private static float targetZoomLevel = ClientConfig.ZoomLevel;
    private static final float ZOOM_SMOOTH_FACTOR = 0.3f; // How fast zoom transitions happen

    /**
     * Toggles zoom on/off
     */
    public static void toggleZoom() {
        isZooming = !isZooming;
        if (isZooming) {
            targetZoomLevel = ClientConfig.ZoomLevel;
            LOGGER.info("Zoom enabled at level {}", targetZoomLevel);
        } else {
            targetZoomLevel = 1.0f;
            LOGGER.info("Zoom disabled");
        }
        LOGGER.info("Zoom state: isZooming={}, currentLevel={}, targetLevel={}",
                isZooming, currentZoomLevel, targetZoomLevel);
    }

    /**
     * Sets zoom on/off state directly
     */
    public static void setZoom(boolean zoom) {
        if (isZooming != zoom) {
            toggleZoom();
        }
    }

    /**
     * Adjusts zoom level by a delta amount (for mouse wheel)
     */
    public static void adjustZoom(float delta) {
        if (!isZooming) {
            return;
        }

        float oldLevel = targetZoomLevel;
        targetZoomLevel = Math.max(ClientConfig.MinZoomLevel,
                Math.min(ClientConfig.MaxZoomLevel,
                        targetZoomLevel + delta * ClientConfig.ZoomScrollSensitivity));

        if (oldLevel != targetZoomLevel) {
            LOGGER.debug("Zoom level adjusted from {} to {}", oldLevel, targetZoomLevel);
        }
    }

    /**
     * Updates the current zoom level with smooth transitions
     * Should be called every tick
     */
    public static void updateZoom() {
        if (!ClientConfig.EnableZoom) {
            currentZoomLevel = 1.0f;
            isZooming = false;
            return;
        }

        // Smooth transition to target zoom level
        if (Math.abs(currentZoomLevel - targetZoomLevel) > 0.01f) {
            currentZoomLevel += (targetZoomLevel - currentZoomLevel) * ZOOM_SMOOTH_FACTOR;
        } else {
            currentZoomLevel = targetZoomLevel;
            // Only set isZooming to false when fully zoomed out
            if (targetZoomLevel == 1.0f && currentZoomLevel == 1.0f) {
                isZooming = false;
                // Reset mouse accumulators to prevent snap
                resetMouseDeltas();
            }
        }
    }

    /**
     * Resets mouse deltas in Mouse class to prevent snap after zoom
     */
    private static void resetMouseDeltas() {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client != null
                && client.mouse instanceof me.simplyzetax.limitless.client.features.zoom.util.ZoomMouseAccumulatorReset resettable) {
            resettable.resetMouseAccumulators();
        }
    }

    /**
     * Gets the current zoom multiplier for FOV calculation
     * 
     * @return Zoom multiplier (1.0 = no zoom, higher = more zoomed)
     */
    public static float getZoomMultiplier() {
        return currentZoomLevel;
    }

    /**
     * Gets whether zoom is currently active
     */
    public static boolean isZooming() {
        // Consider zooming active if not at default zoom, even if toggled off but still
        // transitioning
        return (isZooming || currentZoomLevel != 1.0f) && ClientConfig.EnableZoom;
    }

    /**
     * Gets the current zoom level
     */
    public static float getCurrentZoomLevel() {
        return currentZoomLevel;
    }

    /**
     * Gets the target zoom level
     */
    public static float getTargetZoomLevel() {
        return targetZoomLevel;
    }

    /**
     * Resets zoom to default settings
     */
    public static void reset() {
        isZooming = false;
        currentZoomLevel = 1.0f;
        targetZoomLevel = 1.0f;
        LOGGER.info("Zoom reset to default");
    }
}
