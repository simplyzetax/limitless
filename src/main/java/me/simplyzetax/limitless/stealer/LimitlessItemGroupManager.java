package me.simplyzetax.limitless.stealer;

import me.simplyzetax.limitless.Limitless;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

public class LimitlessItemGroupManager {
    public static final Set<ItemStack> EQUIPPED_ITEMS = new LinkedHashSet<>();
    public static ItemGroup LIMITLESS_ITEM_GROUP;

    public static void initialize() {
        createItemGroup();
        registerItemGroup();
        Limitless.LOGGER.info("Limitless item group registered successfully!");
    }

    /**
     * Adds an equipped item to the collection.
     * Creative inventory refresh is handled client-side by the mixin.
     */
    public static void addEquippedItem(ItemStack itemStack) {
        boolean wasAdded = EQUIPPED_ITEMS.add(itemStack.copy());

        if (wasAdded) {
            Limitless.LOGGER.info("Added equipped item: {}", itemStack.getName().getString());
        }
    }

    private static void createItemGroup() {
        LIMITLESS_ITEM_GROUP = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.ENDER_EYE))
                .displayName(Text.literal("§dItem Stealer"))
                .entries((context, entries) -> {
                    if (EQUIPPED_ITEMS.isEmpty()) {
                        ItemStack emptyIndicator = new ItemStack(Items.BARRIER);
                        emptyIndicator.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                                Text.literal("§cNo equipped items captured yet"));
                        entries.add(emptyIndicator);
                        return;
                    }

                    ItemStackProcessor processor = new ItemStackProcessor();
                    processor.processItemsForDisplay(EQUIPPED_ITEMS, entries, context.lookup());
                })
                .build();
    }

    private static void registerItemGroup() {
        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(Limitless.MOD_ID, "main"),
                LIMITLESS_ITEM_GROUP);
    }

    /**
     * Clears all equipped items.
     */
    public static void clearEquippedItems() {
        EQUIPPED_ITEMS.clear();
        Limitless.LOGGER.info("Cleared all equipped items");
    }

    /**
     * Removes a specific equipped item.
     */
    public static boolean removeEquippedItem(ItemStack itemStack) {
        boolean wasRemoved = EQUIPPED_ITEMS.remove(itemStack);

        if (wasRemoved) {
            Limitless.LOGGER.info("Removed equipped item: {}", itemStack.getName().getString());
        }

        return wasRemoved;
    }

    /**
     * Gets the count of equipped items.
     */
    public static int getEquippedItemCount() {
        return EQUIPPED_ITEMS.size();
    }

    /**
     * Checks if a specific item is already in the equipped items collection.
     */
    public static boolean containsEquippedItem(ItemStack itemStack) {
        return EQUIPPED_ITEMS.contains(itemStack);
    }
}
