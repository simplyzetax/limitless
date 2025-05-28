package me.simplyzetax.limitless.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.simplyzetax.limitless.client.config.ClientConfig;
import me.simplyzetax.limitless.client.util.ThreatArrowData;

@Mixin(Entity.class)
public class EntityGlowMixin {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void forceEntityGlowing(CallbackInfoReturnable<Boolean> cir) {
        try {
            Entity entity = (Entity) (Object) this;

            // Handle player glowing
            if (ClientConfig.PlayersShouldGlow && entity instanceof PlayerEntity) {
                MinecraftClient client = MinecraftClient.getInstance();

                if (client != null && client.player != null) {
                    PlayerEntity thisPlayer = (PlayerEntity) entity;

                    // Additional null checks for player properties
                    if (thisPlayer.getUuid() != null && thisPlayer.getWorld() != null) {
                        // Set up team with color before making glow
                        setupPlayerTeamColor(thisPlayer);
                        cir.setReturnValue(true);
                    }
                }
            }
            
            // Handle arrow glowing for threatening arrows
            if (entity instanceof ArrowEntity) {
                ArrowEntity arrow = (ArrowEntity) entity;
                
                // Additional null checks for arrow properties
                if (arrow.getUuid() != null && arrow.getWorld() != null) {
                    // Check if this arrow is marked as threatening
                    if (ThreatArrowData.isArrowThreatening(arrow.getUuid())) {
                        // Set up red team for the arrow
                        setupArrowRedTeam(arrow);
                        cir.setReturnValue(true);
                    }
                }
            }
        } catch (Exception e) {
            // Log any unexpected errors to prevent crashes
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                System.err.println("EntityGlowMixin error: " + e.getMessage());
            }
        }
    }

    private void setupPlayerTeamColor(PlayerEntity player) {
        try {
            if (player != null && player.getWorld() != null && player.getUuid() != null) {
                Scoreboard scoreboard = player.getWorld().getScoreboard();
                if (scoreboard != null) {
                    String teamName = "glowTeam_" + player.getUuid().toString().substring(0, 8);

                    Team team = scoreboard.getTeam(teamName);
                    if (team == null) {
                        team = scoreboard.addTeam(teamName);
                        // Set the team color - you can change this to any Formatting color
                        team.setColor(Formatting.RED); // Try RED, BLUE, GREEN, YELLOW, etc.
                        team.setShowFriendlyInvisibles(false);
                    }

                    // Add player to the colored team
                    String playerName = player.getNameForScoreboard();
                    if (playerName != null) {
                        scoreboard.addScoreHolderToTeam(playerName, team);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting up player team color: " + e.getMessage());
        }
    }

    private void setupArrowRedTeam(ArrowEntity arrow) {
        try {
            if (arrow != null && arrow.getWorld() != null && arrow.getUuid() != null) {
                Scoreboard scoreboard = arrow.getWorld().getScoreboard();
                if (scoreboard != null) {
                    String teamName = "threatArrow_" + arrow.getUuid().toString().substring(0, 8);

                    Team team = scoreboard.getTeam(teamName);
                    if (team == null) {
                        team = scoreboard.addTeam(teamName);
                        // Set the team color to red for threatening arrows
                        team.setColor(Formatting.RED);
                        team.setShowFriendlyInvisibles(false);
                    }

                    // Add arrow to the red team
                    String arrowUuidString = arrow.getUuidAsString();
                    if (arrowUuidString != null) {
                        scoreboard.addScoreHolderToTeam(arrowUuidString, team);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error setting up arrow red team: " + e.getMessage());
        }
    }
}
