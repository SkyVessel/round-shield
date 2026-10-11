package studio.round_shield_.mixin;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.round_shield_.Equipment;
// Animation resource packs inspect this client snapshot. Persistent inventory serialization stays native.
@Mixin(NbtPredicate.class)
public abstract class WheelAnimationDataMixin {
    @Inject(method="getEntityTagToCompare",at=@At("RETURN"),cancellable=true)
    private static void heldSnapshot(Entity entity,CallbackInfoReturnable<CompoundTag> ci){if(entity instanceof Player p&&p.level().isClientSide()&&Equipment.focused(p)){
        var tag=ci.getReturnValue().copy();var equipment=tag.getCompoundOrEmpty("equipment").copy();
        equipment.put("offhand",ItemStack.OPTIONAL_CODEC.encodeStart(p.registryAccess().createSerializationContext(NbtOps.INSTANCE),p.getOffhandItem()).getOrThrow());
        tag.put("equipment",equipment);ci.setReturnValue(tag);
    }}
}
