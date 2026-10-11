package studio.round_shield_.mixin;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Slot.class)
public interface SlotPositionMixin {
    @Mutable @Accessor("x") void gearX(int value);
    @Mutable @Accessor("y") void gearY(int value);
}
