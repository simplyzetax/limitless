package me.simplyzetax.limitless.client.mixin;

import me.simplyzetax.limitless.shulkerbox.ShulkerBoxData;
import me.simplyzetax.limitless.shulkerbox.ShulkerBoxManager;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class ShulkerBoxPlacementMixin {

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("RETURN"))
    private void onBlockPlaced(net.minecraft.item.ItemPlacementContext context,
            CallbackInfoReturnable<net.minecraft.util.ActionResult> cir) {

        // Check if the placement was successful
        if (!cir.getReturnValue().isAccepted()) {
            return;
        }

        ItemStack stack = context.getStack();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        // Only process on server side and if it's a shulker box
        if (world.isClient() || player == null
                || !(((BlockItem) (Object) this).getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }

        // Capture the shulker box placement
        captureShulkerBoxPlacement(stack, world, pos, player);
    }

    private void captureShulkerBoxPlacement(ItemStack stack, World world, BlockPos pos, PlayerEntity player) {
        try {
            // Get the container contents from the item stack
            ContainerComponent contents = stack.get(DataComponentTypes.CONTAINER);

            // Create shulker box data
            ShulkerBoxData shulkerBoxData = new ShulkerBoxData(
                    stack,
                    player.getName().getString(),
                    pos,
                    contents);

            // Add to manager
            ShulkerBoxManager.addShulkerBox(shulkerBoxData);

        } catch (Exception e) {
            me.simplyzetax.limitless.Limitless.LOGGER.error("Failed to capture shulker box placement", e);
        }
    }
}
