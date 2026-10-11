package studio.round_shield_.mixin;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import studio.round_shield_.Equipment;
@Mixin(Gui.class)
public abstract class WheelHudMixin {
    @WrapWithCondition(method="extractItemHotbar",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private boolean selection(GuiGraphicsExtractor g,RenderPipeline pipeline,Identifier sprite,int x,int y,int w,int h){var p=Minecraft.getInstance().player;return !sprite.getPath().equals("hud/hotbar_selection")||p==null||!Equipment.ownsHand(p,0);}
}
