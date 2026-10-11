package studio.round_shield_;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
public record QuiverArrowProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<QuiverArrowProperty> CODEC=MapCodec.unit(new QuiverArrowProperty());
    @Override public float get(ItemStack stack,ClientLevel level,ItemOwner owner,int seed){return QuiverItem.visibleArrows(stack);}
    @Override public MapCodec<QuiverArrowProperty> type(){return CODEC;}
}
