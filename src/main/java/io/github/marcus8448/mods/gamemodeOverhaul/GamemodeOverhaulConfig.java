package io.github.marcus8448.mods.gamemodeOverhaul;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GamemodeOverhaulConfig {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File file = new File(FabricLoader.getInstance().getConfigDirectory(), "gamemode_overhaul.json");
    private Data config = new Data();

    public GamemodeOverhaulConfig() {
        this.load();
    }

    public Data getConfig() {
        return config;
    }

    public void save() {
        try {
            GamemodeOverhaulFabric.LOGGER.info("Saving config!");
            FileUtils.writeStringToFile(this.file, this.gson.toJson(this.config), Charsets.UTF_8);
        } catch (IOException e) {
            GamemodeOverhaulFabric.LOGGER.error("Failed to save config.", e);
        }
    }

    public void load() {
        try {
            this.file.getParentFile().mkdirs();
            if (!this.file.exists()) {
                GamemodeOverhaulFabric.LOGGER.info("Failed to find config file, creating one.");
                this.save();
            } else {
                byte[] bytes = Files.readAllBytes(Paths.get(this.file.getPath()));
                this.config = this.gson.fromJson(new String(bytes, Charsets.UTF_8), Data.class);
            }
        } catch (IOException e) {
            GamemodeOverhaulFabric.LOGGER.error("Failed to load config.", e);
        }
    }

    public static class Data {
        @Expose
        public boolean enable_gamemode_numbers = true;
        @Expose
        public boolean enable_gamemode_letters = true;
        @Expose
        public boolean enable_defaultgamemode_numbers = true;
        @Expose
        public boolean enable_defaultgamemode_letters = true;
        @Expose
        public boolean enable_excessively_short_commands = false;
        @Expose
        public boolean enable_difficulty_numbers = true;
        @Expose
        public boolean enable_toggledownfall = true;
    }
}
