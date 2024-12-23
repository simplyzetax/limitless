package me.simplyzetax.limitless;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Limitless implements ModInitializer {
    public static final String MOD_ID = "limitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // We'll store unique Items here that we've "seen" from any player
    public static final Set<ItemStack> EQUIPPED_ITEMS = new LinkedHashSet<>();

    public static ItemGroup LIMITLESS_ITEM_GROUP;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Limitless mod...");

        LIMITLESS_ITEM_GROUP = FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE))
                .displayName(Text.literal("§9Limitless"))
                .entries((context, entries) -> {
                    if (EQUIPPED_ITEMS.isEmpty()) {
                        ItemStack stack = new ItemStack(Items.BARRIER);
                        entries.add(stack);
                    }

                    // Ensure no duplicate ItemStacks are added
                    Set<String> addedKeys = new HashSet<>();

                    // Obtain the WrapperLookup from the context
                    RegistryWrapper.WrapperLookup wrapperLookup = context.lookup();

                    Limitless.EQUIPPED_ITEMS.forEach(itemStack -> {
                        try {
                            // Use toNbt() to generate a unique key
                            String nbtData = itemStack.toNbt(wrapperLookup).toString(); // Convert the full stack to NBT string
                            String uniqueKey = itemStack.getItem().toString() + nbtData;

                            if (!addedKeys.contains(uniqueKey)) {
                                ItemStack singleItemStack = itemStack.copy();

                                //NbtElement nbtElement = itemStack.toNbt(wrapperLookup);

                                //ItemStack.fromNbt(wrapperLookup, nbtElement); // Set the NBT data

                                Text text = Text.literal("Hello world");
                                List<Text> textList = Collections.singletonList(text);


                                Item.TooltipContext tooltipContext = new Item.TooltipContext() {

                                    @Nullable
                                    @Override
                                    public RegistryWrapper.WrapperLookup getRegistryLookup() {
                                        return null;
                                    }

                                    @Override
                                    public float getUpdateTickRate() {
                                        return 0;
                                    }

                                    @Nullable
                                    @Override
                                    public MapState getMapState(MapIdComponent mapIdComponent) {
                                        return null;
                                    }
                                };

                                singleItemStack.getItem().appendTooltip(singleItemStack, tooltipContext, textList, TooltipType.BASIC);
                                singleItemStack.setCount(1); // Ensure stack size is 1
                                entries.add(singleItemStack);
                                addedKeys.add(uniqueKey);
                            }
                        } catch (Exception e) {
                            e.printStackTrace(); // Log any errors
                        }
                    });
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
