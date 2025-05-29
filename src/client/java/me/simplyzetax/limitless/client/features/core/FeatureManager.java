package me.simplyzetax.limitless.client.features.core;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import me.simplyzetax.limitless.client.features.itemstealing.managers.LimitlessItemGroupManager;
import me.simplyzetax.limitless.client.features.shulkerboxes.managers.ShulkerBoxItemGroupManager;
import me.simplyzetax.limitless.client.features.bowtrajectory.render.BowTrajectoryRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the initialization and lifecycle of all feature modules.
 */
public class FeatureManager {
    private static final List<FeatureModule> features = new ArrayList<>();

    /**
     * Initialize all feature modules.
     */
    public static void initializeAllFeatures() {
        Limitless.LOGGER.info("Initializing all features...");

        // Register all features
        registerFeatures();

        // Initialize each enabled feature
        for (FeatureModule feature : features) {
            if (feature.isEnabled()) {
                try {
                    feature.initialize();
                    Limitless.LOGGER.info("Initialized feature: {}", feature.getFeatureName());
                } catch (Exception e) {
                    Limitless.LOGGER.error("Failed to initialize feature: {}", feature.getFeatureName(), e);
                }
            } else {
                Limitless.LOGGER.info("Skipped disabled feature: {}", feature.getFeatureName());
            }
        }

        Limitless.LOGGER.info("Feature initialization complete!");
    }

    /**
     * Register all available features.
     */
    private static void registerFeatures() {
        // Item Stealing Feature
        features.add(new FeatureModule() {
            @Override
            public void initialize() {
                LimitlessItemGroupManager.initialize();
            }

            @Override
            public String getFeatureName() {
                return "Item Stealing System";
            }

            @Override
            public boolean isEnabled() {
                return ClientConfig.EnableItemStealingFeature;
            }
        });

        // Shulker Box Feature
        features.add(new FeatureModule() {
            @Override
            public void initialize() {
                ShulkerBoxItemGroupManager.initialize();
            }

            @Override
            public String getFeatureName() {
                return "Shulker Box Integration";
            }

            @Override
            public boolean isEnabled() {
                return ClientConfig.EnableShulkerBoxFeature;
            }
        });

        // Bow Trajectory Feature
        features.add(new FeatureModule() {
            @Override
            public void initialize() {
                BowTrajectoryRenderer.register();
            }

            @Override
            public String getFeatureName() {
                return "Bow Trajectory Visualization";
            }

            @Override
            public boolean isEnabled() {
                return ClientConfig.EnableBowTrajectoryFeature;
            }
        });
    }

    /**
     * Cleanup all features.
     */
    public static void cleanupAllFeatures() {
        Limitless.LOGGER.info("Cleaning up all features...");

        for (FeatureModule feature : features) {
            try {
                feature.cleanup();
                Limitless.LOGGER.info("Cleaned up feature: {}", feature.getFeatureName());
            } catch (Exception e) {
                Limitless.LOGGER.error("Failed to cleanup feature: {}", feature.getFeatureName(), e);
            }
        }

        features.clear();
        Limitless.LOGGER.info("Feature cleanup complete!");
    }

    /**
     * Get all registered features.
     */
    public static List<FeatureModule> getFeatures() {
        return new ArrayList<>(features);
    }
}
