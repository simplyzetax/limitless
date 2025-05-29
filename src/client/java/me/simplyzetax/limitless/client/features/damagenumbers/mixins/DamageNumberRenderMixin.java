package me.simplyzetax.limitless.client.features.damagenumbers.mixins;

import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import me.simplyzetax.limitless.client.features.damagenumbers.util.DamageNumber;
import me.simplyzetax.limitless.client.features.damagenumbers.util.DamageNumberManager;
import me.simplyzetax.limitless.client.features.damagenumbers.util.DamageFontManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
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
                LOGGER.debug("Rendering {} damage numbers on HUD", totalDamageNumbers);
                renderDamageNumbers(context, client);
            }

        } catch (Exception e) {
            LOGGER.error("Error rendering damage numbers: {}", e.getMessage());
        }
    }

    private void renderDamageNumbers(DrawContext context, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;

        DamageNumberManager.getAllDamageNumbers().values().forEach(damageNumberList -> {
            for (DamageNumber damageNumber : damageNumberList) {
                LOGGER.debug("Attempting to render damage number: {} at {}", damageNumber.getDamageText(),
                        damageNumber.currentPosition);
                renderDamageNumber(context, damageNumber, textRenderer, client);
            }
        });
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

            // Apply distance-based scaling for text size
            float scale = 1.0f;
            if (distance > 5.0) {
                // Gradually decrease size with distance
                scale = (float) Math.max(0.6f, 1.0f - (distance - 5.0) / 45.0);
            }

            // Apply animation scaling
            scale *= damageNumber.scale;

            // Create text
            String damageText = damageNumber.getDamageText();

            // Calculate text width for centering
            int textWidth = textRenderer.getWidth(damageText);

            // Create text with custom font
            Identifier fontId = DamageFontManager.getCurrentFont();
            Text styledText;
            if (fontId != null) {
                styledText = Text.literal(damageText).styled(style -> style.withFont(fontId));
            } else {
                // Use default font
                styledText = Text.literal(damageText);
            }

            // Apply alpha and color
            int alpha = Math.max(128, (int) (damageNumber.alpha * 255)); // Ensure minimum visibility
            int color = (alpha << 24) | (damageNumber.damageType.color & 0xFFFFFF);

            // Save the current matrix state
            context.getMatrices().push();

            // Apply the scaling
            context.getMatrices().scale(scale, scale, 1.0f);
            float scaledX = x / scale;
            float scaledY = y / scale;

            // Render text with thick black outline for better visibility
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        context.drawText(textRenderer, styledText, (int) (scaledX - textWidth / 2) + dx,
                                (int) scaledY + dy, 0xFF000000, false);
                    }
                }
            }

            // Render main text
            context.drawText(textRenderer, styledText, (int) (scaledX - textWidth / 2), (int) scaledY, color, false);

            // Restore the matrix state
            context.getMatrices().pop();

            // LOGGER.debug("Rendered damage number '{}' at screen position ({}, {})",
            // damageText, x, y);

        } catch (Exception e) {
            LOGGER.error("Error rendering individual damage number: {}", e.getMessage());
        }
    }

    private Vec3d worldToScreen(Vec3d worldPos, MinecraftClient client) {
        try {
            Camera camera = client.gameRenderer.getCamera();
            if (camera == null) {
                return null;
            }

            // Get camera position and orientation
            Vec3d cameraPos = camera.getPos();
            float pitch = camera.getPitch();
            float yaw = camera.getYaw();

            // Calculate relative position from camera to world position
            Vec3d relative = worldPos.subtract(cameraPos);
            double distance = relative.length();

            // Don't render if too far or too close
            if (distance > 50.0 || distance < 0.5) {
                return null;
            }

            // Get projection and model-view matrices
            MatrixStack matrixStack = new MatrixStack();
            Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(70.0f); // Use standard FOV

            // Create model-view matrix based on camera rotation
            org.joml.Quaternionf pitchRotation = new org.joml.Quaternionf().rotateX((float) Math.toRadians(pitch));
            org.joml.Quaternionf yawRotation = new org.joml.Quaternionf().rotateY((float) Math.toRadians(yaw + 180));

            matrixStack.multiply(pitchRotation); // Pitch rotation
            matrixStack.multiply(yawRotation); // Yaw rotation
            Matrix4f modelViewMatrix = matrixStack.peek().getPositionMatrix();

            // Transform world position to camera space
            Vector4f worldVector = new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1.0f);
            Vector4f cameraSpace = new Vector4f();
            modelViewMatrix.transform(worldVector, cameraSpace);

            // Check if behind camera
            if (cameraSpace.z > 0) {
                return null;
            }

            // Project to normalized device coordinates
            Vector4f clipSpace = new Vector4f();
            projectionMatrix.transform(cameraSpace, clipSpace);

            // Perform perspective divide
            if (clipSpace.w == 0) {
                return null;
            }

            float ndcX = clipSpace.x / clipSpace.w;
            float ndcY = clipSpace.y / clipSpace.w;
            float ndcZ = clipSpace.z / clipSpace.w;

            // Check if within view frustum
            if (Math.abs(ndcX) > 1.0f || Math.abs(ndcY) > 1.0f || ndcZ < -1.0f || ndcZ > 1.0f) {
                return null;
            }

            // Convert to screen coordinates
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            int screenX = (int) ((ndcX + 1.0f) * 0.5f * screenWidth);
            int screenY = (int) ((1.0f - ndcY) * 0.5f * screenHeight);

            // LOGGER.debug("World pos {} -> Screen pos ({}, {}) at distance {}", worldPos,
            // screenX, screenY, distance);

            return new Vec3d(screenX, screenY, distance);

        } catch (Exception e) {
            LOGGER.error("Error in worldToScreen conversion: {}", e.getMessage());
            return null;
        }
    }
}
