package me.simplyzetax.limitless.client.mixin;

import me.simplyzetax.limitless.Limitless;
import me.simplyzetax.limitless.client.LimitlessClient;
import me.simplyzetax.limitless.client.config.ClientConfig;
import me.simplyzetax.limitless.stealer.ShulkerBoxItemGroupManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Date;

@Environment(EnvType.CLIENT)
@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxInteractionMixin {

    @Inject(method = "onUse", at = @At("HEAD"))
    private void onShulkerBoxInteraction(net.minecraft.block.BlockState state, World world, BlockPos pos,
            net.minecraft.entity.player.PlayerEntity player,
            BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {

        // Only proceed if we're on the client side
        if (!world.isClient()) {
            return;
        }

        // Check if stealing is enabled
        if (!ClientConfig.EnableStealing) {
            return;
        }

        // Check if this is the client player
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || !client.player.equals(player)) {
            return;
        }

        // Get the shulker box block entity
        var blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof net.minecraft.block.entity.ShulkerBoxBlockEntity shulkerBoxEntity)) {
            return;
        }

        // Create an ItemStack representation of the shulker box with its contents
        ItemStack shulkerBoxStack = new ItemStack(state.getBlock().asItem());

        // Check if the shulker box has any items
        boolean hasItems = false;
        for (int i = 0; i < shulkerBoxEntity.size(); i++) {
            if (!shulkerBoxEntity.getStack(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            return; // Skip empty shulker boxes
        }

        // Copy the inventory contents from the block entity to the ItemStack
        // Use the same approach as ShulkerBoxBlock does when creating drops
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        for (int i = 0; i < shulkerBoxEntity.size(); i++) {
            ItemStack slotStack = shulkerBoxEntity.getStack(i);
            if (!slotStack.isEmpty()) {
                items.add(slotStack.copy());
            }
        }

        // Create ContainerComponent from the items
        ContainerComponent container = ContainerComponent.fromStacks(items);
        shulkerBoxStack.set(DataComponentTypes.CONTAINER, container);

        // Check if we already have this exact shulker box
        if (containsEquivalentShulkerBox(shulkerBoxStack)) {
            return;
        }

        // Add custom data
        NbtCompound customDataNbt = new NbtCompound();
        customDataNbt.putString("ObtainedFrom", "Block Interaction");
        customDataNbt.putString("ObtainedTime", new Date().toString());
        customDataNbt.putString("Position", pos.toShortString());
        NbtComponent customDataComponent = NbtComponent.of(customDataNbt);

        shulkerBoxStack.set(DataComponentTypes.CUSTOM_DATA, customDataComponent);

        // Log and add the shulker box
        Limitless.LOGGER.info("Adding shulker box from interaction at {} to Shulker Box tab: {}",
                pos.toShortString(), shulkerBoxStack.getItem().getTranslationKey());

        ShulkerBoxItemGroupManager.addShulkerBox(shulkerBoxStack);

        // Refresh the creative inventory screen
        Limitless.LOGGER.info("Scheduling creative screen refresh for new shulker box...");
        client.execute(() -> {
            ClientPlayerEntity clientPlayer = client.player;
            if (clientPlayer != null) {
                boolean operatorTabEnabled = clientPlayer.hasPermissionLevel(2);

                Screen currentScreen = client.currentScreen;

                client.setScreen(null);
                client.setScreen(
                        new CreativeInventoryScreen(clientPlayer, LimitlessClient.enabledFeatures, operatorTabEnabled));
                client.setScreen(currentScreen);
            }
        });
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
