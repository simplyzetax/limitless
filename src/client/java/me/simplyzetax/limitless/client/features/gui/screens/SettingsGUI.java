package me.simplyzetax.limitless.client.features.gui.screens;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.LimitlessClient;
import me.simplyzetax.limitless.client.shared.config.ClientConfig;
import me.simplyzetax.limitless.client.features.damagenumbers.util.DamageFontManager;
import me.simplyzetax.limitless.client.features.itemstealing.managers.LimitlessItemGroupManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class SettingsGUI extends BaseOwoScreen<FlowLayout> {

        @Override
        protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
                return OwoUIAdapter.create(this, Containers::verticalFlow);
        }

        @Override
        protected void build(FlowLayout rootComponent) {
                rootComponent
                                .surface(Surface.blur(3f, 5f))
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                .verticalAlignment(VerticalAlignment.CENTER)
                                .padding(Insets.both(10, 10)); // Add some padding around the content

                // Add a title
                rootComponent.child(
                                Components.label(Text.literal("Limitless Settings").formatted(Formatting.BOLD,
                                                Formatting.WHITE))
                                                .horizontalSizing(Sizing.content())
                                                .margins(Insets.bottom(15)) // Space below the title
                );

                FlowLayout buttonContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
                buttonContainer.gap(8); // Add gap between buttons
                buttonContainer.horizontalAlignment(HorizontalAlignment.CENTER);

                buttonContainer.child(
                                Components.button(
                                                // Initialize the button text with the current value of the config
                                                Text.literal("Only Player Items: ")
                                                                .append(Text.literal(
                                                                                ClientConfig.OnlyStealPlayerItems ? "✔"
                                                                                                : "✖")
                                                                                .styled(style -> style.withColor(
                                                                                                ClientConfig.OnlyStealPlayerItems
                                                                                                                ? Formatting.GREEN
                                                                                                                : Formatting.RED))),
                                                button -> {
                                                        // Toggle the configuration value
                                                        ClientConfig.OnlyStealPlayerItems = !ClientConfig.OnlyStealPlayerItems;

                                                        // Update the button label
                                                        button.setMessage(
                                                                        Text.literal("Only Player Items: ")
                                                                                        .append(Text.literal(
                                                                                                        ClientConfig.OnlyStealPlayerItems
                                                                                                                        ? "✔"
                                                                                                                        : "✖")
                                                                                                        .styled(style -> style
                                                                                                                        .withColor(ClientConfig.OnlyStealPlayerItems
                                                                                                                                        ? Formatting.GREEN
                                                                                                                                        : Formatting.RED))));
                                                })
                                                .horizontalSizing(Sizing.fixed(180)) // Adjust the button width
                                                .tooltip(Text.literal(
                                                                "If enabled, only items from other players will be added to the creative tab.")
                                                                .styled(style -> style.withColor(Formatting.GRAY))));

                buttonContainer.child(
                                Components.button(
                                                // Initialize the button text with the current value of the config
                                                Text.literal("Enable Stealing: ")
                                                                .append(Text.literal(
                                                                                ClientConfig.EnableStealing ? "✔" : "✖")
                                                                                .styled(style -> style.withColor(
                                                                                                ClientConfig.EnableStealing
                                                                                                                ? Formatting.GREEN
                                                                                                                : Formatting.RED))),
                                                button -> {
                                                        // Toggle the configuration value
                                                        ClientConfig.EnableStealing = !ClientConfig.EnableStealing;

                                                        // Update the button label
                                                        button.setMessage(
                                                                        Text.literal("Enable Stealing: ")
                                                                                        .append(Text.literal(
                                                                                                        ClientConfig.EnableStealing
                                                                                                                        ? "✔"
                                                                                                                        : "✖")
                                                                                                        .styled(style -> style
                                                                                                                        .withColor(ClientConfig.EnableStealing
                                                                                                                                        ? Formatting.GREEN
                                                                                                                                        : Formatting.RED))));
                                                })
                                                .horizontalSizing(Sizing.fixed(180)) // Adjust the button width
                                                .tooltip(Text.literal(
                                                                "Toggles whether items seen from other players are added to the creative tab.")
                                                                .styled(style -> style.withColor(Formatting.GRAY))));

                buttonContainer.child(
                                Components.button(
                                                // Initialize the button text with the current value of the config
                                                Text.literal("Show Bow Trajectory: ")
                                                                .append(Text.literal(
                                                                                ClientConfig.ShowBowTrajectory ? "✔"
                                                                                                : "✖")
                                                                                .styled(style -> style.withColor(
                                                                                                ClientConfig.ShowBowTrajectory
                                                                                                                ? Formatting.GREEN
                                                                                                                : Formatting.RED))),
                                                button -> {
                                                        // Toggle the configuration value
                                                        ClientConfig.ShowBowTrajectory = !ClientConfig.ShowBowTrajectory;

                                                        // Update the button label
                                                        button.setMessage(
                                                                        Text.literal("Show Bow Trajectory: ")
                                                                                        .append(Text.literal(
                                                                                                        ClientConfig.ShowBowTrajectory
                                                                                                                        ? "✔"
                                                                                                                        : "✖")
                                                                                                        .styled(style -> style
                                                                                                                        .withColor(ClientConfig.ShowBowTrajectory
                                                                                                                                        ? Formatting.GREEN
                                                                                                                                        : Formatting.RED))));
                                                })
                                                .horizontalSizing(Sizing.fixed(180)) // Adjust the button width
                                                .tooltip(Text.literal(
                                                                "Toggles the display of bow trajectory predictions when aiming with a bow.")
                                                                .styled(style -> style.withColor(Formatting.GRAY))));

                buttonContainer.child(
                                Components.button(
                                                // Initialize the button text with the current value of the config
                                                Text.literal("Enable Arrow Dodging: ")
                                                                .append(Text.literal(
                                                                                ClientConfig.EnableArrowDodging ? "✔"
                                                                                                : "✖")
                                                                                .styled(style -> style.withColor(
                                                                                                ClientConfig.EnableArrowDodging
                                                                                                                ? Formatting.GREEN
                                                                                                                : Formatting.RED))),
                                                button -> {
                                                        // Toggle the configuration value
                                                        ClientConfig.EnableArrowDodging = !ClientConfig.EnableArrowDodging;

                                                        // Update the button label
                                                        button.setMessage(
                                                                        Text.literal("Enable Arrow Dodging: ")
                                                                                        .append(Text.literal(
                                                                                                        ClientConfig.EnableArrowDodging
                                                                                                                        ? "✔"
                                                                                                                        : "✖")
                                                                                                        .styled(style -> style
                                                                                                                        .withColor(ClientConfig.EnableArrowDodging
                                                                                                                                        ? Formatting.GREEN
                                                                                                                                        : Formatting.RED))));
                                                })
                                                .horizontalSizing(Sizing.fixed(180)) // Adjust the button width
                                                .tooltip(Text.literal(
                                                                "Automatically detects incoming arrows and executes evasive movements to avoid hits.")
                                                                .styled(style -> style.withColor(Formatting.GRAY))));

                buttonContainer.child(
                                Components.button(
                                                // Initialize the button text with the current value of the config
                                                Text.literal("Show Damage Numbers: ")
                                                                .append(Text.literal(
                                                                                ClientConfig.ShowDamageNumbers ? "✔"
                                                                                                : "✖")
                                                                                .styled(style -> style.withColor(
                                                                                                ClientConfig.ShowDamageNumbers
                                                                                                                ? Formatting.GREEN
                                                                                                                : Formatting.RED))),
                                                button -> {
                                                        // Toggle the configuration value
                                                        ClientConfig.ShowDamageNumbers = !ClientConfig.ShowDamageNumbers;

                                                        // Update the button label
                                                        button.setMessage(
                                                                        Text.literal("Show Damage Numbers: ")
                                                                                        .append(Text.literal(
                                                                                                        ClientConfig.ShowDamageNumbers
                                                                                                                        ? "✔"
                                                                                                                        : "✖")
                                                                                                        .styled(style -> style
                                                                                                                        .withColor(ClientConfig.ShowDamageNumbers
                                                                                                                                        ? Formatting.GREEN
                                                                                                                                        : Formatting.RED))));
                                                })
                                                .horizontalSizing(Sizing.fixed(180)) // Adjust the button width
                                                .tooltip(Text.literal(
                                                                "Displays floating damage numbers above entities when they take damage or heal.")
                                                                .styled(style -> style.withColor(Formatting.GRAY))));

                buttonContainer.child(
                                Components.button(
                                                // Initialize the button text with the current font
                                                Text.literal("Damage Font: ")
                                                                .append(Text.literal(ClientConfig.DamageNumberFont
                                                                                .toUpperCase())
                                                                                .styled(style -> style.withColor(
                                                                                                Formatting.YELLOW))),
                                                button -> {
                                                        // Cycle to next font
                                                        DamageFontManager.cycleFont();

                                                        // Update the button label
                                                        button.setMessage(
                                                                        Text.literal("Damage Font: ")
                                                                                        .append(Text.literal(
                                                                                                        ClientConfig.DamageNumberFont
                                                                                                                        .toUpperCase())
                                                                                                        .styled(style -> style
                                                                                                                        .withColor(Formatting.YELLOW))));
                                                })
                                                .horizontalSizing(Sizing.fixed(180)) // Adjust the button width
                                                .tooltip(Text.literal(
                                                                "Changes the font used for damage numbers. Options: Default, Uniform, Alt, Compact, Custom")
                                                                .styled(style -> style.withColor(Formatting.GRAY))));

                buttonContainer.child(
                                Components.button(
                                                Text.literal("Clear Stolen Items")
                                                                .styled(style -> style.withColor(Formatting.GOLD)),
                                                button -> {
                                                        LimitlessItemGroupManager.EQUIPPED_ITEMS.clear();
                                                        Limitless.LOGGER.info(
                                                                        "Cleared all stolen items from the creative tab.");
                                                        updateCreativeInventoryScreen();
                                                })
                                                .horizontalSizing(Sizing.fixed(180)) // Adjust the button width
                                                .tooltip(Text.literal(
                                                                "Removes all items collected by Limitless from the creative tab.")
                                                                .styled(style -> style.withColor(Formatting.GRAY))));

                buttonContainer.child(
                                Components.button(
                                                Text.literal("Refresh Creative Tab")
                                                                .styled(style -> style.withColor(Formatting.AQUA)),
                                                button -> {
                                                        Limitless.LOGGER.info("Refreshed the Limitless creative tab.");
                                                        updateCreativeInventoryScreen();
                                                })
                                                .horizontalSizing(Sizing.fixed(180)) // Adjust the button width
                                                .tooltip(Text.literal("Forces a refresh of the Limitless creative tab.")
                                                                .styled(style -> style.withColor(Formatting.GRAY))));

                rootComponent.child(buttonContainer);

        }

        public void updateCreativeInventoryScreen() {
                MinecraftClient client = MinecraftClient.getInstance();
                ClientPlayerEntity player = client.player;
                if (player == null)
                        return;

                client.setScreen(null);
                client.setScreen(new CreativeInventoryScreen(player, LimitlessClient.enabledFeatures, true));
                client.setScreen(this);
        }
}