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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("FieldCanBeLocal")
public class FabricConfig implements GamemodeOverhaulConfig {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("gamemodeoverhaul.json").toFile();

    private boolean enableGamemode = true;
    private boolean enableGm = true;
    private boolean enableNoArgsGm = false;
    private boolean enableDefaultGamemode = true;
    private boolean enableDgm = false;
    private boolean enableDifficulty = true;
    private boolean enableToggledownfall = true;

    private FabricConfig() {}

    public static FabricConfig create() {
        if (FILE.exists()) {
            try (FileReader json = new FileReader(FILE)) {
                FabricConfig config = GSON.fromJson(json, FabricConfig.class);
                if (config != null) {
                    return config;
                } else {
                    GamemodeOverhaulCommon.LOGGER.warn("Failed to read the gamemodeoverhaul config file. Regenerating it...");
                    FILE.delete();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read configuration file!", e);
            }
        }
        try {
            FILE.getParentFile().mkdirs();
            FabricConfig src = new FabricConfig();
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(src, writer);
                writer.flush();
            }
            return src;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create configuration file!", e);
        }
    }

    @Override
    public boolean enableGamemode() {
        return this.enableGamemode;
    }

    @Override
    public boolean enableGm() {
        return this.enableGm;
    }

    @Override
    public boolean enableNoArgsGm() {
        return this.enableNoArgsGm;
    }

    @Override
    public boolean enableDefaultGamemode() {
        return this.enableDefaultGamemode;
    }

    @Override
    public boolean enableDgm() {
        return this.enableDgm;
    }

    @Override
    public boolean enableDifficulty() {
        return this.enableDifficulty;
    }

    @Override
    public boolean enableToggledownfall() {
        return this.enableToggledownfall;
    }

    @Override
    public void enableGamemode(boolean value) {
        this.enableGamemode = value;
    }

    @Override
    public void enableGm(boolean value) {
        this.enableGm = value;
    }

    @Override
    public void enableNoArgsGm(boolean value) {
        this.enableNoArgsGm = value;
    }

    @Override
    public void enableDefaultGamemode(boolean value) {
        this.enableDefaultGamemode = value;
    }

    @Override
    public void enableDgm(boolean value) {
        this.enableDgm = value;
    }

    @Override
    public void enableDifficulty(boolean value) {
        this.enableDifficulty = value;
    }

    @Override
    public void enableToggledownfall(boolean value) {
        this.enableToggledownfall = value;
    }

    @Override
    public void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(this, writer);
            writer.flush();
        } catch (IOException e) {
            GamemodeOverhaulCommon.LOGGER.error("Failed to save config file!", e);
        }
    }
}
