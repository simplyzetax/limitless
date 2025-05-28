package me.simplyzetax.limitless.client.mixin;

import me.simplyzetax.limitless.client.config.ClientConfig;
import me.simplyzetax.limitless.client.util.DamageNumber;
import me.simplyzetax.limitless.client.util.DamageNumberManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class DamageNumberRenderMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("DamageNumberRender");

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ClientConfig.ShowDamageNumbers) {
            return;
        }

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) {
                return;
            }

            // Update damage number animations
            DamageNumberManager.updateDamageNumbers();

            // Check if we have any damage numbers to render
            int totalDamageNumbers = DamageNumberManager.getTotalDamageNumberCount();
            if (totalDamageNumbers > 0) {
                LOGGER.info("Rendering {} damage numbers on HUD", totalDamageNumbers);
                renderDamageNumbers(context, client);
            }

        } catch (Exception e) {
            LOGGER.error("Error rendering damage numbers: {}", e.getMessage());
        }
    }
    private void renderDamageNumbers(DrawContext context, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;
        
        int renderedCount = 0;
        DamageNumberManager.getAllDamageNumbers().values().forEach(damageNumberList -> {
            for (DamageNumber damageNumber : damageNumberList) {
                LOGGER.info("Attempting to render damage number: {} at {}", damageNumber.getDamageText(), damageNumber.currentPosition);
                renderDamageNumber(context, damageNumber, textRenderer, client);
            }
        });
        
        if (renderedCount > 0) {
            LOGGER.info("Successfully rendered {} damage numbers", renderedCount);
        }
    }

    private void renderDamageNumber(DrawContext context, DamageNumber damageNumber,
            TextRenderer textRenderer, MinecraftClient client) {
        try {
            Vec3d pos = damageNumber.currentPosition;
            
            // Get world to screen coordinates
            Vec3d screenPos = worldToScreen(pos, client);
            if (screenPos == null) {
                return; // Behind camera or too far
            }

            // Calculate distance for scaling
            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            double distance = cameraPos.distanceTo(pos);
            if (distance > 50.0) {
                return; // Don't render if too far away
            }

            // Calculate screen position
            int x = (int) screenPos.x;
            int y = (int) screenPos.y;

            // Create text
            String damageText = damageNumber.getDamageText();

            // Calculate text width for centering
            int textWidth = textRenderer.getWidth(damageText);

            // Apply alpha and color
            int alpha = (int) (damageNumber.alpha * 255);
            int color = (alpha << 24) | (damageNumber.damageType.color & 0xFFFFFF);

            // Render text with outline for better visibility
            context.drawText(textRenderer, damageText, x - textWidth / 2 - 1, y - 1, 0x000000, false);
            context.drawText(textRenderer, damageText, x - textWidth / 2 + 1, y - 1, 0x000000, false);
            context.drawText(textRenderer, damageText, x - textWidth / 2 - 1, y + 1, 0x000000, false);
            context.drawText(textRenderer, damageText, x - textWidth / 2 + 1, y + 1, 0x000000, false);
            
            // Render main text
            context.drawText(textRenderer, damageText, x - textWidth / 2, y, color, false);

            LOGGER.info("Rendered damage number '{}' at screen position ({}, {})", damageText, x, y);

        } catch (Exception e) {
            LOGGER.error("Error rendering individual damage number: {}", e.getMessage());
        }
    }

    private Vec3d worldToScreen(Vec3d worldPos, MinecraftClient client) {
        try {
            // Get camera position and transformation matrices
            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            
            // Simple approach: project relative position to screen center
            Vec3d relative = worldPos.subtract(cameraPos);
            
            // Basic distance-based positioning (simplified)
            double distance = relative.length();
            if (distance > 50 || distance < 1) {
                return null;
            }
            
            // Project to screen coordinates (simplified approach)
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            
            // Place near screen center with some offset based on relative position
            int x = screenWidth / 2 + (int) (relative.x * 10);
            int y = screenHeight / 2 - (int) (relative.y * 10);
            
            return new Vec3d(x, y, 0);
            
        } catch (Exception e) {
            LOGGER.error("Error in worldToScreen conversion: {}", e.getMessage());
            return null;
        }
    }
}
