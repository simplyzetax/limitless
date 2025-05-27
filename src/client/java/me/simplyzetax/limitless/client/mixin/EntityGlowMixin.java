package me.simplyzetax.limitless.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.simplyzetax.limitless.client.config.ClientConfig;

@Mixin(Entity.class)
public class EntityGlowMixin {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void forcePlayerGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (ClientConfig.PlayersShouldGlow) {
            if ((Object) this instanceof PlayerEntity) {
                MinecraftClient client = MinecraftClient.getInstance();

                if (client != null && client.player != null) {
                    PlayerEntity thisPlayer = (PlayerEntity) (Object) this;

                    // if (!thisPlayer.getUuid().equals(client.player.getUuid())) {
                    // Set up team with color before making glow
                    setupPlayerTeamColor(thisPlayer);
                    cir.setReturnValue(true);
                    // }
                }
            }
        }
    }

    private void setupPlayerTeamColor(PlayerEntity player) {
        if (player.getWorld() != null) {
            Scoreboard scoreboard = player.getWorld().getScoreboard();
            String teamName = "glowTeam_" + player.getUuid().toString().substring(0, 8);

            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.addTeam(teamName);
                // Set the team color - you can change this to any Formatting color
                team.setColor(Formatting.RED); // Try RED, BLUE, GREEN, YELLOW, etc.
                team.setShowFriendlyInvisibles(false);
            }

            // Add player to the colored team
            scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
        }
    }
}
