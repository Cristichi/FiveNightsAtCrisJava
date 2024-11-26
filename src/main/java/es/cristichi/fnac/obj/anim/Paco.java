package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AssetNotFound;
import es.cristichi.fnac.io.FNACResources;
import es.cristichi.fnac.obj.Camera;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Paco extends Animatronic{
    //TODO: So Paco right now is just basic af. Perhaps he can punish for not using cams much,
    // I need to allow this by either adding the tick info on move opportunity attempts or just
    // allow animatronics to do something each and every tick. Or maybe nights can keep track
    // of the total time spent in cams. I think it might be best to think of all the options
    // and implement whatever is more adapted to how nights work now, which may also give it
    // some more distinction from other FNAC games.
    private boolean kill = false;
    private Integer startKillTick = null;
    private final double targetPatienceKillSec = 5.1;

    public Paco(double secInterval, HashMap<Integer, Integer> aiDuringNight, List<String> forbiddenCams) throws AssetNotFound {
        super("Paco", secInterval, aiDuringNight, 20, FNACResources.loadImageResource("anims/paco/camImg.png"), "anims/paco/jumpscare.gif", 1, forbiddenCams);
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
