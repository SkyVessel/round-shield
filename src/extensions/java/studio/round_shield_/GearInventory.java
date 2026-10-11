package studio.round_shield_;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class GearInventory extends SimpleContainer implements ValueIOSerializable {
    private final Player owner;
    private boolean loading;
    public boolean wheelSelected,legacy;
    public static final class Hand {
        public int mount=-1;
        // Only read for migration from the former hotbar-transfer implementation.
        public String token="";
        public ItemStack display=ItemStack.EMPTY;
        public void clear(){mount=-1;token="";display=ItemStack.EMPTY;}
    }
    public final Hand[] hands={new Hand(),new Hand()};
    public GearInventory(Player owner){super(6);this.owner=owner;}
    public boolean unlocked(){return getItem(0).is(Items.LEAD);}
    public boolean active(int mount){return hands[0].mount==mount||hands[1].mount==mount;}
    public ItemStack display(int mount){return getItem(mount+1);}
    /** Storage slot is independent of the lantern's fixed rear-waist render anchor. */
    public ItemStack mountedLantern(){
        if(!unlocked())return ItemStack.EMPTY;
        for(int i=0;i<5;i++){var stack=getItem(i+1);if(!active(i)&&stack.is(Items.LANTERN))return stack;}
        return ItemStack.EMPTY;
    }
    public ItemStack held(int channel){int m=hands[channel].mount;return m>=0&&m<5?getItem(m+1):ItemStack.EMPTY;}
    public ItemStack replaceHeld(int channel,ItemStack item){int m=hands[channel].mount;var old=held(channel);if(m>=0){setItem(m+1,item);if(item.isEmpty())hands[channel].clear();setChanged();}return old;}
    public boolean canRemoveLead(){return hands[0].mount<0&&hands[1].mount<0&&java.util.stream.IntStream.range(1,6).allMatch(i->getItem(i).isEmpty());}
    @Override public void setChanged(){super.setChanged();if(!loading&&owner!=null&&!owner.level().isClientSide()&&Equipment.GEAR!=null&&(!(owner instanceof net.minecraft.server.level.ServerPlayer sp)||sp.connection!=null))owner.syncData(Equipment.GEAR);}
    @Override public void serialize(ValueOutput out){
        out.putInt("storageVersion",2);out.putBoolean("wheelSelected",wheelSelected);
        for(int i=0;i<6;i++)if(!getItem(i).isEmpty())out.store("slot"+i,ItemStack.CODEC,getItem(i));
        for(int i=0;i<2;i++)out.putInt("mount"+i,hands[i].mount);
    }
    @Override public void deserialize(ValueInput in){
        loading=true;legacy=in.getIntOr("storageVersion",1)<2;wheelSelected=in.getBooleanOr("wheelSelected",false);
        for(int i=0;i<6;i++)setItem(i,in.read("slot"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY));
        for(int i=0;i<2;i++){var hand=hands[i];hand.mount=in.getIntOr("mount"+i,-1);if(hand.mount < -1||hand.mount>4)hand.clear();hand.token=legacy?in.getStringOr("token"+i,""):"";}
        loading=false;
    }
    public static final StreamCodec<RegistryFriendlyByteBuf,GearInventory> STREAM=new StreamCodec<>(){
        public GearInventory decode(RegistryFriendlyByteBuf b){var g=new GearInventory(null);for(int i=0;i<6;i++)g.setItem(i,ItemStack.OPTIONAL_STREAM_CODEC.decode(b));g.wheelSelected=b.readBoolean();for(var hand:g.hands)hand.mount=b.readVarInt();return g;}
        public void encode(RegistryFriendlyByteBuf b,GearInventory g){for(int i=0;i<6;i++)ItemStack.OPTIONAL_STREAM_CODEC.encode(b,g.getItem(i));b.writeBoolean(g.wheelSelected);for(var hand:g.hands)b.writeVarInt(hand.mount);}
    };
}
