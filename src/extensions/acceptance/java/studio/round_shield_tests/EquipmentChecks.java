package studio.round_shield_tests;
import studio.round_shield_.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.inventory.ContainerInput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
@Mod("round_shield_tests")
public final class EquipmentChecks {
    static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS=DeferredRegister.create(Registries.TEST_FUNCTION,"round_shield_tests");
    static final List<String> NAMES=List.of("quiver_insert","quiver_creative","quiver_wheel_insert","quiver_cursor","quiver_eject","quiver_wheel_eject","migration","wheel_drop","wheel_damage","wheel_quiver","lead_click","lead_creative","rules","transactions","full_inventory","persistence","quiver","ammo","infinity","multishot","death_drops");
    public EquipmentChecks(IEventBus bus){for(String name:NAMES)FUNCTIONS.register(name,()->h->run(h,name));FUNCTIONS.register(bus);bus.addListener(EquipmentChecks::register);}
    static Identifier id(String n){return Identifier.fromNamespaceAndPath("round_shield_tests",n);}
    static void register(RegisterGameTestsEvent e){var env=e.registerEnvironment(id("equipment"));for(String n:NAMES)e.registerTest(id(n),new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION,id(n)),new TestData<>(env,id("empty"),200,0,true,Rotation.NONE,false,1,1,false,32)));}
    static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    static void run(GameTestHelper h,String name){
        var p=h.makeMockServerPlayerInLevel();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);var g=Equipment.gear(p);g.setItem(0,new ItemStack(Items.LEAD));
        switch(name){
            case "lead_click", "lead_creative" -> {
                g.setItem(0,ItemStack.EMPTY);g.setItem(1,new ItemStack(Items.DIAMOND_SWORD));
                var menu=p.inventoryMenu;var lead=menu.getSlot(46);
                if(name.equals("lead_creative")){p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);check(lead.mayPickup(p),"creative must permit clicking empty lead slot even with stored gear");}
                menu.setCarried(new ItemStack(Items.LEAD,12));menu.clicked(46,0,ContainerInput.PICKUP,p);
                check(g.unlocked()&&g.getItem(0).getCount()==1&&menu.getCarried().getCount()==11,"lead click inserts one and keeps cursor remainder");
                menu.setCarried(ItemStack.EMPTY);menu.clicked(46,0,ContainerInput.PICKUP,p);check(g.unlocked()&&menu.getCarried().isEmpty(),"equipped lead cannot be removed with gear");
                g.setItem(1,ItemStack.EMPTY);menu.clicked(46,0,ContainerInput.PICKUP,p);check(!g.unlocked()&&menu.getCarried().is(Items.LEAD),"empty harness allows lead removal");
            }
            case "rules" -> {
                check(p.inventoryMenu.slots.size()==52,"six real menu slots");
                for(int i=0;i<5;i++){check(GearRules.sector(Math.cos(-Math.PI/2+i*2*Math.PI/5)*30,Math.sin(-Math.PI/2+i*2*Math.PI/5)*30)==i,"sector");check(Equipment.accepts(i,new ItemStack(Equipment.QUIVER))==(i==1),"fixed quiver mount");check(Equipment.accepts(i,new ItemStack(Items.LANTERN)),"lantern accepts any storage slot");check(Equipment.accepts(i,new ItemStack(Items.DIAMOND_SWORD)),"rear storage is not reserved");check(!Equipment.accepts(i,new ItemStack(Items.DIRT)),"unsupported item");}check(GearRules.sector(0,0)==-1,"center cancel");check(!Equipment.slot(p,2,0,0).mayPlace(new ItemStack(Items.TIPPED_ARROW)),"arrows are not mountable");
            }
            case "transactions" -> {
                p.getInventory().setItem(0,new ItemStack(Items.STONE,32));p.getInventory().setItem(1,new ItemStack(Items.DIRT,7));p.getInventory().setItem(40,new ItemStack(Items.TORCH,8));
                var sword=new ItemStack(Items.DIAMOND_SWORD);g.setItem(1,sword);g.setItem(5,new ItemStack(Items.LANTERN));
                Equipment.toggle(p,0);check(p.getMainHandItem()==sword&&g.getItem(1)==sword,"held item remains owned by wheel");check(p.getInventory().getItem(0).getCount()==32,"hotbar unchanged while drawn");check(p.getOffhandItem().is(Items.TORCH)&&p.getOffhandItem().getCount()==8,"drawing main wheel preserves native offhand");
                Equipment.toggle(p,4);check(p.getOffhandItem()==g.getItem(5)&&p.getMainHandItem()==sword,"two independent virtual hands");check(p.getInventory().getItem(40).getCount()==8,"native offhand untouched");
                Equipment.toggle(p,0);check(p.getMainHandItem().isEmpty()&&g.getItem(1)==sword,"repeat selects empty hand without activating hotbar");
                p.getInventory().setSelectedSlot(0);check(!g.wheelSelected&&p.getMainHandItem().is(Items.STONE)&&p.getOffhandItem().is(Items.LANTERN),"same number key restores main inventory and preserves wheel offhand");
                Equipment.toggle(p,0);p.getInventory().setSelectedSlot(1);check(p.getMainHandItem().is(Items.DIRT)&&g.getItem(1)==sword,"scroll restores normal selected item");check(p.getOffhandItem().is(Items.LANTERN),"scroll leaves wheel offhand equipped");Equipment.toggle(p,4);check(p.getMainHandItem().is(Items.DIRT)&&p.getOffhandItem().is(Items.TORCH),"stowing offhand restores original offhand without changing main");Equipment.toggle(p,4);check(p.getMainHandItem().is(Items.DIRT),"drawing offhand alone preserves native main");
            }
            case "full_inventory" -> {
                for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.TORCH,64));g.setItem(5,new ItemStack(Items.LANTERN));Equipment.toggle(p,4);
                check(p.getOffhandItem().is(Items.LANTERN)&&g.getItem(5).is(Items.LANTERN),"full inventory does not block independent wheel");for(int i=0;i<36;i++)check(p.getInventory().getItem(i).getCount()==64,"full normal inventory preserved");check(p.getInventory().getItem(40).is(Items.TORCH),"hidden offhand preserved");check(p.getMainHandItem().is(Items.STONE),"offhand selection leaves ordinary main equipped");
            }
            case "persistence" -> {
                g.wheelSelected=true;g.hands[0].mount=0;var connection=p.connection;p.connection=null;try{g.setChanged();p.getInventory().setSelectedSlot(0);check(g.wheelSelected,"loading native slot must preserve wheel focus before connection exists");}finally{p.connection=connection;}g.wheelSelected=false;g.hands[0].clear();
                g.setItem(1,new ItemStack(Items.DIAMOND_SWORD));Equipment.toggle(p,0);var out=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());g.serialize(out);var copy=new GearInventory(null);copy.deserialize(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()));
                check(copy.wheelSelected&&copy.hands[0].mount==0&&copy.getItem(1).is(Items.DIAMOND_SWORD)&&!copy.legacy,"independent storage persistence");p.setData(Equipment.GEAR,copy);Equipment.toggle(p,0);check(copy.getItem(1).is(Items.DIAMOND_SWORD)&&p.getMainHandItem().isEmpty(),"restored selection stows without moving item");
            }
            case "migration" -> {
                var sword=new ItemStack(Items.DIAMOND_SWORD);net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,sword,n->n.putString(Equipment.TOKEN,"legacy-test"));p.getInventory().setItem(0,sword);g.legacy=true;g.hands[0].mount=0;g.hands[0].token="legacy-test";Equipment.migrate(p);
                check(p.getInventory().getItem(0).isEmpty()&&g.getItem(1)==sword&&!g.legacy&&!g.wheelSelected,"old real item migrated exactly once");Equipment.migrate(p);check(g.getItem(1)==sword,"migration idempotent");
            }
            case "wheel_drop" -> {
                p.getInventory().setItem(0,new ItemStack(Items.STONE,64));g.setItem(1,new ItemStack(Items.DIAMOND_SWORD));Equipment.toggle(p,0);var dropped=p.getInventory().removeFromSelected(false);
                check(dropped.is(Items.DIAMOND_SWORD)&&g.getItem(1).isEmpty()&&p.getMainHandItem().isEmpty()&&p.getInventory().getItem(0).getCount()==64,"Q drops wheel item only");
            }
            case "wheel_damage" -> {
                p.getInventory().setItem(0,new ItemStack(Items.STONE,64));g.setItem(1,new ItemStack(Items.DIAMOND_SWORD));Equipment.toggle(p,0);p.getMainHandItem().setDamageValue(13);check(g.getItem(1).getDamageValue()==13,"durability mutates owned wheel stack");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);check(g.getItem(1).isEmpty()&&p.getInventory().getItem(0).getCount()==64,"breaking clears wheel only");
            }
            case "wheel_quiver" -> {var bag=new ItemStack(Equipment.QUIVER);g.setItem(2,bag);Equipment.toggle(p,1);var menu=new QuiverMenu(1,p.getInventory(),bag);check(menu.stillValid(p),"virtual held quiver menu remains valid");}
            case "death_drops" -> {
                g.setItem(1,new ItemStack(Items.DIAMOND_SWORD));var bag=new ItemStack(Equipment.QUIVER);QuiverItem.save(bag,List.of(new ItemStack(Items.ARROW,64)));g.setItem(2,bag);
                var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();var rules=h.getLevel().getGameRules();boolean old=rules.get(net.minecraft.world.level.gamerules.GameRules.KEEP_INVENTORY);
                rules.set(net.minecraft.world.level.gamerules.GameRules.KEEP_INVENTORY,false,null);
                Equipment.Events.drops(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(p,p.damageSources().generic(),drops,false));
                check(drops.size()==3&&g.isEmpty(),"death drops each stored item once");check(drops.stream().filter(e->e.getItem().is(Equipment.QUIVER)).allMatch(e->QuiverItem.count(e.getItem())==64),"arrows persist inside dropped bag");
                g.setItem(0,new ItemStack(Items.LEAD));g.setItem(5,new ItemStack(Items.LANTERN));drops.clear();rules.set(net.minecraft.world.level.gamerules.GameRules.KEEP_INVENTORY,true,null);Equipment.Events.drops(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(p,p.damageSources().generic(),drops,false));check(drops.isEmpty()&&g.getItem(5).is(Items.LANTERN),"keep inventory keeps equipment");rules.set(net.minecraft.world.level.gamerules.GameRules.KEEP_INVENTORY,old,null);
            }

            case "quiver_insert", "quiver_creative", "quiver_wheel_insert" -> {
                if(name.equals("quiver_creative"))p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                var bag=new ItemStack(Equipment.QUIVER);var menu=p.inventoryMenu;int slot=name.equals("quiver_wheel_insert")?48:9;
                if(slot==48)g.setItem(2,bag);else p.getInventory().setItem(9,bag);
                menu.setCarried(new ItemStack(Items.ARROW,64));menu.clicked(slot,0,ContainerInput.PICKUP,p);
                check(QuiverItem.count(bag)==64&&menu.getCarried().isEmpty(),"cursor arrows insert into bag without swapping");
                if(slot==48)Equipment.toggle(p,1);
                for(int i=0;i<4;i++){menu.setCarried(new ItemStack(Items.ARROW,64));menu.clicked(slot,0,ContainerInput.PICKUP,p);}
                check(QuiverItem.count(bag)==320,"five stacks fit");menu.setCarried(new ItemStack(Items.ARROW,23));menu.clicked(slot,0,ContainerInput.PICKUP,p);
                check(QuiverItem.count(bag)==320&&menu.getCarried().getCount()==23&&menu.getSlot(slot).getItem()==bag,"full bag retains cursor remainder");
                menu.setCarried(new ItemStack(Items.TIPPED_ARROW,3));menu.clicked(slot,0,ContainerInput.PICKUP,p);check(QuiverItem.count(bag)==320,"tipped arrows not stored");
            }
            case "quiver_cursor" -> {
                var bag=new ItemStack(Equipment.QUIVER);var menu=p.inventoryMenu;p.getInventory().setItem(9,new ItemStack(Items.ARROW,64));menu.setCarried(bag);menu.clicked(9,0,ContainerInput.PICKUP,p);
                check(QuiverItem.count(bag)==64&&p.getInventory().getItem(9).isEmpty()&&menu.getCarried()==bag,"carried bag collects clicked arrows");
            }
            case "quiver_eject", "quiver_wheel_eject" -> {
                p.setPos(net.minecraft.world.phys.Vec3.atCenterOf(h.absolutePos(new net.minecraft.core.BlockPos(1,1,1))));
                var bag=new ItemStack(Equipment.QUIVER);QuiverItem.save(bag,List.of(new ItemStack(Items.ARROW,40),new ItemStack(Items.ARROW,30)));
                if(name.equals("quiver_wheel_eject")){g.setItem(2,bag);Equipment.toggle(p,1);}else p.setItemInHand(InteractionHand.MAIN_HAND,bag);
                var before=new java.util.HashSet<>(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(5)));
                Equipment.QUIVER.use(h.getLevel(),p,InteractionHand.MAIN_HAND);check(QuiverItem.count(bag)==6,"use ejects one full stack across partial slots");
                check(p.containerMenu==p.inventoryMenu&&p.getMainHandItem()==bag,"right click leaves held quiver and inventory intact");
                Equipment.QUIVER.use(h.getLevel(),p,InteractionHand.MAIN_HAND);check(QuiverItem.count(bag)==0,"last partial stack ejected");Equipment.QUIVER.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
                var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(5));drops.removeAll(before);check(drops.size()==2&&drops.stream().mapToInt(x->x.getItem().getCount()).sum()==70&&drops.stream().allMatch(x->x.getItem().is(Items.ARROW)),"exactly 64 + 6 dropped; empty use adds nothing: "+drops.stream().map(x->x.getItem().toString()).toList());for(var drop:drops)drop.discard();
            }
            case "quiver" -> {
                var bag=new ItemStack(Equipment.QUIVER);var arrows=new ArrayList<ItemStack>();for(int i=0;i<5;i++)arrows.add(new ItemStack(Items.ARROW,64));QuiverItem.save(bag,arrows);check(QuiverItem.count(bag)==320,"capacity");p.setItemInHand(InteractionHand.MAIN_HAND,bag);var menu=new QuiverMenu(1,p.getInventory(),bag);check(!menu.getSlot(0).mayPlace(new ItemStack(Items.TIPPED_ARROW))&&!menu.getSlot(0).mayPlace(new ItemStack(Items.SPECTRAL_ARROW)),"ordinary arrows only");menu.clicked(0,0,ContainerInput.SWAP,p);check(p.getMainHandItem()==bag&&QuiverItem.count(bag)==320,"hotbar swap cannot detach open bag");menu.quickMoveStack(p,0);check(QuiverItem.count(bag)==256,"withdraw stack saved");
            }
            default -> {
                var bag=new ItemStack(Equipment.QUIVER);QuiverItem.save(bag,List.of(new ItemStack(Items.ARROW,64),new ItemStack(Items.ARROW,64),ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY));g.setItem(2,bag);
                var bow=new ItemStack(name.equals("multishot")?Items.CROSSBOW:Items.BOW);if(name.equals("infinity"))bow.enchant(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY),1);if(name.equals("multishot"))bow.enchant(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT),1);
                var arrow=p.getProjectile(bow);check(arrow.is(Items.ARROW),"equipped bag supplies native ammo");
                try{var method=ProjectileWeaponItem.class.getDeclaredMethod("draw",ItemStack.class,ItemStack.class,net.minecraft.world.entity.LivingEntity.class);method.setAccessible(true);var projectiles=(List<?>)method.invoke(null,bow,arrow,p);check(projectiles.size()==(name.equals("multishot")?3:1),"native projectile count");}catch(ReflectiveOperationException ex){throw new RuntimeException(ex);}
                check(QuiverItem.count(bag)==(name.equals("infinity")?128:127),"native ammo consumption "+name+" got "+QuiverItem.count(bag));
            }
        }
        p.discard();h.succeed();
    }
}
