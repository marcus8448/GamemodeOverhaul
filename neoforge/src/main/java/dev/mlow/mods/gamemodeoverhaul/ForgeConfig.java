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

package dev.mlow.mods.gamemodeoverhaul;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class ForgeConfig implements GamemodeOverhaulConfig {
    public final Common common;
    public final IConfigSpec commonSpec;

    @SubscribeEvent
    public void onLoad(ModConfigEvent.Loading configEvent) {
        GamemodeOverhaulCommon.LOGGER.debug("Successfully loaded GamemodeOverhaul's config file!");
    }

    public ForgeConfig() {
        Pair<Common, ModConfigSpec> configure = new ModConfigSpec.Builder().configure(Common::new);
        this.common = configure.getLeft();
        this.commonSpec = configure.getRight();
    }

    @Override
    public boolean enableGamemode() {
        return this.common.enableDefaultGamemode.get();
    }

    @Override
    public boolean enableGm() {
        return this.common.enableGm.get();
    }

    @Override
    public boolean enableNoArgsGm() {
        return this.common.enableNoArgsGm.get();
    }

    @Override
    public boolean enableDefaultGamemode() {
        return this.common.enableDefaultGamemode.get();
    }

    @Override
    public boolean enableDgm() {
        return this.common.enableDgm.get();
    }

    @Override
    public boolean enableDifficulty() {
        return this.common.enableDifficulty.get();
    }

    @Override
    public boolean enableToggledownfall() {
        return this.common.enableToggledownfall.get();
    }

    @Override
    public void enableGamemode(boolean value) {
        this.common.enableGamemode.set(value);
    }

    @Override
    public void enableGm(boolean value) {
        this.common.enableGm.set(value);
    }

    @Override
    public void enableNoArgsGm(boolean value) {
        this.common.enableNoArgsGm.set(value);
    }

    @Override
    public void enableDefaultGamemode(boolean value) {
        this.common.enableDefaultGamemode.set(value);
    }

    @Override
    public void enableDgm(boolean value) {
        this.common.enableDgm.set(value);
    }

    @Override
    public void enableDifficulty(boolean value) {
        this.common.enableDifficulty.set(value);
    }

    @Override
    public void enableToggledownfall(boolean value) {
        this.common.enableToggledownfall.set(value);
    }

    @Override
    public void save() {
        //no-op on forge
    }

    public static class Common {
        final ModConfigSpec.BooleanValue enableGamemode;
        final ModConfigSpec.BooleanValue enableGm;
        final ModConfigSpec.BooleanValue enableNoArgsGm;
        final ModConfigSpec.BooleanValue enableDefaultGamemode;
        final ModConfigSpec.BooleanValue enableDgm;
        final ModConfigSpec.BooleanValue enableDifficulty;
        final ModConfigSpec.BooleanValue enableToggledownfall;

        Common(@NotNull ModConfigSpec.Builder builder) {
            builder.comment("GamemodeOverhaul's command config settings").push("commands");
            this.enableGamemode = builder.comment("Set this to false if you don't want the mod to add additional arguments to the '/gamemode' command").translation("option.gamemodeoverhaul.enable_gamemode").worldRestart().define("enableGamemode", true);
            this.enableGm = builder.comment("Set this to false if you don't want the mod to add the '/gm' command").translation("option.gamemodeoverhaul.enable_gm").worldRestart().define("enableGm", true);
            this.enableNoArgsGm = builder.comment("Set this to false if you don't the mod to add '/gmc', '/gms', '/gmsp' and '/gma' commands").translation("option.gamemodeoverhaul.enable_no_args_gm").worldRestart().define("enableNoArgsGm", false);
            this.enableDefaultGamemode = builder.comment("Set this to false if you don't want the mod to add additional arguments to the '/defaultgamemode' command").translation("option.gamemodeoverhaul.enable_default_gamemode").worldRestart().define("enableDefaultGamemode", true);
            this.enableDgm = builder.comment("Set this to false if you don't want the mod to add the '/dgm' command").translation("option.gamemodeoverhaul.enable_dgm").worldRestart().define("enableDgm", false);
            this.enableDifficulty = builder.comment("Set this to false if you don't want the mod to add the integer values for '/difficulty'").translation("option.gamemodeoverhaul.enable_difficulty").worldRestart().define("enableDifficulty", true);
            this.enableToggledownfall = builder.comment("Set this to false if you don't want to have the mod add '/toggledownfall' back").translation("option.gamemodeoverhaul.enable_toggledownfall").worldRestart().define("enableToggledownfall", true);

            builder.pop();
        }
    }
}
