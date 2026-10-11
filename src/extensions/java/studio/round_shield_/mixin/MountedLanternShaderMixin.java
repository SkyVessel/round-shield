package studio.round_shield_.mixin;

import net.irisshaders.iris.api.v0.item.IrisItemLightProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import studio.round_shield_.Equipment;

/** Include the stowed light in Iris's existing item ID, brightness and color calculation. */
@Pseudo
@Mixin(targets="net.irisshaders.iris.uniforms.IdMapUniforms$HeldItemSupplier",remap=false)
public abstract class MountedLanternShaderMixin {
    @Redirect(method={"update","applyOldHandLighting"},at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"),remap=false)
    private ItemStack roundshield$includeMountedLantern(LocalPlayer player,InteractionHand hand){
        var held=player.getItemInHand(hand);
        if(hand!=InteractionHand.OFF_HAND||Equipment.GEAR==null)return held;
        var gear=Equipment.gear(player);
        var lantern=gear.mountedLantern();
        if(lantern.isEmpty())return held;
        var mountedLight=(IrisItemLightProvider)lantern.getItem();
        var heldLight=(IrisItemLightProvider)held.getItem();
        return mountedLight.getLightEmission(player,lantern)>heldLight.getLightEmission(player,held)?lantern:held;
    }
}
