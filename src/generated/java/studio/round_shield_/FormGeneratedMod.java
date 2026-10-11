package studio.round_shield_;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(FormGeneratedMod.MOD_ID)
public final class FormGeneratedMod {
 public static final String MOD_ID = "round_shield_";
 public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD_ID);
 public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD_ID);
 public static final DeferredItem<Item> ITEM_67F0D82F_A1B5_4002_94F3_B14430393C31 = ITEMS.registerSimpleItem("67f0d82f_a1b5_4002_94f3_b14430393c31", Item.Properties::new);
 public static final DeferredBlock<Block> BLOCK_B9353109_D6CB_44DB_9165_5205E8C1165B = BLOCKS.registerSimpleBlock("b9353109_d6cb_44db_9165_5205e8c1165b", () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).noOcclusion());
 public static final DeferredItem<BlockItem> BLOCK_ITEM_B9353109_D6CB_44DB_9165_5205E8C1165B = ITEMS.registerSimpleBlockItem("b9353109_d6cb_44db_9165_5205e8c1165b", BLOCK_B9353109_D6CB_44DB_9165_5205E8C1165B);
 public FormGeneratedMod(IEventBus bus) { BLOCKS.register(bus); ITEMS.register(bus); bus.addListener(this::creativeTab); }
 private void creativeTab(BuildCreativeModeTabContentsEvent event) { if(event.getTabKey()!=CreativeModeTabs.BUILDING_BLOCKS)return;
  event.accept(ITEM_67F0D82F_A1B5_4002_94F3_B14430393C31);
  event.accept(BLOCK_ITEM_B9353109_D6CB_44DB_9165_5205E8C1165B);
 }
}
