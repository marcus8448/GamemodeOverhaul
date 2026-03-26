/*
 * GamemodeOverhaul
 * Copyright (C) 2019-2026 marcus8448
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package dev.mlow.mods.gamemodeoverhaul;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.mlow.mods.gamemodeoverhaul.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamemodeOverhaulCommon {
    public static final String MOD_ID = "gamemodeoverhaul";
    public static final String MOD_NAME = "Gamemode Overhaul";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final GamemodeOverhaulConfig CONFIG = Services.PLATFORM.createConfig();

    public static void registerCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        if (CONFIG.enableGamemode()) registerGamemode(dispatcher);
        if (CONFIG.enableGm()) registerGm(dispatcher);
        if (CONFIG.enableNoArgsGm()) registerGmNoArgs(dispatcher);
        if (CONFIG.enableDefaultGamemode()) registerDefaultGamemode(dispatcher);
        if (CONFIG.enableDgm()) registerDgm(dispatcher);
        if (CONFIG.enableDifficulty()) registerDifficulty(dispatcher);
        if (CONFIG.enableToggledownfall()) registerToggleDownfall(dispatcher);

        LOGGER.info("Commands registered!");
    }

    private static void registerGamemode(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("gamemode").requires(Commands.hasPermission(GameModeCommand.PERMISSION_CHECK));
        for (GameType type : GameType.values()) {
            node.then(Commands.literal(String.valueOf(type.ordinal()))
                            .executes(gamemodeCommand(dispatcher, type))
                            .then(Commands.argument("target", EntityArgument.players())
                                    .executes(targettedGamemodeCommand(dispatcher, type))))
                    .then(Commands.literal(createShort(type))
                            .executes(gamemodeCommand(dispatcher, type))
                            .then(Commands.argument("target", EntityArgument.players())
                                    .executes(targettedGamemodeCommand(dispatcher, type))));
        }
        dispatcher.register(node);
    }

    private static void registerGm(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> gm = Commands.literal("gm").requires(Commands.hasPermission(GameModeCommand.PERMISSION_CHECK)).build();
        for (CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChild("gamemode").getChildren()) {
            gm.addChild(node);
        }
        dispatcher.getRoot().addChild(gm);
    }

    private static void registerGmNoArgs(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        for (GameType type : GameType.values()) {
            dispatcher.register(Commands.literal("gm" + createShort(type))
                    .requires(Commands.hasPermission(GameModeCommand.PERMISSION_CHECK))
                    .executes(gamemodeCommand(dispatcher, type))
                    .then(Commands.argument("target", EntityArgument.players())
                            .executes(targetedGamemodeCommandShort(dispatcher, type))));
        }
    }

    private static void registerDefaultGamemode(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("defaultgamemode").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (GameType type : GameType.values()) {
            node.then(Commands.literal(String.valueOf(type.ordinal())).executes(defaultgamemodeCommand(dispatcher, type)))
                    .then(Commands.literal(createShort(type)).executes(defaultgamemodeCommand(dispatcher, type)));
        }
        dispatcher.register(node);
    }

    private static void registerDgm(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> gm = Commands.literal("dgm").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).build();
        for (CommandNode<CommandSourceStack> node : dispatcher.getRoot().getChild("defaultgamemode").getChildren()) {
            gm.addChild(node);
        }
        dispatcher.getRoot().addChild(gm);
    }

    private static void registerDifficulty(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("difficulty").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        CommandNode<CommandSourceStack> difficulty = dispatcher.getRoot().getChild("difficulty");
        for (Difficulty value : Difficulty.values()) {
            node.then(Commands.literal(String.valueOf(value.ordinal()))
                    .executes(difficulty.getChild(value.getSerializedName()).getCommand()));
        }
        dispatcher.register(node);
    }

    private static void registerToggleDownfall(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("toggledownfall").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes((context) -> toggleDownfall(context.getSource())));
    }

    private static int toggleDownfall(@NotNull CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        if (server.getWeatherData().isRaining() || server.getWeatherData().isThundering()) {
            server.setWeatherParameters(6000, 0, false, false);
        } else {
            server.setWeatherParameters(0, 6000, true, false);
        }
        source.sendSuccess(() -> Component.translatable("commands.toggle_downfall"), false);
        return 1;
    }

    public static String getNthArgument(CommandContext<CommandSourceStack> context, int n) {
        return context.getNodes().get(n).getRange().get(context.getInput());
    }

    @NotNull
    private static Command<CommandSourceStack> gamemodeCommand(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, GameType type) {
        return context -> dispatcher.execute("gamemode " + type.getName(), context.getSource());
    }

    @NotNull
    private static Command<CommandSourceStack> defaultgamemodeCommand(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, GameType type) {
        return context -> dispatcher.execute("defaultgamemode " + type.getName(), context.getSource());
    }

    @NotNull
    private static Command<CommandSourceStack> targettedGamemodeCommand(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, GameType type) {
        return context -> dispatcher.execute("gamemode " + type.getName() + " " + getNthArgument(context, 2), context.getSource());
    }

    @NotNull
    private static Command<CommandSourceStack> targetedGamemodeCommandShort(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, GameType type) {
        return context -> dispatcher.execute("gamemode " + type.getName() + " " + getNthArgument(context, 1), context.getSource());
    }

    @Contract(pure = true)
    public static @NotNull String createShort(@NotNull GameType type) {
        return switch (type) {
            case SURVIVAL -> "s";
            case CREATIVE -> "c";
            case ADVENTURE -> "a";
            case SPECTATOR -> "sp";
        };
    }
}