package me.simplyzetax.limitless.client.features.damagenumbers.mixins;

import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import me.simplyzetax.limitless.client.features.damagenumbers.util.DamageNumber;
import me.simplyzetax.limitless.client.features.damagenumbers.util.DamageNumberManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class DamageNumberMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("DamageNumbers");

    @Inject(method = "setHealth", at = @At("HEAD"))
    private void onHealthChanged(float health, CallbackInfo ci) {
        // Only run on client side
        if (!FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)) {
            return;
        }

        if (!ClientConfig.ShowDamageNumbers) {
            return;
        }

        try {
            LivingEntity entity = (LivingEntity) (Object) this;
            float currentHealth = entity.getHealth();
            float newHealth = health;

            // Only show if health actually decreased (took damage)
            if (newHealth < currentHealth) {
                float damage = currentHealth - newHealth;

                // Determine if this might be a critical hit (simple heuristic)
                DamageNumber.DamageType damageType = DamageNumber.DamageType.NORMAL;

                // Check if this entity is a player and if the damage seems unusually high
                // This is a simple heuristic since we don't have access to the damage source
                // here
                if (damage > 6.0f) { // Assume damage > 6 might be critical
                    damageType = DamageNumber.DamageType.CRITICAL;
                }

                // Add damage number
                DamageNumberManager.addDamageNumber(entity, damage, damageType);

                LOGGER.debug("Health change detected: {} damage to {}", damage,
                        entity.getType().getTranslationKey());
            }
            // Check for healing
            else if (newHealth > currentHealth) {
                float healing = newHealth - currentHealth;

                // Add healing number
                DamageNumberManager.addDamageNumber(entity, healing, DamageNumber.DamageType.HEALING);

                LOGGER.debug("Healing detected: {} to {}", healing, entity.getType().getTranslationKey());
            }

        } catch (Exception e) {
            LOGGER.error("Error in health change detection: {}", e.getMessage());
        }
    }
}
