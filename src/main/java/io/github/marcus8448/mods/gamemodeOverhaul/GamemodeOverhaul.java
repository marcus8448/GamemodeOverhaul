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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
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

    private static final DynamicCommandExceptionType FAILED_EXCEPTION = new DynamicCommandExceptionType((object) -> new TranslationTextComponent("commands.difficulty.failure", object));

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
            CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
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

    private static void sendGameModeFeedback(CommandSource source, ServerPlayerEntity player, GameType gameTypeIn) {
        ITextComponent itextcomponent = new TranslationTextComponent("gameMode." + gameTypeIn.getName());
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslationTextComponent("commands.gamemode.success.self", itextcomponent), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(new TranslationTextComponent("gameMode.changed", itextcomponent), Util.DUMMY_UUID);
            }
            source.sendFeedback(new TranslationTextComponent("commands.gamemode.success.other", player.getDisplayName(), itextcomponent), true);
        }
    }

    private int setGameMode(CommandContext<CommandSource> source, Collection<? extends Entity> players, GameType gameTypeIn) {
        int i = 0;

        for (Entity entity : players) {
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                if (player.interactionManager.getGameType() != gameTypeIn) {
                    player.setGameType(gameTypeIn);
                    sendGameModeFeedback(source.getSource(), player, gameTypeIn);
                    ++i;
                }
            }
        }
        return i;
    }

    private int setGameType(CommandSource commandSourceIn, GameType gamemode) {
        int i = 0;
        MinecraftServer minecraftserver = commandSourceIn.getServer();
        minecraftserver.setGameType(gamemode);
        if (minecraftserver.getForceGamemode()) {
            for (ServerPlayerEntity player : minecraftserver.getPlayerList().getPlayers()) {
                if (player.interactionManager.getGameType() != gamemode) {
                    player.setGameType(gamemode);
                    ++i;
                }
            }
        }
        commandSourceIn.sendFeedback(new TranslationTextComponent("commands.defaultgamemode.success", gamemode.getDisplayName()), true);
        return i;
    }

    private int setDifficulty(CommandSource source, Difficulty difficulty) throws CommandSyntaxException {
        MinecraftServer minecraftserver = source.getServer();
        if (source.getWorld().getDifficulty() == difficulty) {
            throw FAILED_EXCEPTION.create(difficulty.getTranslationKey());
        } else {
            minecraftserver.setDifficultyForAllWorlds(difficulty, true);
            source.sendFeedback(new TranslationTextComponent("commands.difficulty.success", difficulty.getDisplayName()), true);
            return 0;
        }
    }

    private int killEntities(CommandSource source) {
        Entity e = source.getEntity();
        if (e != null) {
            e.onKillCommand();
            source.sendFeedback(new TranslationTextComponent("commands.kill.success.single", e.getDisplayName()), true);
        }
        return 0;
    }

    private static int addExperience(CommandSource source, Collection<ServerPlayerEntity> players, int amount, boolean levels) {
        if (players.size() <= 0) {
            return 0;
        }
        for (ServerPlayerEntity player : players) {
            if (levels) {
                player.addExperienceLevel(amount);
            } else {
                player.giveExperiencePoints(amount);
            }
        }
        if (players.size() == 1) {
            source.sendFeedback(new TranslationTextComponent("commands.experience.add." + (levels ? "levels" : "points") + ".success.single", amount, players.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(new TranslationTextComponent("commands.experience.add." + (levels ? "levels" : "points") + ".success.multiple", amount, players.size()), true);
        }
        return players.size();
    }

    private void registerDGM(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = Commands.literal("dgm").requires((source) -> source.hasPermissionLevel(2));

        for (GameType gametype : GameType.values()) {
            if (gametype != GameType.NOT_SET) {
                literalArgumentBuilder.then(Commands.literal(gametype.getName()).executes((context) -> setGameType(context.getSource(), gametype)));
            }
        }
        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("dgm").requires((source) -> source.hasPermissionLevel(2));

        for (GameType gametype : GameType.values()) {
            if (gametype != GameType.NOT_SET) {
                literalArgumentBuilder.then(Commands.literal(Integer.toString(gametype.getID())).executes((context) -> setGameType(context.getSource(), gametype)));
            }
        }
        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("dgm").requires((source) -> source.hasPermissionLevel(2));

        for (GameType gametype : GameType.values()) {
            if (gametype != GameType.NOT_SET) {
                if (gametype != GameType.SPECTATOR) {
                    literalArgumentBuilder.then(Commands.literal(Character.toString(gametype.getName().toLowerCase().charAt(0))).executes((context) -> setGameType(context.getSource(), gametype)));
                } else {
                    literalArgumentBuilder.then(Commands.literal("sp").executes((context) -> setGameType(context.getSource(), gametype)));
                }
            }
        }
        dispatcher.register(literalArgumentBuilder);
    }

    private void registerDefaultGamemode(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = Commands.literal("defaultgamemode").requires((source) -> source.hasPermissionLevel(2));

        for (GameType gametype : GameType.values()) {
            if (gametype != GameType.NOT_SET) {
                literalArgumentBuilder.then(Commands.literal(Integer.toString(gametype.getID())).executes((context) -> setGameType(context.getSource(), gametype)));
            }
        }
        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("defaultgamemode").requires((source) -> source.hasPermissionLevel(2));

        for (GameType gametype : GameType.values()) {
            if (gametype != GameType.NOT_SET) {
                if (gametype != GameType.SPECTATOR) {
                    literalArgumentBuilder.then(Commands.literal(Character.toString(gametype.getName().toLowerCase().charAt(0))).executes((context) -> setGameType(context.getSource(), gametype)));
                } else {
                    literalArgumentBuilder.then(Commands.literal("sp").executes((context) -> setGameType(context.getSource(), gametype)));
                }
            }
        }
        dispatcher.register(literalArgumentBuilder);
    }

    private void registerDifficulty(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = Commands.literal("difficulty").requires((source) -> source.hasPermissionLevel(2));
        Difficulty[] difficulties = Difficulty.values();

        for (Difficulty difficulty : difficulties) {
            literalArgumentBuilder.then(Commands.literal(Integer.toString(difficulty.getId())).executes((context) -> setDifficulty(context.getSource(), difficulty)));
        }

        dispatcher.register(literalArgumentBuilder);
    }

    private void registerGM(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermissionLevel(2));
        GameType[] gameTypes = GameType.values();

        for (GameType gameType : gameTypes) {
            if (gameType != GameType.NOT_SET) {
                literalArgumentBuilder.then((Commands.literal(gameType.getName()).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
            }
        }

        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermissionLevel(2));

        for (GameType gameType : gameTypes) {
            if (gameType != GameType.NOT_SET) {
                literalArgumentBuilder.then((Commands.literal(Integer.toString(gameType.getID())).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
            }
        }

        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermissionLevel(2));

        for (GameType gameType : gameTypes) {
            if (gameType != GameType.NOT_SET) {
                if (gameType != GameType.SPECTATOR) {
                    literalArgumentBuilder.then((Commands.literal(Character.toString(gameType.getName().toLowerCase().charAt(0))).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
                } else {
                    literalArgumentBuilder.then((Commands.literal("sp").executes((context) -> setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
                }
            }
        }

        dispatcher.register(literalArgumentBuilder);
    }

    private void registerGamemode(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = Commands.literal("gamemode").requires((commandSource) -> commandSource.hasPermissionLevel(2));
        GameType[] gameTypes = GameType.values();

        for (GameType gameType : gameTypes) {
            if (gameType != GameType.NOT_SET) {
                literalArgumentBuilder.then((Commands.literal(Integer.toString(gameType.getID())).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
            }
        }

        dispatcher.register(literalArgumentBuilder);

        literalArgumentBuilder = Commands.literal("gamemode").requires((commandSource) -> commandSource.hasPermissionLevel(2));

        for (GameType gameType : gameTypes) {
            if (gameType != GameType.NOT_SET) {
                if (gameType != GameType.SPECTATOR) {
                    literalArgumentBuilder.then((Commands.literal(Character.toString(gameType.getName().toLowerCase().charAt(0))).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
                } else {
                    literalArgumentBuilder.then((Commands.literal("sp").executes((context) -> setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "target"), gameType))));
                }
            }
        }

        dispatcher.register(literalArgumentBuilder);
    }

    private void registerKill(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("kill").requires((source) -> source.hasPermissionLevel(2)).executes((context) -> killEntities(context.getSource())));
    }

    private void registerShortGM(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("gms").requires((source) -> source.hasPermissionLevel(2)).executes((context) -> setGameMode(context, Collections.singleton(context.getSource().asPlayer()), GameType.SURVIVAL)).then(Commands.argument("targets", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "targets"), GameType.SURVIVAL))));
        dispatcher.register(Commands.literal("gmc").requires((source) -> source.hasPermissionLevel(2)).executes((context) -> setGameMode(context, Collections.singleton((context.getSource()).asPlayer()), GameType.CREATIVE)).then(Commands.argument("targets", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "targets"), GameType.CREATIVE))));
        dispatcher.register(Commands.literal("gma").requires((source) -> source.hasPermissionLevel(2)).executes((context) -> setGameMode(context, Collections.singleton((context.getSource()).asPlayer()), GameType.ADVENTURE)).then(Commands.argument("targets", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "targets"), GameType.ADVENTURE))));
        dispatcher.register(Commands.literal("gmsp").requires((source) -> source.hasPermissionLevel(2)).executes((context) -> setGameMode(context, Collections.singleton((context.getSource()).asPlayer()), GameType.SPECTATOR)).then(Commands.argument("targets", EntityArgument.players()).executes((cmdContext) -> setGameMode(cmdContext, EntityArgument.getPlayers(cmdContext, "targets"), GameType.SPECTATOR))));
    }

    private void registerToggledownfall(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("toggledownfall").requires((source) -> source.hasPermissionLevel(2)).executes(context -> {
            if (!(context.getSource().getWorld().isRaining() || context.getSource().getWorld().getWorldInfo().isRaining() || context.getSource().getWorld().isThundering() || context.getSource().getWorld().getWorldInfo().isThundering())) {
                context.getSource().getWorld().func_241113_a_(0, 6000, true, false);
            } else {
                context.getSource().getWorld().func_241113_a_(6000, 0, false, false);
            }
            context.getSource().sendFeedback(new TranslationTextComponent("commands.toggledownfall"), false);
            return 6000;
        }));
    }

    private void registerXP(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilder = Commands.literal(GMOConfig.COMMON.xpCommandID.get()).requires((source) -> source.hasPermissionLevel(2)).then(Commands.argument("amount[L]", StringArgumentType.word()).executes(context -> {
            String input = StringArgumentType.getString(context, "amount[L]").toUpperCase();
            if (input.endsWith("L")) {
                try {
                    int i = Integer.parseInt(input.replace("L", ""));
                    return addExperience(context.getSource(), Collections.singleton(context.getSource().asPlayer()), i, true);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendErrorMessage(new TranslationTextComponent("commands.xp.nan"));
                }
            } else {
                try {
                    int i = Integer.parseInt(input);
                    return addExperience(context.getSource(), Collections.singleton(context.getSource().asPlayer()), i, false);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendErrorMessage(new TranslationTextComponent("commands.xp.nan"));
                }
            }
            return 0;
        }).then(Commands.argument("players", EntityArgument.players()).executes(context -> {
            String input = StringArgumentType.getString(context, "amount[L]").toUpperCase();
            if (input.endsWith("L")) {
                try {
                    return addExperience(context.getSource(), EntityArgument.getPlayers(context, "players"), Integer.parseInt(input.replace("L", "")), true);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendErrorMessage(new TranslationTextComponent("commands.xp.nan"));
                }
            } else {
                try {
                    return addExperience(context.getSource(), EntityArgument.getPlayers(context, "players"), Integer.parseInt(input), false);
                } catch (NumberFormatException ignore) {
                    context.getSource().sendErrorMessage(new TranslationTextComponent("commands.xp.nan"));
                }
            }
            return 0;
        })));
        dispatcher.register(literalArgumentBuilder);
    }
}
