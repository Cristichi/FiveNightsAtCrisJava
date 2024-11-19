package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AssetNotFound;
import es.cristichi.fnac.io.FNACResources;
import es.cristichi.fnac.obj.Camera;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Maria extends Animatronic{
    private boolean kill = false;
    private Integer startKillTick = null;
    private final double targetPatienceKillSec = 5.1;

    public Maria(double secInterval, HashMap<Integer, Integer> aiDuringNight, List<String> forbiddenCams) throws AssetNotFound {
        super("María", secInterval, aiDuringNight, 20, FNACResources.loadImageResource("imgs/anims/maria/camImg.png"), "imgs/anims/maria/jumpscare.gif", 1, forbiddenCams);
    }

    @Override
    public boolean onMovementOpportunityAttempt(Random rng) {
        if (kill){
            return false;
        }
        return super.onMovementOpportunityAttempt(rng);
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
