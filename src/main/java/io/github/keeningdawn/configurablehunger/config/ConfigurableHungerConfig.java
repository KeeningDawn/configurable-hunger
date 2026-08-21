/*
 * Copyright (C) 2026 KeeningDawn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.keeningdawn.configurablehunger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.keeningdawn.configurablehunger.ConfigurableHunger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraftforge.fml.loading.FMLPaths;

// Lives in common since this could be ran by a dedicated server.
public class ConfigurableHungerConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Path PATH =
      FMLPaths.CONFIGDIR.get().resolve(ConfigurableHunger.MOD_ID + ".json");
  private static ConfigurableHungerConfig instance;

  public boolean enabled = true;
  public boolean depleteHunger = true;
  public StarvationDifficulty starvationDifficulty = StarvationDifficulty.NORMAL;
  public PeacefulRegeneration peacefulRegeneration = PeacefulRegeneration.NEVER;

  public static ConfigurableHungerConfig get() {
    if (instance == null) {
      instance = load();
    }
    return instance;
  }

  public static ConfigurableHungerConfig load() {
    if (Files.exists(PATH)) {
      try {
        ConfigurableHungerConfig loaded =
            GSON.fromJson(Files.readString(PATH), ConfigurableHungerConfig.class);
        if (loaded != null) {
          instance = loaded;
          return instance;
        }
      } catch (IOException e) {
        ConfigurableHunger.LOGGER.error("Failed to load config, using defaults", e);
      }
    }
    instance = new ConfigurableHungerConfig();
    return instance;
  }

  public void save() {
    try {
      Files.writeString(PATH, GSON.toJson(this));
    } catch (IOException e) {
      ConfigurableHunger.LOGGER.error("Failed to save config", e);
    }
  }

  // ALWAYS only makes sense on Peaceful starvation difficulty (otherwise you'd be starving and
  // healing at the same time). Non-destructive: the saved value stays ALWAYS, so switching
  // starvationDifficulty back to PEACEFUL silently restores it.
  public PeacefulRegeneration getEffectivePeacefulRegeneration() {
    if (peacefulRegeneration == PeacefulRegeneration.ALWAYS
        && starvationDifficulty != StarvationDifficulty.PEACEFUL) {
      return PeacefulRegeneration.SPRINT_BASED;
    }
    return peacefulRegeneration;
  }
}
