package studio.round_shield_;

import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class QuiverItem extends Item {
    public QuiverItem(Properties p){super(p);}
    public static NonNullList<ItemStack> contents(ItemStack bag){var out=NonNullList.withSize(5,ItemStack.EMPTY);bag.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).copyInto(out);return out;}
    public static int count(ItemStack bag){return contents(bag).stream().filter(s->s.is(Items.ARROW)).mapToInt(ItemStack::getCount).sum();}
    public static void save(ItemStack bag,List<ItemStack> slots){bag.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(slots));}
    public static int visibleArrows(ItemStack bag){return Math.min(5,(count(bag)+63)/64);}
    private static int insert(ItemStack bag,ItemStack input){
        if(!input.is(Items.ARROW))return 0;
        var slots=contents(bag);int before=input.getCount(),space=Math.max(0,GearRules.CAPACITY-count(bag));
        for(int i=0;i<5&&space>0&&!input.isEmpty();i++){
            var stored=slots.get(i);if(!stored.isEmpty()&&!ItemStack.isSameItemSameComponents(stored,input))continue;
            int n=Math.min(space,Math.min(input.getCount(),64-stored.getCount()));if(n<=0)continue;
            if(stored.isEmpty())slots.set(i,input.split(n));else {stored.grow(n);input.shrink(n);}space-=n;
        }
        int moved=before-input.getCount();if(moved>0)save(bag,slots);return moved;
    }
    private static void changed(Player p){Equipment.gear(p).setChanged();p.getInventory().setChanged();p.containerMenu.broadcastChanges();}
    @Override public boolean overrideOtherStackedOnMe(ItemStack bag,ItemStack input,net.minecraft.world.inventory.Slot slot,net.minecraft.world.inventory.ClickAction action,Player p,net.minecraft.world.entity.SlotAccess cursor){
        if(!input.is(Items.ARROW)||bag.getCount()!=1)return false;
        // Filling changes only the bag contents, so a drawn wheel bag need not be movable.
        boolean equipped=Equipment.gear(p).unlocked()&&Equipment.gear(p).getItem(GearRules.BACK_RIGHT+1)==bag&&slot.getItem()==bag&&slot.isActive();
        if((slot.allowModification(p)||equipped)&&insert(bag,input)>0){slot.setChanged();changed(p);}return true;
    }
    @Override public boolean overrideStackedOnOther(ItemStack bag,net.minecraft.world.inventory.Slot slot,net.minecraft.world.inventory.ClickAction action,Player p){
        if(!slot.getItem().is(Items.ARROW)||bag.getCount()!=1)return false;
        if(slot.mayPickup(p)){
            var offered=slot.getItem().copy();int moved=insert(bag.copy(),offered);
            if(moved>0){var taken=slot.safeTake(moved,moved,p);if(insert(bag,taken)>0){slot.setChanged();changed(p);}}
        }return true;
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        ItemStack bag=player.getItemInHand(hand);
        if(!level.isClientSide()){
            var slots=contents(bag);ItemStack dropped=ItemStack.EMPTY;
            for(var stored:slots){if(!stored.is(Items.ARROW))continue;if(dropped.isEmpty()){dropped=stored.split(Math.min(64,stored.getCount()));if(dropped.getCount()==64)break;continue;}
                if(!ItemStack.isSameItemSameComponents(stored,dropped))continue;
                int n=Math.min(64-dropped.getCount(),stored.getCount());dropped.grow(n);stored.shrink(n);if(dropped.getCount()==64)break;
            }
            if(!dropped.isEmpty()&&player.drop(dropped,false)!=null){save(bag,slots);changed(player);}
        }
        return InteractionResult.SUCCESS;
    }
    @Override public boolean isBarVisible(ItemStack stack){return count(stack)>0;}
    @Override public int getBarWidth(ItemStack stack){return Math.round(13f*count(stack)/GearRules.CAPACITY);}
    @Override public int getBarColor(ItemStack stack){return 0xA6BD78;}
    private record Ammo(ItemStack bag,int before,Player player){}
    private static final Map<ItemStack,Ammo> AMMO=Collections.synchronizedMap(new WeakHashMap<>());
    public static void finishAmmo(ItemStack arrow){
        Ammo ref=AMMO.remove(arrow);if(ref==null)return;
        int used=ref.before-arrow.getCount();if(used<=0)return;
        var slots=contents(ref.bag);for(var s:slots){if(!s.is(Items.ARROW))continue;int n=Math.min(used,s.getCount());s.shrink(n);used-=n;if(used==0)break;}
        save(ref.bag,slots);Equipment.gear(ref.player).setChanged();ref.player.inventoryMenu.broadcastChanges();
    }
    @EventBusSubscriber(modid=FormGeneratedMod.MOD_ID)
    public static final class Events {
        @SubscribeEvent public static void ammo(LivingGetProjectileEvent e){
            if(!(e.getEntity() instanceof Player p)||!(e.getProjectileWeaponItemStack().getItem() instanceof BowItem||e.getProjectileWeaponItemStack().getItem() instanceof CrossbowItem))return;
            var g=Equipment.gear(p);if(!g.unlocked())return;
            ItemStack bag=g.getItem(GearRules.BACK_RIGHT+1);
            if(!bag.is(Equipment.QUIVER))return;int count=count(bag);if(count==0)return;
            var arrow=new ItemStack(Items.ARROW,Math.min(count,64));e.setProjectileItemStack(arrow);
            if(!p.level().isClientSide())AMMO.put(arrow,new Ammo(bag,arrow.getCount(),p));
        }
        @SubscribeEvent public static void tooltip(ItemTooltipEvent e){if(e.getItemStack().is(Equipment.QUIVER)){e.getToolTip().add(Component.translatable("tooltip.round_shield_.arrows",count(e.getItemStack()),GearRules.CAPACITY));e.getToolTip().add(Component.translatable("tooltip.round_shield_.quiver_insert"));e.getToolTip().add(Component.translatable("tooltip.round_shield_.quiver_eject"));}}
    }
}
