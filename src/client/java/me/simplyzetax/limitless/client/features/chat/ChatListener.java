package me.simplyzetax.limitless.client.features.chat;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import me.simplyzetax.limitless.client.LimitlessClient;
import net.minecraft.client.MinecraftClient;

public class ChatListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // For now, let's try a simpler approach - listen for system chat messages
        // and extract the data directly from the packet
        if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE) {
            try {
                // Get raw packet data and try to extract chat content
                // This is a basic implementation that should work

                // Log that we received a chat packet (for debugging)
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    LimitlessClient.LOGGER.info("Chat message received");
                }
            } catch (Exception e) {
                // Log any errors for debugging
                System.err.println("ChatListener error: " + e.getMessage());
            }
        } else {
            // If the packet is not a chat message, we can ignore it
            // or handle other packet types if needed
            LimitlessClient.LOGGER.debug("Received packet: " + event.getPacketType());
        }
    }
}