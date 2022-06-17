package draylar.tiered.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.access.AnvilScreenHandlerAccess;
import draylar.tiered.config.ConfigInit;
import draylar.tiered.network.TieredClientPacket;
import draylar.tiered.reforge.ReforgeScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends ForgingScreen<AnvilScreenHandler> {

    @Shadow
    @Mutable
    @Final
    private PlayerEntity player;

    public AnvilScreenMixin(AnvilScreenHandler handler, PlayerInventory playerInventory, Text title, Identifier texture) {
        super(handler, playerInventory, title, texture);
    }

    @Inject(method = "Lnet/minecraft/client/gui/screen/ingame/AnvilScreen;<init>(Lnet/minecraft/screen/AnvilScreenHandler;Lnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/text/Text;)V", at = @At("TAIL"))
    public void initMixin(AnvilScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo info) {
        TieredClientPacket.writeC2SSyncPosPacket(true);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        super.drawBackground(matrices, delta, mouseX, mouseY);

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        RenderSystem.setShaderTexture(0, ReforgeScreen.TEXTURE);

        // // bag icon
        // this.drawTexture(matrices, this.x, this.y - 23, 0, 110, 24, 27);
        // // skill icon
        // this.drawTexture(matrices, this.x + 25, this.y - 21, 48, 110, 24, 21);

        // if (this.isPointWithinBounds(26, -20, 22, 19, (double) mouseX, (double) mouseY))
        // this.renderTooltip(matrices, Text.translatable("screen.levelz.skill_screen"), mouseX, mouseY);

        if (this.isPointWithinBounds(6 + ConfigInit.CONFIG.xIconPosition, -17 + ConfigInit.CONFIG.yIconPosition, 20, 20, (double) mouseX, (double) mouseY))
            AnvilScreenMixin.drawTexture(matrices, i + 6 + ConfigInit.CONFIG.xIconPosition, j - 17 + ConfigInit.CONFIG.yIconPosition, 196, 0, 20, 18, 256, 256);
        else
            AnvilScreenMixin.drawTexture(matrices, i + 6 + ConfigInit.CONFIG.xIconPosition, j - 17 + ConfigInit.CONFIG.yIconPosition, 176, 0, 20, 18, 256, 256);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isPointWithinBounds(6 + ConfigInit.CONFIG.xIconPosition, -17 + ConfigInit.CONFIG.yIconPosition, 20, 18, (double) mouseX, (double) mouseY))
            TieredClientPacket.writeC2SScreenPacket(((AnvilScreenHandlerAccess) handler).getPos(), (int) this.client.mouse.getX(), (int) this.client.mouse.getY(), true);

        return super.mouseClicked(mouseX, mouseY, button);

    }
}
