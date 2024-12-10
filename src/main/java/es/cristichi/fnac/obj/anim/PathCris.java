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

    public PathCris(double secInterval, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                    boolean globalCameraStalled, List<List<String>> camPaths, String returnToCam,
                    double secsToKill, float fakeMovementSoundChance, Random rng) throws ResourceException {
        super("Cris", secInterval, secsToKill, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/cris/camImg.png",
                null, camPaths, fakeMovementSoundChance, Color.PINK);

        if (jumpscareNormal == null || jumpscareItsMe == null){
            jumpscareNormal = new Jumpscare("anims/cris/jumpscareNormal.gif", 0,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump1.wav"), 1, JumpscareVisual.STRETCHED);
            jumpscareItsMe = new Jumpscare("anims/cris/jumpscareItsMe.gif", 7,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump2.wav"), 12, JumpscareVisual.CENTERED);
        }
        jumpscare = rng.nextFloat()<.9? jumpscareNormal : jumpscareItsMe;

        this.sounds.put("move", Resources.loadSound("anims/cris/sounds/move.wav", "crisMove.wav"));
    }

    @Override
    public MoveSuccessRet onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveSuccessRet ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveSuccessRet(ret.moveToCam(), sounds.getOrDefault("move", null));
    }
}
