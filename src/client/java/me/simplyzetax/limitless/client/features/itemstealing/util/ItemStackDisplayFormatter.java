package me.simplyzetax.limitless.client.features.itemstealing.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemStackDisplayFormatter {
    private final ItemStackDataExtractor dataExtractor;

    public ItemStackDisplayFormatter() {
        this.dataExtractor = new ItemStackDataExtractor();
    }

    /**
     * Creates a single ItemStack with updated lore and display name for the
     * creative tab.
     *
     * @param originalStack The original ItemStack
     * @return The modified ItemStack for display
     */
    public ItemStack createDisplayItemStack(ItemStack originalStack) {
        ItemStack displayStack = originalStack.copy();
        displayStack.setCount(1);

        setCustomDisplayName(displayStack, originalStack);
        setCustomLore(displayStack);

        // Store the original item for later retrieval
        OriginalItemStorage.storeOriginalItem(originalStack, displayStack);

        return displayStack;
    }

    /**
     * Sets a custom display name for the ItemStack.
     */
    private void setCustomDisplayName(ItemStack stack, ItemStack originalStack) {
        MutableText displayName = Text.literal(originalStack.getName().getString())
                .styled(style -> style.withColor(0x5090D9).withItalic(false));

        stack.set(DataComponentTypes.CUSTOM_NAME, displayName);
    }

    /**
     * Sets custom lore for the ItemStack with obtained information.
     */
    private void setCustomLore(ItemStack stack) {
        Optional<String> obtainedFromEntity = dataExtractor.getEntityName(stack).flatMap(x -> x);
        Optional<String> obtainedTime = dataExtractor.getObtainedTime(stack).flatMap(x -> x);

        LoreComponent currentLore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
        List<Text> lore = new ArrayList<>(currentLore.lines());

        addObtainedFromLine(lore, obtainedFromEntity);
        addObtainedTimeLine(lore, obtainedTime);
        addWarningLines(lore);

        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
    }

    /**
     * Adds the "Obtained from:" line to the lore.
     */
    private void addObtainedFromLine(List<Text> lore, Optional<String> obtainedFromEntity) {
        lore.add(Text.literal("§7Obtained from: ")
                .append(Text.literal(obtainedFromEntity.orElse("Unknown"))
                        .styled(style -> style.withColor(Formatting.GOLD).withItalic(false))));
    }

    /**
     * Adds the "Obtained time:" line to the lore.
     */
    private void addObtainedTimeLine(List<Text> lore, Optional<String> obtainedTime) {
        lore.add(Text.literal("§7Obtained time: ")
                .append(Text.literal(obtainedTime.orElse("Unknown"))
                        .styled(style -> style.withColor(Formatting.GOLD).withItalic(false))));
    }

    /**
     * Adds warning lines to the lore.
     */
    private void addWarningLines(List<Text> lore) {
        lore.add(Text.literal(""));
        lore.add(Text.literal("§7These items will §creset §7on a game restart."));
    }
}
