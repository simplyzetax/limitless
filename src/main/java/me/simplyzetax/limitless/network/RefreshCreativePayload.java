// src/main/java/me/simplyzetax/limitless/network/RefreshCreativePayload.java
package me.simplyzetax.limitless.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import me.simplyzetax.limitless.Limitless;

public record RefreshCreativePayload(String action, String extra) implements CustomPayload {

    public static final CustomPayload.Id<RefreshCreativePayload> ID = new CustomPayload.Id<>(
            Identifier.of(Limitless.MOD_ID, "refresh_creative"));

    public static final PacketCodec<PacketByteBuf, RefreshCreativePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, RefreshCreativePayload::action,
            PacketCodecs.STRING, RefreshCreativePayload::extra,
            RefreshCreativePayload::new);

    // Constructor for simple refresh
    public RefreshCreativePayload(String action) {
        this(action, "");
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
