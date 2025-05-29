package me.simplyzetax.limitless.client.mixins.bowtrajectory;

import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import me.simplyzetax.limitless.client.features.bowtrajectory.render.BowTrajectoryData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntity.class)
public class BowTrajectoryMixin {

    private static final int MAX_TRAJECTORY_TICKS = 200;
    private static final double GRAVITY = 0.05; // Minecraft's actual gravity for arrows (0.05 per tick)
    private static final double AIR_RESISTANCE = 0.99; // Arrows slow down by 1% per tick in air

    // Smooth trajectory variables
    private static int trajectoryUpdateCounter = 0;
    private static final int UPDATE_INTERVAL = 1; // Update every tick for smoothness

    // Variables for rotation change detection
    private static Vec3d lastPlayerRotation = null;

    @Inject(method = "tick", at = @At("HEAD"))
    private void showBowTrajectory(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        // Only show trajectory for the local client player
        if (client.player != player || client.world == null) {
            return;
        }

        // Check if bow trajectory is enabled in config
        if (!ClientConfig.ShowBowTrajectory) {
            BowTrajectoryData.clearTrajectory();
            trajectoryUpdateCounter = 0;
            lastPlayerRotation = null;
            return;
        }

        // Check if player is using a bow
        if (!isUsingBow(player)) {
            BowTrajectoryData.clearTrajectory();
            trajectoryUpdateCounter = 0;
            lastPlayerRotation = null;
            return;
        }

        // Check if player rotation has changed significantly
        Vec3d currentRotation = player.getRotationVec(1.0f);
        boolean rotationChanged = false;

        if (lastPlayerRotation != null) {
            double rotationDifference = lastPlayerRotation.distanceTo(currentRotation);
            if (rotationDifference > 0.01) { // Small threshold for rotation change
                rotationChanged = true;
            }
        }

        // Update trajectory immediately if rotation changed or on interval
        trajectoryUpdateCounter++;
        if (rotationChanged || trajectoryUpdateCounter >= UPDATE_INTERVAL) {
            trajectoryUpdateCounter = 0;
            lastPlayerRotation = currentRotation;
            calculateAccurateTrajectory(player, client.world);
        }
    }

    private boolean isUsingBow(PlayerEntity player) {
        // Check if player is actively using an item
        if (!player.isUsingItem()) {
            return false;
        }

        // Get the item being used
        ItemStack activeItem = player.getActiveItem();
        if (activeItem.isEmpty()) {
            return false;
        }

        // Check if it's a bow and has the correct use action
        return activeItem.getItem() instanceof BowItem &&
                activeItem.getUseAction() == UseAction.BOW;
    }

    private void calculateAccurateTrajectory(PlayerEntity player, World world) {
        // Clear previous trajectory
        BowTrajectoryData.clearTrajectory();
        BowTrajectoryData.setShouldRender(true);

        // Get bow charge progress
        int useTicks = player.getItemUseTime();
        float bowPower = BowItem.getPullProgress(useTicks);

        // Don't show trajectory if bow isn't charged enough
        if (bowPower < 0.1f) {
            BowTrajectoryData.setShouldRender(false);
            return;
        }

        // Calculate arrow velocity (exactly matches Minecraft bow mechanics)
        // Bow velocity ranges from 0 to 3 based on charge level
        float velocity = bowPower * 3.0f;

        // Get player position and rotation for realistic bow mechanics
        Vec3d eyePos = player.getEyePos();
        float yaw = player.getYaw();

        // Use smooth interpolated rotation for better movement
        Vec3d lookDirection = player.getRotationVec(1.0f);

        // Calculate realistic bow position (bow is held to the side and slightly down)
        // Simulate holding a bow: offset to the right side and down from eye level
        double sideOffset = 0.3; // Offset to the right (positive for right-handed)
        double downOffset = 0.2; // Slightly below eye level
        double forwardOffset = 0.4; // Slight forward offset for bow length

        // Calculate side vector (perpendicular to look direction)
        Vec3d sideVector = new Vec3d(
                -Math.sin(Math.toRadians(yaw + 90)), // Perpendicular to yaw
                0,
                Math.cos(Math.toRadians(yaw + 90)));

        Vec3d startPos = eyePos
                .add(lookDirection.multiply(forwardOffset)) // Forward offset
                .add(sideVector.multiply(sideOffset)) // Side offset (bow position)
                .add(0, -downOffset, 0); // Down offset

        // Add slight upward trajectory for realistic archery (bows shoot slightly up)
        double upwardAngle = Math.toRadians(2.0); // 2 degree upward angle
        Vec3d adjustedLookDirection = new Vec3d(
                lookDirection.x,
                lookDirection.y + Math.sin(upwardAngle), // Add upward component
                lookDirection.z).normalize();

        // Calculate initial velocity vector using Minecraft's actual velocity
        Vec3d velocityVec = adjustedLookDirection.multiply(velocity);

        // Simulate trajectory tick by tick
        Vec3d currentPos = startPos;
        Vec3d currentVelocity = velocityVec;

        for (int tick = 0; tick < MAX_TRAJECTORY_TICKS; tick++) {
            // Store current position more frequently for smoother visualization
            BowTrajectoryData.addTrajectoryPoint(currentPos);

            // Calculate next position
            Vec3d nextPos = currentPos.add(currentVelocity);

            // Check for block collision using raycast
            BlockHitResult hitResult = world.raycast(new RaycastContext(
                    currentPos,
                    nextPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BowTrajectoryData.setImpactPoint(hitResult.getPos());
                break;
            }

            // Update position
            currentPos = nextPos;

            // Apply physics to velocity
            // 1. Apply gravity
            currentVelocity = currentVelocity.subtract(0, GRAVITY, 0);

            // 2. Apply air resistance
            currentVelocity = currentVelocity.multiply(AIR_RESISTANCE);

            // Stop if arrow goes too far or too low
            if (currentPos.y < world.getBottomY() ||
                    currentPos.distanceTo(startPos) > 200) {
                break;
            }

            // Stop if velocity becomes too small (arrow would fall)
            if (currentVelocity.length() < 0.01) {
                break;
            }
        }
    }
}
