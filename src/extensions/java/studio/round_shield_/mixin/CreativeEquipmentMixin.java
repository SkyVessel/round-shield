package studio.round_shield_.mixin;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.PacketUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CreativeEquipmentMixin {
    @Shadow public ServerPlayer player;
    @Inject(method="handleSetCreativeModeSlot",at=@At("HEAD"),cancellable=true)
    private void equipment(ServerboundSetCreativeModeSlotPacket packet,CallbackInfo ci){
        int i=packet.slotNum();if(i<46||i>=52)return;
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.level());
        ci.cancel();if(!player.hasInfiniteMaterials())return;
        var slot=player.inventoryMenu.getSlot(i);var stack=packet.itemStack();
        if(slot.mayPickup(player)&&(stack.isEmpty()||stack.getCount()==1&&slot.mayPlace(stack)&&stack.isItemEnabled(player.level().enabledFeatures()))){slot.setByPlayer(stack);player.inventoryMenu.setRemoteSlot(i,stack);}
        player.inventoryMenu.broadcastChanges();
    }
}
