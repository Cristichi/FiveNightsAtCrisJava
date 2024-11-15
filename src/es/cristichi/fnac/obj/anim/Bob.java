package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AssetNotFound;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import es.cristichi.fnac.util.AssetsIO;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Bob extends Animatronic{
    private boolean kill = false;
    private Integer startKillTick = null;
    private final double targetPatienceKillSec = 4.9;

    public Bob(double secInterval, HashMap<Integer, Integer> aiDuringNight) throws AssetNotFound {
        super("Bob", secInterval, aiDuringNight, 20, AssetsIO.loadImageResource("imgs/anims/bob/camImg.png"), "imgs/anims/bob/jumpscare.gif");
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
        connections.removeIf(camera -> camera.getName().contains("4"));
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
                }
                return !camsUp && kill;
            }
        } else {
            kill = false;
            startKillTick = null;
        }
        return false;
    }
}
