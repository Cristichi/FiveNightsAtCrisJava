package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import kuusisto.tinysound.Sound;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Maria extends AvoidCamsAnimatronic {
    private final Sound moveSound;
    private final double secsToKill;

    public Maria(double secInterval, HashMap<Integer, Integer> aiDuringNight,
                 List<String> forbiddenCams, double secsToKill) throws ResourceException {
        super("María", secInterval, aiDuringNight, 20, "anims/maria/camImg.png",
                new Jumpscare("anims/maria/jumpscare.gif", 1, Resources.loadSound("anims/maria/sounds/jumpscare.wav", "mariaJump.wav"), 0), forbiddenCams, Color.YELLOW);
        this.secsToKill = secsToKill;

        this.moveSound = Resources.loadSound("anims/maria/sounds/move.wav", "mariaMove.wav");
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
            if (cam.isLeftDoorOfOffice()){
                return new TickReturn(false, sounds.getOrDefault("knock", null), 1, -1);
            } else if (cam.isRightDoorOfOffice()){
                return new TickReturn(false, sounds.getOrDefault("knock", null), 1, 1);
            }
        }
        return new TickReturn(false, null, 0, 0);
    }
}
