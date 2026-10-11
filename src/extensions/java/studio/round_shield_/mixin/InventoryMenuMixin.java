package studio.round_shield_.mixin;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import studio.round_shield_.Equipment;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {
    protected InventoryMenuMixin(){super(null,0);}
    @Inject(method="<init>",at=@At("RETURN"))
    private void addEquipment(Inventory inv,boolean active,Player player,CallbackInfo ci){
        addSlot(Equipment.slot(player,0,77,35));
        for(int i=0;i<5;i++){int[] xy=Equipment.slotPosition(i);addSlot(Equipment.slot(player,i+1,xy[0],xy[1]));}
    }
    @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
    private void shiftEquipment(Player player,int i,CallbackInfoReturnable<ItemStack> cir){
        if(i<0||i>=slots.size())return;var slot=slots.get(i);if(!slot.hasItem()||!slot.mayPickup(player))return;
        var stack=slot.getItem();var before=stack.copy();
        if(i>=46){if(!moveItemStackTo(stack,9,45,false)){cir.setReturnValue(ItemStack.EMPTY);return;}}
        else if(i>=9&&i<45){if(!moveItemStackTo(stack,46,52,false))return;}
        else return;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);cir.setReturnValue(before);
    }
}
