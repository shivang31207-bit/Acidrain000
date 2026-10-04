package com.example.acidocean;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.BiomeTags;

public class AcidOceanClient implements ClientModInitializer {
    @Override
    @SuppressWarnings("deprecation")
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(AcidPayload.ID, (payload, context) -> {
            AcidState.clientAcid = payload.acid();
            // Re-draw all chunks so the water colour updates immediately
            context.client().execute(() -> {
                if (context.client().worldRenderer != null) {
                    context.client().worldRenderer.reload();
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AcidState.clientAcid = false);

        // Green tint over the screen while you are underwater in an acid ocean
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (!AcidState.clientAcid) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return;
            if (!mc.player.isSubmergedInWater()) return;
            if (!mc.world.getBiome(mc.player.getBlockPos()).isIn(BiomeTags.IS_OCEAN)) return;
            context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), 0x8800DD22);
        });
    }
}
