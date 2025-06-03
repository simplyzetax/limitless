package me.simplyzetax.limitless.client.listeners;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import me.simplyzetax.limitless.client.LimitlessClient;

/**
 * A simple packet listener that logs all packets
 */
public class TestPacketListener extends PacketListenerAbstract {

    private int receivedPacketCounter = 0;
    private int sentPacketCounter = 0;
    private long lastLogTime = System.currentTimeMillis();

    public TestPacketListener() {
        super(PacketListenerPriority.MONITOR);
        LimitlessClient.LOGGER.info("TestPacketListener initialized - logging all packets.");
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        receivedPacketCounter++;

        PacketTypeCommon packetType = event.getPacketType();
        LimitlessClient.LOGGER.info("[PACKET RECV] {}", packetType.toString());

        logStatistics();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        sentPacketCounter++;

        PacketTypeCommon packetType = event.getPacketType();
        LimitlessClient.LOGGER.info("[PACKET SENT] {}", packetType.toString());

        logStatistics();
    }

    private void logStatistics() {
        long now = System.currentTimeMillis();
        if (now - lastLogTime > 5000) {
            LimitlessClient.LOGGER.info("[PACKET STATS | 5s] Received: {}, Sent: {}", receivedPacketCounter,
                    sentPacketCounter);
            receivedPacketCounter = 0;
            sentPacketCounter = 0;
            lastLogTime = now;
        }
    }
}