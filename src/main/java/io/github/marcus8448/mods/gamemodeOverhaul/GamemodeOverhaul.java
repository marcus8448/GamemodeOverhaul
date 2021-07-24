/*
 * Copyright (C) 2019-2020 marcus8448
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
 */
package io.github.marcus8448.mods.gamemodeOverhaul;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Collection;
import java.util.Collections;

@Mod("gamemodeoverhaul")
public class GamemodeOverhaul {
    static final Logger LOGGER = LogManager.getLogger("GamemodeOverhaul");
    private static final Marker GAMEMODE_OVERHAUL = MarkerManager.getMarker("GamemodeOverhaul");
    private static final Marker DEBUG = MarkerManager.getMarker("Debug");
    private static final Marker ERROR = MarkerManager.getMarker("Error");

    private static final DynamicCommandExceptionType FAILED_EXCEPTION = new DynamicCommandExceptionType((object) -> new TranslatableComponent("commands.difficulty.failure", object));

    public GamemodeOverhaul() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(Type.COMMON, GMOConfig.commonSpec);
        FMLJavaModLoadingContext.get().getModEventBus().register(GMOConfig.class);
        LOGGER.info(GAMEMODE_OVERHAUL, "GamemodeOverhaul has been loaded!");
        LOGGER.info(GAMEMODE_OVERHAUL, "If you find an issue, please report it to: https://github.com/marcus8448/GamemodeOverhaul/issues/ ");
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void registerCommands(RegisterCommandsEvent event) {
        try {
            CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
            LOGGER.info(GAMEMODE_OVERHAUL, "Registering Commands...");

            this.registerGamemode(dispatcher);
            if (GMOConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug(DEBUG, "Successfully registered '/gamemode' integer + short values!");
            }

            if (GMOConfig.COMMON.enableGMCommand.get()) {
                this.registerGM(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/gm'!");
                }
            }

            if (GMOConfig.COMMON.enableReallyShortGMCommands.get()) {
                this.registerShortGM(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/gm*' commands!");
                }
            }

            this.registerDefaultGamemode(dispatcher);
            if (GMOConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug(DEBUG, "Successfully registered '/defaultgamemode' integer + short values!");
            }

            if (GMOConfig.COMMON.enableDGMCommand.get()) {
                this.registerDGM(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/dgm' command!");
                }
            }

            if (GMOConfig.COMMON.enableDifficultyCommand.get()) {
                this.registerDifficulty(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/difficulty' integer values!");
                }
            }

            if (GMOConfig.COMMON.enableKillCommand.get()) {
                this.registerKill(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered immediate '/kill'");
                }
            }

            if (GMOConfig.COMMON.enableToggledownfallCommand.get()) {
                this.registerToggledownfall(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/toggledownfall'");
                }
            }

            if (GMOConfig.COMMON.enableXPCommand.get()) {
                this.registerXP(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/{}'", GMOConfig.COMMON.xpCommandID.get());
                }
            }
            LOGGER.info(GAMEMODE_OVERHAUL, "All commands have successfully been registered!");
        } catch (Throwable var4) {
            LOGGER.fatal(ERROR, "Failed to register commands!");
            var4.printStackTrace();
            LOGGER.fatal(ERROR, "Please report this to the issues page at https://github.com/marcus8448/GamemodeOverhaul/issues/ ");
            LOGGER.fatal(ERROR, "You will still be able to play regularly, but you won't be able to use certain (or all) things added by the mod ('/gm' etc)");
        }

    }

    private static void sendGameModeFeedback(CommandSourceStack source, ServerPlayer player, GameType gameTypeIn) {
        Component itextcomponent = new TranslatableComponent("gameMode." + gameTypeIn.getName());
        if (source.getEntity() == player) {
            source.sendSuccess(new TranslatableComponent("commands.gamemode.success.self", itextcomponent), true);
        } else {
            if (source.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                player.sendMessage(new TranslatableComponent("gameMode.changed", itextcomponent), Util.NIL_UUID);
            }
            source.sendSuccess(new TranslatableComponent("commands.gamemode.success.other", player.getDisplayName(), itextcomponent), true);
        }
    }

    private int setGameMode(CommandContext<CommandSourceStack> source, Collection<? extends Entity> players, GameType gameTypeIn) {
        int i = 0;

        for (Entity entity : players) {
            if (entity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer) entity;
                if (player.gameMode.getGameModeForPlayer() != gameTypeIn) {
                    player.setGameMode(gameTypeIn);
                    sendGameModeFeedback(source.getSource(), player, gameTypeIn);
                    ++i;
                }
            }
        }
        return i;
    }

    private int setGameType(CommandSourceStack commandSourceIn, GameType gamemode) {
        int i = 0;
        MinecraftServer minecraftserver = commandSourceIn.getServer();
        minecraftserver.setDefaultGameType(gamemode);
        if (minecraftserver.getForcedGameType() != null) {
            for (ServerPlayer player : minecraftserver.getPlayerList().getPlayers()) {
                if (player.gameMode.getGameModeForPlayer() != gamemode) {
                    player.setGameMode(gamemode);
                    ++i;
                }
            }
        }
        commandSourceIn.sendSuccess(new TranslatableComponent("commands.defaultgamemode.success", gamemode.getLongDisplayName()), true);
        return i;
    }

    private int setDifficulty(CommandSourceStack source, Difficulty difficulty) throws CommandSyntaxException {
        MinecraftServer minecraftserver = source.getServer();
        if (source.getLevel().getDifficulty() == difficulty) {
            throw FAILED_EXCEPTION.create(difficulty.getKey());
        } else {
            minecraftserver.setDifficulty(difficulty, true);
            source.sendSuccess(new TranslatableComponent("commands.difficulty.success", difficulty.getDisplayName()), true);
            return 0;
        }
    }

    private int killEntities(CommandSourceStack source) {
        Entity e = source.getEntity();
        if (e != null) {
            e.kill();
            source.sendSuccess(new TranslatableComponent("commands.kill.success.single", e.getDisplayName()), true);
        }
        return 0;
    }

    private static int addExperience(CommandSourceStack source, Collection<ServerPlayer> players, int amount, boolean levels) {
        if (players.size() <= 0) {
            return 0;
        }
        for (ServerPlayer player : players) {
            if (levels) {
                player.giveExperienceLevels(amount);
            } else {
                player.giveExperiencePoints(amount);
            }
        }
        if (players.size() == 1) {
            source.sendSuccess(new TranslatableComponent("commands.experience.add." + (levels ? "levels" : "points") + ".success.single", amount, players.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslatableComponent("commands.experience.add." + (levels ? "levels" : "points") + ".success.multiple", amount, players.size()), true);
        }
        return players.size();
    }

    private void registerDGM(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("dgm").requires((source) -> source.hasPermission(2));

        for (GameType gametype : GameType.values()) {
                literalArgumentBuilder.then(Commands.literal(gametype.getName()).executes((context) -> setGameType(context.getSource(), gametype)));
            
        }
        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("dgm").requires((source) -> source.hasPermission(2));

        for (GameType gametype : GameType.values()) {
                literalArgumentBuilder.then(Commands.literal(Integer.toString(gametype.getId())).executes((context) -> setGameType(context.getSource(), gametype)));
            
        }
        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("dgm").requires((source) -> source.hasPermission(2));

        for (GameType gametype : GameType.values()) {
                if (gametype != GameType.SPECTATOR) {
                    literalArgumentBuilder.then(Commands.literal(Character.toString(gametype.getName().toLowerCase().charAt(0))).executes((context) -> setGameType(context.getSource(), gametype)));
                } else {
                    literalArgumentBuilder.then(Commands.literal("sp").executes((context) -> setGameType(context.getSource(), gametype)));
                }
        }
        dispatcher.register(literalArgumentBuilder);
    }

    private void registerDefaultGamemode(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("defaultgamemode").requires((source) -> source.hasPermission(2));

        for (GameType gametype : GameType.values()) {
                literalArgumentBuilder.then(Commands.literal(Integer.toString(gametype.getId())).executes((context) -> setGameType(context.getSource(), gametype)));
            
        }
        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("defaultgamemode").requires((source) -> source.hasPermission(2));

        for (GameType gametype : GameType.values()) {
                if (gametype != GameType.SPECTATOR) {
                    literalArgumentBuilder.then(Commands.literal(Character.toString(gametype.getName().toLowerCase().charAt(0))).executes((context) -> setGameType(context.getSource(), gametype)));
                } else {
                    literalArgumentBuilder.then(Commands.literal("sp").executes((context) -> setGameType(context.getSource(), gametype)));
                }
            
        }
        dispatcher.register(literalArgumentBuilder);
    }

    private void registerDifficulty(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("difficulty").requires((source) -> source.hasPermission(2));
        Difficulty[] difficulties = Difficulty.values();

        for (Difficulty difficulty : difficulties) {
            literalArgumentBuilder.then(Commands.literal(Integer.toString(difficulty.getId())).executes((context) -> setDifficulty(context.getSource(), difficulty)));
        }

        dispatcher.register(literalArgumentBuilder);
    }

    private void registerGM(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermission(2));
        GameType[] gameTypes = GameType.values();

        for (GameType gameType : gameTypes) {
                literalArgumentBuilder.then((Commands.literal(gameType.getName()).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().getPlayerOrException()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
            
        }

        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermission(2));

        for (GameType gameType : gameTypes) {
                literalArgumentBuilder.then((Commands.literal(Integer.toString(gameType.getId())).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().getPlayerOrException()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
            
        }

        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermission(2));

        for (GameType gameType : gameTypes) {
                if (gameType != GameType.SPECTATOR) {
                    literalArgumentBuilder.then((Commands.literal(Character.toString(gameType.getName().toLowerCase().charAt(0))).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().getPlayerOrException()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
                } else {
                    literalArgumentBuilder.then((Commands.literal("sp").executes((context) -> setGameMode(context, Collections.singleton(context.getSource().getPlayerOrException()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
                }
            
        }

        dispatcher.register(literalArgumentBuilder);
    }

    private void registerGamemode(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("gamemode").requires((commandSource) -> commandSource.hasPermission(2));
        GameType[] gameTypes = GameType.values();

        for (GameType gameType : gameTypes) {
                literalArgumentBuilder.then((Commands.literal(Integer.toString(gameType.getId())).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().getPlayerOrException()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
            
        }

        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("gamemode").requires((commandSource) -> commandSource.hasPermission(2));

        for (GameType gameType : gameTypes) {
                if (gameType != GameType.SPECTATOR) {
                    literalArgumentBuilder.then((Commands.literal(Character.toString(gameType.getName().toLowerCase().charAt(0))).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().getPlayerOrException()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
                } else {
                    literalArgumentBuilder.then((Commands.literal("sp").executes((context) -> setGameMode(context, Collections.singleton(context.getSource().getPlayerOrException()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
                }
            
        }

        dispatcher.register(literalArgumentBuilder);
    }

    private void registerKill(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kill").requires((source) -> source.hasPermission(2)).executes((context) -> killEntities(context.getSource())));
    }

    private void registerShortGM(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gms").requires((source) -> source.hasPermission(2)).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().getPlayerOrException()), GameType.SURVIVAL)).then(Commands.argument("targets", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "targets"), GameType.SURVIVAL))));
        dispatcher.register(Commands.literal("gmc").requires((source) -> source.hasPermission(2)).executes((context) -> setGameMode(context, Collections.singleton((context.getSource()).getPlayerOrException()), GameType.CREATIVE)).then(Commands.argument("targets", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "targets"), GameType.CREATIVE))));
        dispatcher.register(Commands.literal("gma").requires((source) -> source.hasPermission(2)).executes((context) -> setGameMode(context, Collections.singleton((context.getSource()).getPlayerOrException()), GameType.ADVENTURE)).then(Commands.argument("targets", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "targets"), GameType.ADVENTURE))));
        dispatcher.register(Commands.literal("gmsp").requires((source) -> source.hasPermission(2)).executes((context) -> setGameMode(context, Collections.singleton((context.getSource()).getPlayerOrException()), GameType.SPECTATOR)).then(Commands.argument("targets", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "targets"), GameType.SPECTATOR))));
    }

    private void registerToggledownfall(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("toggledownfall").requires((source) -> source.hasPermission(2)).executes(context -> {
            if (!(context.getSource().getLevel().isRaining() || context.getSource().getLevel().getLevelData().isRaining() || context.getSource().getLevel().isThundering() || context.getSource().getLevel().getLevelData().isThundering())) {
                context.getSource().getLevel().setWeatherParameters(0, 6000, true, false);
            } else {
                context.getSource().getLevel().setWeatherParameters(6000, 0, false, false);
            }
            context.getSource().sendSuccess(new TranslatableComponent("commands.toggledownfall"), false);
            return 6000;
        }));
    }

    private void registerXP(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal(GMOConfig.COMMON.xpCommandID.get()).requires((source) -> source.hasPermission(2)).then(Commands.argument("amount[L]", StringArgumentType.word()).executes(context -> {
            String input = StringArgumentType.getString(context, "amount[L]").toUpperCase();
            if (input.endsWith("L")) {
                try {
                    int i = Integer.parseInt(input.replace("L", ""));
                    return addExperience(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()), i, true);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.xp.nan"));
                }
            } else {
                try {
                    int i = Integer.parseInt(input);
                    return addExperience(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException()), i, false);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.xp.nan"));
                }
            }
            return 0;
        }).then(Commands.argument("players", EntityArgument.players()).executes(context -> {
            String input = StringArgumentType.getString(context, "amount[L]").toUpperCase();
            if (input.endsWith("L")) {
                try {
                    return addExperience(context.getSource(), EntityArgument.getPlayers(context, "players"), Integer.parseInt(input.replace("L", "")), true);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.xp.nan"));
                }
            } else {
                try {
                    return addExperience(context.getSource(), EntityArgument.getPlayers(context, "players"), Integer.parseInt(input), false);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendFailure(new TranslatableComponent("commands.xp.nan"));
                }
            }
            return 0;
        })));
        dispatcher.register(literalArgumentBuilder);
    }
}
