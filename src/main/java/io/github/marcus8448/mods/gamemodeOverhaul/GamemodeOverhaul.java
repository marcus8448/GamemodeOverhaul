package io.github.marcus8448.mods.gamemodeOverhaul;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
 * Copyright (C) 2019  marcus8448
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * @author marcus8448
 */
public class GamemodeOverhaul implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("GamemodeOverhaul");
    private static final Marker GAMEMODE_OVERHAUL = MarkerManager.getMarker("GamemodeOverhaul");

    @Override
    public void onInitialize() {
        LOGGER.info(GAMEMODE_OVERHAUL, "GamemodeOverhaul is Initializing!");
        CommandRegistry.INSTANCE.register(false, this::registerGamemodeCommands);
        CommandRegistry.INSTANCE.register(false, this::registerDefaultGamemodeCommands);
        CommandRegistry.INSTANCE.register(false, this::registerDifficultyCommand);
        CommandRegistry.INSTANCE.register(false, this::registerQuickKillCommand);
        CommandRegistry.INSTANCE.register(false, this::registerToggledownfallCommand);
        CommandRegistry.INSTANCE.register(false, this::registerXPCommand);
    }

    private static void commandFeedback(ServerCommandSource source, ServerPlayerEntity player, GameMode mode) {
        LiteralText text = new LiteralText(new TranslatableText("gameMode." + mode.getName()).asString());
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("commands.gamemode.success.self", text), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendChatMessage(new TranslatableText("gameMode.changed", text), MessageType.GAME_INFO);
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

    private static int addExperience(ServerCommandSource source, Collection<ServerPlayerEntity> players, int amount, boolean levels) {
        if (players.size() <= 0) {
            return 0;
        }

        for (ServerPlayerEntity player : players) {
            if (levels) {
                player.addExperienceLevels(amount);
            } else {
                player.addExperience(amount);
            }
        }

        if (players.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.experience.add." + (levels ? "levels" : "points") + ".success.single", amount, players.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.experience.add." + (levels ? "levels" : "points") + ".success.multiple", amount, players.size()), true);
        }

        return players.size();
    }

    private void registerGamemodeCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> gamemode = CommandManager.literal("gamemode").requires((source) -> source.hasPermissionLevel(2));
        LiteralArgumentBuilder<ServerCommandSource> modeify = CommandManager.literal("modeify").requires((source) -> source.hasPermissionLevel(2));
        LiteralArgumentBuilder<ServerCommandSource> gm = CommandManager.literal("gm").requires((source) -> source.hasPermissionLevel(2));
        LiteralArgumentBuilder<ServerCommandSource> gms = CommandManager.literal("gms").requires((source) -> source.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.SURVIVAL))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), GameMode.SURVIVAL))));
        LiteralArgumentBuilder<ServerCommandSource> gmc = CommandManager.literal("gmc").requires((source) -> source.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.CREATIVE))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), GameMode.CREATIVE))));
        LiteralArgumentBuilder<ServerCommandSource> gma = CommandManager.literal("gma").requires((source) -> source.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.ADVENTURE))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), GameMode.ADVENTURE))));
        LiteralArgumentBuilder<ServerCommandSource> gmsp = CommandManager.literal("gmsp").requires((source) -> source.hasPermissionLevel(2)).executes((context -> changeMode(context, GameMode.SPECTATOR))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), GameMode.SPECTATOR))));
        GameMode[] gameModes = GameMode.values();
        for (GameMode mode : gameModes) {
            if (mode != GameMode.NOT_SET) {
                gamemode.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode)).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode))));
                modeify.then(CommandManager.literal(mode.getName()).executes((context) -> changeModes(context, mode)));
                modeify.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeModes(context, mode)));
                gm.then(CommandManager.literal(mode.getName()).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode)));
                gm.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeMode(context, Collections.singleton(context.getSource().getPlayer()), mode))).then(CommandManager.argument("target", EntityArgumentType.players()).executes((context) -> changeMode(context, EntityArgumentType.getPlayers(context, "target"), mode)));
                if (mode != GameMode.SPECTATOR) {
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

    private void registerDefaultGamemodeCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> defaultgamemode = CommandManager.literal("defaultgamemode").requires((source) -> source.hasPermissionLevel(2));
        LiteralArgumentBuilder<ServerCommandSource> dgm = CommandManager.literal("dgm").requires((source) -> source.hasPermissionLevel(2));
        LiteralArgumentBuilder<ServerCommandSource> dgms = CommandManager.literal("dgms").requires((source) -> source.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.SURVIVAL));
        LiteralArgumentBuilder<ServerCommandSource> dgmc = CommandManager.literal("dgmc").requires((source) -> source.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.CREATIVE));
        LiteralArgumentBuilder<ServerCommandSource> dgma = CommandManager.literal("dgma").requires((source) -> source.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.ADVENTURE));
        LiteralArgumentBuilder<ServerCommandSource> dgmsp = CommandManager.literal("dgmsp").requires((source) -> source.hasPermissionLevel(2)).executes(context -> changeDefaultMode(context.getSource(), GameMode.SPECTATOR));
        GameMode[] modes = GameMode.values();
        for (GameMode mode : modes) {
            if (mode != GameMode.NOT_SET) {
                defaultgamemode.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                dgm.then(CommandManager.literal(Integer.toString(mode.getId())).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                dgm.then(CommandManager.literal(mode.getName()).executes((context) -> changeDefaultMode(context.getSource(), mode)));
                if (mode != GameMode.SPECTATOR) {
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

    private void registerDifficultyCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
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
    }

    private void registerQuickKillCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((CommandManager.literal("kill").requires((source) -> source.hasPermissionLevel(2))).executes((context) -> kill(context.getSource())));
    }

    private void registerToggledownfallCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("toggledownfall").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).executes(context -> {
            if (!(context.getSource().getWorld().isRaining() || context.getSource().getWorld().getLevelProperties().isRaining() || context.getSource().getWorld().isThundering() || context.getSource().getWorld().getLevelProperties().isThundering())) {
                context.getSource().getWorld().getLevelProperties().setClearWeatherTime(0);
                context.getSource().getWorld().getLevelProperties().setRainTime(6000);
                context.getSource().getWorld().getLevelProperties().setThunderTime(6000);
                context.getSource().getWorld().getLevelProperties().setRaining(true);
                context.getSource().getWorld().getLevelProperties().setThundering(false);
                context.getSource().getWorld().getLevelProperties().setThundering(false);
                context.getSource().sendFeedback(/*new TranslatableText("gamemodeoverhaul.command.toggledownfall.feedback")*/ new LiteralText("Toggled downfall"), false);
                return 6000;
            } else {
                context.getSource().getWorld().getLevelProperties().setClearWeatherTime(6000);
                context.getSource().getWorld().getLevelProperties().setRainTime(0);
                context.getSource().getWorld().getLevelProperties().setThunderTime(0);
                context.getSource().getWorld().getLevelProperties().setRaining(false);
                context.getSource().getWorld().getLevelProperties().setThundering(false);
                context.getSource().sendFeedback(/*new TranslatableText("gamemodeoverhaul.command.toggledownfall.feedback")*/ new LiteralText("Toggled downfall"), false);
                return 6000;
            }
        }));
    }

    private void registerXPCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("xps").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2)).then(CommandManager.argument("amount[L]", StringArgumentType.word()).executes(context -> {
            String input = StringArgumentType.getString(context, "amount[L]");
            if (input.toLowerCase().endsWith("l")) {
                try {
                    int i = Integer.parseInt(input.toLowerCase().replace("l", ""));
                    return addExperience(context.getSource(), Collections.singleton(context.getSource().getPlayer()), i, true);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendError(new TranslatableText("commands.xp.nan"));
                }
            } else {
                try {
                    int i = Integer.parseInt(input);
                    return addExperience(context.getSource(), Collections.singleton(context.getSource().getPlayer()), i, false);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendError(new TranslatableText("commands.xp.nan"));
                }
            }
            return 0;
        }).then(CommandManager.argument("players", EntityArgumentType.players()).executes(context -> {
            String input = StringArgumentType.getString(context, "amount[L]");
            if (input.toLowerCase().endsWith("l")) {
                try {
                    int i = Integer.parseInt(input.toLowerCase().replace("l", ""));
                    return addExperience(context.getSource(), EntityArgumentType.getPlayers(context, "players"), i, true);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendError(/*new TranslatableText("gamemodeoverhaul.command.xp.nan")*/ new LiteralText("Not a number!"));
                }
            } else {
                try {
                    int i = Integer.parseInt(input);
                    return addExperience(context.getSource(), EntityArgumentType.getPlayers(context, "players"), i, false);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendError(/*new TranslatableText("gamemodeoverhaul.command.xp.nan")*/ new LiteralText("Not a number!"));
                }
            }
            return 0;
        }))));
    }
}
