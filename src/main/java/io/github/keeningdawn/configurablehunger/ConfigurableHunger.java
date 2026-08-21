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

import io.github.keeningdawn.configurablehunger.client.ConfigurableHungerClient;
import io.github.keeningdawn.configurablehunger.config.ConfigurableHungerConfig;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ConfigurableHunger.MOD_ID)
public class ConfigurableHunger {
  public static final String MOD_ID = "configurable_hunger";

  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private static final String PROTOCOL_VERSION = "1";
  public static final SimpleChannel CHANNEL =
      NetworkRegistry.newSimpleChannel(
          id("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

  // Server branch for the handshake, so we don't enable the mod on unsupported servers.
  // See PlayerMixin for the client side of this branch.
  public static boolean serverRegenSuppressionActive = false;

  public ConfigurableHunger() {
    ConfigurableHungerConfig.load();
    LOGGER.info("Configurable Hunger initialized");

    CHANNEL.registerMessage(
        0,
        RegenSuppressionPayload.class,
        RegenSuppressionPayload::encode,
        RegenSuppressionPayload::decode,
        RegenSuppressionPayload::handle);

    MinecraftForge.EVENT_BUS.addListener(ConfigurableHunger::onPlayerLoggedIn);
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ConfigurableHungerClient::init);
  }

  private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) {
      return;
    }
    CHANNEL.send(
        PacketDistributor.PLAYER.with(() -> player),
        new RegenSuppressionPayload(ConfigurableHungerConfig.get().enabled));
  }

  public static ResourceLocation id(String path) {
    return new ResourceLocation(MOD_ID, path);
  }

  public record RegenSuppressionPayload(boolean active) {
    static void encode(RegenSuppressionPayload payload, FriendlyByteBuf buf) {
      buf.writeBoolean(payload.active());
    }

    static RegenSuppressionPayload decode(FriendlyByteBuf buf) {
      return new RegenSuppressionPayload(buf.readBoolean());
    }

    static void handle(RegenSuppressionPayload payload, Supplier<NetworkEvent.Context> ctx) {
      ctx.get().enqueueWork(() -> serverRegenSuppressionActive = payload.active());
      ctx.get().setPacketHandled(true);
    }
  }
}
