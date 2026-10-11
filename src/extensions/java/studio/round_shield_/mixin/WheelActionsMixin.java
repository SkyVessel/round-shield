package studio.round_shield_.mixin;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import studio.round_shield_.*;
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class WheelActionsMixin {
    @Shadow public ServerPlayer player;
    @Inject(method="handlePlayerAction",at=@At("HEAD"),cancellable=true)
    private void swap(ServerboundPlayerActionPacket packet,CallbackInfo ci){if(packet.getAction()==ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND&&Equipment.focused(player)){
        PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.level());
        if(!player.isSpectator()){
            var g=Equipment.gear(player);int main=g.hands[0].mount,off=g.hands[1].mount;
            var mainStack=player.getMainHandItem();var offStack=player.getOffhandItem();
            if(main>=0&&off>=0){player.stopUsingItem();g.hands[0].mount=off;g.hands[1].mount=main;g.wheelSelected=true;g.setChanged();}
            else if((main>=0&&(offStack.isEmpty()||Equipment.accepts(main,offStack)))||(off>=0&&!g.wheelSelected&&(mainStack.isEmpty()||Equipment.accepts(off,mainStack)))){
                player.stopUsingItem();player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,offStack);player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,mainStack);
            }
        }ci.cancel();
    }}
}
