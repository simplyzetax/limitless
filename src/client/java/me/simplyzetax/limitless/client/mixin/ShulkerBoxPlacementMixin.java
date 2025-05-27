package me.simplyzetax.limitless.client.mixin;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.config.ClientConfig;
import me.simplyzetax.limitless.stealer.ShulkerBoxItemGroupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;

@Environment(EnvType.CLIENT)
@Mixin(Block.class)
public class ShulkerBoxPlacementMixin {

    @Inject(method = "onPlaced", at = @At("HEAD"))
    private void onShulkerBoxPlaced(World world, BlockPos pos, BlockState state,
            net.minecraft.entity.LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {

        // Only proceed if we're on the client side
        if (!world.isClient()) {
            return;
        }

        // Check if stealing is enabled
        if (!ClientConfig.EnableStealing) {
            return;
        }

        // Check if the placed block is a shulker box
        if (!(state.getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }

        // Check if the item stack has container contents (items inside)
        ContainerComponent container = itemStack.get(DataComponentTypes.CONTAINER);
        if (container == null || container.stream().allMatch(ItemStack::isEmpty)) {
            return; // Skip empty shulker boxes
        }

        // Check if we already have this exact shulker box
        if (containsEquivalentShulkerBox(itemStack)) {
            return;
        }

        // Create a copy of the shulker box with metadata
        ItemStack shulkerBoxCopy = itemStack.copy();

        // Add custom data
        NbtCompound customDataNbt = new NbtCompound();
        String placerName = placer != null ? placer.getDisplayName().getString() : "Unknown";
        customDataNbt.putString("ObtainedFrom", placerName);
        customDataNbt.putString("ObtainedTime", new Date().toString());
        customDataNbt.putString("PlacedAt", pos.toShortString());
        NbtComponent customDataComponent = NbtComponent.of(customDataNbt);

        shulkerBoxCopy.set(DataComponentTypes.CUSTOM_DATA, customDataComponent);

        // Log and add the shulker box
        Limitless.LOGGER.info("Adding shulker box placed by '{}' at {} to Shulker Box tab: {}",
                placerName, pos.toShortString(), shulkerBoxCopy.getItem().getTranslationKey());

        ShulkerBoxItemGroupManager.addShulkerBox(shulkerBoxCopy);

        // Refresh the creative inventory screen
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            Limitless.LOGGER.info("Scheduling creative screen refresh for new shulker box...");
            client.execute(() -> {
                ClientPlayerEntity player = client.player;
                if (player != null) {
                    net.minecraft.resource.featuretoggle.FeatureSet enabledFeatures = client.world.getEnabledFeatures();
                    boolean operatorTabEnabled = player.hasPermissionLevel(2);

                    Screen currentScreen = client.currentScreen;

                    client.setScreen(null);
                    client.setScreen(new CreativeInventoryScreen(player, enabledFeatures, operatorTabEnabled));
                    client.setScreen(currentScreen);
                }
            });
        }
    }

    /**
     * Checks if an equivalent shulker box already exists in SHULKER_BOX_ITEMS.
     */
    private static boolean containsEquivalentShulkerBox(ItemStack shulkerBox) {
        for (ItemStack existing : ShulkerBoxItemGroupManager.SHULKER_BOX_ITEMS) {
            if (existing.getItem() == shulkerBox.getItem()
                    && ItemStack.areItemsEqual(existing, shulkerBox)
                    && existing.getName().equals(shulkerBox.getName())) {
                return true;
            }
        }
        return false;
    }
}
