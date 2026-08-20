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
package io.github.keeningdawn.configurablehunger.mixin;

import io.github.keeningdawn.configurablehunger.config.ConfigurableHungerConfig;
import io.github.keeningdawn.configurablehunger.config.PeacefulRegeneration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Server side logic
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
  @Inject(method = "tickRegeneration", at = @At("HEAD"), cancellable = true)
  private void configurableHunger$tickRegeneration(CallbackInfo ci) {
    ConfigurableHungerConfig config = ConfigurableHungerConfig.get();
    if (!config.enabled) {
      return;
    }
    ci.cancel();

    ServerPlayer self = (ServerPlayer) (Object) this;
    ServerLevel level = self.level();
    if (!level.getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION)) {
      return;
    }

    if (!configurableHunger$isRegenActive(self, config.getEffectivePeacefulRegeneration())) {
      return;
    }

    if (self.tickCount % 20 == 0) {
      if (self.getHealth() < self.getMaxHealth()) {
        self.heal(1.0f);
      }
      float saturation = self.getFoodData().getSaturationLevel();
      if (saturation < 20.0f) {
        self.getFoodData().setSaturation(saturation + 1.0f);
      }
    }
    if (self.tickCount % 10 == 0 && self.getFoodData().needsFood()) {
      self.getFoodData().setFoodLevel(self.getFoodData().getFoodLevel() + 1);
    }
  }

  private static boolean configurableHunger$isRegenActive(
      ServerPlayer player, PeacefulRegeneration mode) {
    return switch (mode) {
      case NEVER -> false;
      case ALWAYS -> true;
      case SPRINT_BASED ->
          ((PlayerInvokerMixin) (Object) player)
              .configurableHunger$hasEnoughFoodToDoExhaustiveManoeuvres();
      case NATURAL_REGEN_BASED -> player.getFoodData().getFoodLevel() >= 18;
    };
  }
}
