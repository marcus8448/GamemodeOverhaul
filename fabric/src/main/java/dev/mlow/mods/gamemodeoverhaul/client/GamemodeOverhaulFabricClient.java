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

package dev.mlow.mods.gamemodeoverhaul.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.mlow.mods.gamemodeoverhaul.GamemodeOverhaulCommon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GamemodeOverhaulFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            if (GamemodeOverhaulCommon.CONFIG.enableGamemode()) registerGamemode(dispatcher);
            if (GamemodeOverhaulCommon.CONFIG.enableGm()) registerGm(dispatcher);
            if (GamemodeOverhaulCommon.CONFIG.enableNoArgsGm()) registerGmNoArgs(dispatcher);
            if (GamemodeOverhaulCommon.CONFIG.enableDefaultGamemode()) registerDefaultGamemode(dispatcher);
            if (GamemodeOverhaulCommon.CONFIG.enableDgm()) registerDgm(dispatcher);
            if (GamemodeOverhaulCommon.CONFIG.enableDifficulty()) registerDifficulty(dispatcher);
            if (GamemodeOverhaulCommon.CONFIG.enableToggledownfall()) registerToggleDownfall(dispatcher);

            GamemodeOverhaulCommon.LOGGER.info("Client commands registered!");
        });
    }

    private static void registerGamemode(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommands.literal("gamemode")
                .requires(Commands.hasPermission(GameModeCommand.PERMISSION_CHECK));
        for (GameType type : GameType.values()) {
            node.then(ClientCommands.literal(type.getName())
                            .executes(context -> redirectToServer(context, "gamemode " + type.getName()))
                            .then(ClientCommands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer(context, "gamemode " + type.getName() + ' ' + captureLastArgument(context)))))
                    .then(ClientCommands.literal(String.valueOf(type.ordinal()))
                            .executes(context -> redirectToServer(context, "gamemode " + type.getName()))
                            .then(ClientCommands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer(context, "gamemode " + type.getName() + ' ' + captureLastArgument(context)))))
                    .then(ClientCommands.literal(GamemodeOverhaulCommon.createShort(type))
                            .executes(context -> redirectToServer(context, "gamemode " + type.getName()))
                            .then(ClientCommands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer(context, "gamemode " + type.getName() + ' ' + captureLastArgument(context)))));
        }
        dispatcher.register(node);
    }

    private static void registerGm(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommands.literal("gm")
                .requires(stack -> GameModeCommand.PERMISSION_CHECK.check(stack.permissions()));
        for (GameType type : GameType.values()) {
            node.then(ClientCommands.literal(type.getName())
                            .executes(context -> redirectToServer(context, "gamemode " + type.getName()))
                            .then(ClientCommands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer(context, "gamemode " + type.getName() + ' ' + captureLastArgument(context)))))
                    .then(ClientCommands.literal(String.valueOf(type.ordinal()))
                            .executes(context -> redirectToServer(context, "gamemode " + type.getName()))
                            .then(ClientCommands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer(context, "gamemode " + type.getName() + ' ' + captureLastArgument(context)))))
                    .then(ClientCommands.literal(GamemodeOverhaulCommon.createShort(type))
                            .executes(context -> redirectToServer(context, "gamemode " + type.getName()))
                            .then(ClientCommands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer(context, "gamemode " + type.getName() + ' ' + captureLastArgument(context)))));
        }
        dispatcher.register(node);
    }

    private static void registerGmNoArgs(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        for (GameType type : GameType.values()) {
            dispatcher.register(ClientCommands.literal("gm" + GamemodeOverhaulCommon.createShort(type))
                    .requires(stack -> GameModeCommand.PERMISSION_CHECK.check(stack.permissions()))
                    .executes(context -> redirectToServer(context, "gamemode " + type.getName()))
                    .then(ClientCommands.argument("target", EntityArgument.players())
                            .executes(context -> redirectToServer(context, "gamemode " + type.getName() + ' ' + captureLastArgument(context)))));
        }
    }

    private static void registerDefaultGamemode(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommands.literal("defaultgamemode").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (GameType type : GameType.values()) {
            node.then(ClientCommands.literal(type.getName())
                            .executes(context -> redirectToServer(context, "defaultgamemode " + type.getName())))
                    .then(ClientCommands.literal(String.valueOf(type.ordinal()))
                            .executes(context -> redirectToServer(context, "defaultgamemode " + type.getName())))
                    .then(ClientCommands.literal(GamemodeOverhaulCommon.createShort(type))
                            .executes(context -> redirectToServer(context, "defaultgamemode " + type.getName())));
        }
        dispatcher.register(node);
    }

    private static void registerDgm(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommands.literal("dgm")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (GameType type : GameType.values()) {
            node.then(ClientCommands.literal(type.getName())
                            .executes(context -> redirectToServer(context, "defaultgamemode " + type.getName())))
                    .then(ClientCommands.literal(String.valueOf(type.ordinal()))
                            .executes(context -> redirectToServer(context, "defaultgamemode " + type.getName())))
                    .then(ClientCommands.literal(GamemodeOverhaulCommon.createShort(type))
                            .executes(context -> redirectToServer(context, "defaultgamemode " + type.getName())));
        }
        dispatcher.register(node);
    }

    private static void registerDifficulty(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommands.literal("difficulty").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Difficulty value : Difficulty.values()) {
            node.then(ClientCommands.literal(value.getSerializedName())
                            .executes(context -> redirectToServer(context, "difficulty " + value.getSerializedName())))
                    .then(ClientCommands.literal(String.valueOf(value.ordinal()))
                            .executes(context -> redirectToServer(context, "difficulty " + value.getSerializedName())));
        }
        dispatcher.register(node);
    }

    private static void registerToggleDownfall(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("toggledownfall").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(GamemodeOverhaulFabricClient::toggleDownfall));
    }

    private static int toggleDownfall(@NotNull CommandContext<FabricClientCommandSource> source) throws CommandSyntaxException {
        ClientLevel level = source.getSource().getLevel();
        if (level.isRaining() || level.isThundering()) {
            redirectToServer(source, "weather clear");
        } else {
            redirectToServer(source, "weather rain");
        }
        return 1;
    }

    private static int redirectToServer(@NotNull CommandContext<FabricClientCommandSource> source, @NotNull String command) throws CommandSyntaxException {
        ParseResults<ClientSuggestionProvider> serverParse = source.getSource().getPlayer().connection.getCommands().parse(command, source.getSource().getPlayer().connection.getSuggestionsProvider());
        if (serverParse.getExceptions().isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create(); // todo: mixin around this?
        }

        source.getSource().getPlayer().connection.sendCommand(command);
        return 1;
    }

    @Contract(pure = true)
    private static @NotNull String captureLastArgument(@NotNull CommandContext<FabricClientCommandSource> source) {
        List<ParsedCommandNode<FabricClientCommandSource>> nodes = source.getNodes();
        StringRange node = nodes.get(nodes.size() - 1).getRange();
        return source.getInput().substring(node.getStart(), node.getEnd());
    }
}
