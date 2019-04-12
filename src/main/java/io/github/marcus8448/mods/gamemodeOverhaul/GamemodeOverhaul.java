package io.github.marcus8448.mods.gamemodeOverhaul;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static net.minecraft.server.command.DifficultyCommand.method_13173;

/**
 * Copyright (c) marcus8448 2019. All rights reserved.
 *
 * @author marcus8448
 */
public class GamemodeOverhaul implements ModInitializer {
    static final Logger LOGGER = LogManager.getLogger("GamemodeOverhaul");
    private static final Marker GAMEMODE_OVERHAUL = MarkerManager.getMarker("GamemodeOverhaul");

    private static void commandFeedback(ServerCommandSource serverCommandSource_1, ServerPlayerEntity serverPlayerEntity_1, GameMode gameMode_1) {
        TextComponent textComponent_1 = new TranslatableTextComponent("gameMode." + gameMode_1.getName());
        if (serverCommandSource_1.getEntity() == serverPlayerEntity_1) {
            serverCommandSource_1.sendFeedback(new TranslatableTextComponent("commands.gamemode.success.self", textComponent_1), true);
        } else {
            if (serverCommandSource_1.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                serverPlayerEntity_1.appendCommandFeedback(new TranslatableTextComponent("gameMode.changed", textComponent_1));
            }

            serverCommandSource_1.sendFeedback(new TranslatableTextComponent("commands.gamemode.success.other", serverPlayerEntity_1.getDisplayName(), textComponent_1), true);
        }

    }

    private static int changeMode(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> collection_1, GameMode gameMode_1) {
        int int_1 = 0;
        Iterator<ServerPlayerEntity> var4 = collection_1.iterator();

        while (var4.hasNext()) {
            ServerPlayerEntity serverPlayerEntity_1 = var4.next();
            if (serverPlayerEntity_1.interactionManager.getGameMode() != gameMode_1) {
                serverPlayerEntity_1.setGameMode(gameMode_1);
                commandFeedback(context.getSource(), serverPlayerEntity_1, gameMode_1);
                ++int_1;
            }
        }

        return int_1;
    }

    private static int changeMode(CommandContext<ServerCommandSource> context, GameMode gameMode_1) {
        ServerPlayerEntity serverPlayerEntity_1;
        try {
            serverPlayerEntity_1 = context.getSource().getPlayer();
            if (serverPlayerEntity_1.interactionManager.getGameMode() != gameMode_1) {
                serverPlayerEntity_1.setGameMode(gameMode_1);
                commandFeedback(context.getSource(), serverPlayerEntity_1, gameMode_1);
            }
        } catch (CommandSyntaxException ignore) {
            return 0;
        }
        return 1;
    }

    private static int changeDefaultMode(ServerCommandSource serverCommandSource_1, GameMode gameMode_1) {
        int int_1 = 0;
        MinecraftServer minecraftServer_1 = serverCommandSource_1.getMinecraftServer();
        minecraftServer_1.setDefaultGameMode(gameMode_1);
        if (minecraftServer_1.shouldForceGameMode()) {
            Iterator<ServerPlayerEntity> var4 = minecraftServer_1.getPlayerManager().getPlayerList().iterator();

            while(var4.hasNext()) {
                ServerPlayerEntity serverPlayerEntity_1 = var4.next();
                if (serverPlayerEntity_1.interactionManager.getGameMode() != gameMode_1) {
                    serverPlayerEntity_1.setGameMode(gameMode_1);
                    ++int_1;
                }
            }
        }

        serverCommandSource_1.sendFeedback(new TranslatableTextComponent("commands.defaultgamemode.success", new Object[]{gameMode_1.getTextComponent()}), true);
        return int_1;
    }

    private static int kill(ServerCommandSource serverCommandSource_1) {
        serverCommandSource_1.getEntity().kill();
        serverCommandSource_1.sendFeedback(new TranslatableTextComponent("commands.kill.success.single", serverCommandSource_1.getEntity().getDisplayName()), true);
        return 1;
    }

    @Override
    public void onInitialize() {
        LOGGER.info(GAMEMODE_OVERHAUL, "GamemodeOverhaul is Initializing!");
        CommandRegistry.INSTANCE.register(false, GameModeCommands::register);
        CommandRegistry.INSTANCE.register(false, DefaultGamemodeCommands::register);
        CommandRegistry.INSTANCE.register(false, KillCommand::register);
        CommandRegistry.INSTANCE.register(false, DifficultyCommand::register);
    }

    public static class GameModeCommands {
        static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            LiteralArgumentBuilder<ServerCommandSource> gamemode = ServerCommandManager.literal("gamemode").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> gm = ServerCommandManager.literal("gm").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> gms = ServerCommandManager.literal("gms").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.SURVIVAL)));
            LiteralArgumentBuilder<ServerCommandSource> gmc = ServerCommandManager.literal("gmc").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.CREATIVE)));
            LiteralArgumentBuilder<ServerCommandSource> gma = ServerCommandManager.literal("gma").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.ADVENTURE)));
            LiteralArgumentBuilder<ServerCommandSource> gmsp = ServerCommandManager.literal("gmsp").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.SPECTATOR)));
            GameMode[] gameModes = GameMode.values();
            for (GameMode mode : gameModes) {
                if (mode != GameMode.INVALID) {
                    gamemode.then(((LiteralArgumentBuilder) ServerCommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(ServerCommandManager.argument("target", EntityArgumentType.multiplePlayer()).executes((context) -> changeMode(context, EntityArgumentType.method_9312(context, "target"), mode))));
                    gm.then(((LiteralArgumentBuilder) ServerCommandManager.literal(mode.getName()).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(ServerCommandManager.argument("target", EntityArgumentType.multiplePlayer()).executes((context) -> changeMode(context, EntityArgumentType.method_9312(context, "target"), mode))));
                    gm.then(((LiteralArgumentBuilder) ServerCommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(ServerCommandManager.argument("target", EntityArgumentType.multiplePlayer()).executes((context) -> changeMode(context, EntityArgumentType.method_9312(context, "target"), mode))));
                    if (mode != GameMode.SPECTATOR) {
                        gamemode.then(((LiteralArgumentBuilder) ServerCommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(ServerCommandManager.argument("target", EntityArgumentType.multiplePlayer()).executes((context) -> changeMode(context, EntityArgumentType.method_9312(context, "target"), mode))));
                        gm.then(((LiteralArgumentBuilder) ServerCommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(ServerCommandManager.argument("target", EntityArgumentType.multiplePlayer()).executes((context) -> changeMode(context, EntityArgumentType.method_9312(context, "target"), mode))));
                    } else {
                        gamemode.then(((LiteralArgumentBuilder) ServerCommandManager.literal("sp").executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(ServerCommandManager.argument("target", EntityArgumentType.multiplePlayer()).executes((context) -> changeMode(context, EntityArgumentType.method_9312(context, "target"), mode))));
                        gm.then(((LiteralArgumentBuilder) ServerCommandManager.literal("sp").executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(ServerCommandManager.argument("target", EntityArgumentType.multiplePlayer()).executes((context) -> changeMode(context, EntityArgumentType.method_9312(context, "target"), mode))));
                    }
                }
            }
            dispatcher.register(gamemode);
            dispatcher.register(gm);
            dispatcher.register(gms);
            dispatcher.register(gmc);
            dispatcher.register(gma);
            dispatcher.register(gmsp);
        }
    }

    public static class DefaultGamemodeCommands {
        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            LiteralArgumentBuilder<ServerCommandSource> defaultgamemode = ServerCommandManager.literal("defaultgamemode").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> dgm = ServerCommandManager.literal("dgm").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> dgms = ServerCommandManager.literal("dgms").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.SURVIVAL));
            LiteralArgumentBuilder<ServerCommandSource> dgmc = ServerCommandManager.literal("dgmc").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.CREATIVE));
            LiteralArgumentBuilder<ServerCommandSource> dgma = ServerCommandManager.literal("dgma").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.ADVENTURE));
            LiteralArgumentBuilder<ServerCommandSource> dgmsp = ServerCommandManager.literal("dgmsp").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.SPECTATOR));
            GameMode[] modes = GameMode.values();
            for (GameMode mode : modes) {
                if (mode != GameMode.INVALID) {
                    defaultgamemode.then(ServerCommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    dgm.then(ServerCommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    dgm.then(ServerCommandManager.literal(mode.getName()).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    if (mode != GameMode.SPECTATOR) {
                        defaultgamemode.then(ServerCommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                        dgm.then(ServerCommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    } else {
                        defaultgamemode.then(ServerCommandManager.literal("sp").executes((context) -> changeDefaultMode(context.getSource(), mode)));
                        dgm.then(ServerCommandManager.literal("sp").executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    }
                }
            }
            dispatcher.register(defaultgamemode);
            dispatcher.register(dgm);
            dispatcher.register(dgms);
            dispatcher.register(dgmc);
            dispatcher.register(dgma);
            dispatcher.register(dgmsp);
        }
    }

    public static class DifficultyCommand {
        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder_1 = ServerCommandManager.literal("difficulty");
            Difficulty[] difficulties = Difficulty.values();

            for (Difficulty difficulty : difficulties) {
                literalArgumentBuilder_1.then(ServerCommandManager.literal(Integer.toString(difficulty.getId())).executes((context) -> method_13173(context.getSource(), difficulty)));
            }

            dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder_1.requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2))).executes((context) -> {
                Difficulty difficulty = ((ServerCommandSource)context.getSource()).getWorld().getDifficulty();
                ((ServerCommandSource)context.getSource()).sendFeedback(new TranslatableTextComponent("commands.difficulty.query", new Object[]{difficulty.toTextComponent()}), false);
                return difficulty.getId();
            }));
        }    }

    public static class KillCommand {
        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)ServerCommandManager.literal("kill").requires((serverCommandSource_1) -> serverCommandSource_1.hasPermissionLevel(2))).executes((context) -> kill((ServerCommandSource)context.getSource())));
        }
    }
}
