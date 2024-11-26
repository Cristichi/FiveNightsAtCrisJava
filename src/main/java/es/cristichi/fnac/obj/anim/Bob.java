package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.obj.Camera;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Bob extends Animatronic{
    private boolean kill = false;
    private Integer startKillTick = null;
    private final double targetPatienceKillSec = 4.9;

    public Bob(double secInterval, HashMap<Integer, Integer> aiDuringNight,
               List<String> forbiddenCams) throws ResourceNotFound {
        super("Bob", secInterval, aiDuringNight, 20, "anims/bob/camImg.png",
                "anims/bob/jumpscare.gif", 1, forbiddenCams);
    }

    @Override
    public boolean onMovementOpportunityAttempt(Camera cam, Random rng) {
        if (kill){
            return false;
        }
        return super.onMovementOpportunityAttempt(cam, rng);
    }

    @Override
    public boolean onJumpscareAttempt(int tick, int fps, boolean camsUp, boolean doorOpen, Camera cam, Random rng) {
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
    public boolean showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng) {
        return !kill;
    }
}
