package me.simplyzetax.limitless.shulkerbox;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.network.ServerNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import me.simplyzetax.limitless.network.RefreshCreativePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

import java.util.LinkedHashSet;
import java.util.Set;

public class ShulkerBoxManager {
    public static final Set<ShulkerBoxData> PLACED_SHULKER_BOXES = new LinkedHashSet<>();
    public static ItemGroup SHULKER_BOX_ITEM_GROUP;

    public static void initialize() {
        createShulkerBoxItemGroup();
        registerShulkerBoxItemGroup();
        Limitless.LOGGER.info("Shulker box item group registered successfully!");
    }

    public static void debugPrintShulkerBoxes() {
        Limitless.LOGGER.info("DEBUG: ShulkerBoxManager - Current shulker box count: {}", PLACED_SHULKER_BOXES.size());
        for (ShulkerBoxData box : PLACED_SHULKER_BOXES) {
            Limitless.LOGGER.info("DEBUG: ShulkerBoxManager - Box: {} at {}", box.getPlacedBy(), box.getPosition());
        }
    }

    /**
     * Adds a placed shulker box to the collection and refreshes the creative
     * screen.
     */
    public static void addShulkerBox(ShulkerBoxData shulkerBoxData) {
        boolean wasAdded = PLACED_SHULKER_BOXES.add(shulkerBoxData);

        if (wasAdded) {
            Limitless.LOGGER.info("Captured shulker box placed by {} at {}",
                    shulkerBoxData.getPlacedBy(), shulkerBoxData.getPosition());

            // Send packet to all clients to refresh the creative screen
            notifyAllClientsToRefresh();
        }
    }

    /**
     * Sends a packet to all clients to refresh the shulker box item group.
     */
    private static void notifyAllClientsToRefresh() {
        MinecraftServer server = Limitless.getCurrentServer();
        if (server != null) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerNetworking.notifyClientToRefreshShulkerBoxGroup(player);
            }
        }
    }

    /**
     * Creates the shulker box creative tab.
     */
    private static void createShulkerBoxItemGroup() {
        SHULKER_BOX_ITEM_GROUP = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.SHULKER_BOX))
                .displayName(Text.literal("§5Stolen Shulker Boxes"))
                .entries((context, entries) -> {
                    Limitless.LOGGER.info("DEBUG: ShulkerBoxItemGroup - Building entries. Current count: {}",
                            PLACED_SHULKER_BOXES.size());

                    if (PLACED_SHULKER_BOXES.isEmpty()) {
                        Limitless.LOGGER.info("DEBUG: ShulkerBoxItemGroup - Adding empty indicator");
                        ItemStack emptyIndicator = new ItemStack(Items.BARRIER);
                        emptyIndicator.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                                Text.literal("§cNo shulker boxes captured yet"));
                        entries.add(emptyIndicator);
                        return;
                    }

                    Limitless.LOGGER.info("DEBUG: ShulkerBoxItemGroup - Processing {} shulker boxes for display",
                            PLACED_SHULKER_BOXES.size());
                    ShulkerBoxProcessor processor = new ShulkerBoxProcessor();
                    processor.processShulkerBoxesForDisplay(PLACED_SHULKER_BOXES, entries);
                })
                .build();
    }

    /**
     * Registers the shulker box item group.
     */
    private static void registerShulkerBoxItemGroup() {
        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(Limitless.MOD_ID, "shulker_boxes"),
                SHULKER_BOX_ITEM_GROUP);
    }

    /**
     * Clears all captured shulker boxes and refreshes the screen.
     */
    public static void clearShulkerBoxes() {
        PLACED_SHULKER_BOXES.clear();
        Limitless.LOGGER.info("Cleared all captured shulker boxes");
        notifyAllClientsToRefresh();
    }

    /**
     * Gets the count of captured shulker boxes.
     */
    public static int getShulkerBoxCount() {
        return PLACED_SHULKER_BOXES.size();
    }

    /**
     * Checks if a specific shulker box is already captured.
     */
    public static boolean containsShulkerBox(ShulkerBoxData shulkerBoxData) {
        return PLACED_SHULKER_BOXES.contains(shulkerBoxData);
    }
}
