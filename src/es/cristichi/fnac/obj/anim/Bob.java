package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AssetNotFound;
import es.cristichi.fnac.io.FNACResources;
import es.cristichi.fnac.obj.Camera;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Bob extends Animatronic{
    private boolean kill = false;
    private Integer startKillTick = null;
    private final double targetPatienceKillSec = 4.9;

    public Bob(double secInterval, HashMap<Integer, Integer> aiDuringNight, List<String> forbiddenCams) throws AssetNotFound {
        super("Bob", secInterval, aiDuringNight, 20, FNACResources.loadImageResource("anims/bob/camImg.png"), "anims/bob/jumpscare.gif", 1, forbiddenCams);
    }

    @Override
    public boolean onMovementOpportunityAttempt(Random rng) {
        if (kill){
            return false;
        }
        return super.onMovementOpportunityAttempt(rng);
    }

    @Override
    public boolean onJumpscareAttempt(int tick, boolean camsUp, Camera cam, boolean doorOpen, Random rng, int fps) {
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
