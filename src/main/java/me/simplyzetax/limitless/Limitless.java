package me.simplyzetax.limitless;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Limitless implements ModInitializer {
    private static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<ItemStack> EQUIPPED_ITEMS = new LinkedHashSet<>();
    public static ItemGroup LIMITLESS_ITEM_GROUP;

    private static final String OBTAINED_FROM_KEY = "ObtainedFrom";
    private static final String OBTAINED_TIME_KEY = "ObtainedTime";

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Limitless mod...");
        initializeItemGroup();
        registerItemGroup();
        LOGGER.info("Limitless item group registered successfully!");
    }

    /**
     * Initializes the Limitless item group.
     */
    private void initializeItemGroup() {
        LIMITLESS_ITEM_GROUP = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE))
                .displayName(Text.literal("§9Limitless"))
                .entries((context, entries) -> {
                    if (EQUIPPED_ITEMS.isEmpty()) {
                        entries.add(new ItemStack(Items.BARRIER));
                        return;
                    }

                    Set<String> addedKeys = new HashSet<>();
                    RegistryWrapper.WrapperLookup wrapperLookup = context.lookup();

                    for (ItemStack itemStack : EQUIPPED_ITEMS) {
                        try {
                            addUniqueItemStack(itemStack, addedKeys, entries, wrapperLookup);
                        } catch (Exception e) {
                            LOGGER.error("Error processing ItemStack: {}", itemStack, e);
                        }
                    }
                })
                .build();
    }

    /**
     * Adds a unique ItemStack to the creative tab entries.
     *
     * @param itemStack     The ItemStack to add.
     * @param addedKeys     The set of unique keys to track duplicates.
     * @param entries       The creative tab entries.
     * @param wrapperLookup The registry wrapper lookup.
     */
    private void addUniqueItemStack(ItemStack itemStack, Set<String> addedKeys, ItemGroup.Entries entries,
            RegistryWrapper.WrapperLookup wrapperLookup) {
        // Generate a unique key based on item ID and OBTAINED_FROM component
        String uniqueKey = generateUniqueKey(itemStack, wrapperLookup);

        if (addedKeys.add(uniqueKey)) { // Adds to set and checks if it was not already present
            ItemStack singleItemStack = createSingleItemStackWithTooltip(itemStack);
            entries.add(singleItemStack);
        }
    }

    /**
     * Generates a unique key for an ItemStack based on its registry ID and the
     * obtained entity name.
     *
     * @param itemStack     The ItemStack.
     * @param wrapperLookup The registry wrapper lookup.
     * @return The unique key as a String.
     */
    private String generateUniqueKey(ItemStack itemStack, RegistryWrapper.WrapperLookup wrapperLookup) {
        // Use Registry IDs and OBTAINED_FROM component for more reliable uniqueness
        Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
        String obtainedFrom = getEntityName(itemStack).orElse("Unknown");
        return itemId.toString() + ":" + obtainedFrom;
    }

    /**
     * Creates a single ItemStack with updated lore and display name for the
     * creative tab.
     *
     * @param originalStack The original ItemStack.
     * @return The modified ItemStack for display.
     */
    private ItemStack createSingleItemStackWithTooltip(ItemStack originalStack) {
        ItemStack singleItemStack = originalStack.copy();
        singleItemStack.setCount(1);

        // Retrieve the stored entity and time name from CUSTOM_DATA
        Optional<String> obtainedFromEntity = getEntityName(singleItemStack);
        Optional<String> obtainedTime = getObtainedTime(singleItemStack);

        MutableText displayName = Text.literal(originalStack.getName().getString())
                .styled(style -> style.withColor(0x5090D9).withItalic(false));

        // Update lore with the obtained entity name
        LoreComponent loreComponent = singleItemStack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
        List<Text> lore = new ArrayList<>(loreComponent.lines());

        // Add the "Obtained from:" line
        lore.add(Text.literal("§7Obtained from: ")
                .append(Text.literal(obtainedFromEntity.orElse("Unknown"))
                        .styled(style -> style.withColor(Formatting.GOLD)
                                .withItalic(false))));

        // Add the new "Obtained time:" line
        lore.add(Text.literal("§7Obtained time: ")
                .append(Text.literal(obtainedTime.orElse("Unknown")).styled(style -> style.withColor(Formatting.GOLD)
                        .withItalic(false))));

        lore.add(Text.literal(""));

        lore.add(Text.literal("§7These items will §creset §7on a game restart."));

        // Set the updated lore
        singleItemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));

        // Set the custom display name
        singleItemStack.set(DataComponentTypes.CUSTOM_NAME, displayName);

        return singleItemStack;
    }

    /**
     * Retrieves the entity name from the CUSTOM_DATA component of the ItemStack.
     *
     * @param stack The ItemStack to retrieve the name from.
     * @return The name of the entity, or "Unknown" if not set.
     */
    private Optional<String> getEntityName(ItemStack stack) {
        // Retrieve the CUSTOM_DATA component
        @Nullable
        NbtComponent customDataComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customDataComponent != null) {
            NbtCompound customData = customDataComponent.copyNbt();
            if (customData != null && customData.contains(OBTAINED_FROM_KEY)) {
                return customData.getString(OBTAINED_FROM_KEY);
            }
        } else {
            LOGGER.warn("Missing CUSTOM_DATA component for ItemStack: {}", stack);
        }
        return Optional.ofNullable("Unknown");
    }

    private Optional<String> getObtainedTime(ItemStack stack) {
        // Retrieve the CUSTOM_DATA component
        @Nullable
        NbtComponent customDataComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customDataComponent != null) {
            NbtCompound customData = customDataComponent.copyNbt();
            if (customData != null && customData.contains(OBTAINED_TIME_KEY)) {
                return customData.getString(OBTAINED_TIME_KEY);
            }
        } else {
            LOGGER.warn("Missing CUSTOM_DATA component for ItemStack: {}", stack);
        }
        return Optional.ofNullable("Unknown");
    }

    /**
     * Registers the Limitless item group.
     */
    private void registerItemGroup() {
        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(MOD_ID, "main"),
                LIMITLESS_ITEM_GROUP);
    }
}
