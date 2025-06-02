package me.simplyzetax.limitless.client.mixins.itemstealing;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;

/**
 * This mixin was previously used for Q key handling but has been consolidated
 * into CreativeInventoryInteractionMixin to avoid conflicts.
 * Keeping this file for potential future drop-related functionality.
 */
@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryDropMixin {

    // Q key functionality has been moved to CreativeInventoryInteractionMixin
    // to avoid mixin conflicts. This class is reserved for future drop-related
    // functionality if needed.

}
