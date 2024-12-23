package me.simplyzetax.limitless.client.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void modifyPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        Text originalName = cir.getReturnValue();

        if (entry.getProfile().getName().equalsIgnoreCase("lenesce")) {
            Text customText = Text.literal(" [LIM]").styled(style -> style.withColor(TextColor.fromRgb(0xFFD700))); // Gold color
            cir.setReturnValue(Text.literal("").append(originalName).append(customText));
        }
    }
}
