package studio.round_shield_;
import dev.lambdaurora.lambdynlights.api.*;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/** LambDynamicLights evaluates this for local and tracked players, respecting its own switches. */
public final class LanternLights implements DynamicLightsInitializer,EntityLuminance {
    private static Type TYPE;
    public Type type(){return TYPE;}
    @Override public void onInitializeDynamicLights(DynamicLightsContext context){
        TYPE=Type.registerSimple(Equipment.id("mounted_lantern"),this);
        context.entityLightSourceManager().onRegisterEvent().register(registry->registry.register(EntityType.PLAYER,this));
    }
    @Override public int getLuminance(ItemLightSourceManager manager,Entity entity){
        if(!(entity instanceof Player p)||Equipment.GEAR==null)return 0;
        var gear=Equipment.gear(p);var lantern=gear.mountedLantern();
        return gear.unlocked()&&lantern.is(Items.LANTERN)?manager.getLuminance(lantern,p.isUnderWater()):0;
    }
}
