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

import dev.mlow.mods.gamemodeoverhaul.client.GamemodeOverhaulClientForge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.NotNull;

@Mod(GamemodeOverhaulCommon.MOD_ID)
public class GamemodeOverhaulForge {
    public static final ForgeConfig CONFIG = new ForgeConfig();

    public GamemodeOverhaulForge(IEventBus modEventBus, Dist dist, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CONFIG.commonSpec);
        container.getEventBus().addListener(CONFIG::onLoad);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(GamemodeOverhaulClientForge::registerClientCommands);
        }
    }

    private void registerCommands(@NotNull RegisterCommandsEvent event) {
        GamemodeOverhaulCommon.registerCommands(event.getDispatcher());
    }
}