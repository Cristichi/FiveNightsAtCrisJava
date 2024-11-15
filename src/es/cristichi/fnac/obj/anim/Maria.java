package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AssetNotFound;
import es.cristichi.fnac.io.FNACResources;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Maria extends Animatronic{
    private boolean kill = false;
    private Integer startKillTick = null;
    private final double targetPatienceKillSec = 5.1;

    public Maria(double secInterval, HashMap<Integer, Integer> aiDuringNight) throws AssetNotFound {
        super("María", secInterval, aiDuringNight, 20, FNACResources.loadImageResource("imgs/anims/maria/camImg.png"), "imgs/anims/maria/jumpscare.gif");
    }

    @Override
    public boolean onMovementOpportunityAttempt(Random rng) {
        if (kill){
            return false;
        }
        return super.onMovementOpportunityAttempt(rng);
    }

    @Override
    public Camera onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        LinkedList<Camera> connections = currentLoc.getConnections();
        connections.removeIf(camera -> camera.getName().contains("3")||camera.getName().contains("1"));
        return connections.get(rng.nextInt(connections.size()));
    }

    @Override
    public boolean onJumpscareAttempt(int tick, boolean doorOpen, boolean camsUp, Camera cam, Random rng, int fps) {
        if (doorOpen){
            if (startKillTick == null){
                startKillTick = tick;
            } else {
                if (Math.round(targetPatienceKillSec*fps) <= tick-startKillTick){
                    kill = true;
                    return true;
                }
            }
        } else {
            kill = false;
            startKillTick = null;
        }
        return false;
    }

    @Override
    public boolean hideFromCam(int tick, boolean openDoor, Camera cam, Random rng, int fps) {
        return kill;
    }
}
