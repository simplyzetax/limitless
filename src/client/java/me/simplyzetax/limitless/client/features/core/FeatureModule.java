package me.simplyzetax.limitless.client.features.core;

/**
 * Base interface for all feature modules in the Limitless mod.
 * Each feature should implement this interface to provide standardized
 * initialization and cleanup behavior.
 */
public interface FeatureModule {

    /**
     * Initialize the feature module.
     * This method is called during client initialization.
     */
    void initialize();

    /**
     * Get the name of this feature module.
     * 
     * @return A human-readable name for this feature
     */
    String getFeatureName();

    /**
     * Check if this feature is enabled.
     * 
     * @return true if the feature should be active
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Cleanup resources when the feature is disabled or mod is unloaded.
     */
    default void cleanup() {
        // Default implementation does nothing
    }
}
