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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ConfigurableHungerClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ClientPlayNetworking.registerGlobalReceiver(
        ConfigurableHunger.REGEN_SUPPRESSION_CHANNEL,
        (client, handler, buf, sender) -> {
          boolean active = buf.readBoolean();
          client.execute(() -> ConfigurableHunger.serverRegenSuppressionActive = active);
        });

    // Reset back to inactive on disconnect, so we dont get a stale state
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> ConfigurableHunger.serverRegenSuppressionActive = false);
  }
}
