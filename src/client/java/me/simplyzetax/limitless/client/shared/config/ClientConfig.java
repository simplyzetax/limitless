package me.simplyzetax.limitless.client.shared.config;

public class ClientConfig {

    // ================================
    // Feature Enable/Disable Settings
    // ================================
    public static boolean EnableItemStealingFeature = true;
    public static boolean EnableShulkerBoxFeature = true;
    public static boolean EnableGlowingFeature = true;
    public static boolean EnableBowTrajectoryFeature = true;
    public static boolean EnableArrowDodgeFeature = true;
    public static boolean EnableDamageNumbersFeature = true;
    public static boolean EnableGuiFeature = true;

    // ================================
    // Item Stealing Feature Settings
    // ================================
    public static boolean OnlyStealPlayerItems = false;
    public static boolean EnableStealing = true;

    // ================================
    // Glowing Feature Settings
    // ================================
    public static boolean PlayersShouldGlow = false;

    // ================================
    // Bow Trajectory Feature Settings
    // ================================
    public static boolean ShowBowTrajectory = false;

    // ================================
    // Arrow Dodge Feature Settings
    // ================================
    public static boolean EnableArrowDodging = false;

    // ================================
    // Damage Numbers Feature Settings
    // ================================
    public static boolean ShowDamageNumbers = true;
    public static String DamageNumberFont = "default"; // Options: "default", "uniform", "alt", "compact"

    // ================================
    // Zoom Feature Settings
    // ================================
    public static boolean EnableZoom = true;
    public static float ZoomLevel = 3.0f; // Default zoom level (OptiFine-like, higher = more zoomed)
    public static float MinZoomLevel = 1.5f; // Minimum zoom level
    public static float MaxZoomLevel = 10.0f; // Maximum zoom level
    public static float ZoomScrollSensitivity = 0.5f; // How much mouse wheel affects zoom
}
