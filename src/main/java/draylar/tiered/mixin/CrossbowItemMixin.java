package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import draylar.tiered.api.CustomEntityAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(method = "createArrow", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setCritical(Z)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void createArrowMixin(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrow, CallbackInfoReturnable<PersistentProjectileEntity> info, ArrowItem arrowItem,
            PersistentProjectileEntity persistentProjectileEntity) {
        EntityAttributeInstance instance = entity.getAttributeInstance(CustomEntityAttributes.CRIT_CHANCE);
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
