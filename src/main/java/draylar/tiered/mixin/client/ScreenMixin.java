package draylar.tiered.mixin.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import draylar.tiered.TieredClient;
import draylar.tiered.api.BorderTemplate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public class ScreenMixin {

    @Shadow
    protected TextRenderer textRenderer;
    @Shadow
    protected ItemRenderer itemRenderer;
    @Shadow
    public int width;
    @Shadow
    public int height;

    private Map<ItemStack, BorderTemplate> borderMap = new HashMap<ItemStack, BorderTemplate>();

    @Inject(method = "Lnet/minecraft/client/gui/screen/Screen;renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
    protected void renderTooltipMixin(MatrixStack matrices, ItemStack stack, int x, int y, CallbackInfo info) {
        if (stack.hasNbt() && stack.getNbt().contains("Tiered")) {

            String nbtString = stack.getNbt().getCompound("Tiered").asString();
            for (int i = 0; i < TieredClient.BORDER_TEMPLATES.size(); i++) {
                if (!TieredClient.BORDER_TEMPLATES.get(i).containsStack(stack) && TieredClient.BORDER_TEMPLATES.get(i).containsDecider(nbtString)) {
                    TieredClient.BORDER_TEMPLATES.get(i).addStack(stack);
                } else if (TieredClient.BORDER_TEMPLATES.get(i).containsStack(stack)) {
                    List<TooltipComponent> list = getTooltipFromItem(stack).stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
                    stack.getTooltipData().ifPresent(data -> list.add(1, TooltipComponent.of(data)));
                    renderTieredTooltipFromComponents(matrices, list, x, y, TieredClient.BORDER_TEMPLATES.get(i));
                    info.cancel();
                    break;
                }
            }
            // ItemStack itemStack2 = stack.copy();
            // itemStack2.setDamage(0);

            // // System.out.println(borderMap);
            // if (!borderMap.containsKey(itemStack2)) {
            // String nbtString = stack.getNbt().getCompound("Tiered").asString();
            // boolean foundTemplate = false;
            // for (int i = 0; i < TieredClient.BORDER_TEMPLATES.size(); i++)
            // if (TieredClient.BORDER_TEMPLATES.get(i).containsDecider(nbtString)) {
            // borderMap.put(itemStack2, TieredClient.BORDER_TEMPLATES.get(i));
            // System.out.println("PUT: " + itemStack2);
            // foundTemplate = true;
            // break;
            // }
            // if (!foundTemplate)
            // borderMap.put(itemStack2, null);
            // } else if (borderMap.get(itemStack2) != null) {
            // List<TooltipComponent> list = getTooltipFromItem(stack).stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
            // stack.getTooltipData().ifPresent(data -> list.add(1, TooltipComponent.of(data)));
            // renderTieredTooltipFromComponents(matrices, list, x, y, borderMap.get(itemStack2));
            // System.out.println("FOUND");
            // info.cancel();
            // }

            // ItemStack itemStack2 = stack.copy();
            // itemStack2.setDamage(0);

            // System.out.println(borderMap);
            // if (!borderMap.containsKey(itemStack2)) {
            // String nbtString = stack.getNbt().getCompound("Tiered").asString();
            // boolean foundTemplate = false;
            // for (int i = 0; i < TieredClient.BORDER_TEMPLATES.size(); i++)
            // if (TieredClient.BORDER_TEMPLATES.get(i).containsDecider(nbtString)) {
            // borderMap.put(itemStack2, TieredClient.BORDER_TEMPLATES.get(i));
            // System.out.println("PUT: " + itemStack2);
            // foundTemplate = true;
            // break;
            // }
            // if (!foundTemplate)
            // borderMap.put(itemStack2, null);
            // } else if (borderMap.get(itemStack2) != null) {
            // List<TooltipComponent> list = getTooltipFromItem(stack).stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
            // stack.getTooltipData().ifPresent(data -> list.add(1, TooltipComponent.of(data)));
            // renderTieredTooltipFromComponents(matrices, list, x, y, borderMap.get(itemStack2));
            // System.out.println("FOUND");
            // info.cancel();
            // }
        }
    }

    private void renderTieredTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> components, int x, int y, BorderTemplate borderTemplate) {
        TooltipComponent tooltipComponent2;
        int t;
        int k;
        if (components.isEmpty()) {
            return;
        }
        int i = 0;
        int j = components.size() == 1 ? -2 : 0;
        for (TooltipComponent tooltipComponent : components) {
            k = tooltipComponent.getWidth(this.textRenderer);
            if (k > i) {
                i = k;
            }
            j += tooltipComponent.getHeight();
        }
        if (i < 64)
            i = 64;
        if (j < 16)
            j = 16;

        int l = x + 12;
        int m = y - 12;
        k = i;
        int n = j;
        if (l + i > this.width) {
            l -= 28 + i;
        }
        if (m + n + 6 > this.height) {
            m = this.height - n - 6;
        }
        matrices.push();
        float f = this.itemRenderer.zOffset;
        this.itemRenderer.zOffset = 400.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        // background
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l - 3, m - 4, l + k + 3, m - 3, 400, -267386864, -267386864);
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l - 3, m + n + 3, l + k + 3, m + n + 4, 400, -267386864, -267386864);
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l - 3, m - 3, l + k + 3, m + n + 3, 400, -267386864, -267386864);
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l - 4, m - 3, l - 3, m + n + 3, 400, -267386864, -267386864);
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l + k + 3, m - 3, l + k + 4, m + n + 3, 400, -267386864, -267386864);

        int colorStart = borderTemplate.getStartGradient();
        int colorEnd = borderTemplate.getEndGradient();

        // border
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l - 3, m - 3 + 1, l - 3 + 1, m + n + 3 - 1, 400, colorStart, colorEnd);
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l + k + 2, m - 3 + 1, l + k + 3, m + n + 3 - 1, 400, colorStart, colorEnd);
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l - 3, m - 3, l + k + 3, m - 3 + 1, 400, colorStart, colorStart);
        DrawableHelperAccessor.callFillGradient(matrix4f, bufferBuilder, l - 3, m + n + 2, l + k + 3, m + n + 3, 400, colorEnd, colorEnd);

        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        matrices.translate(0.0, 0.0, 400.0);
        int s = m;
        for (t = 0; t < components.size(); ++t) {
            tooltipComponent2 = components.get(t);
            tooltipComponent2.drawText(this.textRenderer, l, s, matrix4f, immediate);
            s += tooltipComponent2.getHeight() + (t == 0 ? 2 : 0);
        }
        immediate.draw();
        matrices.pop();
        s = m;
        for (t = 0; t < components.size(); ++t) {
            tooltipComponent2 = components.get(t);
            tooltipComponent2.drawItems(this.textRenderer, l, s, matrices, this.itemRenderer, 400);
            s += tooltipComponent2.getHeight() + (t == 0 ? 2 : 0);
        }
        this.itemRenderer.zOffset = f;

        int border = borderTemplate.getIndex();
        int secondHalf = border > 7 ? 1 : 0;
        if (border > 7)
            border -= 8;

        matrices.push();
        RenderSystem.setShaderTexture(0, borderTemplate.getIdentifier());
        // left top corner
        DrawableHelper.drawTexture(matrices, l - 6, m - 6, 0 + secondHalf * 64, 0 + border * 16, 8, 8, 128, 128);
        // right top corner
        DrawableHelper.drawTexture(matrices, l + k - 2, m - 6, 56 + secondHalf * 64, 0 + border * 16, 8, 8, 128, 128);

        // left down corner
        DrawableHelper.drawTexture(matrices, l - 6, m + n - 2, 0 + secondHalf * 64, 8 + border * 16, 8, 8, 128, 128);
        // right down corner
        DrawableHelper.drawTexture(matrices, l + k - 2, m + n - 2, 56 + secondHalf * 64, 8 + border * 16, 8, 8, 128, 128);

        // edge filler
        // if (i > 64) {
        // // top
        // DrawableHelper.drawTexture(matrices, l + 2, m - 6, l + k - 3, 8, 7, 0, 1, 8, 128, 128);
        // // bottom
        // DrawableHelper.drawTexture(matrices, l + 2, m + n - 2, l + k - 3, 8, 56, 0, 1, 8, 128, 128);
        // }

        // middle header
        DrawableHelper.drawTexture(matrices, (l - 6 + l + k + 6) / 2 - 24, m - 9, 8 + secondHalf * 64, 0 + border * 16, 48, 8, 128, 128);
        // bottom footer
        DrawableHelper.drawTexture(matrices, (l - 6 + l + k + 6) / 2 - 24, m + n + 1, 8 + secondHalf * 64, 8 + border * 16, 48, 8, 128, 128);

        matrices.pop();
    }

    @Shadow
    public List<Text> getTooltipFromItem(ItemStack stack) {
        return null;
    }

}
