package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.obj.Camera;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Maria extends AvoidCamsAnimatronic {
    private final double secsToKill;

    public Maria(double secInterval, HashMap<Integer, Integer> aiDuringNight,
                 List<String> forbiddenCams, double secsToKill) throws ResourceException {
        super("María", secInterval, aiDuringNight, 20, "anims/maria/camImg.png",
                "anims/maria/jumpscare.gif", 1, forbiddenCams, Color.YELLOW);
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
