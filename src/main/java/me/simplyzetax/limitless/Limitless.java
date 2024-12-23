package me.simplyzetax.limitless;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.Component;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Limitless implements ModInitializer {
    private static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<ItemStack> EQUIPPED_ITEMS = new LinkedHashSet<>();
    public static ItemGroup LIMITLESS_ITEM_GROUP;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Limitless mod...");
        initializeItemGroup();
        registerItemGroup();
        LOGGER.info("Limitless item group registered successfully!");
    }

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

    private void addUniqueItemStack(ItemStack itemStack, Set<String> addedKeys, ItemGroup.Entries entries, RegistryWrapper.WrapperLookup wrapperLookup) {
        // Generate a unique key based on item ID and NBT data
        String uniqueKey = generateUniqueKey(itemStack, wrapperLookup);

        if (addedKeys.add(uniqueKey)) { // Adds to set and checks if it was not already present
            ItemStack singleItemStack = createSingleItemStackWithTooltip(itemStack);
            entries.add(singleItemStack);
        }
    }

    private String generateUniqueKey(ItemStack itemStack, RegistryWrapper.WrapperLookup wrapperLookup) {
        // Use Registry IDs for more reliable uniqueness
        Identifier itemId = Registries.ITEM.getId(itemStack.getItem());
        String nbtData = itemStack.toNbt(wrapperLookup).toString();
        return itemId.toString() + ":" + nbtData;
    }

    private ItemStack createSingleItemStackWithTooltip(ItemStack originalStack) {
        ItemStack singleItemStack = originalStack.copy();
        singleItemStack.setCount(1);

        // Adding the display name
        MutableText displayName = Text.literal(originalStack.getName().getString())
                .styled(style -> style.withColor(0x5090D9).withItalic(false)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "help"))); // Add click event to lore


                        // Retrieve existing lore and filter duplicates
        LoreComponent loreComponent = singleItemStack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
        List<Text> lore = new ArrayList<>(loreComponent.lines());

        // Ensure only one "Obtained from:" entry exists
        lore.removeIf(line -> line.getString().startsWith("§7Obtained from:"));
        lore.removeIf(line -> line.getString().startsWith("§7Obtained from:"));
        lore.add(Text.literal("§7Obtained from: ")
                .append(Text.literal(getEntityName(singleItemStack))
                        .styled(style -> style.withColor(0x5090D9))));

        singleItemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        singleItemStack.set(DataComponentTypes.CUSTOM_NAME, displayName);

        return singleItemStack;
    }

    private String getEntityName(ItemStack stack) {
        // Extract the entity name from the lore if present
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                if (line.getString().startsWith("§7Obtained from:")) {
                    return line.getString().replace("§7Obtained from: ", "").trim();
                }
            }
        }
        return "Unknown";
    }

    private void registerItemGroup() {
        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(MOD_ID, "main"), // Keeping Identifier usage as is
                LIMITLESS_ITEM_GROUP
        );
    }
}
