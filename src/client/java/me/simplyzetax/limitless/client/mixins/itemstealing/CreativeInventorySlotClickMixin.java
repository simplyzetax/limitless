package me.simplyzetax.limitless.client.mixins.itemstealing;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.features.itemstealing.util.ItemStackMetadataCleaner;
import me.simplyzetax.limitless.client.features.itemstealing.managers.LimitlessItemGroupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

/**
 * A mixin that intercepts slot click events in the Creative Inventory Screen.
 * Used to clean item metadata when items are moved from Limitless tab to player
 * inventory.
 */
@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public class CreativeInventorySlotClickMixin {

    private static Field selectedTabField;
    private static boolean fieldInitialized = false;

    /**
     * Inject into the mouseClicked method to intercept mouse clicks in the creative
     * inventory.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        try {
            CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
            ItemGroup currentTab = getCurrentTab(screen);

            // Only proceed if we're in the Limitless tab
            if (currentTab != LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP) {
                return;
            }

            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
            ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();

            // If player has an item on cursor from our tab, prepare to clean it
            if (!cursorStack.isEmpty()) {
                Limitless.LOGGER.debug("Player clicking with item on cursor from Limitless tab: {}",
                        cursorStack.getName().getString());
            }
        } catch (Exception e) {
            Limitless.LOGGER.error("Error handling mouse click in creative inventory", e);
        }
    }

    /**
     * Inject into the mouseClicked method after processing to clean items that were
     * placed.
     */
    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void afterMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        try {
            CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
            ItemGroup currentTab = getCurrentTab(screen);

            // Only proceed if we're in the Limitless tab
            if (currentTab != LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP) {
                return;
            }

            net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();

            // Check all player inventory slots for items that need cleaning
            PlayerInventory playerInventory = client.player.getInventory();
            for (int i = 0; i < playerInventory.size(); i++) {
                ItemStack stack = playerInventory.getStack(i);
                if (!stack.isEmpty()) {
                    ItemStack cleanedStack = ItemStackMetadataCleaner.cleanItemMetadata(stack.copy());
                    if (cleanedStack != stack) { // If the stack was actually cleaned
                        playerInventory.setStack(i, cleanedStack);
                        Limitless.LOGGER.info("Cleaned item in player inventory slot {}: {}", i,
                                cleanedStack.getName().getString());
                    }
                }
            }

            // Also clean cursor stack
            ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
            if (!cursorStack.isEmpty()) {
                ItemStack cleanedCursorStack = ItemStackMetadataCleaner.cleanItemMetadata(cursorStack.copy());
                if (cleanedCursorStack != cursorStack) {
                    client.player.currentScreenHandler.setCursorStack(cleanedCursorStack);
                    Limitless.LOGGER.info("Cleaned cursor stack: {}", cleanedCursorStack.getName().getString());
                }
            }

        } catch (Exception e) {
            Limitless.LOGGER.error("Error handling post-click cleanup", e);
        }
    }

    /**
     * Gets the current tab using reflection.
     */
    private ItemGroup getCurrentTab(CreativeInventoryScreen screen) {
        try {
            initializeReflection();
            if (selectedTabField != null) {
                return (ItemGroup) selectedTabField.get(screen);
            }
        } catch (Exception e) {
            Limitless.LOGGER.debug("Could not get current tab via reflection", e);
        }
        return null;
    }

    /**
     * Initializes reflection fields.
     */
    private void initializeReflection() {
        if (fieldInitialized) {
            return;
        }

        try {
            Class<?> creativeScreenClass = CreativeInventoryScreen.class;

            // Try different possible field names for the selected tab
            String[] possibleTabFieldNames = { "selectedTab", "field_2897", "f_98593_", "currentTab" };

            for (String fieldName : possibleTabFieldNames) {
                try {
                    selectedTabField = creativeScreenClass.getDeclaredField(fieldName);
                    selectedTabField.setAccessible(true);
                    break;
                } catch (NoSuchFieldException ignored) {
                    // Try next field name
                }
            }
        } catch (Exception e) {
            Limitless.LOGGER.debug("Failed to initialize reflection for CreativeInventoryScreen", e);
        }

        fieldInitialized = true;
    }
}
