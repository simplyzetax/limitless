package me.simplyzetax.limitless;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public class Limitless implements ModInitializer {
    public static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // We'll store unique Items here that we've "seen" from any player
    public static final Set<Item> EQUIPPED_ITEMS = new LinkedHashSet<>();

    public static ItemGroup LIMITLESS_ITEM_GROUP;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Limitless mod...");

        LIMITLESS_ITEM_GROUP = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE))
                .displayName(Text.literal("§9Limitless"))
                .entries((context, entries) -> {

                    // Add a barrier item if we haven't seen any items yet to prevent the tab from being empty
                    // and not showing up in the creative menu
                    if(EQUIPPED_ITEMS.isEmpty()) {
                        entries.add(new ItemStack(Items.BARRIER));
                    }

                    // Add any items we've recorded from players' equipment
                    Limitless.EQUIPPED_ITEMS.forEach(entries::add);
                })
                .build();

        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(MOD_ID, "main"),
                LIMITLESS_ITEM_GROUP
        );

        LOGGER.info("Limitless tab registered successfully!");
    }
}
