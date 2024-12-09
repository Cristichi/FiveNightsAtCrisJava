package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisual;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PathCris extends PathedMoveAnimatronicDrawing {
    private static Jumpscare jumpscareNormal, jumpscareItsMe;

    private final double secsToKill;

    public PathCris(double secInterval, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                    List<List<String>> camPaths, String returnToCam, double secsToKill, Random rng) throws ResourceException {
        super("Cris", secInterval, aiDuringNight, 20, cameraStalled, "anims/cris/camImg.png",
                null, camPaths, Color.PINK);

        if (jumpscareNormal == null || jumpscareItsMe == null){
            jumpscareNormal = new Jumpscare("anims/cris/jumpscareNormal.gif", 0,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump1.wav"), 1, JumpscareVisual.STRETCHED);
            jumpscareItsMe = new Jumpscare("anims/cris/jumpscareItsMe.gif", 7,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump2.wav"), 12, JumpscareVisual.CENTERED);
        }
        jumpscare = rng.nextFloat()<.9? jumpscareNormal : jumpscareItsMe;
        this.secsToKill = secsToKill;

        this.sounds.put("move", Resources.loadSound("anims/cris/sounds/move.wav", "crisMove.wav"));
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
