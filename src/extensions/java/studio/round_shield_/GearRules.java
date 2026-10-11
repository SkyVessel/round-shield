package studio.round_shield_;

public final class GearRules {
    public static final int BACK_LEFT=0, BACK_RIGHT=1, HIP_LEFT=2, HIP_RIGHT=3, REAR=4;
    public static final int CAPACITY=320;
    private GearRules() {}
    public static int sector(double x, double y) {
        if (x*x+y*y<100) return -1;
        return Math.floorMod((int)Math.floor((Math.atan2(y,x)+Math.PI/2+Math.PI/5)/(Math.PI*2/5)),5);
    }
    public static boolean accepts(int slot, boolean quiver, boolean lantern, boolean weaponOrTool) {
        if(slot<0||slot>=5)return false;
        if(quiver)return slot==BACK_RIGHT;
        if(lantern)return true;
        return weaponOrTool;
    }
    public static float sway(float phase,float speed,float maxDegrees) {
        return (float)Math.sin(phase)*Math.min(Math.max(speed,0),1)*maxDegrees;
    }
}
