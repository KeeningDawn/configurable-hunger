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

import io.github.keeningdawn.configurablehunger.config.ConfigurableHungerConfig;
import io.github.keeningdawn.configurablehunger.config.PeacefulRegeneration;
import io.github.keeningdawn.configurablehunger.config.StarvationDifficulty;
import java.util.Optional;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

@OnlyIn(Dist.CLIENT)
public class ConfigurableHungerConfigScreen {
  static boolean isClothConfigLoaded() {
    return ModList.get().isLoaded("cloth_config");
  }

  static Screen build(Screen parent) {
    ConfigurableHungerConfig config = ConfigurableHungerConfig.get();

    ConfigBuilder builder =
        ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("title.configurable-hunger.config"))
            .solidBackground()
            .setSavingRunnable(config::save);

    ConfigCategory general =
        builder.getOrCreateCategory(Component.translatable("category.configurable-hunger.general"));
    ConfigEntryBuilder entryBuilder = builder.entryBuilder();

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("option.configurable-hunger.enabled"), config.enabled)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.enabled = value)
            .build());

    general.addEntry(
        entryBuilder
            .startBooleanToggle(
                Component.translatable("option.configurable-hunger.deplete_hunger"),
                config.depleteHunger)
            .setDefaultValue(true)
            .setSaveConsumer(value -> config.depleteHunger = value)
            .build());

    general.addEntry(
        entryBuilder
            .startEnumSelector(
                Component.translatable("option.configurable-hunger.starvation_difficulty"),
                StarvationDifficulty.class,
                config.starvationDifficulty)
            .setDefaultValue(StarvationDifficulty.NORMAL)
            .setEnumNameProvider(value -> starvationDifficultyName((StarvationDifficulty) value))
            .setTooltipSupplier(ConfigurableHungerConfigScreen::starvationDifficultyTooltip)
            .setSaveConsumer(value -> config.starvationDifficulty = value)
            .build());

    general.addEntry(
        entryBuilder
            .startEnumSelector(
                Component.translatable("option.configurable-hunger.peaceful_regeneration"),
                PeacefulRegeneration.class,
                config.peacefulRegeneration)
            .setDefaultValue(PeacefulRegeneration.NEVER)
            .setEnumNameProvider(value -> peacefulRegenerationName((PeacefulRegeneration) value))
            .setTooltipSupplier(ConfigurableHungerConfigScreen::peacefulRegenerationTooltip)
            .setSaveConsumer(value -> config.peacefulRegeneration = value)
            .build());

    return builder.build();
  }

  private static Component starvationDifficultyName(StarvationDifficulty value) {
    return switch (value) {
      case PEACEFUL ->
          Component.translatable("option.configurable-hunger.starvation_difficulty.peaceful");
      case EASY -> Component.translatable("option.configurable-hunger.starvation_difficulty.easy");
      case NORMAL ->
          Component.translatable("option.configurable-hunger.starvation_difficulty.normal");
      case HARD -> Component.translatable("option.configurable-hunger.starvation_difficulty.hard");
    };
  }

  // Cloth Config doesn't seem to support smart tooltips based on mouse context, so i have to
  // merge the general explanation and the currently selected option's detail into one tooltip.
  // A bit of a hack, but it works, and I don't know of a better way to do it.
  private static Optional<Component[]> starvationDifficultyTooltip(StarvationDifficulty value) {
    Component detail =
        switch (value) {
          case PEACEFUL ->
              Component.translatable(
                  "option.configurable-hunger.starvation_difficulty.peaceful.tooltip");
          case EASY ->
              Component.translatable(
                  "option.configurable-hunger.starvation_difficulty.easy.tooltip");
          case NORMAL ->
              Component.translatable(
                  "option.configurable-hunger.starvation_difficulty.normal.tooltip");
          case HARD ->
              Component.translatable(
                  "option.configurable-hunger.starvation_difficulty.hard.tooltip");
        };
    return Optional.of(
        new Component[] {
          Component.translatable("option.configurable-hunger.starvation_difficulty.tooltip"), detail
        });
  }

  private static Component peacefulRegenerationName(PeacefulRegeneration value) {
    return switch (value) {
      case ALWAYS ->
          Component.translatable("option.configurable-hunger.peaceful_regeneration.always");
      case SPRINT_BASED ->
          Component.translatable("option.configurable-hunger.peaceful_regeneration.sprint_based");
      case NATURAL_REGEN_BASED ->
          Component.translatable(
              "option.configurable-hunger.peaceful_regeneration.natural_regen_based");
      case NEVER ->
          Component.translatable("option.configurable-hunger.peaceful_regeneration.never");
    };
  }

  private static Optional<Component[]> peacefulRegenerationTooltip(PeacefulRegeneration value) {
    Component detail =
        switch (value) {
          case ALWAYS ->
              Component.translatable(
                  "option.configurable-hunger.peaceful_regeneration.always.tooltip");
          case SPRINT_BASED ->
              Component.translatable(
                  "option.configurable-hunger.peaceful_regeneration.sprint_based.tooltip");
          case NATURAL_REGEN_BASED ->
              Component.translatable(
                  "option.configurable-hunger.peaceful_regeneration.natural_regen_based.tooltip");
          case NEVER ->
              Component.translatable(
                  "option.configurable-hunger.peaceful_regeneration.never.tooltip");
        };
    return Optional.of(
        new Component[] {
          Component.translatable("option.configurable-hunger.peaceful_regeneration.tooltip"), detail
        });
  }
}
