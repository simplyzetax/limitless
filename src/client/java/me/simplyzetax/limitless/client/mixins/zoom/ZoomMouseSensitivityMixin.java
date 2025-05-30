package me.simplyzetax.limitless.client.mixins.zoom;

import me.simplyzetax.limitless.client.features.zoom.util.ZoomMouseAccumulatorReset;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to handle mouse accumulator reset for zoom transitions.
 * Mouse sensitivity modification has been disabled to prevent snapping -
 * the zoom effect is now purely FOV-based for smooth operation.
 */
@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class ZoomMouseSensitivityMixin implements ZoomMouseAccumulatorReset {
    private static final Logger LOGGER = LoggerFactory.getLogger("ZoomMouseSensitivityMixin");

    // --- Accumulator reset for zoom snap fix ---
    // These fields match the names in net.minecraft.client.Mouse (1.21.5+)
    @org.spongepowered.asm.mixin.Shadow
    private double cursorDeltaX;
    @org.spongepowered.asm.mixin.Shadow
    private double cursorDeltaY;

    /**
     * Mouse sensitivity modification disabled to prevent zoom snapping.
     * Pure FOV-based zoom provides smooth zooming without sensitivity scaling
     * issues.
     */
    @ModifyVariable(method = "onCursorPos", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double modifyMouseDeltaX(double deltaX) {
        // DISABLED: Mouse sensitivity scaling causes snapping when zoom levels change
        // FOV modification alone provides proper zoom functionality
        return deltaX;
    }

    /**
     * Mouse sensitivity modification disabled to prevent zoom snapping.
     * Pure FOV-based zoom provides smooth zooming without sensitivity scaling
     * issues.
     */
    @ModifyVariable(method = "onCursorPos", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private double modifyMouseDeltaY(double deltaY) {
        // DISABLED: Mouse sensitivity scaling causes snapping when zoom levels change
        // FOV modification alone provides proper zoom functionality
        return deltaY;
    }

    @Override
    public void resetMouseAccumulators() {
        this.cursorDeltaX = 0.0;
        this.cursorDeltaY = 0.0;
        LOGGER.info("Mouse deltas reset after zoom transition");
    }

    /**
     * Static helper to reset accumulators from outside the mixin.
     * (Moved to ZoomManager for Mixin compliance)
     */
    // Removed static method resetAccumulatorsStatic()
}
