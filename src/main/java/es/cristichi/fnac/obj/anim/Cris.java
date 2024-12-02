package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import kuusisto.tinysound.Sound;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Cris extends AvoidCamsAnimatronic {
    private final Sound moveSound;
    private final double secsToKill;

    public Cris(double secInterval, Map<Integer, Integer> aiDuringNight,
                List<String> forbiddenCams, double secsToKill) throws ResourceException {
        super("Cris", secInterval, aiDuringNight, 20, "anims/cris/camImg.png",
                new Jumpscare("anims/cris/jumpscare.gif", 4,
                        Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump.wav"), 6), forbiddenCams, Color.PINK);
        this.secsToKill = secsToKill;

        this.moveSound = Resources.loadSound("anims/cris/sounds/move.wav", "crisMove.wav");
    }

    @Override
    public MoveOppReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveOppReturn ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveOppReturn(ret.moveToCam(), moveSound);
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
