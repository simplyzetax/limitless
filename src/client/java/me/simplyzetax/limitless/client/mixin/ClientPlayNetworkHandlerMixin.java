package me.simplyzetax.limitless.client.mixin;

import com.mojang.datafixers.util.Pair;
import io.netty.channel.ChannelHandlerContext;
import me.simplyzetax.limitless.Limitless;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static me.simplyzetax.limitless.Limitless.EQUIPPED_ITEMS;

@Mixin(ClientConnection.class)
public class ClientPlayNetworkHandlerMixin {

    private static boolean containsEquivalentItem(ItemStack stack) {
        for (ItemStack existing : EQUIPPED_ITEMS) {
            if (existing.getItem() == stack.getItem()
                    && ItemStack.areItemsEqual(existing, stack)
                    && existing.getName().equals(stack.getName())) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof EntityEquipmentUpdateS2CPacket eqPacket) {
            MinecraftClient client = MinecraftClient.getInstance();

            List<Pair<EquipmentSlot, ItemStack>> list = eqPacket.getEquipmentList();
            Entity entity = client.world.getEntityById(eqPacket.getEntityId());
            String entityName = entity != null ? entity.getDisplayName().getString() : "Unknown";

            boolean foundNewItem = false;

            for (Pair<EquipmentSlot, ItemStack> pair : list) {
                ItemStack equippedStack = pair.getSecond();
                if (!equippedStack.isEmpty()) {
                    ItemStack copy = equippedStack.copy();
                    if (!containsEquivalentItem(copy)) {
                        copy.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, lore -> {
                            List<Text> newLore = new ArrayList<>(lore.lines());
                            // Check if "Obtained from:" already exists
                            if (newLore.stream().noneMatch(line -> line.getString().startsWith("§7Obtained from:"))) {
                                newLore.add(Text.literal("§7Obtained from: ").append(Text.literal(entityName).formatted(Formatting.AQUA)));
                                newLore.add(Text.literal("§7Time added: ")
                                        .append(Text.literal(new Date().toString())
                                                .styled(style -> style.withColor(0x5090D9))));
                            }
                            return new LoreComponent(newLore);
                        });

                        Limitless.LOGGER.info("Adding new item from entity '{}' with attributes to Limitless tab: {}",
                                entityName, copy.getItem().getTranslationKey());
                        Limitless.EQUIPPED_ITEMS.add(copy);
                        foundNewItem = true;
                    }
                }
            }

            // Refresh the creative inventory screen if needed
            if (foundNewItem && client.currentScreen instanceof CreativeInventoryScreen) {
                Limitless.LOGGER.info("Scheduling creative screen refresh for new items...");
                client.execute(() -> {
                    ClientPlayerEntity player = client.player;
                    FeatureSet enabledFeatures = client.world.getEnabledFeatures();
                    boolean operatorTabEnabled = player.hasPermissionLevel(2);

                    client.setScreen(null);
                    client.setScreen(new CreativeInventoryScreen(player, enabledFeatures, operatorTabEnabled));
                });
            }
        }
    }
}