package studio.round_shield_;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.*;

@EventBusSubscriber(modid=FormGeneratedMod.MOD_ID)
public final class Equipment {
    public static QuiverItem QUIVER;
    public static boolean clientWheelVisible=true;
    public static MenuType<QuiverMenu> QUIVER_MENU;
    public static AttachmentType<GearInventory> GEAR;
    public static final String TOKEN="round_shield_drawn";
    public static Identifier id(String path){return Identifier.fromNamespaceAndPath(FormGeneratedMod.MOD_ID,path);}
    @SubscribeEvent public static void register(RegisterEvent e){
        e.register(Registries.ITEM,id("quiver"),()->QUIVER=new QuiverItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id("quiver"))).stacksTo(1).component(DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.EMPTY)));
        e.register(Registries.MENU,id("quiver"),()->QUIVER_MENU=new MenuType<>(QuiverMenu::new,FeatureFlags.VANILLA_SET));
        e.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,id("equipment"),()->GEAR=AttachmentType.serializable(holder->new GearInventory((Player)holder)).copyOnDeath().sync(GearInventory.STREAM).build());
    }
    @SubscribeEvent public static void tab(BuildCreativeModeTabContentsEvent e){if(e.getTabKey()==CreativeModeTabs.COMBAT&&QUIVER!=null)e.accept(QUIVER);}
    public static GearInventory gear(Player p){return p.getData(GEAR);}
    public static boolean accepts(int mount,ItemStack s){
        return GearRules.accepts(mount,s.is(QUIVER),s.is(Items.LANTERN),s.is(ItemTags.SWORDS)||s.is(ItemTags.AXES)||s.is(ItemTags.PICKAXES)||s.is(ItemTags.SHOVELS)||s.is(ItemTags.HOES)||s.getItem() instanceof ShieldItem);
    }
    public static Slot slot(Player player,int gearIndex,int x,int y){
        // Resolve the attachment on every access; the client replaces it on a sync packet.
        var proxy=new net.minecraft.world.SimpleContainer(6){
            @Override public ItemStack getItem(int i){return gear(player).getItem(i);}
            @Override public void setItem(int i,ItemStack s){gear(player).setItem(i,s);}
            @Override public ItemStack removeItem(int i,int n){return gear(player).removeItem(i,n);}
            @Override public void setChanged(){gear(player).setChanged();}
        };
        return new Slot(proxy,gearIndex,x,y){
            @Override public int getMaxStackSize(){return 1;}
            @Override public boolean isActive(){return gearIndex==0||gear(player).unlocked()&&(!player.level().isClientSide()||clientWheelVisible);}
            @Override public boolean mayPlace(ItemStack s){return gearIndex==0?s.is(Items.LEAD):gear(player).unlocked()&&!gear(player).active(gearIndex-1)&&accepts(gearIndex-1,s);}
            @Override public boolean mayPickup(Player p){return gearIndex==0?(getItem().isEmpty()||gear(p).canRemoveLead()):!gear(p).active(gearIndex-1);}
        };
    }
    public static int[] slotPosition(int mount){double a=-Math.PI/2+mount*Math.PI*2/5;return new int[]{-58+(int)Math.round(Math.cos(a)*32)-8,43+(int)Math.round(Math.sin(a)*32)-8};}
    private static String token(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(TOKEN,"");}
    private static void mark(ItemStack s,String t){CustomData.update(DataComponents.CUSTOM_DATA,s,n->{if(t.isEmpty())n.remove(TOKEN);else n.putString(TOKEN,t);});}
    public static boolean focused(Player p){return GEAR!=null&&p.hasData(GEAR)&&(gear(p).wheelSelected||gear(p).hands[1].mount>=0)&&!gear(p).legacy;}
    public static boolean ownsHand(Player p,int channel){return focused(p)&&(channel==0?gear(p).wheelSelected:gear(p).hands[1].mount>=0);}
    public static void stow(Player p,GearInventory g,int channel){if(p.getUsedItemHand()==(channel==0?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND))p.stopUsingItem();g.hands[channel].clear();g.setChanged();}
    public static void selectHotbar(Player p){if(!ownsHand(p,0))return;if(p.getUsedItemHand()==InteractionHand.MAIN_HAND)p.stopUsingItem();var g=gear(p);g.wheelSelected=false;g.hands[0].clear();g.setChanged();}
    public static void migrate(Player p){var g=gear(p);if(!g.legacy)return;
        for(var hand:g.hands){if(hand.mount>=0&&!hand.token.isEmpty())for(int i=0;i<p.getInventory().getContainerSize();i++){
            var item=p.getInventory().getItem(i);if(!token(item).equals(hand.token))continue;
            mark(item,"");if(g.getItem(hand.mount+1).isEmpty()){p.getInventory().setItem(i,ItemStack.EMPTY);g.setItem(hand.mount+1,item);}break;
        }hand.clear();}g.wheelSelected=false;g.legacy=false;g.setChanged();
    }
    public static void toggle(ServerPlayer p,int mount){
        if(p.isSpectator()||!p.isAlive()||p.containerMenu!=p.inventoryMenu)return;
        migrate(p);if(mount==-1){selectHotbar(p);return;}if(mount<0||mount>=5)return;
        var g=gear(p);if(!g.unlocked())return;
        for(int c=0;c<2;c++)if(g.active(mount)&&g.hands[c].mount==mount){stow(p,g,c);return;}
        ItemStack item=g.getItem(mount+1);if(item.isEmpty()||!accepts(mount,item))return;
        int channel=item.getItem() instanceof ShieldItem||item.is(Items.LANTERN)?1:0;
        if(p.getUsedItemHand()==(channel==0?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND))p.stopUsingItem();if(channel==0)g.wheelSelected=true;g.hands[channel].mount=mount;g.setChanged();
    }
    public record Select(int mount) implements CustomPacketPayload {
        public static final Type<Select> TYPE=new Type<>(id("select"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Select> CODEC=new StreamCodec<>(){public Select decode(RegistryFriendlyByteBuf b){return new Select(b.readVarInt());}public void encode(RegistryFriendlyByteBuf b,Select p){b.writeVarInt(p.mount);}};
        public Type<Select> type(){return TYPE;}
    }
    @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){e.registrar("2").playToServer(Select.TYPE,Select.CODEC,(msg,ctx)->{if(ctx.player() instanceof ServerPlayer p)toggle(p,msg.mount);});}
    @EventBusSubscriber(modid=FormGeneratedMod.MOD_ID)
    public static final class Events {
        @SubscribeEvent public static void tick(PlayerTickEvent.Post e){
            Player p=e.getEntity();if(p.level().isClientSide())return;var g=gear(p);
            migrate(p);boolean changed=false;
            for(int c=0;c<2;c++){var hand=g.hands[c];var now=g.held(c);if(hand.mount>=0&&now.isEmpty()){hand.clear();changed=true;}else if(!ItemStack.matches(now,hand.display)){hand.display=now.copy();changed=true;}}
            if(changed)g.setChanged();

        }
        @SubscribeEvent public static void drops(LivingDropsEvent e){
            if(!(e.getEntity() instanceof ServerPlayer p)||p.level().getGameRules().get(GameRules.KEEP_INVENTORY))return;
            for(var drop:e.getDrops())mark(drop.getItem(),"");
            var g=gear(p);for(int i=0;i<6;i++){ItemStack s=g.removeItemNoUpdate(i);if(!s.isEmpty())e.getDrops().add(new ItemEntity(p.level(),p.getX(),p.getY(),p.getZ(),s));}
            for(var hand:g.hands)hand.clear();g.wheelSelected=false;g.setChanged();
        }
    }
}
