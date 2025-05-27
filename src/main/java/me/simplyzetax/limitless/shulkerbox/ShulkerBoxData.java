package me.simplyzetax.limitless.shulkerbox;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ShulkerBoxData {
    private final ItemStack shulkerBoxStack;
    private final String placedBy;
    private final BlockPos position;
    private final String timestamp;
    private final ContainerComponent contents;

    public ShulkerBoxData(ItemStack shulkerBoxStack, String placedBy, BlockPos position, ContainerComponent contents) {
        this.shulkerBoxStack = shulkerBoxStack.copy();
        this.placedBy = placedBy;
        this.position = position;
        this.contents = contents;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ItemStack getShulkerBoxStack() {
        return shulkerBoxStack.copy();
    }

    public String getPlacedBy() {
        return placedBy;
    }

    public BlockPos getPosition() {
        return position;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ContainerComponent getContents() {
        return contents;
    }

    /**
     * Creates a display ItemStack for the creative tab with enhanced tooltips.
     */
    public ItemStack createDisplayStack() {
        ItemStack displayStack = shulkerBoxStack.copy();
        displayStack.setCount(1);

        // Preserve the original contents
        if (contents != null) {
            displayStack.set(DataComponentTypes.CONTAINER, contents);
        }

        // Add custom name with position info
        Text customName = Text.literal("§b" + getShulkerBoxName() + " §7[" + position.getX() + ", " + position.getY()
                + ", " + position.getZ() + "]");
        displayStack.set(DataComponentTypes.CUSTOM_NAME, customName);

        return displayStack;
    }

    private String getShulkerBoxName() {
        return shulkerBoxStack.getName().getString();
    }

    public String getUniqueKey() {
        return placedBy + "_" + position.toString() + "_" + timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ShulkerBoxData that = (ShulkerBoxData) obj;
        return Objects.equals(getUniqueKey(), that.getUniqueKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUniqueKey());
    }
}
