package me.simplyzetax.limitless.client.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.simplyzetax.limitless.client.util.ThreatArrowData;

@Mixin(Entity.class)
public class ArrowGlowMixin {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void makeThreateningArrowsGlow(CallbackInfoReturnable<Boolean> cir) {
        // Check if this entity is an ArrowEntity
        if ((Object) this instanceof ArrowEntity) {
            ArrowEntity arrow = (ArrowEntity) (Object) this;
            
            // Check if this arrow is marked as threatening
            if (ThreatArrowData.isArrowThreatening(arrow.getUuid())) {
                // Set up red team for the arrow
                setupArrowRedTeam(arrow);
                cir.setReturnValue(true);
            }
        }
    }

    private void setupArrowRedTeam(ArrowEntity arrow) {
        if (arrow.getWorld() != null) {
            Scoreboard scoreboard = arrow.getWorld().getScoreboard();
            String teamName = "threatArrow_" + arrow.getUuid().toString().substring(0, 8);

            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.addTeam(teamName);
                // Set the team color to red for threatening arrows
                team.setColor(Formatting.RED);
                team.setShowFriendlyInvisibles(false);
            }

            // Add arrow to the red team
            scoreboard.addScoreHolderToTeam(arrow.getUuidAsString(), team);
        }
    }
}
