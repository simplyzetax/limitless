package me.simplyzetax.limitless.client.screens;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.config.ClientConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;


public class TestGUI extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.blur(3f, 5f))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        rootComponent.child(
                Components.button(
                                // Initialize the button text with the current value of the config
                                Text.literal("Only Player Items: ")
                                        .append(Text.literal(ClientConfig.OnlyStealPlayerItems ? "✔" : "✖")
                                                .styled(style -> style.withColor(ClientConfig.OnlyStealPlayerItems ? Formatting.GREEN : Formatting.RED))),
                                button -> {
                                    // Toggle the configuration value
                                    ClientConfig.OnlyStealPlayerItems = !ClientConfig.OnlyStealPlayerItems;

                                    // Update the button label
                                    button.setMessage(
                                            Text.literal("Only Player Items: ")
                                                    .append(Text.literal(ClientConfig.OnlyStealPlayerItems ? "✔" : "✖")
                                                            .styled(style -> style.withColor(ClientConfig.OnlyStealPlayerItems ? Formatting.GREEN : Formatting.RED)))
                                    );
                                }
                        )
                        .horizontalSizing(Sizing.fixed(150)) // Adjust the button width as needed
                        .tooltip(Text.literal("Toggle whether only player items are stolen").styled(style -> style.withColor(Formatting.GRAY))) // Add tooltip
        );

        rootComponent.child(
                Components.button(
                                // Initialize the button text with the current value of the config
                                Text.literal("Enable Stealing: ")
                                        .append(Text.literal(ClientConfig.EnableStealing ? "✔" : "✖")
                                                .styled(style -> style.withColor(ClientConfig.EnableStealing ? Formatting.GREEN : Formatting.RED))),
                                button -> {
                                    // Toggle the configuration value
                                    ClientConfig.EnableStealing = !ClientConfig.EnableStealing;

                                    // Update the button label
                                    button.setMessage(
                                            Text.literal("Enable Stealing: ")
                                                    .append(Text.literal(ClientConfig.EnableStealing ? "✔" : "✖")
                                                            .styled(style -> style.withColor(ClientConfig.EnableStealing ? Formatting.GREEN : Formatting.RED)))
                                    );
                                }
                        )
                        .horizontalSizing(Sizing.fixed(150)) // Adjust the button width as needed
                        .tooltip(Text.literal("Toggles item stealing").styled(style -> style.withColor(Formatting.GRAY))) // Add tooltip
        );

        rootComponent.child(
                Components.button(
                                // Initialize the button text with the current value of the config
                                Text.literal("Clear items")
                                        .styled(style -> style.withColor(Formatting.RED)),
                                button -> {
                                    // Toggle the configuration value
                                    Limitless.EQUIPPED_ITEMS.clear();
                                    Limitless.LOGGER.info("Cleared all items");

                                    updateCreativeInventoryScreen();
                                }
                        )
                        .horizontalSizing(Sizing.fixed(150)) // Adjust the button width as needed
                        .tooltip(Text.literal("Toggles item stealing").styled(style -> style.withColor(Formatting.GRAY))) // Add tooltip
        );

    }

    public void updateCreativeInventoryScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        net.minecraft.resource.featuretoggle.FeatureSet enabledFeatures = client.world.getEnabledFeatures();
        boolean operatorTabEnabled = player.hasPermissionLevel(2);

        client.setScreen(null);
        client.setScreen(new CreativeInventoryScreen(player, enabledFeatures, operatorTabEnabled));
        client.setScreen(this);
    }
}