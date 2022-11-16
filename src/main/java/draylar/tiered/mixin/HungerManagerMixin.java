package draylar.tiered.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.HungerManager;

@SuppressWarnings("unused")
@Mixin(HungerManager.class)
public class HungerManagerMixin {

    // Test
    // @Inject(method = "addExhaustion", at = @At("HEAD"))
    // public void addExhaustionMixin(float exhaustion, CallbackInfo info) {
    //     System.out.println("Add: " + exhaustion);
    // }
}
