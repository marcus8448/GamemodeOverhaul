package io.github.marcus8448.mods.gamemodeOverhaul;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig.ConfigReloading;
import net.minecraftforge.fml.config.ModConfig.Loading;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Copyright (c) marcus8448 2019. All rights reserved.
 * @since 1.13.2-1.3.5.0
 * @author marcus8448
 */
@SuppressWarnings("unused")
public class GMOConfig {
    private static final Marker CONFIG = MarkerManager.getMarker("Config");
    static final ForgeConfigSpec commonSpec;
    public static final GMOConfig.Common COMMON;

    @SubscribeEvent
    public static void onLoad(Loading configEvent) {
        GamemodeOverhaul.LOGGER.debug(CONFIG, "Successfully loaded GamemodeOverhaul's config file!");
    }

    @SubscribeEvent
    public static void onFileChange(ConfigReloading configEvent) {
        GamemodeOverhaul.LOGGER.fatal(CONFIG, "GamemodeOverhaul's config just got changed on the file system! This shouldn't happen!");
    }

    static {
        Pair specPair = (new Builder()).configure(GMOConfig.Common::new);
        commonSpec = (ForgeConfigSpec)specPair.getRight();
        COMMON = (GMOConfig.Common)specPair.getLeft();
    }

    public static class Common {
        final BooleanValue enableGMCommand;
        final BooleanValue enableReallyShortGMCommands;
        final BooleanValue enableDGMCommand;
        final BooleanValue enableWelcomeMessage;
        final BooleanValue enableDebugLogging;

        Common(Builder builder) {
            builder.comment("GamemodeOverhaul's command config settings").push("commands");
            this.enableGMCommand = builder.comment("Set this to false if you don't want the mod to add the '/gm' command").translation("gamemodeoverhaul.configgui.enablegmcommand").worldRestart().define("enableGMCommand", true);
            this.enableReallyShortGMCommands = builder.comment("Set this to false if you don't the mod to add '/gmc', '/gms', '/gmsp' and '/gma'").translation("gamemodeoverhaul.configgui.enablereallyshortgmcommands").worldRestart().define("enableReallyShortGMCommands", true);
            this.enableDGMCommand = builder.comment("Set this to false if you don't want the mod to add the '/dgm' command").translation("gamemodeoverhaul.configgui.enabledgmcommand").worldRestart().define("enableDGMCommand", true);
            builder.pop();
            builder.comment("GamemodeOverhaul's misc. config settings").push("misc");
            this.enableWelcomeMessage = builder.comment("Set this to true if you want GamemodeOverhaul to send a welcome message when you join a world").translation("gamemodeoverhaul.configgui.enablewelcomemessage").worldRestart().define("enableWelcomeMessage", false);
            this.enableDebugLogging = builder.comment("Set this to true if you want GamemodeOverhaul to log debug messages").translation("gamemodeoverhaul.configgui.enabledebugmessages").worldRestart().define("enableDebugMessages", false);
            builder.pop();
        }
    }
}
