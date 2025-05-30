package me.simplyzetax.limitless.client.mixins.zoom;

import me.simplyzetax.limitless.client.features.zoom.util.ZoomManager;
import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify the field of view (FOV) for zoom functionality.
 * This injects into GameRenderer to modify the camera's FOV when zooming.
 */
@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class ZoomMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ZoomMixin");
    private static long lastLogTime = 0;
    private static final long LOG_INTERVAL_MS = 5000; // Log only once every 5 seconds

    /**
     * Inject into the getBasicProjectionMatrix method to modify FOV based on zoom
     * level.
     * This is called when the game calculates the projection matrix with FOV.
     */
    @Inject(method = "getBasicProjectionMatrix", at = @At("HEAD"), cancellable = true)
    private void modifyFovForZoom(float fov, CallbackInfoReturnable<org.joml.Matrix4f> cir) {
        if (!ClientConfig.EnableZoom) {
            return;
        }

        // Update zoom state
        ZoomManager.updateZoom();

        if (ZoomManager.isZooming()) {
            float zoomMultiplier = ZoomManager.getZoomMultiplier();

            // Apply zoom by dividing FOV by zoom multiplier
            // Higher zoom multiplier = smaller FOV = more zoomed in
            float zoomedFov = fov / zoomMultiplier;

            // Ensure we don't go below a minimum FOV to prevent issues
            zoomedFov = Math.max(zoomedFov, 1.0f);

            // Only log occasionally to prevent excessive spam
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLogTime > LOG_INTERVAL_MS) {
                LOGGER.debug("Zoom applied: originalFov={}, zoomMultiplier={}, zoomedFov={}",
                        fov, zoomMultiplier, zoomedFov);
                lastLogTime = currentTime;
            }

            // Get MinecraftClient for window dimensions
            MinecraftClient client = MinecraftClient.getInstance();

            // Create projection matrix with modified FOV
            float aspectRatio = (float) client.getWindow().getFramebufferWidth()
                    / (float) client.getWindow().getFramebufferHeight();
            org.joml.Matrix4f matrix = new org.joml.Matrix4f();
            matrix.setPerspective((float) Math.toRadians(zoomedFov), aspectRatio, 0.05f, 1024.0f);

            cir.setReturnValue(matrix);
        }
    }
}
