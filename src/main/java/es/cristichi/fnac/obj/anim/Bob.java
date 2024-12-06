package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Bob extends AvoidCamsAnimatronicDrawing {
    private final double secsToKill;

    public Bob(double secInterval, Map<Integer, Integer> aiDuringNight,
               List<String> forbiddenCams, double secsToKill) throws ResourceException {
        super("Bob", secInterval, aiDuringNight, 20, "anims/bob/camImg.png",
                new Jumpscare("anims/bob/jumpscare.gif", 0,
                        Resources.loadSound("anims/bob/sounds/jumpscare.wav", "bobJump.wav"), 0, false),
                forbiddenCams, Color.RED);
        this.secsToKill = secsToKill;

        this.sounds.put("move", Resources.loadSound("anims/bob/sounds/move.wav", "bobMove.wav"));
    }

    @Override
    public MoveOppReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveOppReturn ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveOppReturn(ret.moveToCam(), sounds.getOrDefault("move", null));
    }

    @Override
    public TickReturn onTick(int tick, int fps, boolean camsUp, boolean doorOpen, Camera cam, Random rng) {
        if (doorOpen){
            if (startKillTick == null){
                startKillTick = tick;
            } else {
                if (Math.round(secsToKill *fps) <= tick-startKillTick){
                    kill = true;
                    return new TickReturn(true, null, 0, 0);
                }
            }
        } else {
            kill = false;
            startKillTick = null;
        }
        return new TickReturn(false, null, 0, 0);
    }
}
