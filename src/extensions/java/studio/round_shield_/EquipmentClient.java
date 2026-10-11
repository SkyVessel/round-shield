package studio.round_shield_;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid=FormGeneratedMod.MOD_ID,value=Dist.CLIENT)
public final class EquipmentClient {
    public static final KeyMapping WHEEL=new KeyMapping("key.round_shield_.wheel",KeyConflictContext.IN_GAME,InputConstants.Type.MOUSE,3,KeyMapping.Category.INVENTORY);
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(WHEEL);}
    @SubscribeEvent public static void quiverModels(RegisterRangeSelectItemModelPropertyEvent e){e.register(Equipment.id("quiver_arrows"),QuiverArrowProperty.CODEC);}
    @SubscribeEvent public static void screens(RegisterMenuScreensEvent e){e.register(Equipment.QUIVER_MENU,QuiverScreen::new);}
    @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){for(var skin:e.getSkins()){var r=e.getPlayerRenderer(skin);if(r!=null)r.addLayer(new EquipmentLayer(r));}}
    public static void selectHotbar(){ClientPacketDistributor.sendToServer(new Equipment.Select(-1));}
    public static ItemStack visibleItem(GearInventory gear,int slot){return gear.display(slot);}
    public static void frame(GuiGraphicsExtractor g,int x,int y){g.fill(x-1,y-1,x+17,y+17,0xff24211d);g.fill(x,y,x+16,y+16,0xff8b8173);g.fill(x+1,y+1,x+16,y+16,0xffc9bfae);g.fill(x+1,y+1,x+15,y+15,0xff575149);}
    public static void wheel(GuiGraphicsExtractor g,int cx,int cy,int radius,int selected,GearInventory gear,boolean icons){
        // One-pixel horizontal spans create a hard-edged, equal five-sector ring.
        for(int y=-radius;y<=radius;y++){
            int start=-radius,last=0;
            for(int x=-radius;x<=radius+1;x++){
                int color=0,d=x*x+y*y;
                if(x<=radius&&d<=radius*radius&&d>=100){
                    int sector=GearRules.sector(x,y);double a=Math.atan2(y,x)+Math.PI/2+Math.PI/5;double f=a/(Math.PI*2/5)-Math.floor(a/(Math.PI*2/5));
                    boolean edge=d>(radius-2)*(radius-2)||d<144||f<.018||f>.982;
                    color=edge?0xff2b241b:sector==selected?0xffb99b5d:0xee665b48;
                }
                if(color!=last){if(last!=0)g.fill(cx+start,cy+y,cx+x,cy+y+1,last);start=x;last=color;}
            }
        }
        if(icons)for(int i=0;i<5;i++){double a=-Math.PI/2+i*Math.PI*2/5;int x=cx+(int)Math.round(Math.cos(a)*(radius-16))-8,y=cy+(int)Math.round(Math.sin(a)*(radius-16))-8;ItemStack s=visibleItem(gear,i);if(!s.isEmpty()){g.item(s,x,y);if(s.is(Equipment.QUIVER))g.text(Minecraft.getInstance().font,Integer.toString(QuiverItem.count(s)),x,y+17,0xffeeeecc);}}
    }
    public static void inventory(GuiGraphicsExtractor g,int left,int top,int mx,int my){inventory(g,left,top,mx,my,true);}
    public static void inventory(GuiGraphicsExtractor g,int left,int top,int mx,int my,boolean lead){
        var mc=Minecraft.getInstance();if(mc.player==null)return;var gear=Equipment.gear(mc.player);
        if(lead)frame(g,left+77,top+35);
        if(lead&&gear.getItem(0).isEmpty()){g.pose().pushMatrix();g.pose().translate(left+77,top+35);g.item(new ItemStack(Items.LEAD),0,0);g.pose().popMatrix();}
        if(gear.unlocked()&&Equipment.clientWheelVisible){
            int cx=left-58,cy=top+43;wheel(g,cx,cy,48,GearRules.sector(mx-cx,my-cy),gear,false);
            for(int i=0;i<5;i++){int[] xy=Equipment.slotPosition(i);frame(g,left+xy[0],top+xy[1]);}
        }
    }
    @EventBusSubscriber(modid=FormGeneratedMod.MOD_ID,value=Dist.CLIENT)
    public static final class Events {
        @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){var mc=Minecraft.getInstance();Equipment.clientWheelVisible=!(mc.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen screen)||!((studio.round_shield_.mixin.RecipeBookAccessor)screen).gearBook().isVisible();if(mc.player!=null&&mc.screen==null&&Equipment.gear(mc.player).unlocked()&&WHEEL.consumeClick()&&WHEEL.isDown())mc.setScreen(new WheelScreen());}
    }
    public static final class WheelScreen extends Screen {
        private int selected=-1;
        private final InputConstants.Key key=WHEEL.getKey();
        private final java.util.Map<KeyMapping,net.neoforged.neoforge.client.settings.IKeyConflictContext> movementContexts=new java.util.LinkedHashMap<>();
        @Override protected void init(){
            if(movementContexts.isEmpty()){
                var o=minecraft.options;
                for(var mapping:new KeyMapping[]{o.keyUp,o.keyDown,o.keyLeft,o.keyRight,o.keyJump,o.keyShift,o.keySprint}){
                    var original=mapping.getKeyConflictContext();movementContexts.put(mapping,original);
                    mapping.setKeyConflictContext(new net.neoforged.neoforge.client.settings.IKeyConflictContext(){
                        public boolean isActive(){return minecraft.screen==WheelScreen.this||original.isActive();}
                        public boolean conflicts(net.neoforged.neoforge.client.settings.IKeyConflictContext other){return other==this||original.conflicts(other);}
                    });
                }
                // Opening a Screen releases the keys; restore held movement and native toggles.
                KeyMapping.restoreToggleStatesOnScreenClosed();
                for(var mapping:movementContexts.keySet()){
                    if(mapping==o.keyShift&&o.toggleCrouch().get()||mapping==o.keySprint&&o.toggleSprint().get())continue;
                    var bound=mapping.getKey();boolean down=false;
                    if(!mapping.isUnbound()&&minecraft.isWindowActive()){
                        if(bound.getType()==InputConstants.Type.KEYSYM)down=InputConstants.isKeyDown(minecraft.getWindow(),bound.getValue());
                        else if(bound.getType()==InputConstants.Type.MOUSE)down=GLFW.glfwGetMouseButton(minecraft.getWindow().handle(),bound.getValue())==GLFW.GLFW_PRESS;
                    }
                    mapping.setDown(down);
                }
            }
            clearMouseMotion();
        }
        private void clearMouseMotion(){var mouse=(studio.round_shield_.mixin.WheelMouseAccess)minecraft.mouseHandler;mouse.wheelDX(0);mouse.wheelDY(0);mouse.wheelSmoothX().reset();mouse.wheelSmoothY().reset();minecraft.mouseHandler.setIgnoreFirstMove();}
        private boolean movement(KeyEvent event,boolean down){boolean handled=false;for(var mapping:movementContexts.keySet())if(mapping.matches(event)){mapping.setDown(down);handled=true;}return handled;}
        @Override public boolean keyPressed(KeyEvent event){return movement(event,true)||super.keyPressed(event);}
        @Override public void removed(){
            // Consume wheel deltas before Minecraft grabs the cursor for camera look.
            clearMouseMotion();
            movementContexts.forEach(KeyMapping::setKeyConflictContext);movementContexts.clear();
        }
        public WheelScreen(){super(Component.translatable("screen.round_shield_.wheel"));}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float a){}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float a){
            if(minecraft.player==null)return;
            selected=GearRules.sector(x-width/2.0,y-height/2.0);
            wheel(g,width/2,height/2,64,selected,Equipment.gear(minecraft.player),true);
            if(selected>=0)g.text(font,Component.translatable("mount.round_shield_."+selected),width/2-40,height/2+73,0xffefdfbb);
        }
        private void finish(){selected=GearRules.sector(minecraft.mouseHandler.getScaledXPos(minecraft.getWindow())-width/2.0,minecraft.mouseHandler.getScaledYPos(minecraft.getWindow())-height/2.0);if(selected>=0&&minecraft.player!=null&&Equipment.gear(minecraft.player).unlocked())ClientPacketDistributor.sendToServer(new Equipment.Select(selected));minecraft.setScreen(null);}
        @Override public boolean mouseReleased(MouseButtonEvent e){if(key.getType()==InputConstants.Type.MOUSE&&key.getValue()==e.button()){finish();return true;}for(var mapping:movementContexts.keySet())if(mapping.matchesMouse(e))mapping.setDown(false);return super.mouseReleased(e);}
        @Override public boolean keyReleased(KeyEvent e){if(key.getType()==InputConstants.Type.KEYSYM&&key.getValue()==e.key()){finish();return true;}return movement(e,false)||super.keyReleased(e);}
        @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){for(var mapping:movementContexts.keySet())if(mapping.matchesMouse(e))mapping.setDown(true);return true;}
    }
    public static final class QuiverScreen extends AbstractContainerScreen<QuiverMenu> {
        public QuiverScreen(QuiverMenu m,Inventory inv,Component title){super(m,inv,title,176,140);inventoryLabelY=46;}
        @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float a){super.extractBackground(g,mx,my,a);g.fill(leftPos-2,topPos-2,leftPos+178,topPos+142,0xff322b23);g.fill(leftPos,topPos,leftPos+176,topPos+140,0xffc8b69a);for(int i=0;i<5;i++)frame(g,leftPos+44+i*18,topPos+20);for(int r=0;r<3;r++)for(int c=0;c<9;c++)frame(g,leftPos+8+c*18,topPos+58+r*18);for(int c=0;c<9;c++)frame(g,leftPos+8+c*18,topPos+116);}
        @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float a){super.extractRenderState(g,mx,my,a);extractTooltip(g,mx,my);}
    }
}
