package me.simplyzetax.limitless.client.mixin;

import me.simplyzetax.limitless.Limitless;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin {

    @Inject(method = "setSelectedTab", at = @At("HEAD"))
    private void onSetSelectedTab(ItemGroup tab, CallbackInfo ci) {
        // Check if the Limitless tab is being selected
        if (tab == Limitless.LIMITLESS_ITEM_GROUP) {
            // Refresh the creative inventory for the Limitless tab
            Limitless.EQUIPPED_ITEMS.forEach(item -> {
                System.out.println("Refreshing Limitless tab with: " + item.getItem().getTranslationKey());
            });
        }
    }
}
