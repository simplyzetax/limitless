package me.simplyzetax.limitless.client.util;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.LimitlessClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CreativeScreenManager {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    // Cache reflection fields/methods
    private static Field selectedTabField;
    private static Method setSelectedTabMethod;
    private static boolean reflectionInitialized = false;

    /**
     * Refreshes the creative inventory screen if it's currently open.
     */
    public static void refreshCreativeInventoryIfOpen() {
        if (shouldRefreshCreativeScreen()) {
            updateCreativeInventoryScreen();
        }
    }

    /**
     * Refreshes the creative inventory screen for a specific item group.
     */
    public static void refreshCreativeInventoryForGroup(ItemGroup targetGroup) {
        Limitless.LOGGER.info("DEBUG: CreativeScreenManager - refreshCreativeInventoryForGroup called for: {}",
                targetGroup);

        boolean shouldRefresh = shouldRefreshCreativeScreen();
        Limitless.LOGGER.info("DEBUG: CreativeScreenManager - shouldRefreshCreativeScreen: {}", shouldRefresh);

        if (shouldRefresh) {
            boolean isShowingTarget = isShowingTargetGroup(targetGroup);
            Limitless.LOGGER.info("DEBUG: CreativeScreenManager - isShowingTargetGroup: {}", isShowingTarget);

            if (isShowingTarget) {
                Limitless.LOGGER.info("DEBUG: CreativeScreenManager - Calling refreshCreativeInventory");
                updateCreativeInventoryScreen();
            } else {
                Limitless.LOGGER.info("DEBUG: CreativeScreenManager - Not showing target group, skipping refresh");
            }
        } else {
            Limitless.LOGGER.info("DEBUG: CreativeScreenManager - Should not refresh creative screen");
        }
    }

    /**
     * Checks if we should refresh the creative screen.
     */
    private static boolean shouldRefreshCreativeScreen() {
        boolean isCreativeScreen = client.currentScreen instanceof CreativeInventoryScreen;
        boolean hasPlayer = client.player != null;
        boolean hasWorld = client.world != null;

        Limitless.LOGGER.info("DEBUG: CreativeScreenManager - isCreativeScreen: {}, hasPlayer: {}, hasWorld: {}",
                isCreativeScreen, hasPlayer, hasWorld);

        return isCreativeScreen && hasPlayer && hasWorld;
    }

    /**
     * Checks if the creative screen is showing our target group using reflection.
     */
    private static boolean isShowingTargetGroup(ItemGroup targetGroup) {
        if (!(client.currentScreen instanceof CreativeInventoryScreen creativeScreen)) {
            return false;
        }

        try {
            initializeReflection();
            if (selectedTabField != null) {
                ItemGroup currentTab = (ItemGroup) selectedTabField.get(creativeScreen);
                return currentTab == targetGroup;
            }
        } catch (Exception e) {
            // If reflection fails, just refresh anyway to be safe
            Limitless.LOGGER.debug("Could not determine current creative tab via reflection", e);
        }

        return true; // Default to refreshing if we can't determine
    }

    /**
     * Gets the current tab using reflection.
     */
    private static ItemGroup getCurrentTab() {
        try {
            if (client.currentScreen instanceof CreativeInventoryScreen creativeScreen) {
                initializeReflection();
                if (selectedTabField != null) {
                    return (ItemGroup) selectedTabField.get(creativeScreen);
                }
            }
        } catch (Exception e) {
            Limitless.LOGGER.debug("Could not get current tab via reflection", e);
        }
        return null;
    }

    /**
     * Restores the selected tab using reflection.
     */
    private static void restoreSelectedTab(CreativeInventoryScreen screen, ItemGroup tab) {
        try {
            initializeReflection();
            if (setSelectedTabMethod != null) {
                setSelectedTabMethod.invoke(screen, tab);
            } else if (selectedTabField != null) {
                selectedTabField.set(screen, tab);
            }
        } catch (Exception e) {
            Limitless.LOGGER.debug("Could not restore tab via reflection", e);
        }
    }

    /**
     * Initializes reflection fields and methods.
     */
    private static void initializeReflection() {
        if (reflectionInitialized) {
            return;
        }

        try {
            Class<?> creativeScreenClass = CreativeInventoryScreen.class;

            // Try to find the selected tab field
            // Common field names in different mappings
            String[] possibleFieldNames = { "selectedTab", "field_2897", "f_98593_", "currentTab" };

            for (String fieldName : possibleFieldNames) {
                try {
                    selectedTabField = creativeScreenClass.getDeclaredField(fieldName);
                    selectedTabField.setAccessible(true);
                    break;
                } catch (NoSuchFieldException ignored) {
                    // Try next field name
                }
            }

            // Try to find the setSelectedTab method
            String[] possibleMethodNames = { "setSelectedTab", "method_2474", "m_98644_", "selectTab" };

            for (String methodName : possibleMethodNames) {
                try {
                    setSelectedTabMethod = creativeScreenClass.getDeclaredMethod(methodName, ItemGroup.class);
                    setSelectedTabMethod.setAccessible(true);
                    break;
                } catch (NoSuchMethodException ignored) {
                    // Try next method name
                }
            }

        } catch (Exception e) {
            Limitless.LOGGER.debug("Failed to initialize reflection for CreativeInventoryScreen", e);
        }

        reflectionInitialized = true;
    }

    /**
     * Schedules a refresh on the next client tick to avoid threading issues.
     */
    public static void scheduleRefresh() {
        if (client.player != null) {
            client.execute(() -> {
                try {
                    refreshCreativeInventoryIfOpen();
                } catch (Exception e) {
                    Limitless.LOGGER.error("Failed to execute scheduled refresh", e);
                }
            });
        }
    }

    /**
     * Schedules a refresh for a specific item group.
     */
    public static void scheduleRefreshForGroup(ItemGroup targetGroup) {
        if (client.player != null) {
            client.execute(() -> {
                try {
                    refreshCreativeInventoryForGroup(targetGroup);
                } catch (Exception e) {
                    Limitless.LOGGER.error("Failed to execute scheduled refresh for group", e);
                }
            });
        }
    }

    public static void updateCreativeInventoryScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null)
            return;

        // Always use all enabled features for maximum functionality

        // Store the currently selected tab before refreshing
        ItemGroup currentTab = getCurrentTab();

        // Create and set a new creative screen - don't set to null afterward
        client.setScreen(null); // Temporary to clear old screen
        client.setScreen(new CreativeInventoryScreen(player, LimitlessClient.enabledFeatures, true));

        // If we had a tab selected, try to restore it
        if (currentTab != null && client.currentScreen instanceof CreativeInventoryScreen) {
            restoreSelectedTab((CreativeInventoryScreen) client.currentScreen, currentTab);
        }
    }
}