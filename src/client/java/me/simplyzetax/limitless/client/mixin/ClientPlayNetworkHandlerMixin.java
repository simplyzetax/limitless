package me.simplyzetax.limitless.client.mixin;

import com.mojang.datafixers.util.Pair;
import io.netty.channel.ChannelHandlerContext;
import me.simplyzetax.limitless.Limitless;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.List;

@Mixin(ClientConnection.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        // Check if it's our equipment update packet
        if (packet instanceof EntityEquipmentUpdateS2CPacket eqPacket) {
            // Retrieve the item list
            List<Pair<EquipmentSlot, ItemStack>> list = eqPacket.getEquipmentList();

            // Do whatever you want with those items
            for (Pair<EquipmentSlot, ItemStack> pair : list) {
                ItemStack equippedStack = pair.getSecond();
                if (!equippedStack.isEmpty()) {
                    // Example: add to a "Limitless" custom tab set
                    Limitless.LOGGER.info("Adding item to Limitless tab: {}", equippedStack.getItem().getTranslationKey());
                    Limitless.EQUIPPED_ITEMS.add(equippedStack.getItem());

                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.currentScreen instanceof CreativeInventoryScreen) {
                        Limitless.LOGGER.info("Scheduling creative screen refresh on render thread...");

                        // Schedule the screen refresh on the Render thread
                        client.execute(() -> {
                            ClientPlayerEntity player = client.player;
                            FeatureSet enabledFeatures = client.world.getEnabledFeatures();
                            boolean operatorTabEnabled = player.hasPermissionLevel(2);
                            client.setScreen(null); // Close the screen
                            client.setScreen(new CreativeInventoryScreen(player, enabledFeatures, operatorTabEnabled)); // Reopen the screen
                        });
                    }
                }
            }
        }
    }
}
