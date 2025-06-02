package me.simplyzetax.limitless.client.mixins.arrowdodge;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import me.simplyzetax.limitless.client.features.arrowdodge.util.ArrowInfo;
import me.simplyzetax.limitless.client.features.arrowdodge.util.BowChargeInfo;
import me.simplyzetax.limitless.client.features.arrowdodge.util.DodgeCalculation;
import me.simplyzetax.limitless.client.features.arrowdodge.util.ThreatArrowData;

@Mixin(ClientPlayerEntity.class)
public class ArrowDodgeMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ArrowDodge");
    private static final Map<UUID, ArrowInfo> trackedArrows = new ConcurrentHashMap<>();
    private static final Map<UUID, BowChargeInfo> bowCharges = new ConcurrentHashMap<>();
    private static final Set<UUID> dodgedArrows = new HashSet<>(); // Track arrows we've already dodged
    private Vec3d dodgeTarget = null;
    private int dodgeTicks = 0;
    private boolean dodgeJustStarted = false; // Track when dodge begins
    private int tickCounter = 0; // For debug logging frequency control

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        tickCounter++;

        if (client.world == null) {
            if (tickCounter % 100 == 0) {
                LOGGER.debug("ArrowDodge: World is null, skipping tick");
            }
            return;
        }

        // Check both feature-level and specific setting flags
        if (!ClientConfig.EnableArrowDodgeFeature || !ClientConfig.EnableArrowDodging) {
            return;
        }

        // Update bow charge tracking
        updateBowCharges(client);

        // Track and update arrows
        updateArrowTracking(client, player);

        // Calculate dodge if needed
        calculateDodge(player);

        // Execute dodge movement
        executeDodge(player);
    }

    private void updateBowCharges(MinecraftClient client) {
        int bowsFound = 0;
        for (AbstractClientPlayerEntity otherPlayer : client.world.getPlayers()) {
            if (otherPlayer == client.player)
                continue;

            ItemStack mainHand = otherPlayer.getMainHandStack();
            ItemStack offHand = otherPlayer.getOffHandStack();

            boolean holdingBow = (mainHand.getItem() instanceof BowItem) ||
                    (offHand.getItem() instanceof BowItem);

            if (holdingBow && otherPlayer.isUsingItem()) {
                bowsFound++;
                int useTime = otherPlayer.getItemUseTime();
                BowChargeInfo chargeInfo = new BowChargeInfo(
                        otherPlayer.getPos(),
                        otherPlayer.getYaw(),
                        otherPlayer.getPitch(),
                        useTime,
                        System.currentTimeMillis());
                bowCharges.put(otherPlayer.getUuid(), chargeInfo);

                if (useTime == 1) { // First tick of charging
                    LOGGER.info("ArrowDodge: Player {} started charging bow", otherPlayer.getName().getString());
                }
            } else {
                BowChargeInfo prevCharge = bowCharges.remove(otherPlayer.getUuid());
                if (prevCharge != null && prevCharge.chargeTime > 5) {
                    LOGGER.info("ArrowDodge: Player {} released bow with charge {}, predicting arrow",
                            otherPlayer.getName().getString(), prevCharge.chargeTime);
                    // Bow was just released - predict arrow
                    predictIncomingArrow(otherPlayer, prevCharge, client.player);
                }
            }
        }

        if (bowsFound > 0 && tickCounter % 20 == 0) {
            LOGGER.debug("ArrowDodge: Found {} players charging bows", bowsFound);
        }
    }

    private void updateArrowTracking(MinecraftClient client, ClientPlayerEntity player) {
        int newArrows = 0;
        int totalArrows = 0;

        // Track existing arrows and update their positions
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof ArrowEntity arrow) {
                totalArrows++;
                UUID arrowUuid = arrow.getUuid();
                Vec3d velocity = arrow.getVelocity();

                // Only track arrows that are actually moving (ignore stuck arrows)
                if (velocity.length() > 0.1) { // Minimum velocity threshold
                    if (!trackedArrows.containsKey(arrowUuid)) {
                        // New arrow detected
                        newArrows++;
                        ArrowInfo info = new ArrowInfo(
                                arrow.getPos(),
                                velocity,
                                System.currentTimeMillis());
                        trackedArrows.put(arrowUuid, info);
                        LOGGER.info("ArrowDodge: New arrow detected at {} with velocity {}",
                                arrow.getPos(), velocity);
                    } else {
                        // Update existing arrow's position and velocity
                        ArrowInfo existingInfo = trackedArrows.get(arrowUuid);
                        ArrowInfo updatedInfo = new ArrowInfo(
                                arrow.getPos(),
                                velocity,
                                existingInfo.spawnTime); // Keep original spawn time
                        trackedArrows.put(arrowUuid, updatedInfo);
                        LOGGER.debug("ArrowDodge: Updated arrow {} position to {} velocity {}",
                                arrowUuid, arrow.getPos(), velocity);
                    }
                } else {
                    LOGGER.debug("ArrowDodge: Ignoring stationary arrow at {} (velocity={})",
                            arrow.getPos(), velocity);
                }
            }
        }

        // Clean up old/removed arrows
        int removedArrows = 0;
        int initialSize = trackedArrows.size();

        // Create set of existing arrow UUIDs for efficient lookup
        Set<UUID> existingArrowUUIDs = new HashSet<>();
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof ArrowEntity) {
                existingArrowUUIDs.add(entity.getUuid());
            }
        }

        trackedArrows.entrySet().removeIf(entry -> {
            UUID arrowUuid = entry.getKey();
            ArrowInfo arrowInfo = entry.getValue();

            // Remove if arrow no longer exists in world or is too old
            boolean arrowGone = !existingArrowUUIDs.contains(arrowUuid);
            boolean tooOld = System.currentTimeMillis() - arrowInfo.spawnTime > 10000;

            // Also remove arrows that have become stationary (stuck in blocks/ground)
            boolean stationary = arrowInfo.velocity.length() < 0.1;

            boolean shouldRemove = arrowGone || tooOld || stationary;

            if (shouldRemove) {
                // Also remove from dodged arrows set and threatening arrows
                dodgedArrows.remove(arrowUuid);
                if (ThreatArrowData.isArrowThreatening(arrowUuid)) {
                    ThreatArrowData.removeThreateningArrow(arrowUuid);
                    LOGGER.info("ArrowDodge: Arrow {} removed from threatening list - no longer glowing", arrowUuid);
                }

                if (arrowGone) {
                    LOGGER.debug("ArrowDodge: Removing arrow {} - no longer in world", arrowUuid);
                } else if (tooOld) {
                    LOGGER.debug("ArrowDodge: Removing arrow {} - too old", arrowUuid);
                } else if (stationary) {
                    LOGGER.debug("ArrowDodge: Removing arrow {} - stationary/stuck", arrowUuid);
                }
            }

            return shouldRemove;
        });
        removedArrows = initialSize - trackedArrows.size();

        if (newArrows > 0) {
            LOGGER.info("ArrowDodge: Tracking {} new arrows (total world arrows: {})", newArrows, totalArrows);
        }
        if (removedArrows > 0) {
            LOGGER.debug("ArrowDodge: Removed {} old/invalid arrows", removedArrows);
        }
    }

    private void predictIncomingArrow(AbstractClientPlayerEntity shooter,
            BowChargeInfo chargeInfo,
            ClientPlayerEntity target) {
        // Calculate arrow velocity based on bow charge
        float chargePercent = Math.min(chargeInfo.chargeTime / 20.0f, 1.0f);
        float velocity = chargePercent * 3.0f; // Max bow velocity is ~3.0

        // Calculate direction from shooter to target
        Vec3d shooterPos = chargeInfo.shooterPos.add(0, shooter.getEyeHeight(shooter.getPose()), 0);
        Vec3d targetPos = target.getPos().add(0, target.getEyeHeight(target.getPose()), 0);
        Vec3d direction = targetPos.subtract(shooterPos).normalize();

        LOGGER.info("ArrowDodge: Predicting arrow - charge={}%, velocity={}, direction={}",
                (int) (chargePercent * 100), velocity, direction);

        // Create predicted arrow
        ArrowInfo predictedArrow = new ArrowInfo(
                shooterPos,
                direction.multiply(velocity),
                System.currentTimeMillis());

        // Add to tracking with special UUID
        UUID predictedId = UUID.nameUUIDFromBytes(
                ("predicted_" + shooter.getUuid().toString()).getBytes());
        trackedArrows.put(predictedId, predictedArrow);

        LOGGER.info("ArrowDodge: Added predicted arrow to tracking");
    }

    private void calculateDodge(ClientPlayerEntity player) {
        if (dodgeTicks > 0) {
            if (tickCounter % 5 == 0) {
                LOGGER.debug("ArrowDodge: Already dodging, {} ticks remaining", dodgeTicks);
            }
            return; // Already dodging
        }

        Box playerBox = player.getBoundingBox();
        Vec3d playerPos = player.getPos();
        int threatsChecked = 0;

        for (Map.Entry<UUID, ArrowInfo> entry : trackedArrows.entrySet()) {
            UUID arrowUuid = entry.getKey();
            ArrowInfo arrow = entry.getValue();

            // Skip arrows we've already dodged
            if (dodgedArrows.contains(arrowUuid)) {
                LOGGER.debug("ArrowDodge: Skipping arrow {} - already dodged", arrowUuid);
                continue;
            }

            threatsChecked++;
            DodgeCalculation calc = calculateArrowThreat(arrow, playerPos, playerBox);

            LOGGER.debug("ArrowDodge: Threat analysis - willHit={}, timeToImpact={}, arrowPos={}",
                    calc.willHit, calc.timeToImpact, arrow.position);

            if (!calc.willHit && calc.timeToImpact == 0 && calc.impactPoint == null) {
                // This indicates the path was obstructed - add specific logging
                LOGGER.debug("ArrowDodge: No threat from arrow {} - path likely obstructed by blocks", arrowUuid);
            }

            if (calc.willHit && calc.timeToImpact > 3) { // Require at least 3 ticks warning for human-like reaction
                LOGGER.warn("ArrowDodge: THREAT DETECTED! Impact in {} ticks at {}",
                        calc.timeToImpact, calc.impactPoint);

                // Mark arrow as threatening for red glow effect
                ThreatArrowData.markArrowAsThreatening(arrowUuid);
                LOGGER.info("ArrowDodge: Arrow {} marked as THREATENING - will glow red!", arrowUuid);

                // Calculate safe dodge direction
                Vec3d safeDirection = calculateSafeDodgeDirection(
                        player, arrow, calc.timeToImpact);

                if (safeDirection != null) {
                    // Mark this arrow as dodged to prevent repeated dodges
                    dodgedArrows.add(arrowUuid);

                    // Calculate more conservative dodge distance for subtlety
                    double dodgeDistance = Math.min(1.5, Math.max(0.8, calc.timeToImpact * 0.08));
                    dodgeTarget = playerPos.add(safeDirection.multiply(dodgeDistance));
                    dodgeTicks = Math.max(3, (int) (calc.timeToImpact * 0.6)); // Shorter, more subtle dodge duration
                    dodgeJustStarted = true; // Mark that dodge just started

                    LOGGER.warn(
                            "ArrowDodge: INITIATING DODGE! Arrow={}, Direction={}, distance={}, target={}, duration={}",
                            arrowUuid, safeDirection, dodgeDistance, dodgeTarget, dodgeTicks);
                    break;
                } else {
                    LOGGER.error("ArrowDodge: NO SAFE DODGE DIRECTION FOUND!");
                }
            }
        }

        if (threatsChecked > 0 && tickCounter % 20 == 0) {
            LOGGER.debug("ArrowDodge: Checked {} threats this tick", threatsChecked);
        }
    }

    private DodgeCalculation calculateArrowThreat(ArrowInfo arrow, Vec3d playerPos, Box playerBox) {
        Vec3d arrowPos = arrow.position;
        Vec3d arrowVel = arrow.velocity;

        // Don't calculate threats for stationary or very slow arrows
        if (arrowVel.length() < 0.1) {
            LOGGER.debug("ArrowDodge: Skipping threat calculation for slow arrow (velocity={})", arrowVel);
            return new DodgeCalculation(false, 0, null);
        }

        // Log initial arrow state
        LOGGER.debug("ArrowDodge: Simulating trajectory from {} with velocity {}", arrowPos, arrowVel);

        // Simulate arrow trajectory with gravity
        double gravity = 0.05; // Minecraft arrow gravity
        double drag = 0.99; // Air resistance

        for (int tick = 1; tick <= 100; tick++) { // Check next 5 seconds
            // Update arrow position with physics
            arrowVel = arrowVel.multiply(drag);
            arrowVel = arrowVel.add(0, -gravity, 0);
            arrowPos = arrowPos.add(arrowVel);

            // Check collision with player
            Box arrowBox = new Box(arrowPos.subtract(0.1, 0.1, 0.1), arrowPos.add(0.1, 0.1, 0.1));

            if (arrowBox.intersects(playerBox.offset(playerPos.subtract(playerPos)))) {
                // Before declaring a threat, check if blocks obstruct the path
                if (isPathObstructed(arrow.position, arrowPos, playerPos)) {
                    LOGGER.debug(
                            "ArrowDodge: Collision detected at tick {} but path is obstructed by blocks - no threat",
                            tick);
                    return new DodgeCalculation(false, 0, null);
                }

                LOGGER.debug("ArrowDodge: Collision detected at tick {} position {} - clear path confirmed", tick,
                        arrowPos);
                return new DodgeCalculation(true, tick, arrowPos);
            }

            // Stop if arrow hits ground or goes too far
            if (arrowPos.y < playerPos.y - 10 || arrowPos.distanceTo(playerPos) > 50) {
                LOGGER.debug("ArrowDodge: Arrow trajectory ended at tick {} (ground/distance)", tick);
                break;
            }
        }

        return new DodgeCalculation(false, 0, null);
    }

    /**
     * Check if blocks obstruct the path between the arrow and the player.
     * Uses raycasting to detect solid blocks that would stop the arrow.
     */
    private boolean isPathObstructed(Vec3d arrowStart, Vec3d arrowCurrent, Vec3d playerPos) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null) {
            return false; // Assume clear if no world context
        }

        // Raycast from current arrow position towards the player center
        Vec3d playerCenter = playerPos.add(0, 0.9, 0); // Player eye height approximately

        // Use Minecraft's built-in raycasting
        RaycastContext raycastContext = new RaycastContext(
                arrowCurrent,
                playerCenter,
                RaycastContext.ShapeType.COLLIDER, // Check collision shapes
                RaycastContext.FluidHandling.NONE, // Ignore fluids
                player);

        BlockHitResult hitResult = client.world.raycast(raycastContext);

        // If we hit a block before reaching the player, path is obstructed
        boolean isObstructed = hitResult.getType() == HitResult.Type.BLOCK;

        if (isObstructed) {
            LOGGER.debug("ArrowDodge: Path obstructed by block at {} between arrow {} and player {}",
                    hitResult.getPos(), arrowCurrent, playerCenter);
        } else {
            LOGGER.debug("ArrowDodge: Clear path from arrow {} to player {}", arrowCurrent, playerCenter);
        }

        return isObstructed;
    }

    private Vec3d calculateSafeDodgeDirection(ClientPlayerEntity player, ArrowInfo arrow, int timeToImpact) {
        Vec3d arrowDirection = arrow.velocity.normalize();

        // Calculate perpendicular directions
        Vec3d right = new Vec3d(-arrowDirection.z, 0, arrowDirection.x).normalize();
        Vec3d left = right.multiply(-1);
        Vec3d back = arrowDirection.multiply(-1);

        // Test dodge directions in order of preference
        Vec3d[] directions = { right, left, back };
        String[] dirNames = { "right", "left", "back" };

        for (int i = 0; i < directions.length; i++) {
            Vec3d direction = directions[i];
            boolean safe = isSafeDodgeDirection(player, direction, timeToImpact);
            LOGGER.debug("ArrowDodge: Testing {} direction: {}, safe={}",
                    dirNames[i], direction, safe);

            if (safe) {
                LOGGER.info("ArrowDodge: Selected {} direction for dodge", dirNames[i]);
                return direction;
            }
        }

        LOGGER.warn("ArrowDodge: No safe direction found after testing all options");
        return null; // No safe direction found
    }

    private boolean isSafeDodgeDirection(ClientPlayerEntity player, Vec3d direction, int timeToImpact) {
        Vec3d testPos = player.getPos().add(direction.multiply(2.0));

        // Check if position is safe (no blocks, no void, etc.)
        BlockPos blockPos = new BlockPos((int) testPos.x, (int) testPos.y, (int) testPos.z);

        boolean airAbove = player.getWorld().isAir(blockPos);
        boolean airHead = player.getWorld().isAir(blockPos.up());
        boolean groundBelow = !player.getWorld().isAir(blockPos.down());
        boolean aboveVoid = testPos.y > player.getWorld().getBottomY();

        boolean safe = airAbove && airHead && groundBelow && aboveVoid;

        LOGGER.debug("ArrowDodge: Safety check at {} - air={}, head={}, ground={}, void={}, safe={}",
                blockPos, airAbove, airHead, groundBelow, aboveVoid, safe);

        return safe;
    }

    private void executeDodge(ClientPlayerEntity player) {
        if (dodgeTicks <= 0 || dodgeTarget == null) {
            // Reset dodge state when finished
            if (dodgeTarget != null) {
                LOGGER.info("ArrowDodge: Dodge complete");
                dodgeTarget = null;
                dodgeJustStarted = false;
            }
            return;
        }

        Vec3d currentPos = player.getPos();
        Vec3d direction = dodgeTarget.subtract(currentPos).normalize();

        // Apply dodge as a single impulse only when dodge just started
        if (dodgeJustStarted) {
            double dodgeSpeed = 0.35; // More subtle movement speed
            Vec3d dodgeVelocity = direction.multiply(dodgeSpeed);

            // Add very slight upward component to help with movement
            dodgeVelocity = dodgeVelocity.add(0, 0.03, 0);

            // Set the velocity once as an impulse
            player.setVelocity(dodgeVelocity);
            dodgeJustStarted = false; // Only apply impulse once

            LOGGER.info("ArrowDodge: Applied subtle dodge impulse - direction={}, velocity={}", direction,
                    dodgeVelocity);
        }
        // No continuous velocity adjustments - let physics handle the rest

        if (dodgeTicks == 1 || dodgeTicks % 5 == 0) {
            LOGGER.info("ArrowDodge: Dodge tick {} remaining, current velocity={}",
                    dodgeTicks, player.getVelocity());
        }

        dodgeTicks--;

        if (dodgeTicks <= 0) {
            LOGGER.info("ArrowDodge: Dodge complete");
            dodgeTarget = null;
            dodgeJustStarted = false;
        }
    }
}
