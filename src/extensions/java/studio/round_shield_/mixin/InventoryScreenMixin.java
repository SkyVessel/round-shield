package studio.round_shield_.mixin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import studio.round_shield_.*;
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu>{
    protected InventoryScreenMixin(InventoryMenu menu,Inventory inv,Component title){super(menu,inv,title);}
    @Inject(method="extractBackground",at=@At("TAIL"))
    private void gearBackground(GuiGraphicsExtractor g,int mx,int my,float a,CallbackInfo ci){EquipmentClient.inventory(g,leftPos,topPos,mx,my);}
    @Inject(method={"init","onRecipeBookButtonClick"},at=@At("TAIL"))
    private void fitWheel(CallbackInfo ci){
        if(minecraft.player==null||!Equipment.gear(minecraft.player).unlocked()||((RecipeBookAccessor)this).gearBook().isVisible())return;
        int delta=Math.max(0,108-leftPos);leftPos+=delta;
        for(var child:children())if(child instanceof net.minecraft.client.gui.components.ImageButton button)button.setX(button.getX()+delta);
    }
    @Override protected boolean hasClickedOutside(double x,double y,int left,int top){
        if(minecraft.player!=null&&Equipment.gear(minecraft.player).unlocked()&&x>=left-107&&x<=left-9&&y>=top-6&&y<=top+92)return false;
        return super.hasClickedOutside(x,y,left,top);
    }
}
