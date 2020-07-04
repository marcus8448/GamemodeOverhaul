package io.github.marcus8448.mods.gamemodeOverhaul;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ModMenuEntrypoint implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(screen)
                    .setTitle(new TranslatableText("title.gamemodeoverhaul.config"));
            builder.setGlobalized(true);

            ConfigCategory testing = builder.getOrCreateCategory(new TranslatableText("category.gamemodeoverhaul.general"));
            testing.addEntry(ConfigEntryBuilder.create().startBooleanToggle(new TranslatableText("config.gamemodeoverhaul.enable_gamemode_numbers"), GamemodeOverhaulFabric.config.getConfig().enable_gamemode_numbers).setDefaultValue(true).setTooltip(new TranslatableText("config.gamemodeoverhaul.enable_gamemode_numbers.desc")).setSaveConsumer((enabled) -> GamemodeOverhaulFabric.config.getConfig().enable_gamemode_numbers = enabled).build());
            testing.addEntry(ConfigEntryBuilder.create().startBooleanToggle(new TranslatableText("config.gamemodeoverhaul.enable_gamemode_letters"), GamemodeOverhaulFabric.config.getConfig().enable_gamemode_letters).setDefaultValue(true).setTooltip(new TranslatableText("config.gamemodeoverhaul.enable_gamemode_letters.desc")).setSaveConsumer((enabled) -> GamemodeOverhaulFabric.config.getConfig().enable_gamemode_letters = enabled).build());
            testing.addEntry(ConfigEntryBuilder.create().startBooleanToggle(new TranslatableText("config.gamemodeoverhaul.enable_defaultgamemode_numbers"), GamemodeOverhaulFabric.config.getConfig().enable_defaultgamemode_numbers).setDefaultValue(true).setTooltip(new TranslatableText("config.gamemodeoverhaul.enable_defaultgamemode_numbers.desc")).setSaveConsumer((enabled) -> GamemodeOverhaulFabric.config.getConfig().enable_defaultgamemode_numbers = enabled).build());
            testing.addEntry(ConfigEntryBuilder.create().startBooleanToggle(new TranslatableText("config.gamemodeoverhaul.enable_defaultgamemode_letters"), GamemodeOverhaulFabric.config.getConfig().enable_defaultgamemode_letters).setDefaultValue(true).setTooltip(new TranslatableText("config.gamemodeoverhaul.enable_defaultgamemode_letters.desc")).setSaveConsumer((enabled) -> GamemodeOverhaulFabric.config.getConfig().enable_defaultgamemode_letters = enabled).build());
            testing.addEntry(ConfigEntryBuilder.create().startBooleanToggle(new TranslatableText("config.gamemodeoverhaul.enable_excessively_short_commands"), GamemodeOverhaulFabric.config.getConfig().enable_excessively_short_commands).setDefaultValue(false).setTooltip(new TranslatableText("config.gamemodeoverhaul.enable_excessively_short_commands.desc")).setSaveConsumer((enabled) -> GamemodeOverhaulFabric.config.getConfig().enable_excessively_short_commands = enabled).build());
            testing.addEntry(ConfigEntryBuilder.create().startBooleanToggle(new TranslatableText("config.gamemodeoverhaul.enable_difficulty_numbers"), GamemodeOverhaulFabric.config.getConfig().enable_difficulty_numbers).setDefaultValue(true).setTooltip(new TranslatableText("config.gamemodeoverhaul.enable_difficulty_numbers.desc")).setSaveConsumer((enabled) -> GamemodeOverhaulFabric.config.getConfig().enable_difficulty_numbers = enabled).build());
            testing.addEntry(ConfigEntryBuilder.create().startBooleanToggle(new TranslatableText("config.gamemodeoverhaul.enable_toggledownfall"), GamemodeOverhaulFabric.config.getConfig().enable_toggledownfall).setDefaultValue(true).setTooltip(new TranslatableText("config.gamemodeoverhaul.enable_toggledownfall.desc")).setSaveConsumer((enabled) -> GamemodeOverhaulFabric.config.getConfig().enable_toggledownfall = enabled).build());

            builder.setSavingRunnable(GamemodeOverhaulFabric.config::save);
            return builder.build();
        };
    }
}
