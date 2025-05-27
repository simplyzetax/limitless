package me.simplyzetax.limitless.client.mixin;

import me.simplyzetax.limitless.shulkerbox.ShulkerBoxData;
import me.simplyzetax.limitless.shulkerbox.ShulkerBoxManager;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin {

    @Inject(method = "onOpen", at = @At("HEAD"))
    private void onShulkerBoxOpened(net.minecraft.entity.player.PlayerEntity player, CallbackInfo ci) {
        if (player.getWorld().isClient() || !(player instanceof ServerPlayerEntity)) {
            return;
        }

        ShulkerBoxBlockEntity shulkerBox = (ShulkerBoxBlockEntity) (Object) this;

        // Capture the shulker box when it's opened (alternative detection method)
        captureShulkerBoxFromBlockEntity(shulkerBox, player);
    }

    private void captureShulkerBoxFromBlockEntity(ShulkerBoxBlockEntity blockEntity,
            net.minecraft.entity.player.PlayerEntity player) {
        try {
            // Create an ItemStack representation of the shulker box
            ItemStack shulkerBoxStack = new ItemStack(blockEntity.getCachedState().getBlock());

            // Get the inventory contents
            DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
            for (int i = 0; i < blockEntity.size(); i++) {
                inventory.set(i, blockEntity.getStack(i).copy());
            }

            // Create container component from inventory
            ContainerComponent contents = ContainerComponent.fromStacks(inventory);
            shulkerBoxStack.set(DataComponentTypes.CONTAINER, contents);

            // Create shulker box data
            ShulkerBoxData shulkerBoxData = new ShulkerBoxData(
                    shulkerBoxStack,
                    player.getName().getString(),
                    blockEntity.getPos(),
                    contents);

            // Add to manager
            ShulkerBoxManager.addShulkerBox(shulkerBoxData);

        } catch (Exception e) {
            me.simplyzetax.limitless.Limitless.LOGGER.error("Failed to capture shulker box from block entity", e);
        }
    }
}
