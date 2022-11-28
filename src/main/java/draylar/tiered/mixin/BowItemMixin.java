package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import draylar.tiered.api.CustomEntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onStoppedUsingMixin(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo info, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i,
            float f, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
        EntityAttributeInstance instance = ((PlayerEntity) persistentProjectileEntity.getOwner()).getAttributeInstance(CustomEntityAttributes.CRIT_CHANCE);
        float customChance = 0.0f;
        if (instance != null)
            for (EntityAttributeModifier modifier : instance.getModifiers())
                customChance += (float) modifier.getValue();

        if (world.getRandom().nextFloat() < (1.0f - Math.abs(customChance))) {
            int extraCrit = world.getRandom().nextInt((int) persistentProjectileEntity.getDamage() / 2 + 3);
            if (extraCrit > persistentProjectileEntity.getDamage())
                extraCrit = (int) persistentProjectileEntity.getDamage();
            persistentProjectileEntity.setDamage(Math.min(persistentProjectileEntity.getDamage() + (customChance > 0.0f ? extraCrit : -extraCrit), Integer.MAX_VALUE));
        }
    }

}
