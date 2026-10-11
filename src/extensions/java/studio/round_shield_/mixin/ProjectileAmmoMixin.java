package studio.round_shield_.mixin;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.round_shield_.QuiverItem;
@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileAmmoMixin {
    @Inject(method="useAmmo",at=@At("RETURN"))
    private static void saveConsumed(ItemStack weapon,ItemStack projectile,LivingEntity holder,boolean forceInfinite,CallbackInfoReturnable<ItemStack> ci){if(!holder.level().isClientSide())QuiverItem.finishAmmo(projectile);}
}
