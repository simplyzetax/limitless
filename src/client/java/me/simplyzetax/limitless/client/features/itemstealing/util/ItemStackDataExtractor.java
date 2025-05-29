package me.simplyzetax.limitless.client.features.itemstealing.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import me.simplyzetax.limitless.Limitless;

import java.util.Optional;

public class ItemStackDataExtractor {
    private static final String OBTAINED_FROM_KEY = "ObtainedFrom";
    private static final String OBTAINED_TIME_KEY = "ObtainedTime";

    /**
     * Retrieves the entity name from the CUSTOM_DATA component of the ItemStack.
     *
     * @param stack The ItemStack to retrieve the name from
     * @return The name of the entity wrapped in Optional
     */
    public Optional<Optional<String>> getEntityName(ItemStack stack) {
        return extractStringFromNbt(stack, OBTAINED_FROM_KEY);
    }

    /**
     * Retrieves the obtained time from the CUSTOM_DATA component of the ItemStack.
     *
     * @param stack The ItemStack to retrieve the time from
     * @return The obtained time wrapped in Optional
     */
    public Optional<Optional<String>> getObtainedTime(ItemStack stack) {
        return extractStringFromNbt(stack, OBTAINED_TIME_KEY);
    }

    /**
     * Extracts a string value from the NBT data of an ItemStack.
     *
     * @param stack The ItemStack to extract from
     * @param key   The NBT key to look for
     * @return The string value wrapped in Optional
     */
    private Optional<Optional<String>> extractStringFromNbt(ItemStack stack, String key) {
        @Nullable
        NbtComponent customDataComponent = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (customDataComponent != null) {
            NbtCompound customData = customDataComponent.copyNbt();
            if (customData != null && customData.contains(key)) {
                return Optional.of(customData.getString(key));
            }
        } else {
            Limitless.LOGGER.warn("Missing CUSTOM_DATA component for ItemStack: {}", stack);
        }

        return Optional.empty();
    }
}
