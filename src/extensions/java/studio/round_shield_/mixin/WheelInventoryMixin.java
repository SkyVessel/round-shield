package studio.round_shield_.mixin;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import studio.round_shield_.*;
@Mixin(Inventory.class)
public abstract class WheelInventoryMixin {
    @Shadow @Final public Player player;
    @Inject(method="getSelectedItem",at=@At("HEAD"),cancellable=true)
    private void held(CallbackInfoReturnable<ItemStack> ci){if(Equipment.ownsHand(player,0))ci.setReturnValue(Equipment.gear(player).held(0));}
    @Inject(method="setSelectedItem",at=@At("HEAD"),cancellable=true)
    private void replace(ItemStack stack,CallbackInfoReturnable<ItemStack> ci){if(Equipment.ownsHand(player,0))ci.setReturnValue(Equipment.gear(player).replaceHeld(0,stack));}
    @Inject(method="removeFromSelected",at=@At("HEAD"),cancellable=true)
    private void drop(boolean all,CallbackInfoReturnable<ItemStack> ci){if(Equipment.ownsHand(player,0)){var g=Equipment.gear(player);var item=g.held(0);var removed=item.split(all?item.getCount():1);if(item.isEmpty())g.hands[0].clear();g.setChanged();ci.setReturnValue(removed);}}
    @Inject(method="setSelectedSlot",at=@At("HEAD"))
    private void normalSelection(int slot,CallbackInfo ci){if(!(player instanceof net.minecraft.server.level.ServerPlayer sp&&sp.connection==null)&&Inventory.isHotbarSlot(slot)&&Equipment.ownsHand(player,0)){Equipment.selectHotbar(player);if(player.level().isClientSide())EquipmentClient.selectHotbar();}}
}
