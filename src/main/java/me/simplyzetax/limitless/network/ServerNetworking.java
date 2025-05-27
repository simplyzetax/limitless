// src/main/java/me/simplyzetax/limitless/network/ServerNetworking.java
package me.simplyzetax.limitless.network;

import me.simplyzetax.limitless.Limitless;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerNetworking {

    public static void notifyClientToRefresh(ServerPlayerEntity player) {
        RefreshCreativePayload payload = new RefreshCreativePayload("refresh_creative");
        ServerPlayNetworking.send(player, payload);
    }

    public static void notifyClientToRefreshCreative(ServerPlayerEntity player) {
        RefreshCreativePayload payload = new RefreshCreativePayload("refresh_creative");
        ServerPlayNetworking.send(player, payload);
    }

    public static void notifyClientToRefreshGroup(ServerPlayerEntity player, String groupName) {
        RefreshCreativePayload payload = new RefreshCreativePayload("refresh_group", groupName);
        ServerPlayNetworking.send(player, payload);
    }

    public static void notifyClientToRefreshLimitlessGroup(ServerPlayerEntity player) {
        Limitless.LOGGER.info("DEBUG: Creating refresh payload for limitless group");
        RefreshCreativePayload payload = new RefreshCreativePayload("refresh_group", "limitless_main");

        Limitless.LOGGER.info("DEBUG: Sending payload to player: {}", player.getName().getString());
        ServerPlayNetworking.send(player, payload);
        Limitless.LOGGER.info("DEBUG: Payload sent successfully");
    }

    public static void notifyClientToRefreshShulkerBoxGroup(ServerPlayerEntity player) {
        RefreshCreativePayload payload = new RefreshCreativePayload("refresh_group", "limitless_shulker_boxes");
        ServerPlayNetworking.send(player, payload);
    }
}
