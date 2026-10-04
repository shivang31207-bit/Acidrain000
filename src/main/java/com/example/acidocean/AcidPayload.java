package com.example.acidocean;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Server -> client message: is the ocean acid right now? */
public record AcidPayload(boolean acid) implements CustomPayload {
    public static final CustomPayload.Id<AcidPayload> ID =
            new CustomPayload.Id<>(Identifier.of("acidocean", "state"));
    public static final PacketCodec<io.netty.buffer.ByteBuf, AcidPayload> CODEC =
            PacketCodecs.BOOLEAN.xmap(AcidPayload::new, AcidPayload::acid);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
