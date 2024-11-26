package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.obj.Camera;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Bob extends AvoidCamsAnimatronic {
    private final double secsToKill;

    public Bob(double secInterval, HashMap<Integer, Integer> aiDuringNight,
               List<String> forbiddenCams, double secsToKill) throws ResourceNotFound {
        super("Bob", secInterval, aiDuringNight, 20, "anims/bob/camImg.png",
                "anims/bob/jumpscare.gif", 1, forbiddenCams);
        this.secsToKill = secsToKill;
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

    /**
     * {@link Bob} will wait for {@value secsToKill} seconds before killing
     * @param tick     Current tick.
     * @param fps      FPS for the current night. They are a constant throught the night.
     * @param camsUp   If cams are up on this tick (this changes as soon as the Player clicks,
     *                on the first frame of the transition)
     * @param doorOpen If there is a door to the Office from the current Camera and it is open.
     * @param cam      Current Camera where the Animatronic is.
     * @param rng      Random in charge of today's night.
     * @return
     */
    @Override
    public TickReturn onTick(int tick, int fps, boolean camsUp, boolean doorOpen, Camera cam, Random rng) {
        if (doorOpen){
            if (startKillTick == null){
                startKillTick = tick;
            } else {
                if (Math.round(secsToKill *fps) <= tick-startKillTick){
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
