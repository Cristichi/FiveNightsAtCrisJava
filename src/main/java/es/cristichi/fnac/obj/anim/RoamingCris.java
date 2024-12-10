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

public class RoamingCris extends AvoidCamsAnimatronicDrawing {
    private static Jumpscare jumpscareNormal, jumpscareItsMe;

    public RoamingCris(double secInterval, double secsToKill, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                       boolean globalCameraStalled, List<String> forbiddenCams,
                       float fakeMovementSoundChance, Random rng) throws ResourceException {
        super("Cris", secInterval, secsToKill, aiDuringNight, 20, cameraStalled, globalCameraStalled,
                "anims/cris/camImg.png", null, forbiddenCams, fakeMovementSoundChance, Color.PINK);

        if (jumpscareNormal == null || jumpscareItsMe == null){
            jumpscareNormal = new Jumpscare("anims/cris/jumpscareNormal.gif", 0,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump1.wav"), 1, JumpscareVisual.STRETCHED);
            jumpscareItsMe = new Jumpscare("anims/cris/jumpscareItsMe.gif", 7,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump2.wav"), 12, JumpscareVisual.CENTERED);
        }
        jumpscare = rng.nextFloat() < 0.9 ? jumpscareNormal : jumpscareItsMe;

        this.sounds.put("move", Resources.loadSound("anims/cris/sounds/move.wav", "crisMove.wav"));
    }

    @Override
    public MovementSuccessReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MovementSuccessReturn ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MovementSuccessReturn(ret.moveToCam(), sounds.getOrDefault("move", null));
    }
}
