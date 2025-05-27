package me.simplyzetax.limitless.shulkerbox;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class ShulkerBoxDisplayFormatter {

    /**
     * Adds placement information to the shulker box lore.
     */
    public ItemStack addPlacementLore(ItemStack stack, ShulkerBoxData shulkerBoxData) {
        LoreComponent currentLore = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
        List<Text> lore = new ArrayList<>(currentLore.lines());

        // Add separator if there's existing lore
        if (!lore.isEmpty()) {
            lore.add(Text.literal(""));
        }

        // Add placement information
        addPlacementInfo(lore, shulkerBoxData);
        addPositionInfo(lore, shulkerBoxData);
        addTimestampInfo(lore, shulkerBoxData);
        addWarningInfo(lore);

        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return stack;
    }

    private void addPlacementInfo(List<Text> lore, ShulkerBoxData shulkerBoxData) {
        lore.add(Text.literal("§7Placed by: ")
                .append(Text.literal(shulkerBoxData.getPlacedBy())
                        .styled(style -> style.withColor(Formatting.AQUA).withItalic(false))));
    }

    private void addPositionInfo(List<Text> lore, ShulkerBoxData shulkerBoxData) {
        lore.add(Text.literal("§7Position: ")
                .append(Text.literal(String.format("X:%d Y:%d Z:%d",
                        shulkerBoxData.getPosition().getX(),
                        shulkerBoxData.getPosition().getY(),
                        shulkerBoxData.getPosition().getZ()))
                        .styled(style -> style.withColor(Formatting.GREEN).withItalic(false))));
    }

    private void addTimestampInfo(List<Text> lore, ShulkerBoxData shulkerBoxData) {
        lore.add(Text.literal("§7Captured: ")
                .append(Text.literal(shulkerBoxData.getTimestamp())
                        .styled(style -> style.withColor(Formatting.YELLOW).withItalic(false))));
    }

    private void addWarningInfo(List<Text> lore) {
        lore.add(Text.literal(""));
        lore.add(Text.literal("§7This is a §ccopy §7of the original shulker box."));
        lore.add(Text.literal("§7Contents will §creset §7on game restart."));
    }
}
