package me.simplyzetax.limitless.client.mixin;

import com.mojang.datafixers.util.Pair;
import io.netty.channel.ChannelHandlerContext;
import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static me.simplyzetax.limitless.Limitless.EQUIPPED_ITEMS;

@Environment(EnvType.CLIENT)
@Mixin(ClientConnection.class) // Ensure this is the correct target class
public class ClientPlayNetworkHandlerMixin {

    /**
     * Checks if an equivalent ItemStack already exists in EQUIPPED_ITEMS.
     *
     * @param stack The ItemStack to check.
     * @return True if an equivalent stack exists, false otherwise.
     */
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

    /**
     * Injects into the channelRead0 method to process EntityEquipmentUpdateS2CPacket.
     *
     * @param context The channel handler context.
     * @param packet  The received packet.
     * @param ci      The callback info.
     */
    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof EntityEquipmentUpdateS2CPacket eqPacket) {

            if(!ClientConfig.EnableStealing) {
                Limitless.LOGGER.info("Skipping entity equipment update as stealing is disabled");
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();

            List<Pair<EquipmentSlot, ItemStack>> equipmentList = eqPacket.getEquipmentList();
            Entity entity = client.world.getEntityById(eqPacket.getEntityId());

            // Check if the entity is a player
            if (ClientConfig.OnlyStealPlayerItems && !(entity instanceof PlayerEntity)) {
                Limitless.LOGGER.info("Skipping entity '{}' as it is not a player", entity.getDisplayName());
                return;
            }

            String entityName = entity != null ? entity.getDisplayName().getString() : "Unknown";

            boolean foundNewItem = false;

            for (Pair<EquipmentSlot, ItemStack> pair : equipmentList) {
                ItemStack equippedStack = pair.getSecond();
                if (!equippedStack.isEmpty()) {
                    ItemStack copy = equippedStack.copy();
                    if (!containsEquivalentItem(copy)) {
                        // Create a new NbtCompound for CUSTOM_DATA
                        NbtCompound customDataNbt = new NbtCompound();
                        customDataNbt.putString("ObtainedFrom", entityName);
                        customDataNbt.putString("ObtainedTime", new Date().toString());
                        NbtComponent customDataComponent = NbtComponent.of(customDataNbt);

                        // Set the CUSTOM_DATA component
                        copy.set(DataComponentTypes.CUSTOM_DATA, customDataComponent);

                        // Log and add to EQUIPPED_ITEMS
                        Limitless.LOGGER.info("Adding new item from entity '{}' to Limitless tab: {}",
                                entityName, copy.getItem().getTranslationKey());
                        Limitless.EQUIPPED_ITEMS.add(copy);
                        foundNewItem = true;
                    }
                }
            }

            // Refresh the creative inventory screen if needed
            if (foundNewItem) {
                Limitless.LOGGER.info("Scheduling creative screen refresh for new items...");
                client.execute(() -> {
                    ClientPlayerEntity player = client.player;
                    net.minecraft.resource.featuretoggle.FeatureSet enabledFeatures = client.world.getEnabledFeatures();
                    boolean operatorTabEnabled = player.hasPermissionLevel(2);

                    Screen currentScreen = client.currentScreen;

                    client.setScreen(null);
                    client.setScreen(new CreativeInventoryScreen(player, enabledFeatures, operatorTabEnabled));
                    client.setScreen(currentScreen);
                });
            }
        }
    }
}
