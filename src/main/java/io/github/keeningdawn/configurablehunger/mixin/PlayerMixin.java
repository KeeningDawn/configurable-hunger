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

import io.github.keeningdawn.configurablehunger.ConfigurableHunger;
import io.github.keeningdawn.configurablehunger.config.ConfigurableHungerConfig;
import io.github.keeningdawn.configurablehunger.config.PeacefulRegeneration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// For some reason, unlike 26.2, vanilla's peaceful auto heal is inlined into Player#aiStep, not
// split into its own ServerPlayer method. So we have to mix into Player instead of ServerPlayer.
// Not removing this means a recurrent, constant desync that practically makes the mod unusable.
@Mixin(Player.class)
public abstract class PlayerMixin {

  @Redirect(
      method = "aiStep",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
  // Client branch for the handshake, so we don't enable the mod on unsupported servers.
  // See ConfigurableHunger for the client side of this branch.
  private boolean configurableHunger$suppressVanillaPeacefulRegen(
      GameRules gameRules, GameRules.Key<GameRules.BooleanValue> rule) {
    boolean actual = gameRules.getBoolean(rule);
    boolean suppress =
        ((Object) this) instanceof ServerPlayer
            ? ConfigurableHungerConfig.get().enabled
            : ConfigurableHunger.serverRegenSuppressionActive;
    return suppress ? false : actual;
  }

  @Inject(method = "aiStep", at = @At("HEAD"))
  private void configurableHunger$tickRegeneration(CallbackInfo ci) {
    if (!(((Object) this) instanceof ServerPlayer self)) {
      return;
    }

    ConfigurableHungerConfig config = ConfigurableHungerConfig.get();
    if (!config.enabled) {
      return;
    }

    Level level = self.level();
    if (!level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
      return;
    }

    if (!configurableHunger$isRegenActive(self, config.getEffectivePeacefulRegeneration())) {
      return;
    }

    // health only
    if (self.tickCount % 20 == 0 && self.getHealth() < self.getMaxHealth()) {
      self.heal(1.0f);
    }
  }

  private static boolean configurableHunger$isRegenActive(
      ServerPlayer player, PeacefulRegeneration mode) {
    return switch (mode) {
      case NEVER -> false;
      case ALWAYS -> true;
      case SPRINT_BASED -> player.getFoodData().getFoodLevel() > 6 || player.getAbilities().mayfly;
      case NATURAL_REGEN_BASED -> player.getFoodData().getFoodLevel() >= 18;
    };
  }
}
