package me.simplyzetax.limitless.stealer;

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

import java.util.LinkedHashSet;
import java.util.Set;

public class LimitlessItemGroupManager {
    public static final Set<ItemStack> EQUIPPED_ITEMS = new LinkedHashSet<>();
    public static ItemGroup LIMITLESS_ITEM_GROUP;

    public static void initialize() {
        createItemGroup();
        registerItemGroup();
        Limitless.LOGGER.info("Limitless item group registered successfully!");
    }

    /**
     * Adds an equipped item to the collection and refreshes the creative screen.
     */
    public static void addEquippedItem(ItemStack itemStack) {
        boolean wasAdded = EQUIPPED_ITEMS.add(itemStack.copy());

        if (wasAdded) {
            Limitless.LOGGER.info("Added equipped item: {}", itemStack.getName().getString());

            // Send packet to all clients to refresh the creative screen
            notifyAllClientsToRefresh();
        }
    }

    /**
     * Sends a packet to all clients to refresh the limitless item group.
     */
    private static void notifyAllClientsToRefresh() {
        MinecraftServer server = Limitless.getCurrentServer();
        Limitless.LOGGER.info("DEBUG: Attempting to notify clients. Server: {}", server);

        if (server != null) {
            var players = server.getPlayerManager().getPlayerList();
            Limitless.LOGGER.info("DEBUG: Found {} players to notify", players.size());

            for (ServerPlayerEntity player : players) {
                Limitless.LOGGER.info("DEBUG: Sending refresh packet to player: {}", player.getName().getString());
                ServerNetworking.notifyClientToRefreshLimitlessGroup(player);
            }
        } else {
            Limitless.LOGGER.warn("DEBUG: Server is null, cannot notify clients");
        }
    }

    private static void createItemGroup() {
        LIMITLESS_ITEM_GROUP = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE))
                .displayName(Text.literal("§9Limitless"))
                .entries((context, entries) -> {
                    if (EQUIPPED_ITEMS.isEmpty()) {
                        ItemStack emptyIndicator = new ItemStack(Items.BARRIER);
                        emptyIndicator.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                                Text.literal("§cNo equipped items captured yet"));
                        entries.add(emptyIndicator);
                        return;
                    }

                    ItemStackProcessor processor = new ItemStackProcessor();
                    processor.processItemsForDisplay(EQUIPPED_ITEMS, entries, context.lookup());
                })
                .build();
    }

    private static void registerItemGroup() {
        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(Limitless.MOD_ID, "main"),
                LIMITLESS_ITEM_GROUP);
    }

    /**
     * Clears all equipped items and refreshes the screen.
     */
    public static void clearEquippedItems() {
        EQUIPPED_ITEMS.clear();
        Limitless.LOGGER.info("Cleared all equipped items");
        notifyAllClientsToRefresh();
    }

    /**
     * Removes a specific equipped item and refreshes the screen.
     */
    public static boolean removeEquippedItem(ItemStack itemStack) {
        boolean wasRemoved = EQUIPPED_ITEMS.remove(itemStack);

        if (wasRemoved) {
            Limitless.LOGGER.info("Removed equipped item: {}", itemStack.getName().getString());
            notifyAllClientsToRefresh();
        }

        return wasRemoved;
    }

    /**
     * Gets the count of equipped items.
     */
    public static int getEquippedItemCount() {
        return EQUIPPED_ITEMS.size();
    }

    /**
     * Checks if a specific item is already in the equipped items collection.
     */
    public static boolean containsEquippedItem(ItemStack itemStack) {
        return EQUIPPED_ITEMS.contains(itemStack);
    }
}
