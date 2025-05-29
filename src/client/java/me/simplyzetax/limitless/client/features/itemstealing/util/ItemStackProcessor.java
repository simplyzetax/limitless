package me.simplyzetax.limitless.client.features.itemstealing.util;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashSet;
import java.util.Set;

import me.simplyzetax.limitless.Limitless;

public class ItemStackProcessor {
    private final ItemStackKeyGenerator keyGenerator;
    private final ItemStackDisplayFormatter displayFormatter;

    public ItemStackProcessor() {
        this.keyGenerator = new ItemStackKeyGenerator();
        this.displayFormatter = new ItemStackDisplayFormatter();
    }

    /**
     * Processes a collection of ItemStacks for display in the creative tab.
     *
     * @param itemStacks    The ItemStacks to process
     * @param entries       The creative tab entries to add to
     * @param wrapperLookup The registry wrapper lookup
     */
    public void processItemsForDisplay(Set<ItemStack> itemStacks, ItemGroup.Entries entries,
            RegistryWrapper.WrapperLookup wrapperLookup) {
        Set<String> addedKeys = new HashSet<>();

        for (ItemStack itemStack : itemStacks) {
            try {
                addUniqueItemStack(itemStack, addedKeys, entries, wrapperLookup);
            } catch (Exception e) {
                Limitless.LOGGER.error("Error processing ItemStack: {}", itemStack, e);
            }
        }
    }

    /**
     * Adds a unique ItemStack to the creative tab entries.
     */
    private void addUniqueItemStack(ItemStack itemStack, Set<String> addedKeys,
            ItemGroup.Entries entries, RegistryWrapper.WrapperLookup wrapperLookup) {
        String uniqueKey = keyGenerator.generateUniqueKey(itemStack, wrapperLookup);

        if (addedKeys.add(uniqueKey)) {
            ItemStack displayStack = displayFormatter.createDisplayItemStack(itemStack);
            entries.add(displayStack);
        }
    }
}
