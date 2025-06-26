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

package dev.mlow.mods.gamemodeoverhaul.compat;

import dev.mlow.mods.gamemodeoverhaul.GamemodeOverhaulCommon;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigScreen {
    public static Screen createScreenFactory(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.gamemodeoverhaul.config"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.gamemodeoverhaul.general"));

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.gamemodeoverhaul.enable_gamemode"), GamemodeOverhaulCommon.CONFIG.enableGamemode())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.gamemodeoverhaul.enable_gamemode.tooltip"))
                .setSaveConsumer(GamemodeOverhaulCommon.CONFIG::enableGamemode)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.gamemodeoverhaul.enable_gm"), GamemodeOverhaulCommon.CONFIG.enableGm())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.gamemodeoverhaul.enable_gm.tooltip"))
                .setSaveConsumer(GamemodeOverhaulCommon.CONFIG::enableGm)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.gamemodeoverhaul.enable_no_args_gm"), GamemodeOverhaulCommon.CONFIG.enableNoArgsGm())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("option.gamemodeoverhaul.enable_no_args_gm.tooltip"))
                .setSaveConsumer(GamemodeOverhaulCommon.CONFIG::enableNoArgsGm)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.gamemodeoverhaul.enable_default_gamemode"), GamemodeOverhaulCommon.CONFIG.enableDefaultGamemode())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.gamemodeoverhaul.enable_default_gamemode.tooltip"))
                .setSaveConsumer(GamemodeOverhaulCommon.CONFIG::enableDefaultGamemode)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.gamemodeoverhaul.enable_dgm"), GamemodeOverhaulCommon.CONFIG.enableDgm())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("option.gamemodeoverhaul.enable_dgm.tooltip"))
                .setSaveConsumer(GamemodeOverhaulCommon.CONFIG::enableDgm)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.gamemodeoverhaul.enable_difficulty"), GamemodeOverhaulCommon.CONFIG.enableDifficulty())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.gamemodeoverhaul.enable_difficulty.tooltip"))
                .setSaveConsumer(GamemodeOverhaulCommon.CONFIG::enableDifficulty)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.gamemodeoverhaul.enable_toggledownfall"), GamemodeOverhaulCommon.CONFIG.enableToggledownfall())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.gamemodeoverhaul.enable_toggledownfall.tooltip"))
                .setSaveConsumer(GamemodeOverhaulCommon.CONFIG::enableToggledownfall).requireRestart()
                .build());

        builder.setSavingRunnable(GamemodeOverhaulCommon.CONFIG::save);
        return builder.build();
    }
}
