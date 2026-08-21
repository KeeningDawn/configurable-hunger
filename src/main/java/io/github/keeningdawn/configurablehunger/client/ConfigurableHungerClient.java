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
package io.github.keeningdawn.configurablehunger.client;

import io.github.keeningdawn.configurablehunger.ConfigurableHunger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class ConfigurableHungerClient {
  public static void init() {
    MinecraftForge.EVENT_BUS.addListener(ConfigurableHungerClient::onLoggingOut);

    // Cloth Config is an optional dependency, no config screen without it.
    if (ConfigurableHungerConfigScreen.isClothConfigLoaded()) {
      ModLoadingContext.get()
          .registerExtensionPoint(
              ConfigScreenHandler.ConfigScreenFactory.class,
              () ->
                  new ConfigScreenHandler.ConfigScreenFactory(
                      ConfigurableHungerConfigScreen::build));
    }
  }

  // Reset back to inactive on disconnect, so we dont get a stale state
  private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
    ConfigurableHunger.serverRegenSuppressionActive = false;
  }
}
