// src/client/java/me/simplyzetax/limitless/client/ItemGroupUtil.java
package me.simplyzetax.limitless.client;

import me.simplyzetax.limitless.stealer.LimitlessItemGroupManager;
import me.simplyzetax.limitless.shulkerbox.ShulkerBoxManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;

public class ItemGroupUtil {

    public static ItemGroup getItemGroupByName(String name) {
        return switch (name) {
            // Vanilla groups
            case "building_blocks" -> Registries.ITEM_GROUP.get(ItemGroups.BUILDING_BLOCKS);
            case "colored_blocks" -> Registries.ITEM_GROUP.get(ItemGroups.COLORED_BLOCKS);
            case "natural" -> Registries.ITEM_GROUP.get(ItemGroups.NATURAL);
            case "functional" -> Registries.ITEM_GROUP.get(ItemGroups.FUNCTIONAL);
            case "redstone" -> Registries.ITEM_GROUP.get(ItemGroups.REDSTONE);
            case "tools" -> Registries.ITEM_GROUP.get(ItemGroups.TOOLS);
            case "combat" -> Registries.ITEM_GROUP.get(ItemGroups.COMBAT);
            case "food" -> Registries.ITEM_GROUP.get(ItemGroups.FOOD_AND_DRINK);
            case "ingredients" -> Registries.ITEM_GROUP.get(ItemGroups.INGREDIENTS);
            case "spawn_eggs" -> Registries.ITEM_GROUP.get(ItemGroups.SPAWN_EGGS);

            // Your custom groups
            case "limitless_main" -> LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP;
            case "limitless_shulker_boxes" -> ShulkerBoxManager.SHULKER_BOX_ITEM_GROUP;

            default -> null;
        };
    }

    /**
     * Gets the string name for an ItemGroup (useful for sending over network)
     */
    public static String getNameForItemGroup(ItemGroup group) {
        RegistryKey<ItemGroup> key = Registries.ITEM_GROUP.getKey(group).orElse(null);

        if (group == LimitlessItemGroupManager.LIMITLESS_ITEM_GROUP)
            return "limitless_main";
        if (group == ShulkerBoxManager.SHULKER_BOX_ITEM_GROUP)
            return "limitless_shulker_boxes";

        if (key == ItemGroups.BUILDING_BLOCKS)
            return "building_blocks";
        if (key == ItemGroups.COLORED_BLOCKS)
            return "colored_blocks";
        if (key == ItemGroups.NATURAL)
            return "natural";
        if (key == ItemGroups.FUNCTIONAL)
            return "functional";
        if (key == ItemGroups.REDSTONE)
            return "redstone";
        if (key == ItemGroups.TOOLS)
            return "tools";
        if (key == ItemGroups.COMBAT)
            return "combat";
        if (key == ItemGroups.INGREDIENTS)
            return "ingredients";
        if (key == ItemGroups.SPAWN_EGGS)
            return "spawn_eggs";

        return "unknown";
    }
}
