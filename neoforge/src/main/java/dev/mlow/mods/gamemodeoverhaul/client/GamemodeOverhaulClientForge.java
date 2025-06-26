/*
 * GamemodeOverhaul
 * Copyright (C) 2019-2025 marcus8448
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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import dev.mlow.mods.gamemodeoverhaul.GamemodeOverhaulCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GamemodeOverhaulClientForge {
    public static void registerClientCommands(@NotNull RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        if (GamemodeOverhaulCommon.CONFIG.enableGamemode()) registerGamemode(dispatcher);
        if (GamemodeOverhaulCommon.CONFIG.enableGm()) registerGm(dispatcher);
        if (GamemodeOverhaulCommon.CONFIG.enableNoArgsGm()) registerGmNoArgs(dispatcher);
        if (GamemodeOverhaulCommon.CONFIG.enableDefaultGamemode()) registerDefaultGamemode(dispatcher);
        if (GamemodeOverhaulCommon.CONFIG.enableDgm()) registerDgm(dispatcher);
        if (GamemodeOverhaulCommon.CONFIG.enableDifficulty()) registerDifficulty(dispatcher);
        if (GamemodeOverhaulCommon.CONFIG.enableToggledownfall()) registerToggleDownfall(dispatcher);

        GamemodeOverhaulCommon.LOGGER.info("Client commands registered!");
    }


    private static void registerGamemode(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("gamemode")
                .requires(stack -> stack.hasPermission(2));
        for (GameType type : GameType.values()) {
            node.then(Commands.literal(String.valueOf(type.ordinal()))
                            .executes(context -> redirectToServer("gamemode " + type.getName()))
                            .then(Commands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer("gamemode " + type.getName() + ' ' + captureLastArgument(context)))))
                    .then(Commands.literal(GamemodeOverhaulCommon.createShort(type))
                            .executes(context -> redirectToServer("gamemode " + type.getName()))
                            .then(Commands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer("gamemode " + type.getName() + ' ' + captureLastArgument(context)))));
        }
        dispatcher.register(node);
    }

    private static void registerGm(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("gm")
                .requires(stack -> stack.hasPermission(2));
        for (GameType type : GameType.values()) {
            node.then(Commands.literal(String.valueOf(type.ordinal()))
                            .executes(context -> redirectToServer("gamemode " + type.getName()))
                            .then(Commands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer("gamemode " + type.getName() + ' ' + captureLastArgument(context)))))
                    .then(Commands.literal(GamemodeOverhaulCommon.createShort(type))
                            .executes(context -> redirectToServer("gamemode " + type.getName()))
                            .then(Commands.argument("target", EntityArgument.players())
                                    .executes(context -> redirectToServer("gamemode " + type.getName() + ' ' + captureLastArgument(context)))));
        }
        dispatcher.register(node);
    }

    private static void registerGmNoArgs(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        for (GameType type : GameType.values()) {
            dispatcher.register(Commands.literal("gm" + GamemodeOverhaulCommon.createShort(type))
                    .requires(stack -> stack.hasPermission(2))
                    .executes(context -> redirectToServer("gamemode " + type.getName()))
                    .then(Commands.argument("target", EntityArgument.players())
                            .executes(context -> redirectToServer("gamemode " + type.getName() + ' ' + captureLastArgument(context)))));
        }
    }

    private static void registerDefaultGamemode(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("defaultgamemode").requires(stack -> stack.hasPermission(2));
        for (GameType type : GameType.values()) {
            node.then(Commands.literal(String.valueOf(type.ordinal()))
                            .executes(context -> redirectToServer("defaultgamemode " + type.getName())))
                    .then(Commands.literal(GamemodeOverhaulCommon.createShort(type))
                            .executes(context -> redirectToServer("defaultgamemode " + type.getName())));
        }
        dispatcher.register(node);
    }

    private static void registerDgm(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("dgm")
                .requires(stack -> stack.hasPermission(2));
        for (GameType type : GameType.values()) {
            node.then(Commands.literal(String.valueOf(type.ordinal()))
                            .executes(context -> redirectToServer("defaultgamemode " + type.getName())))
                    .then(Commands.literal(GamemodeOverhaulCommon.createShort(type))
                            .executes(context -> redirectToServer("defaultgamemode " + type.getName())));
        }
        dispatcher.register(node);
    }

    private static void registerDifficulty(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("difficulty").requires(stack -> stack.hasPermission(2));
        for (Difficulty value : Difficulty.values()) {
            node.then(Commands.literal(String.valueOf(value.ordinal())).executes(context -> redirectToServer("difficulty " + value.getKey())));
        }
        dispatcher.register(node);
    }

    private static void registerToggleDownfall(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("toggledownfall").requires(stack -> stack.hasPermission(2))
                .executes((context) -> toggleDownfall(context.getSource())));
    }

    private static int toggleDownfall(@NotNull CommandSourceStack source) {
        Level level = source.getUnsidedLevel();
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        if (level.isRaining() || level.getLevelData().isRaining() || level.isThundering() || level.getLevelData().isThundering()) {
            player.connection.sendUnsignedCommand("weather clear");
        } else {
            player.connection.sendUnsignedCommand("weather rain");
        }
        return 1;
    }

    private static int redirectToServer(@NotNull String command) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        player.connection.sendUnsignedCommand(command);
        return 1;
    }

    @Contract(pure = true)
    private static @NotNull String captureLastArgument(@NotNull CommandContext<CommandSourceStack> source) {
        List<ParsedCommandNode<CommandSourceStack>> nodes = source.getNodes();
        StringRange node = nodes.get(nodes.size() - 1).getRange();
        return source.getInput().substring(node.getStart(), node.getEnd());
    }
}
