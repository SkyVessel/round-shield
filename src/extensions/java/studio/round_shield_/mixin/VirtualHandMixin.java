package studio.round_shield_.mixin;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import studio.round_shield_.*;
@Mixin(LivingEntity.class)
public abstract class VirtualHandMixin {
    @Inject(method="getItemBySlot",at=@At("HEAD"),cancellable=true)
    private void wheelRead(EquipmentSlot slot,CallbackInfoReturnable<ItemStack> ci){if((Object)this instanceof Player p&&(slot==EquipmentSlot.MAINHAND||slot==EquipmentSlot.OFFHAND)&&Equipment.ownsHand(p,slot==EquipmentSlot.MAINHAND?0:1))ci.setReturnValue(Equipment.gear(p).held(slot==EquipmentSlot.MAINHAND?0:1));}
    @Inject(method="setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;Z)V",at=@At("HEAD"),cancellable=true)
    private void wheelWrite(EquipmentSlot slot,ItemStack stack,boolean transaction,CallbackInfo ci){if((Object)this instanceof Player p&&(slot==EquipmentSlot.MAINHAND||slot==EquipmentSlot.OFFHAND)&&Equipment.ownsHand(p,slot==EquipmentSlot.MAINHAND?0:1)){
        var old=Equipment.gear(p).replaceHeld(slot==EquipmentSlot.MAINHAND?0:1,stack);if(!transaction)p.onEquipItem(slot,old,stack);ci.cancel();
    }}
}
