package studio.round_shield_;
import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

/** Triangle surfaces attach to ordinary animated ModelPart bones. */
public final class FormMeshSupport {
 static Field field(String name)throws Exception{Field f=ModelPart.class.getDeclaredField(name);f.setAccessible(true);return f;}
 public static void install(ModelPart root,String id,boolean complete){
  try(var stream=Minecraft.getInstance().getResourceManager().open(Identifier.fromNamespaceAndPath(FormGeneratedMod.MOD_ID,"form/meshes/"+id+".json"))){
   var definitions=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonArray();
   // Preserve vanilla ModelPart identities: game animation code holds references to them.
   if(complete)for(var p:root.getAllParts())field("cubes").set(p,new ArrayList<ModelPart.Cube>());
   for(var value:definitions){var data=value.getAsJsonObject();ModelPart part=root;
    for(var key:data.getAsJsonArray("path")){String name=key.getAsString();if(!part.hasChild(name)){Map<String,ModelPart> children=new HashMap<>((Map<String,ModelPart>)field("children").get(part));children.put(name,new ModelPart(new ArrayList<>(),new HashMap<>()));field("children").set(part,children);}part=part.getChild(name);}
    if(complete){var p=data.getAsJsonArray("position");var r=data.getAsJsonArray("rotation");var s=data.getAsJsonArray("scale");PartPose pose=new PartPose(p.get(0).getAsFloat(),p.get(1).getAsFloat(),p.get(2).getAsFloat(),r.get(0).getAsFloat(),r.get(1).getAsFloat(),r.get(2).getAsFloat(),s.get(0).getAsFloat(),s.get(1).getAsFloat(),s.get(2).getAsFloat());part.setInitialPose(pose);part.loadPose(pose);part.visible=data.get("visible").getAsBoolean();}
    List<ModelPart.Cube> cubes=new ArrayList<>();for(var triangle:data.getAsJsonArray("triangles")){var t=triangle.getAsJsonArray();ModelPart.Vertex[] vertices=new ModelPart.Vertex[4];for(int i=0;i<3;i++)vertices[i]=new ModelPart.Vertex(t.get(i*5).getAsFloat(),t.get(i*5+1).getAsFloat(),t.get(i*5+2).getAsFloat(),t.get(i*5+3).getAsFloat(),t.get(i*5+4).getAsFloat());vertices[3]=vertices[2];ModelPart.Cube cube=new ModelPart.Cube(0,0,0,0,0,0,0,0,0,0,0,false,1,1,Set.of(Direction.UP));cube.polygons[0]=new ModelPart.Polygon(vertices,new Vector3f(t.get(15).getAsFloat(),t.get(16).getAsFloat(),t.get(17).getAsFloat()));cubes.add(cube);}field("cubes").set(part,cubes);
   }
  }catch(Exception e){throw new IllegalStateException("FORM could not load mesh "+id,e);}
 }
}
