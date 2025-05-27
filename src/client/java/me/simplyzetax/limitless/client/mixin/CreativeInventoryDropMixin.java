package me.simplyzetax.limitless.client.mixin;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.CreativeScreenManager;
import me.simplyzetax.limitless.stealer.LimitlessItemGroupManager;
import me.simplyzetax.limitless.stealer.ShulkerBoxItemGroupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryDropMixin {

    private static Field selectedTabField;
    private static Field focusedSlotField;
    private static boolean fieldInitialized = false;

    @Inject(method = "keyPressed", at = @At("RETURN"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        // Check if Q key was pressed
        if (keyCode == GLFW.GLFW_KEY_Q) {
            if (handleQKeyPress()) {
                cir.setReturnValue(true); // Set the return value instead of trying to cancel
            }
        }
    }

    private boolean handleQKeyPress() {
        try {
            CreativeInventoryScreen screen = (CreativeInventoryScreen) (Object) this;
            ItemGroup currentTab = getCurrentTab(screen);
            if (currentTab == null)
                return false;

            // Check if we're in one of our custom item groups
            if (currentTab == LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP ||
                    currentTab == ShulkerBoxItemGroupManager.SHULKER_BOX_ITEM_GROUP) {

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

                        // Refresh the creative inventory to reflect changes
                        CreativeScreenManager.scheduleRefreshForGroup(currentTab);
                        return true; // Item was removed from our custom group
                    }
                }
            }
        } catch (Exception e) {
            Limitless.LOGGER.error("Error handling Q key press in creative inventory", e);
        }
        return false; // Let default behavior handle this
    }

    private boolean removeFromLimitlessGroup(ItemStack itemStack) {
        // Try to find and remove a matching item from the equipped items set
        return LimitlessItemGroupManager.EQUIPPED_ITEMS
                .removeIf(equippedItem -> areItemStacksEquivalent(equippedItem, itemStack));
    }

    private boolean removeFromShulkerBoxGroup(ItemStack itemStack) {
        // Try to find and remove a matching item from the shulker box items set
        return ShulkerBoxItemGroupManager.SHULKER_BOX_ITEMS
                .removeIf(shulkerBoxItem -> areItemStacksEquivalent(shulkerBoxItem, itemStack));
    }

    private boolean areItemStacksEquivalent(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) {
            return false;
        }

        // First try exact matching
        if (ItemStack.areEqual(stack1, stack2)) {
            return true;
        }

        // If exact matching fails, try matching just by item type
        // This is helpful when display items might have extra NBT data added for
        // display purposes
        Limitless.LOGGER.debug("ItemStack exact match failed, falling back to item-only comparison for: {}",
                stack1.getItem().getTranslationKey());
        return true; // Match by item type only
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

            // If we couldn't get the focused slot or it's empty, we can try using mouse
            // position
            // but for simplicity, we'll just return null and let the key press through
            Limitless.LOGGER.debug("Unable to find focused slot, falling back to default behavior");
            return null;
        } catch (Exception e) {
            Limitless.LOGGER.debug("Could not get focused slot via reflection", e);
        }
        return null;
    }

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

    private void initializeReflection() {
        if (fieldInitialized) {
            return;
        }

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
        } catch (Exception e) {
            Limitless.LOGGER.debug("Failed to initialize reflection for CreativeInventoryScreen", e);
        }

        fieldInitialized = true;
    }
}
