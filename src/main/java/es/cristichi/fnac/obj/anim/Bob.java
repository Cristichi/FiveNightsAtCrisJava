package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.obj.Camera;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Bob extends AvoidCamsAnimatronic {
    private final double secsToKill;

    public Bob(double secInterval, HashMap<Integer, Integer> aiDuringNight,
               List<String> forbiddenCams, double secsToKill) throws ResourceNotFound {
        super("Bob", secInterval, aiDuringNight, 20, "anims/bob/camImg.png",
                "anims/bob/jumpscare.gif", 1, forbiddenCams, Color.RED);
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
