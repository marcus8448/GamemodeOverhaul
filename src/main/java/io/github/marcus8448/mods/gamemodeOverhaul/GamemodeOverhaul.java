package io.github.marcus8448.mods.gamemodeOverhaul;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Copyright (c) marcus8448 2019. All rights reserved.
 * @author marcus8448
 */
@SuppressWarnings({"unused", "unchecked"})
@Mod("gamemodeoverhaul")
public class GamemodeOverhaul {
    static final Logger LOGGER = LogManager.getLogger("GamemodeOverhaul");
    private static final Marker GAMEMODE_OVERHAUL = MarkerManager.getMarker("GamemodeOverhaul");
    private static final Marker DEBUG = MarkerManager.getMarker("Debug");
    private static final Marker LOADING = MarkerManager.getMarker("Loading...");
    private static final Marker SUCCESS = MarkerManager.getMarker("Success!");
    private static final Marker ERROR = MarkerManager.getMarker("Error");

    private static final DynamicCommandExceptionType FAILED_EXCEPTION = new DynamicCommandExceptionType((object) -> new TextComponentTranslation("commands.difficulty.failure", object));

    public GamemodeOverhaul() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(Type.COMMON, GMOConfig.commonSpec);
        FMLJavaModLoadingContext.get().getModEventBus().register(GMOConfig.class);
        LOGGER.info(GAMEMODE_OVERHAUL, "GamemodeOverhaul has been loaded!");
        LOGGER.info(GAMEMODE_OVERHAUL, "If you find an issue, please report it to: https://github.com/marcus8448/GamemodeOverhaul/issues/ ");
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        CommandDispatcher dispatcher = event.getCommandDispatcher();
        LOGGER.info(LOADING, "Registering Commands...");

        try {
            GamemodeOverhaul.GameModeCommandInteger.register(dispatcher);
            if (GMOConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug(DEBUG, "Successfully registered '/gamemode' integer values!");
            }

            GamemodeOverhaul.GameModeCommandShort.register(dispatcher);
            if (GMOConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug(DEBUG, "Successfully registered '/gamemode' short values!");
            }

            if (GMOConfig.COMMON.enableGMCommand.get()) {
                GamemodeOverhaul.GMCommand.register(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/gm'!");
                }

                GamemodeOverhaul.GMCommandInteger.register(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/gm' integer values!");
                }

                GamemodeOverhaul.GMCommandShort.register(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/gm' short values!");
                }
            }

            if (GMOConfig.COMMON.enableReallyShortGMCommands.get()) {
                GamemodeOverhaul.ReallyShortGMCommand.register(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/gm*' commands!");
                }
            }

            GamemodeOverhaul.DefaultGamemodeInteger.register(dispatcher);
            if (GMOConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug(DEBUG, "Successfully registered '/defaultgamemode' integer values!");
            }

            GamemodeOverhaul.DefaultGamemodeShort.register(dispatcher);
            if (GMOConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug(DEBUG, "Successfully registered '/defaultgamemode' short values!");
            }

            if (GMOConfig.COMMON.enableDGMCommand.get()) {
                GamemodeOverhaul.DGMCommand.register(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/dgm' command!");
                }

                GamemodeOverhaul.DGMInteger.register(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/dgm' integer values!");
                }

                GamemodeOverhaul.DGMShort.register(dispatcher);
                if (GMOConfig.COMMON.enableDebugLogging.get()) {
                    LOGGER.debug(DEBUG, "Successfully registered '/dgm' short values!");
                }
            }

            GamemodeOverhaul.DifficultyCommandInteger.register(dispatcher);
            if (GMOConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug(DEBUG, "Successfully registered '/difficulty' integer values!");
            }

            GamemodeOverhaul.ImmediateKillCommand.register(dispatcher);
            if (GMOConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug(DEBUG, "Successfully registered immediate '/kill'");
            }

            LOGGER.info(SUCCESS, "All commands have successfully been registered!");
        } catch (Exception var4) {
            LOGGER.fatal(ERROR, "Could not register commands! Side: {}", FMLEnvironment.dist.name());
            LOGGER.fatal(ERROR, ExceptionUtils.getStackTrace(var4));
            LOGGER.fatal(ERROR, "Please report this to the issues page at https://github.com/marcus8448/GamemodeOverhaul/issues/ ");
            LOGGER.fatal(ERROR, "You will still be able to play regularly, but you won't be able to use certain (or all) things added by the mod ('/gm' etc)");
        }

    }

    @SubscribeEvent
    public void welcomeMessage(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP && GMOConfig.COMMON.enableWelcomeMessage.get()) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
            player.sendMessage((new TextComponentTranslation("gamemodeoverhaul.welcomemessage")).setStyle((new Style()).setColor(TextFormatting.BLUE)), ChatType.SYSTEM);
        }

    }

    private static void sendGameModeFeedback(CommandSource source, EntityPlayerMP player, GameType gameTypeIn) {
        ITextComponent itextcomponent = new TextComponentTranslation("gameMode." + gameTypeIn.getName());
        if (source.getEntity() == player) {
            source.sendFeedback(new TextComponentTranslation("commands.gamemode.success.self", itextcomponent), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.sendMessage(new TextComponentTranslation("gameMode.changed", itextcomponent));
            }

            source.sendFeedback(new TextComponentTranslation("commands.gamemode.success.other", player.getDisplayName(), itextcomponent), true);
        }

    }

    private static int setGameMode(CommandContext<CommandSource> source, Collection<? extends Entity> players, GameType gameTypeIn) {
        int i = 0;

        for (Entity entity : players) {
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP entityplayermp = (EntityPlayerMP) entity;
                if (entityplayermp.interactionManager.getGameType() != gameTypeIn) {
                    entityplayermp.setGameType(gameTypeIn);
                    sendGameModeFeedback(source.getSource(), entityplayermp, gameTypeIn);
                    ++i;
                }
            }
        }

        return i;
    }

    /**
     * Set Gametype of player who ran the command
     *
     * @param commandSourceIn Source of /gamemode command
     * @param gamemode        New gametype
     */
    private static int setGameType(CommandSource commandSourceIn, GameType gamemode) {
        int i = 0;
        MinecraftServer minecraftserver = commandSourceIn.getServer();
        minecraftserver.setGameType(gamemode);
        if (minecraftserver.getForceGamemode()) {
            for (EntityPlayerMP entityplayermp : minecraftserver.getPlayerList().getPlayers()) {
                if (entityplayermp.interactionManager.getGameType() != gamemode) {
                    entityplayermp.setGameType(gamemode);
                    ++i;
                }
            }
        }

        commandSourceIn.sendFeedback(new TextComponentTranslation("commands.defaultgamemode.success", gamemode.getDisplayName()), true);
        return i;
    }

    private static int setDifficulty(CommandSource source, EnumDifficulty difficulty) throws CommandSyntaxException {
        MinecraftServer minecraftserver = source.getServer();
        if (minecraftserver.getWorld(DimensionType.OVERWORLD).getDifficulty() == difficulty) {
            throw FAILED_EXCEPTION.create(difficulty.getTranslationKey());
        } else {
            minecraftserver.setDifficultyForAllWorlds(difficulty);
            source.sendFeedback(new TextComponentTranslation("commands.difficulty.success", difficulty.getDisplayName()), true);
            return 0;
        }
    }

    private static int killEntities(CommandSource source) {
        Entity e = source.getEntity();
        e.onKillCommand();
        source.sendFeedback(new TextComponentTranslation("commands.kill.success.single", e.getDisplayName()), true);

        return 0;
    }

    public static class DGMCommand {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("dgm").requires((source) -> source.hasPermissionLevel(2));

            for (GameType gametype : GameType.values()) {
                if (gametype != GameType.NOT_SET) {
                    literalBuilder.then(Commands.literal(gametype.getName()).executes((context) -> GamemodeOverhaul.setGameType(context.getSource(), gametype)));
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class DGMInteger {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("dgm").requires((source) -> source.hasPermissionLevel(2));

            for (GameType gametype : GameType.values()) {
                if (gametype != GameType.NOT_SET) {
                    literalBuilder.then(Commands.literal(Integer.toString(gametype.getID())).executes((context) -> GamemodeOverhaul.setGameType(context.getSource(), gametype)));
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class DGMShort {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("dgm").requires((source) -> source.hasPermissionLevel(2));

            for (GameType gametype : GameType.values()) {
                if (gametype != GameType.NOT_SET) {
                    if (gametype != GameType.SPECTATOR) {
                        literalBuilder.then(Commands.literal(Character.toString(gametype.getName().toLowerCase().charAt(0))).executes((context) -> GamemodeOverhaul.setGameType(context.getSource(), gametype)));
                    } else {
                        literalBuilder.then(Commands.literal("sp").executes((context) -> GamemodeOverhaul.setGameType(context.getSource(), gametype)));
                    }
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class DefaultGamemodeInteger {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("defaultgamemode").requires((source) -> source.hasPermissionLevel(2));

            for (GameType gametype : GameType.values()) {
                if (gametype != GameType.NOT_SET) {
                    literalBuilder.then(Commands.literal(Integer.toString(gametype.getID())).executes((context) -> GamemodeOverhaul.setGameType(context.getSource(), gametype)));
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class DefaultGamemodeShort {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("defaultgamemode").requires((source) -> source.hasPermissionLevel(2));

            for (GameType gametype : GameType.values()) {
                if (gametype != GameType.NOT_SET) {
                    if (gametype != GameType.SPECTATOR) {
                        literalBuilder.then(Commands.literal(Character.toString(gametype.getName().toLowerCase().charAt(0))).executes((context) -> GamemodeOverhaul.setGameType(context.getSource(), gametype)));
                    } else {
                        literalBuilder.then(Commands.literal("sp").executes((context) -> GamemodeOverhaul.setGameType(context.getSource(), gametype)));
                    }
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class DifficultyCommandInteger {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("difficulty").requires((source) -> source.hasPermissionLevel(2));
            EnumDifficulty[] difficulties = EnumDifficulty.values();

            for (EnumDifficulty difficulty : difficulties) {
                literalBuilder.then(Commands.literal(Integer.toString(difficulty.getId())).executes((context) -> GamemodeOverhaul.setDifficulty(context.getSource(), difficulty)));
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class GMCommand {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermissionLevel(2));
            GameType[] gameTypes = GameType.values();

            for (GameType gameType : gameTypes) {
                if (gameType != GameType.NOT_SET) {
                    literalBuilder.then(((LiteralArgumentBuilder) Commands.literal(gameType.getName()).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "target"), gameType))));
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class GMCommandInteger {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermissionLevel(2));
            GameType[] gameTypes = GameType.values();

            for (GameType gameType : gameTypes) {
                if (gameType != GameType.NOT_SET) {
                    literalBuilder.then(((LiteralArgumentBuilder) Commands.literal(Integer.toString(gameType.getID())).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "target"), gameType))));
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class GMCommandShort {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("gm").requires((commandSource) -> commandSource.hasPermissionLevel(2));
            GameType[] gameTypes = GameType.values();

            for (GameType gameType : gameTypes) {
                if (gameType != GameType.NOT_SET) {
                    if (gameType != GameType.SPECTATOR) {
                        literalBuilder.then(((LiteralArgumentBuilder) Commands.literal(Character.toString(gameType.getName().toLowerCase().charAt(0))).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "target"), gameType))));
                    } else {
                        literalBuilder.then(((LiteralArgumentBuilder) Commands.literal("sp").executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "target"), gameType))));
                    }
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class GameModeCommandInteger {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("gamemode").requires((commandSource) -> commandSource.hasPermissionLevel(2));
            GameType[] gameTypes = GameType.values();

            for (GameType gameType : gameTypes) {
                if (gameType != GameType.NOT_SET) {
                    literalBuilder.then(((LiteralArgumentBuilder) Commands.literal(Integer.toString(gameType.getID())).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "target"), gameType))));
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class GameModeCommandShort {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("gamemode").requires((commandSource) -> commandSource.hasPermissionLevel(2));
            GameType[] gameTypes = GameType.values();

            for (GameType gameType : gameTypes) {
                if (gameType != GameType.NOT_SET) {
                    if (gameType != GameType.SPECTATOR) {
                        literalBuilder.then(((LiteralArgumentBuilder) Commands.literal(Character.toString(gameType.getName().toLowerCase().charAt(0))).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "target"), gameType))));
                    } else {
                        literalBuilder.then(((LiteralArgumentBuilder) Commands.literal("sp").executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(context.getSource().asPlayer()), gameType))).then(Commands.argument("target", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "target"), gameType))));
                    }
                }
            }

            dispatcher.register(literalBuilder);
        }
    }

    public static class ImmediateKillCommand {
        static void register(CommandDispatcher dispatcher) {
            LiteralArgumentBuilder literalBuilder = Commands.literal("kill").requires((source) -> source.hasPermissionLevel(2));
            literalBuilder.executes((context) -> GamemodeOverhaul.killEntities((CommandSource) context.getSource()));
            dispatcher.register(literalBuilder);
        }
    }

    public static class ReallyShortGMCommand {
        static void register(CommandDispatcher dispatcher) {
            dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) Commands.literal("gms").requires((source) -> source.hasPermissionLevel(2))).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(((CommandSource) context.getSource()).asPlayer()), GameType.SURVIVAL)).then(Commands.argument("targets", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "targets"), GameType.SURVIVAL))));
            dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) Commands.literal("gmc").requires((source) -> source.hasPermissionLevel(2))).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(((CommandSource) context.getSource()).asPlayer()), GameType.CREATIVE)).then(Commands.argument("targets", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "targets"), GameType.CREATIVE))));
            dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) Commands.literal("gma").requires((source) -> source.hasPermissionLevel(2))).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(((CommandSource) context.getSource()).asPlayer()), GameType.ADVENTURE)).then(Commands.argument("targets", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "targets"), GameType.ADVENTURE))));
            dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) Commands.literal("gmsp").requires((source) -> source.hasPermissionLevel(2))).executes((context) -> GamemodeOverhaul.setGameMode(context, Collections.singleton(((CommandSource) context.getSource()).asPlayer()), GameType.SPECTATOR)).then(Commands.argument("targets", EntityArgument.multipleEntities()).executes((cmdContext) -> GamemodeOverhaul.setGameMode(cmdContext, EntityArgument.getEntities(cmdContext, "targets"), GameType.SPECTATOR))));
        }
    }
}
