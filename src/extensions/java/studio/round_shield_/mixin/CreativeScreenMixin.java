package studio.round_shield_.mixin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import studio.round_shield_.*;
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow private static CreativeModeTab selectedTab;
    protected CreativeScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu m,Inventory i,Component t){super(m,i,t);}
    @Inject(method="selectTab",at=@At("TAIL"))
    private void position(CreativeModeTab tab,CallbackInfo ci){if(tab.getType()==CreativeModeTab.Type.INVENTORY)for(int i=46;i<52;i++){var slot=(SlotPositionMixin)menu.slots.get(i);int[] xy=i==46?new int[]{130,20}:Equipment.slotPosition(i-47);slot.gearX(xy[0]);slot.gearY(xy[1]);}}
    @Inject(method="extractBackground",at=@At("TAIL"))
    private void background(GuiGraphicsExtractor g,int x,int y,float a,CallbackInfo ci){if(selectedTab.getType()==CreativeModeTab.Type.INVENTORY){EquipmentClient.inventory(g,leftPos,topPos,x,y,false);EquipmentClient.frame(g,leftPos+130,topPos+20);}}
    @Inject(method="hasClickedOutside",at=@At("HEAD"),cancellable=true)
    private void outside(double x,double y,int left,int top,CallbackInfoReturnable<Boolean> ci){if(selectedTab.getType()==CreativeModeTab.Type.INVENTORY&&Equipment.gear(minecraft.player).unlocked()&&x>=left-107&&x<=left-9&&y>=top-6&&y<=top+92)ci.setReturnValue(false);}
}
