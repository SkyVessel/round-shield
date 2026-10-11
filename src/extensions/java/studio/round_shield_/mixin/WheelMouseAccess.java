package studio.round_shield_.mixin;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(MouseHandler.class)
public interface WheelMouseAccess {
    @Accessor("accumulatedDX") void wheelDX(double value);
    @Accessor("accumulatedDY") void wheelDY(double value);
    @Accessor("smoothTurnX") SmoothDouble wheelSmoothX();
    @Accessor("smoothTurnY") SmoothDouble wheelSmoothY();
}
