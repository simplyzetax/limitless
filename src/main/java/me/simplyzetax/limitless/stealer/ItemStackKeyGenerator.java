package me.simplyzetax.limitless.stealer;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class ItemStackKeyGenerator {
    private final ItemStackDataExtractor dataExtractor;

    public ItemStackKeyGenerator() {
        this.dataExtractor = new ItemStackDataExtractor();
    }

    /**
     * Generates a unique key for an ItemStack based on its registry ID and obtained
     * entity name.
     *
     * @param itemStack     The ItemStack
     * @param wrapperLookup The registry wrapper lookup
     * @return The unique key as a String
     */
    public String generateUniqueKey(ItemStack itemStack, RegistryWrapper.WrapperLookup wrapperLookup) {
        Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
        String obtainedFrom = dataExtractor.getEntityName(itemStack).flatMap(x -> x).orElse("Unknown");
        return itemId.toString() + ":" + obtainedFrom;
    }
}
