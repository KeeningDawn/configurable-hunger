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
import io.github.keeningdawn.configurablehunger.config.StarvationDifficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Server side logic
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
  @Shadow private int foodLevel;
  @Shadow private float saturationLevel;
  @Shadow private float exhaustionLevel;
  @Shadow private int tickTimer;

  @Shadow
  public abstract void addExhaustion(float exhaustion);

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void configurableHunger$tick(Player player, CallbackInfo ci) {
    ConfigurableHungerConfig config = ConfigurableHungerConfig.get();
    if (!config.enabled) {
      return;
    }
    ci.cancel();

    Level level = player.level();
    boolean naturalRegen = level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);

    if (exhaustionLevel > 4.0f) {
      exhaustionLevel -= 4.0f;
      if (saturationLevel > 0.0f) {
        saturationLevel = Math.max(saturationLevel - 1.0f, 0.0f);
      } else if (config.depleteHunger) {
        foodLevel = Math.max(foodLevel - 1, 0);
      }
    }

    if (naturalRegen && saturationLevel > 0.0f && player.isHurt() && foodLevel >= 20) {
      tickTimer++;
      // vanilla has a 10 tick delay, if not emulated it will be practically instant
      if (tickTimer >= 10) {
        float amount = Math.min(saturationLevel, 6.0f);
        player.heal(amount / 6.0f);
        addExhaustion(amount);
        tickTimer = 0;
      }
    } else if (naturalRegen && foodLevel >= 18 && player.isHurt()) {
      tickTimer++;
      if (tickTimer >= 80) {
        player.heal(1.0f);
        addExhaustion(6.0f);
        tickTimer = 0;
      }
    } else if (foodLevel <= 0) {
      tickTimer++;
      if (tickTimer >= 80) {
        if (configurableHunger$shouldStarve(player, config.starvationDifficulty)) {
          player.hurt(player.damageSources().starve(), 1.0f);
        }
        tickTimer = 0;
      }
    } else {
      tickTimer = 0;
    }
  }

  private static boolean configurableHunger$shouldStarve(
      Player player, StarvationDifficulty difficulty) {
    return switch (difficulty) {
      case PEACEFUL -> false;
      case EASY -> player.getHealth() > 10.0f;
      case NORMAL -> player.getHealth() > 1.0f;
      case HARD -> true;
    };
  }
}
