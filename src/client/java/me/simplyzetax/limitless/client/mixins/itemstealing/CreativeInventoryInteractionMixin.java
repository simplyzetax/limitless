package me.simplyzetax.limitless.client.mixins.itemstealing;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.features.gui.util.CreativeScreenManager;
import me.simplyzetax.limitless.client.features.itemstealing.util.ItemStackMetadataCleaner;
import me.simplyzetax.limitless.client.features.itemstealing.managers.LimitlessItemGroupManager;
import me.simplyzetax.limitless.client.features.shulkerboxes.managers.ShulkerBoxItemGroupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

/**
 * Combined mixin that handles both key presses and slot clicks in Creative
 * Inventory.
 * Consolidates functionality to prevent mixin conflicts.
 */
@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryInteractionMixin {

    private static Field selectedTabField;
    private static Field focusedSlotField;
    private static boolean fieldInitialized = false;

    // Handle key presses (Q key for item removal)
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        // Check if Q key was pressed
        if (keyCode == GLFW.GLFW_KEY_Q) {
            CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;

            try {
                // Get current tab
                ItemGroup currentTab = getCurrentTab(screen);
                if (currentTab == null) {
                    return;
                }

                // Only handle our custom tabs
                if (currentTab != LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP
                        && currentTab != ShulkerBoxItemGroupManager.SHULKER_BOX_ITEM_GROUP) {
                    return;
                }

                // Get the currently focused slot
                Slot focusedSlot = getFocusedSlot(screen);
                if (focusedSlot != null && focusedSlot.hasStack()) {
                    ItemStack itemToRemove = focusedSlot.getStack().copy();
                    boolean wasRemoved = false;

                    // Remove from the appropriate item group
                    if (currentTab == LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP) {
                        wasRemoved = removeFromLimitlessGroup(itemToRemove);
                    } else if (currentTab == ShulkerBoxItemGroupManager.SHULKER_BOX_ITEM_GROUP) {
                        wasRemoved = removeFromShulkerBoxGroup(itemToRemove);
                    }

                    if (wasRemoved) {
                        Limitless.LOGGER.info("Removed item via Q key: {}", itemToRemove.getName().getString());

                        // Schedule a creative inventory refresh
                        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient
                                .getInstance();
                        client.execute(() -> {
                            net.minecraft.client.gui.screen.Screen currentScreen = client.currentScreen;
                            CreativeScreenManager.refreshCreativeInventoryForGroup(currentTab);
                            if (client.currentScreen == null && currentScreen instanceof CreativeInventoryScreen) {
                                client.setScreen(currentScreen);
                            }
                        });

                        cir.setReturnValue(true); // Consume the key event
                        return;
                    }
                }
            } catch (Exception e) {
                Limitless.LOGGER.error("Error handling Q key press in creative inventory", e);
            }
        }
    }

    // Handle mouse clicks for item metadata cleaning
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void onMouseClickedPre(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        try {
            ItemGroup currentTab = getCurrentTab(screen);
            if (currentTab == LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP) {
                Limitless.LOGGER.debug("Mouse clicked on Limitless tab at ({}, {})", mouseX, mouseY);
            }
        } catch (Exception e) {
            Limitless.LOGGER.debug("Error in mouseClicked pre-processing", e);
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void onMouseClickedPost(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
        try {
            ItemGroup currentTab = getCurrentTab(screen);
            if (currentTab == LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP) {
                PlayerInventory playerInventory = net.minecraft.client.MinecraftClient.getInstance().player
                        .getInventory();
                ItemStackMetadataCleaner.cleanPlayerInventoryItems(playerInventory);
            }
        } catch (Exception e) {
            Limitless.LOGGER.debug("Error in mouseClicked post-processing", e);
        }
    }

    // Private helper methods
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

    private Slot getFocusedSlot(CreativeInventoryScreen screen) {
        try {
            initializeReflection();
            if (focusedSlotField != null) {
                Slot slot = (Slot) focusedSlotField.get(screen);
                if (slot != null && slot.hasStack()) {
                    Limitless.LOGGER.debug("Found focused slot with item: {}",
                            slot.getStack().getItem().getTranslationKey());
                    return slot;
                }
            }
            Limitless.LOGGER.debug("Unable to find focused slot");
            return null;
        } catch (Exception e) {
            Limitless.LOGGER.debug("Could not get focused slot via reflection", e);
            return null;
        }
    }

    private void initializeReflection() {
        if (!fieldInitialized) {
            try {
                Class<?> creativeScreenClass = CreativeInventoryScreen.class;

                // Try different possible field names for the selected tab
                String[] possibleTabFieldNames = { "selectedTab", "field_2897", "f_98593_", "currentTab" };
                String[] possibleSlotFieldNames = { "focusedSlot", "field_2322", "f_96571_", "hoveredSlot" };

                for (String fieldName : possibleTabFieldNames) {
                    try {
                        selectedTabField = creativeScreenClass.getDeclaredField(fieldName);
                        selectedTabField.setAccessible(true);
                        break;
                    } catch (NoSuchFieldException ignored) {
                        // Try next field name
                    }
                }

                // Look in parent class for focused slot
                Class<?> parentClass = creativeScreenClass.getSuperclass();
                for (String fieldName : possibleSlotFieldNames) {
                    try {
                        focusedSlotField = parentClass.getDeclaredField(fieldName);
                        focusedSlotField.setAccessible(true);
                        break;
                    } catch (NoSuchFieldException ignored) {
                        // Try next field name
                    }
                }

                fieldInitialized = true;
                Limitless.LOGGER.debug("Successfully initialized reflection fields for CreativeInventoryScreen");
            } catch (Exception e) {
                Limitless.LOGGER.error("Failed to initialize reflection fields for CreativeInventoryScreen", e);
            }
        }
    }

    private boolean removeFromLimitlessGroup(ItemStack itemStack) {
        return LimitlessItemGroupManager.EQUIPPED_ITEMS
                .removeIf(equippedItem -> areItemStacksEquivalent(equippedItem, itemStack));
    }

    private boolean removeFromShulkerBoxGroup(ItemStack itemStack) {
        return ShulkerBoxItemGroupManager.SHULKER_BOX_ITEMS
                .removeIf(shulkerBoxItem -> areItemStacksEquivalent(shulkerBoxItem, itemStack));
    }

    private boolean areItemStacksEquivalent(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) {
            return false;
        }

        if (ItemStack.areEqual(stack1, stack2)) {
            return true;
        }

        Limitless.LOGGER.debug("ItemStack exact match failed, falling back to item-only comparison for: {}",
                stack1.getItem().getTranslationKey());
        return true; // Match by item type only
    }
}
