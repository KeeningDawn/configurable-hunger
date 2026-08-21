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
package io.github.keeningdawn.configurablehunger;

import io.github.keeningdawn.configurablehunger.config.ConfigurableHungerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurableHunger implements ModInitializer {
  public static final String MOD_ID = "configurable-hunger";

  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  // Server branch for the handshake, so we don't enable the mod on unsupported servers.
  // See PlayerMixin for the client side of this branch.
  public static final ResourceLocation REGEN_SUPPRESSION_CHANNEL = id("regen_suppression_active");
  public static boolean serverRegenSuppressionActive = false;

  @Override
  public void onInitialize() {
    ConfigurableHungerConfig.load();
    LOGGER.info("Configurable Hunger initialized");

    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> {
          FriendlyByteBuf buf = PacketByteBufs.create();
          buf.writeBoolean(ConfigurableHungerConfig.get().enabled);
          sender.sendPacket(REGEN_SUPPRESSION_CHANNEL, buf);
        });
  }

  public static ResourceLocation id(String path) {
    return new ResourceLocation(MOD_ID, path);
  }
}
