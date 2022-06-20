package draylar.tiered.reforge;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

import org.jetbrains.annotations.Nullable;

import draylar.tiered.Tiered;
import draylar.tiered.api.TieredItemTags;

public class ReforgeScreenHandler extends ScreenHandler {

    private final Inventory inventory = new SimpleInventory(3) {
        @Override
        public void markDirty() {
            super.markDirty();
            ReforgeScreenHandler.this.onContentChanged(this);
        }
    };
    // protected final CraftingResultInventory output = new CraftingResultInventory();
    // protected final Inventory inventory = new SimpleInventory(3) {

    // @Override
    // public void markDirty() {
    // super.markDirty();
    // ReforgeScreenHandler.this.onContentChanged(this);
    // }
    // };
    private final ScreenHandlerContext context;
    private final PlayerEntity player;
    public BlockPos pos;
    public boolean reforgeReady;

    // protected abstract boolean canTakeOutput(PlayerEntity var1, boolean var2);

    // protected abstract void onTakeOutput(PlayerEntity var1, ItemStack var2);

    // protected abstract boolean canUse(BlockState var1);

    // public ReforgeScreenHandler(int syncId, PlayerInventory playerInventory) {
    // this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    // }

    public ReforgeScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(Tiered.REFORGE_SCREEN_HANDLER_TYPE, syncId);

        this.context = context;
        this.player = playerInventory.player;
        this.addSlot(new Slot(this.inventory, 0, 45, 47));
        this.addSlot(new Slot(this.inventory, 1, 80, 34));
        this.addSlot(new Slot(this.inventory, 2, 115, 47) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(TieredItemTags.REFORGE_ADDITION);
            }
        });

        // // @Override
        // // public boolean canTakeItems(PlayerEntity playerEntity) {
        // // return ReforgeScreenHandler.this.canTakeOutput(playerEntity, this.hasStack());
        // // }

        // @Override
        // public void onTakeItem(PlayerEntity player, ItemStack stack) {
        // ReforgeScreenHandler.this.onTakeOutput(player, stack);
        // }
        // });
        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
        this.context.run((world, pos) -> {
            ReforgeScreenHandler.this.pos = pos;
        });
    }

    // public abstract void updateResult();

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.inventory) {
            this.updateResult();
        }

    }

    private void updateResult() {
        if (this.getSlot(2).hasStack()) {
            // if(this.getSlot(1).g)
            this.reforgeReady = true;
        }

    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.inventory));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.context.get((world, pos) -> {
            if (!this.canUse(world.getBlockState((BlockPos) pos))) {
                return false;
            }
            return player.squaredDistanceTo((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= 64.0;
        }, true);
    }

    // protected boolean isUsableAsAddition(ItemStack stack) {
    // return false;
    // }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == 2) {
                if (!this.insertItem(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(itemStack2, itemStack);
            } else if (index == 0 || index == 1) {
                if (!this.insertItem(itemStack2, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 3 && index < 39) {
                int i = this.isUsableAsAddition(itemStack) ? 1 : 0;
                // int n = i = this.isUsableAsAddition(itemStack) ? 1 : 0;
                if (!this.insertItem(itemStack2, i, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, itemStack2);
        }
        return itemStack;
    }

    private boolean canUse(BlockState state) {
        return state.isOf(Blocks.ANVIL);
    }

    // @Override
    // private boolean canTakeOutput(PlayerEntity player, boolean present) {
    // return this.currentRecipe != null && this.currentRecipe.matches(this.input, this.world);
    // }

    // @Override
    // private void onTakeOutput(PlayerEntity player, ItemStack stack) {
    private void onReforging(PlayerEntity player, ItemStack stack) {
        stack.onCraft(player.world, player, stack.getCount());
        // this.output.unlockLastRecipe(player);
        this.decrementStack(0);
        this.decrementStack(1);
        this.context.run((world, pos) -> world.syncWorldEvent(WorldEvents.ANVIL_USED, (BlockPos) pos, 0));
    }

    private void decrementStack(int slot) {
        ItemStack itemStack = this.inventory.getStack(slot);
        itemStack.decrement(1);
        this.inventory.setStack(slot, itemStack);
    }

    // private void updateResult() {
    // List<SmithingRecipe> list = this.world.getRecipeManager().getAllMatches(RecipeType.SMITHING, this.input, this.world);
    // if (list.isEmpty()) {
    // this.output.setStack(0, ItemStack.EMPTY);
    // } else {
    // this.currentRecipe = list.get(0);
    // ItemStack itemStack = this.currentRecipe.craft(this.input);
    // this.output.setLastRecipe(this.currentRecipe);
    // this.output.setStack(0, itemStack);
    // }
    // }

    private boolean isUsableAsAddition(ItemStack stack) {
        System.out.println(stack);
        return true;
        // return this.recipes.stream().anyMatch(recipe -> recipe.testAddition(stack));
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.inventory && super.canInsertIntoSlot(stack, slot);
    }

}
