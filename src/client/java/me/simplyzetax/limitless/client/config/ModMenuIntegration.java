package me.simplyzetax.limitless.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import me.simplyzetax.limitless.client.features.damagenumbers.util.DamageFontManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
                return ModMenuIntegration::createConfigScreen;
        }

        private static Screen createConfigScreen(Screen parent) {
                ConfigBuilder builder = ConfigBuilder.create()
                                .setParentScreen(parent)
                                .setTitle(Text.translatable("config.limitless.title"))
                                .setSavingRunnable(() -> {
                                        // Config is automatically saved via static fields
                                        // No additional saving logic needed for now
                                });

                ConfigEntryBuilder entryBuilder = builder.entryBuilder();

                // Main Features Category
                ConfigCategory mainCategory = builder
                                .getOrCreateCategory(Text.translatable("config.limitless.category.features"));

                mainCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.feature.item_stealing"),
                                                ClientConfig.EnableItemStealingFeature)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.feature.item_stealing.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableItemStealingFeature = newValue)
                                .build());

                mainCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.feature.shulker_box"),
                                                ClientConfig.EnableShulkerBoxFeature)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.feature.shulker_box.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableShulkerBoxFeature = newValue)
                                .build());

                mainCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.feature.glowing"),
                                                ClientConfig.EnableGlowingFeature)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.feature.glowing.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableGlowingFeature = newValue)
                                .build());

                mainCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.feature.bow_trajectory"),
                                                ClientConfig.EnableBowTrajectoryFeature)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.feature.bow_trajectory.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableBowTrajectoryFeature = newValue)
                                .build());

                mainCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.feature.arrow_dodge"),
                                                ClientConfig.EnableArrowDodgeFeature)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.feature.arrow_dodge.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableArrowDodgeFeature = newValue)
                                .build());

                mainCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.feature.damage_numbers"),
                                                ClientConfig.EnableDamageNumbersFeature)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.feature.damage_numbers.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableDamageNumbersFeature = newValue)
                                .build());

                // Item Stealing Settings Category
                ConfigCategory itemStealingCategory = builder
                                .getOrCreateCategory(Text.translatable("config.limitless.category.item_stealing"));

                itemStealingCategory.addEntry(entryBuilder
                                .startBooleanToggle(
                                                Text.translatable("config.limitless.item_stealing.only_player_items"),
                                                ClientConfig.OnlyStealPlayerItems)
                                .setDefaultValue(false)
                                .setTooltip(Text.translatable(
                                                "config.limitless.item_stealing.only_player_items.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.OnlyStealPlayerItems = newValue)
                                .build());

                itemStealingCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.item_stealing.enable_stealing"),
                                                ClientConfig.EnableStealing)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.item_stealing.enable_stealing.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableStealing = newValue)
                                .build());

                // Combat Settings Category
                ConfigCategory combatCategory = builder
                                .getOrCreateCategory(Text.translatable("config.limitless.category.combat"));

                combatCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.combat.players_glow"),
                                                ClientConfig.PlayersShouldGlow)
                                .setDefaultValue(false)
                                .setTooltip(Text.translatable("config.limitless.combat.players_glow.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.PlayersShouldGlow = newValue)
                                .build());

                combatCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.combat.show_bow_trajectory"),
                                                ClientConfig.ShowBowTrajectory)
                                .setDefaultValue(false)
                                .setTooltip(Text.translatable("config.limitless.combat.show_bow_trajectory.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.ShowBowTrajectory = newValue)
                                .build());

                combatCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.combat.enable_arrow_dodging"),
                                                ClientConfig.EnableArrowDodging)
                                .setDefaultValue(false)
                                .setTooltip(Text.translatable("config.limitless.combat.enable_arrow_dodging.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableArrowDodging = newValue)
                                .build());

                combatCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.combat.show_damage_numbers"),
                                                ClientConfig.ShowDamageNumbers)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.combat.show_damage_numbers.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.ShowDamageNumbers = newValue)
                                .build());

                combatCategory.addEntry(entryBuilder
                                .startEnumSelector(Text.translatable("config.limitless.combat.damage_font"),
                                                DamageFontManager.FontOption.class,
                                                DamageFontManager.getFontOption(ClientConfig.DamageNumberFont))
                                .setDefaultValue(DamageFontManager.FontOption.DEFAULT)
                                .setTooltip(Text.translatable("config.limitless.combat.damage_font.tooltip"))
                                .setEnumNameProvider(fontOption -> Text
                                                .literal(((DamageFontManager.FontOption) fontOption).getDisplayName()))
                                .setSaveConsumer(
                                                fontOption -> ClientConfig.DamageNumberFont = ((DamageFontManager.FontOption) fontOption)
                                                                .getKey())
                                .build());

                // Zoom Settings Category
                ConfigCategory zoomCategory = builder
                                .getOrCreateCategory(Text.translatable("config.limitless.category.zoom"));

                zoomCategory.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("config.limitless.zoom.enable_zoom"),
                                                ClientConfig.EnableZoom)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable("config.limitless.zoom.enable_zoom.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.EnableZoom = newValue)
                                .build());

                zoomCategory.addEntry(entryBuilder
                                .startFloatField(Text.translatable("config.limitless.zoom.zoom_level"),
                                                ClientConfig.ZoomLevel)
                                .setDefaultValue(3.0f)
                                .setMin(1.0f)
                                .setMax(20.0f)
                                .setTooltip(Text.translatable("config.limitless.zoom.zoom_level.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.ZoomLevel = newValue)
                                .build());

                zoomCategory.addEntry(entryBuilder
                                .startFloatField(Text.translatable("config.limitless.zoom.min_zoom_level"),
                                                ClientConfig.MinZoomLevel)
                                .setDefaultValue(1.5f)
                                .setMin(1.0f)
                                .setMax(10.0f)
                                .setTooltip(Text.translatable("config.limitless.zoom.min_zoom_level.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.MinZoomLevel = newValue)
                                .build());

                zoomCategory.addEntry(entryBuilder
                                .startFloatField(Text.translatable("config.limitless.zoom.max_zoom_level"),
                                                ClientConfig.MaxZoomLevel)
                                .setDefaultValue(10.0f)
                                .setMin(2.0f)
                                .setMax(50.0f)
                                .setTooltip(Text.translatable("config.limitless.zoom.max_zoom_level.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.MaxZoomLevel = newValue)
                                .build());

                zoomCategory.addEntry(entryBuilder
                                .startFloatField(Text.translatable("config.limitless.zoom.scroll_sensitivity"),
                                                ClientConfig.ZoomScrollSensitivity)
                                .setDefaultValue(0.5f)
                                .setMin(0.1f)
                                .setMax(2.0f)
                                .setTooltip(Text.translatable("config.limitless.zoom.scroll_sensitivity.tooltip"))
                                .setSaveConsumer(newValue -> ClientConfig.ZoomScrollSensitivity = newValue)
                                .build());

                return builder.build();
        }
}
