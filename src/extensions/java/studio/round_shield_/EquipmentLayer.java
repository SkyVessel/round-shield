package studio.round_shield_;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class EquipmentLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
    private static final Identifier TEXTURE=Equipment.id("textures/form/c37c05e8-3b10-489b-ac0f-5ad61faccc5f.png");
    private record Chunk(int arrow,List<float[]> triangles){}
    private record Mount(List<Chunk> chunks,float[] pivot,Matrix4f sword,Matrix4f tool){}
    private static Matrix4f matrix(JsonObject o,String key){float[] m=new float[16];var a=o.has(key)?o.getAsJsonArray(key):o.getAsJsonArray("item");for(int i=0;i<16;i++)m[i]=a.get(i).getAsFloat();return new Matrix4f().set(m);}
    private final List<Mount> mounts=new ArrayList<>();
    public EquipmentLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent){super(parent);
        try(var in=Minecraft.getInstance().getResourceManager().open(Equipment.id("form/equipment/editor-mounts.json"));var reader=new InputStreamReader(in,StandardCharsets.UTF_8)){
            for(var m:JsonParser.parseReader(reader).getAsJsonArray()){
                var o=m.getAsJsonObject();var chunks=new ArrayList<Chunk>();
                for(var ch:o.getAsJsonArray("chunks")){var c=ch.getAsJsonObject();var triangles=new ArrayList<float[]>();
                    for(var tr:c.getAsJsonArray("triangles")){var t=tr.getAsJsonArray();float[] v=new float[18];for(int n=0;n<18;n++)v[n]=t.get(n).getAsFloat();triangles.add(v);}
                    chunks.add(new Chunk(c.get("arrow").getAsInt(),triangles));
                }
                float[] pivot=new float[3],matrix=new float[16];for(int i=0;i<3;i++)pivot[i]=o.getAsJsonArray("pivot").get(i).getAsFloat();for(int i=0;i<16;i++)matrix[i]=o.getAsJsonArray("item").get(i).getAsFloat();mounts.add(new Mount(chunks,pivot,matrix(o,"sword"),matrix(o,"tool")));
            }
        }catch(Exception e){throw new IllegalStateException("Cannot load authored equipment mounts",e);}
    }
    @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,AvatarRenderState state,float yaw,float pitch){
        var mc=Minecraft.getInstance();if(mc.level==null||state.isInvisible||state.isSpectator||!(mc.level.getEntity(state.id) instanceof Player p))return;
        var gear=Equipment.gear(p);if(!gear.unlocked())return;
        for(int i=0;i<5;i++){
            ItemStack stack=gear.getItem(i+1);if(gear.active(i)||stack.isEmpty()||stack.getItem() instanceof ShieldItem)continue;
            boolean lantern=stack.is(Items.LANTERN);if(lantern&&stack!=gear.mountedLantern())continue;
            int location=lantern?GearRules.REAR:i;
            Mount mount=mounts.get(location);boolean sword=stack.is(net.minecraft.tags.ItemTags.SWORDS);Matrix4f itemMatrix=sword?mount.sword:mount.tool;pose.pushPose();getParentModel().body.translateAndRotate(pose);
            if(location>=2){var v=location==GearRules.REAR?new Vector3f(mount.pivot):itemMatrix.transformPosition(new Vector3f(sword?-.25f:.13f,sword?-.25f:.13f,0));pose.translate(v.x,v.y,v.z);float speed=(float)Math.min(1,p.getDeltaMovement().horizontalDistance()*5);float sway=GearRules.sway(state.ageInTicks*.45f,speed,location==GearRules.REAR?3:5);// Hip items swing in the side plane, keeping their distance from the body fixed.
            pose.mulPose((location==GearRules.REAR?Axis.ZP:Axis.XP).rotationDegrees(sway));pose.translate(-v.x,-v.y,-v.z);}
            if(stack.is(Equipment.QUIVER)||stack.is(Items.LANTERN)){
                int arrows=stack.is(Equipment.QUIVER)?QuiverItem.visibleArrows(stack):0;
                // Emit authored vertices directly: Sodium caches Cube geometry at construction,
                // so replacing polygons on a zero-sized Cube loses these custom surfaces.
                collector.submitCustomGeometry(pose,RenderTypes.entityCutout(TEXTURE),(transform,buffer)->{
                    var normal=new Vector3f();var point=new Vector3f();
                    for(var chunk:mount.chunks)if(chunk.arrow==0||chunk.arrow<=arrows)for(var t:chunk.triangles){
                        transform.transformNormal(new Vector3f(t[15],t[16],t[17]),normal);
                        for(int vertex:new int[]{0,5,10,10}){
                            transform.pose().transformPosition(t[vertex]/16f,t[vertex+1]/16f,t[vertex+2]/16f,point);
                            buffer.addVertex(point.x,point.y,point.z,-1,t[vertex+3],t[vertex+4],OverlayTexture.NO_OVERLAY,light,normal.x,normal.y,normal.z);
                        }
                    }
                });
            }else{
                // A body location selects the anchor; the item category selects its grip and orientation.
                pose.mulPose(itemMatrix);var item=new ItemStackRenderState();mc.getItemModelResolver().updateForLiving(item,stack,ItemDisplayContext.NONE,p);item.submit(pose,collector,light,OverlayTexture.NO_OVERLAY,state.outlineColor);
            }
            pose.popPose();
        }
    }
}
