package draylar.tiered.reforge;

import java.util.*;

import com.mojang.blaze3d.systems.RenderSystem;

import draylar.tiered.Tiered;
import draylar.tiered.api.TieredItemTags;
import draylar.tiered.network.TieredClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.Tab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ReforgeScreen extends HandledScreen<ReforgeScreenHandler> implements ScreenHandlerListener, Tab {

    public static final Identifier TEXTURE = new Identifier("tiered", "textures/gui/reforging_screen.png");
    public ReforgeScreen.ReforgeButton reforgeButton;
    private ItemStack last;
    private List<ItemStack> baseItems;

    public ReforgeScreen(ReforgeScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
        this.titleX = 60;
    }

    @Override
    protected void init() {
        super.init();
        ((ReforgeScreenHandler) this.handler).addListener(this);

        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.reforgeButton = (ReforgeScreen.ReforgeButton) this.addDrawableChild(new ReforgeScreen.ReforgeButton(i + 79, j + 56, (button) -> {
            if (button instanceof ReforgeScreen.ReforgeButton && !((ReforgeScreen.ReforgeButton) button).disabled)
                TieredClientPacket.writeC2SReforgePacket();
        }));
    }

    @Override
    public void removed() {
        super.removed();
        ((ReforgeScreenHandler) this.handler).removeListener(this);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        RenderSystem.disableBlend();

        this.drawMouseoverTooltip(context, mouseX, mouseY);

        if (this.isPointWithinBounds(79, 56, 18, 18, (double) mouseX, (double) mouseY)) {
            ItemStack itemStack = this.getScreenHandler().getSlot(1).getStack();
            if (itemStack == null || itemStack.isEmpty()) {
                baseItems = Collections.emptyList();
            } else {
                if (itemStack != last) {
                    last = itemStack;
                    baseItems = new ArrayList<ItemStack>();
                    List<ItemStack> items = Tiered.REFORGE_ITEM_DATA_LOADER.getReforgeItems(itemStack.getItem());
                    if (!items.isEmpty()) {
                        baseItems.addAll(items);
                    } else if (itemStack.getItem() instanceof ToolItem toolItem) {
                        baseItems.addAll(Arrays.asList(toolItem.getMaterial().getRepairIngredient().getMatchingStacks()));
                    } else if (itemStack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().getRepairIngredient() != null) {
                        baseItems.addAll(Arrays.asList(armorItem.getMaterial().getRepairIngredient().getMatchingStacks()));
                    } else {
                        for (RegistryEntry<Item> itemRegistryEntry : Registries.ITEM.getOrCreateEntryList(TieredItemTags.REFORGE_BASE_ITEM)) {
                            baseItems.add(itemRegistryEntry.value().getDefaultStack());
                        }
                    }
                }
            }
            List<Text> tooltip = new ArrayList<Text>();
            if (!baseItems.isEmpty()) {
                ItemStack ingredient = this.getScreenHandler().getSlot(0).getStack();
                if (ingredient != null && !ingredient.isEmpty() && baseItems.contains(ingredient)) {
                } else {
                    tooltip.add(Text.translatable("screen.tiered.reforge_ingredient"));
                    for (ItemStack stack : baseItems) {
                        tooltip.add(stack.getName());
                    }
                }
            }
            if (itemStack.isDamageable() && itemStack.isDamaged()) {
                tooltip.add(Text.translatable("screen.tiered.reforge_damaged"));
            }
            if (!tooltip.isEmpty()) {
                context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
    }

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
    }

    @Override
    public Class<?> getParentScreenClass() {
        return AnvilScreen.class;
    }

    public class ReforgeButton extends ButtonWidget {
        private boolean disabled;

        public ReforgeButton(int x, int y, ButtonWidget.PressAction onPress) {
            super(x, y, 18, 18, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.disabled = true;
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int j = 176;
            if (this.disabled) {
                j += this.width * 2;
            } else if (this.isHovered()) {
                j += this.width;
            }
            context.drawTexture(TEXTURE, this.getX(), this.getY(), j, 0, this.width, this.height);
        }

        public void setDisabled(boolean disable) {
            this.disabled = disable;
        }

    }

}
