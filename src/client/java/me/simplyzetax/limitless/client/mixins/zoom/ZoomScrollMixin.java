package me.simplyzetax.limitless.client.mixins.zoom;

import me.simplyzetax.limitless.client.features.zoom.util.ZoomManager;
import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to handle mouse wheel scrolling for zoom adjustment.
 * This intercepts mouse scroll events when zooming to adjust zoom level.
 */
@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class ZoomScrollMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ZoomScrollMixin");

    /**
     * Inject into the onMouseScroll method to handle zoom adjustment.
     * This is called when the player scrolls the mouse wheel.
     */
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void handleZoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!ClientConfig.EnableZoom) {
            return;
        }

        // Only handle scroll when zooming is active
        if (ZoomManager.isZooming()) {
            // Adjust zoom level based on scroll direction
            // Positive vertical = scroll up = zoom in (increase zoom level)
            // Negative vertical = scroll down = zoom out (decrease zoom level)
            float scrollDelta = (float) vertical;
            ZoomManager.adjustZoom(scrollDelta);

            // Cancel the default scroll behavior (inventory slot changing, etc.)
            ci.cancel();

            LOGGER.debug("Mouse scroll handled for zoom: delta={}, newLevel={}",
                    scrollDelta, ZoomManager.getCurrentZoomLevel());
        }
    }
}
