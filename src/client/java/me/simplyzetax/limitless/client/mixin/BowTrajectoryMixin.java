package me.simplyzetax.limitless.client.mixin;

import me.simplyzetax.limitless.client.render.BowTrajectoryData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

    private static final int MAX_TRAJECTORY_TICKS = 100;
    private static final double GRAVITY = 0.05; // Minecraft's gravity per tick
    private static final double AIR_RESISTANCE = 0.99; // Arrows slow down by 1% per tick
    private static final double ARROW_SIZE = 0.5; // Arrow hitbox size

    @Inject(method = "tick", at = @At("HEAD"))
    private void showBowTrajectory(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        // Only show trajectory for the local client player
        if (client.player != player || client.world == null) {
            return;
        }

        // Check if player is using a bow
        if (!isUsingBow(player)) {
            BowTrajectoryData.clearTrajectory();
            return;
        }

        // Calculate and render trajectory
        calculateAccurateTrajectory(player, client.world);
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
        ItemStack bowStack = player.getActiveItem();
        int useTicks = player.getItemUseTime();
        float bowPower = BowItem.getPullProgress(useTicks);

        // Don't show trajectory if bow isn't charged enough
        if (bowPower < 0.1f) {
            BowTrajectoryData.setShouldRender(false);
            return;
        }

        // Calculate arrow velocity (matches BowItem.use method)
        float velocity = bowPower * 3.0f;
        if (velocity > 1.0f) {
            velocity = 1.0f;
        }

        // Arrow spawns slightly in front of player and offset down from eye level
        Vec3d eyePos = player.getEyePos();
        Vec3d lookDirection = player.getRotationVec(1.0f);

        // Offset the starting position to match where arrows actually spawn
        Vec3d startPos = eyePos.add(lookDirection.multiply(0.5))
                .add(0, -0.1, 0); // Slightly lower than eye level

        // Calculate initial velocity vector (multiply by 20 to convert to
        // blocks/second, then back to blocks/tick)
        Vec3d velocityVec = lookDirection.multiply(velocity * 3.0);

        // Simulate trajectory tick by tick
        Vec3d currentPos = startPos;
        Vec3d currentVelocity = velocityVec;

        for (int tick = 0; tick < MAX_TRAJECTORY_TICKS; tick++) {
            // Store current position every few ticks for visualization
            if (tick % 2 == 0) { // Every 2 ticks for smoother line
                BowTrajectoryData.addTrajectoryPoint(currentPos);
            }

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
