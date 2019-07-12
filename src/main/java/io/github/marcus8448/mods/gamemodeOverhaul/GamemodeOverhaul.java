package io.github.marcus8448.mods.gamemodeOverhaul;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Copyright (c) marcus8448 2019. All rights reserved.
 *
 * @author marcus8448
 */
public class GamemodeOverhaul implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("GamemodeOverhaul");
    private static final Marker GAMEMODE_OVERHAUL = MarkerManager.getMarker("GamemodeOverhaul");

    private static void commandFeedback(ServerCommandSource source, ServerPlayerEntity player, GameMode mode) {
        LiteralText text = new LiteralText(new TranslatableText("gameMode." + mode.getName()).asString());
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("commands.gamemode.success.self", text), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.field_19394)) {
                player.sendChatMessage(new TranslatableText("gameMode.changed", text), MessageType.field_11733);
            }

            source.sendFeedback(new TranslatableText("commands.gamemode.success.other", player.getDisplayName(), text), true);
        }

    }

    private static int changeMode(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> collection, GameMode mode) {
        int i = 0;

        for (ServerPlayerEntity player : collection) {
            if (player.interactionManager.getGameMode() != mode) {
                player.setGameMode(mode);
                commandFeedback(context.getSource(), player, mode);
                ++i;
            }
        }

        return i;
    }

    private static int changeMode(CommandContext<ServerCommandSource> context, GameMode mode) {
        ServerPlayerEntity player;
        try {
            player = context.getSource().getPlayer();
            if (player.interactionManager.getGameMode() != mode) {
                player.setGameMode(mode);
                commandFeedback(context.getSource(), player, mode);
            }
        } catch (CommandSyntaxException ignore) {
            return 0;
        }
        return 1;
    }

    private static int changeModes(CommandContext<ServerCommandSource> context, GameMode mode) {
        for (ServerPlayerEntity p : context.getSource().getWorld().getPlayers()) {
            if (p.interactionManager.getGameMode() != mode) {
                p.setGameMode(mode);
                commandFeedback(context.getSource(), p, mode);
            }
        }
        return context.getSource().getWorld().getPlayers().size();
    }

    private static int changeDefaultMode(ServerCommandSource source, GameMode mode) {
        int i = 0;
        MinecraftServer server = source.getMinecraftServer();
        server.setDefaultGameMode(mode);
        if (server.shouldForceGameMode()) {

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.interactionManager.getGameMode() != mode) {
                    player.setGameMode(mode);
                    ++i;
                }
            }
        }

        source.sendFeedback(new TranslatableText("commands.defaultgamemode.success", mode.getTranslatableName()), true);
        return i;
    }

    private static int kill(ServerCommandSource source) {
        source.getEntity().kill();
        source.sendFeedback(new TranslatableText("commands.kill.success.single", source.getEntity().getDisplayName()), true);
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
            LiteralArgumentBuilder<ServerCommandSource> gamemode = CommandManager.literal("gamemode").requires((source) -> source.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> modeify = CommandManager.literal("modeify").requires((source) -> source.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> gm = CommandManager.literal("gm").requires((source) -> source.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> gms = CommandManager.literal("gms").requires((source) -> source.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.field_9215))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), GameMode.field_9215))));
            LiteralArgumentBuilder<ServerCommandSource> gmc = CommandManager.literal("gmc").requires((source) -> source.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.field_9220))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), GameMode.field_9220))));
            LiteralArgumentBuilder<ServerCommandSource> gma = CommandManager.literal("gma").requires((source) -> source.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.field_9216))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), GameMode.field_9216))));
            LiteralArgumentBuilder<ServerCommandSource> gmsp = CommandManager.literal("gmsp").requires((source) -> source.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.field_9219))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), GameMode.field_9219))));
            GameMode[] gameModes = GameMode.values();
            for (GameMode mode : gameModes) {
                if (mode != GameMode.field_9218) {
                    gamemode.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode)).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode))));
                    modeify.then(CommandManager.literal(mode.getName()).executes((context) -> changeModes(context, mode)));
                    modeify.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeModes(context, mode)));
                    gm.then(CommandManager.literal(mode.getName()).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode)));
                    gm.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode)));
                    if (mode != GameMode.field_9219) {
                        gamemode.then(CommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode)));
                        modeify.then(CommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeModes(context, mode)));
                        gm.then(CommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode)));
                    } else {
                        gamemode.then(CommandManager.literal("sp").executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode)));
                        modeify.then(CommandManager.literal("sp").executes((context) -> changeModes(context, mode)));
                        gm.then(CommandManager.literal("sp").executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode)));
                    }
                }
            }
            dispatcher.register(gamemode);
            dispatcher.register(modeify);
            dispatcher.register(gm);
            dispatcher.register(gms);
            dispatcher.register(gmc);
            dispatcher.register(gma);
            dispatcher.register(gmsp);
        }
    }

    private static class DefaultGamemodeCommands {
        private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            LiteralArgumentBuilder<ServerCommandSource> defaultgamemode = CommandManager.literal("defaultgamemode").requires((source) -> source.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> dgm = CommandManager.literal("dgm").requires((source) -> source.hasPermissionLevel(2));
            LiteralArgumentBuilder<ServerCommandSource> dgms = CommandManager.literal("dgms").requires((source) -> source.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.field_9215));
            LiteralArgumentBuilder<ServerCommandSource> dgmc = CommandManager.literal("dgmc").requires((source) -> source.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.field_9220));
            LiteralArgumentBuilder<ServerCommandSource> dgma = CommandManager.literal("dgma").requires((source) -> source.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.field_9216));
            LiteralArgumentBuilder<ServerCommandSource> dgmsp = CommandManager.literal("dgmsp").requires((source) -> source.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.field_9219));
            GameMode[] modes = GameMode.values();
            for (GameMode mode : modes) {
                if (mode != GameMode.field_9218) {
                    defaultgamemode.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    dgm.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    dgm.then(CommandManager.literal(mode.getName()).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    if (mode != GameMode.field_9219) {
                        defaultgamemode.then(CommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                        dgm.then(CommandManager.literal(Character.toString(mode.getName().charAt(0))).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                    } else {
                        defaultgamemode.then(CommandManager.literal("sp").executes((context) -> changeDefaultMode(context.getSource(), mode)));
                        dgm.then(CommandManager.literal("sp").executes((context) -> changeDefaultMode(context.getSource(), mode)));
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

    private static class DifficultyCommand {
        private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            LiteralArgumentBuilder<ServerCommandSource> difficultyCommand = CommandManager.literal("difficulty");
            Difficulty[] difficulties = Difficulty.values();

            for (Difficulty difficulty : difficulties) {
                difficultyCommand.then(CommandManager.literal(Integer.toString(difficulty.getId())).executes((context) -> net.minecraft.server.command.DifficultyCommand.execute(context.getSource(), difficulty)));
            }

            dispatcher.register((difficultyCommand.requires((source) -> source.hasPermissionLevel(2))).executes((context) -> {
                Difficulty difficulty = context.getSource().getWorld().getDifficulty();
                context.getSource().sendFeedback(new TranslatableText("commands.difficulty.query", difficulty.getTranslatableName()), false);
                return difficulty.getId();
            }));
        }    }

    private static class KillCommand {
        private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            dispatcher.register((CommandManager.literal("kill").requires((source) -> source.hasPermissionLevel(2))).executes((context) -> kill(context.getSource())));
        }
    }
}
