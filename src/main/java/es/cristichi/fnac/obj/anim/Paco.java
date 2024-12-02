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

public class Paco extends PathedMoveAnimatronic{
    private final Sound moveSound;
    private final double secsToKill;

    public Paco(double secInterval, HashMap<Integer, Integer> aiDuringNight, List<String> orderedCamPath,
                String retreatCam, double secsToKill) throws ResourceException {
        super("Paco", secInterval, aiDuringNight, 20, "anims/paco/camImg.png",
                new Jumpscare("anims/paco/jumpscare.gif", 1, Resources.loadSound("anims/paco/sounds/jumpscare.wav", "pacoJump.wav"), 0),
                orderedCamPath, retreatCam, Color.BLUE);

        this.secsToKill = secsToKill;
        this.moveSound = Resources.loadSound("anims/paco/sounds/move.wav", "pacoMove.wav");
    }

    @Override
    public MoveOppReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveOppReturn ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveOppReturn(ret.moveToCam(), moveSound);
        //return new MoveOppReturn(ret.moveToCam(), rng.nextBoolean()? moveSound :null);
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
