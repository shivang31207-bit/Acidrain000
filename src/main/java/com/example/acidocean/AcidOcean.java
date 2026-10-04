package com.example.acidocean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AcidOcean implements ModInitializer {

    /** Saved to config/acidocean.json so the state survives restarts. */
    public static class Config {
        public boolean acid = false;      // is the ocean acid right now?
        public float damage = 2.0f;       // half-hearts per hit (2 = 1 heart)
        public int intervalTicks = 20;    // 20 ticks = 1 second
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("acidocean.json");
    private static Config config = new Config();
    private int tickCounter = 0;

    @Override
    public void onInitialize() {
        loadConfig();
        PayloadTypeRegistry.playS2C().register(AcidPayload.ID, AcidPayload.CODEC);
        registerCommands();

        // Tell players who join the current state (for green water)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity p = handler.getPlayer();
            if (ServerPlayNetworking.canSend(p, AcidPayload.ID)) {
                ServerPlayNetworking.send(p, new AcidPayload(config.acid));
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!config.acid) return;
            if (++tickCounter < Math.max(1, config.intervalTicks)) return;
            tickCounter = 0;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isCreative() || player.isSpectator()) continue;
                if (!player.isTouchingWater()) continue;

                if (!(player.getEntityWorld() instanceof ServerWorld world)) continue;
                if (!world.getBiome(player.getBlockPos()).isIn(BiomeTags.IS_OCEAN)) continue;

                // Burn effect + damage
                // Green swirling (spiral) particles around the player
                world.spawnParticles(
                        EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, 0.15f, 0.9f, 0.2f),
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        20, 0.5, 0.6, 0.5, 0.05);
                player.damage(world, world.getDamageSources().magic(), config.damage);
            }
        });
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("acidocean")
                .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))

                .then(CommandManager.literal("on").executes(ctx -> {
                    config.acid = true;
                    saveConfig();
                    broadcast(ctx.getSource().getServer());
                    ctx.getSource().sendFeedback(() -> Text.literal("§aThe ocean is now ACID."), true);
                    return 1;
                }))

                .then(CommandManager.literal("off").executes(ctx -> {
                    config.acid = false;
                    saveConfig();
                    broadcast(ctx.getSource().getServer());
                    ctx.getSource().sendFeedback(() -> Text.literal("§bThe ocean is back to normal."), true);
                    return 1;
                }))

                .then(CommandManager.literal("damage")
                    .then(CommandManager.argument("amount", FloatArgumentType.floatArg(0.5f, 40f))
                        .executes(ctx -> {
                            config.damage = FloatArgumentType.getFloat(ctx, "amount");
                            saveConfig();
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("Acid damage set to " + config.damage + " half-hearts."), true);
                            return 1;
                        })))

                .then(CommandManager.literal("interval")
                    .then(CommandManager.argument("ticks", IntegerArgumentType.integer(1, 200))
                        .executes(ctx -> {
                            config.intervalTicks = IntegerArgumentType.getInteger(ctx, "ticks");
                            saveConfig();
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("Acid damage interval set to " + config.intervalTicks + " ticks."), true);
                            return 1;
                        })))

                .then(CommandManager.literal("status").executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(
                        "Acid ocean: " + (config.acid ? "§aON" : "§cOFF") + "§r | damage: " + config.damage
                        + " | every " + config.intervalTicks + " ticks"), false);
                    return 1;
                }))
            ));
    }

    private static void broadcast(MinecraftServer server) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (ServerPlayNetworking.canSend(p, AcidPayload.ID)) {
                ServerPlayNetworking.send(p, new AcidPayload(config.acid));
            }
        }
    }

    private static void loadConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                config = GSON.fromJson(Files.readString(CONFIG_PATH), Config.class);
                if (config == null) config = new Config();
            } else {
                saveConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
            config = new Config();
        }
    }

    private static void saveConfig() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
