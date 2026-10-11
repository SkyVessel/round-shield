package studio.round_shield_;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class QuiverMenu extends AbstractContainerMenu {
    private final ItemStack bag;
    private final SimpleContainer arrows;
    public QuiverMenu(int id,Inventory inventory){this(id,inventory,ItemStack.EMPTY);}
    public QuiverMenu(int id,Inventory inventory,ItemStack bag){
        super(Equipment.QUIVER_MENU,id);this.bag=bag;
        arrows=new SimpleContainer(5){@Override public void setChanged(){super.setChanged();if(!bag.isEmpty())QuiverItem.save(bag,java.util.stream.IntStream.range(0,5).mapToObj(this::getItem).toList());}};
        var contents=QuiverItem.contents(bag);for(int i=0;i<5;i++)arrows.setItem(i,contents.get(i));
        for(int i=0;i<5;i++)addSlot(new Slot(arrows,i,44+i*18,20){@Override public boolean mayPlace(ItemStack s){return s.is(Items.ARROW);}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addPlayer(inventory,col+row*9+9,8+col*18,58+row*18);
        for(int col=0;col<9;col++)addPlayer(inventory,col,8+col*18,116);
    }
    private void addPlayer(Inventory inv,int i,int x,int y){addSlot(new Slot(inv,i,x,y){@Override public boolean mayPickup(Player p){return getItem()!=bag;}@Override public boolean mayPlace(ItemStack s){return getItem()!=bag;}});}
    @Override public void clicked(int slot,int button,net.minecraft.world.inventory.ContainerInput input,Player p){
        if(input==net.minecraft.world.inventory.ContainerInput.SWAP&&(button>=0&&button<9||button==40)&&p.getInventory().getItem(button)==bag)return;
        super.clicked(slot,button,input,p);
    }
    @Override public boolean stillValid(Player p){if(bag.isEmpty())return p.level().isClientSide();for(int i=1;i<6;i++)if(Equipment.gear(p).getItem(i)==bag)return true;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i)==bag)return true;return false;}
    @Override public ItemStack quickMoveStack(Player p,int i){var slot=slots.get(i);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var s=slot.getItem();var before=s.copy();if(i<5){if(!moveItemStackTo(s,5,41,true))return ItemStack.EMPTY;}else if(!s.is(Items.ARROW)||!moveItemStackTo(s,0,5,false))return ItemStack.EMPTY;if(s.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();return before;}
}
