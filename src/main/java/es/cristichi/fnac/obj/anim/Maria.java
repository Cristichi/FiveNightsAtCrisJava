package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.obj.Camera;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Maria extends AvoidCamsMovementAnimatronic{
    private final double targetPatienceKillSec = 5.1;

    public Maria(double secInterval, HashMap<Integer, Integer> aiDuringNight,
                 List<String> forbiddenCams) throws ResourceNotFound {
        super("María", secInterval, aiDuringNight, 20, "anims/maria/camImg.png",
                "anims/maria/jumpscare.gif", 1, forbiddenCams);
    }

    @Override
    public boolean onMovementOpportunityAttempt(Camera cam, boolean isOpenDoor, Random rng) {
        if (kill){
            return false;
        }
        if ((cam.isLeftDoorOfOffice() || cam.isRightDoorOfOffice()) && !isOpenDoor){
            return rng.nextInt(maxIaLevel) < aiLevel + 5;
        }
        return super.onMovementOpportunityAttempt(cam, isOpenDoor, rng);
    }

    @Override
    public TickReturn onTick(int tick, int fps, boolean camsUp, boolean doorOpen, Camera cam, Random rng) {
        if (doorOpen){
            if (startKillTick == null){
                startKillTick = tick;
            } else {
                if (Math.round(targetPatienceKillSec*fps) <= tick-startKillTick){
                    kill = true;
                    return new TickReturn(true);
                }
            }
        } else {
            kill = false;
            startKillTick = null;
        }
        return new TickReturn(false);
    }
}
