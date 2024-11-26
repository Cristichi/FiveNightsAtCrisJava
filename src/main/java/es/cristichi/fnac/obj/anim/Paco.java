package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.obj.Camera;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Paco extends PathedMoveAnimatronic{
    private final double secsToKill;

    public Paco(double secInterval, HashMap<Integer, Integer> aiDuringNight, List<String> orderedCamPath,
                String retreatCam, double secsToKill) throws ResourceNotFound {
        super("Paco", secInterval, aiDuringNight, 20, "anims/paco/camImg.png",
                "anims/paco/jumpscare.gif", 1, orderedCamPath, retreatCam, Color.BLUE);

        this.secsToKill = secsToKill;
    }

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
