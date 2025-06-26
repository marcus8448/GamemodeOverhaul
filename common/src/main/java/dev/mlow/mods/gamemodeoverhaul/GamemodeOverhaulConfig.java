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

public interface GamemodeOverhaulConfig {
    boolean enableGamemode();
    boolean enableGm();
    boolean enableNoArgsGm();
    boolean enableDefaultGamemode();
    boolean enableDgm();
    boolean enableDifficulty();
    boolean enableToggledownfall();

    void enableGamemode(boolean value);
    void enableGm(boolean value);
    void enableNoArgsGm(boolean value);
    void enableDefaultGamemode(boolean value);
    void enableDgm(boolean value);
    void enableDifficulty(boolean value);
    void enableToggledownfall(boolean value);

    void save();
}
