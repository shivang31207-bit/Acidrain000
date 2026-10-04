package com.example.acidocean.mixin;

import com.example.acidocean.AcidState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {
    @Inject(method = "getWaterColor", at = @At("RETURN"), cancellable = true)
    private static void acidocean$greenWater(BlockRenderView world, BlockPos pos,
                                             CallbackInfoReturnable<Integer> cir) {
        if (!AcidState.clientAcid) return;
        try {
            var clientWorld = MinecraftClient.getInstance().world;
            if (clientWorld != null && clientWorld.getBiome(pos).isIn(BiomeTags.IS_OCEAN)) {
                cir.setReturnValue(0x55FF11); // toxic green
            }
        } catch (Throwable ignored) {
            // never crash rendering
        }
    }
}
