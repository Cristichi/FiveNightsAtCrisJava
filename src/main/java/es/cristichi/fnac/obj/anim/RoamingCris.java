package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RoamingCris extends AvoidCamsAnimatronicDrawing {
    private static Jumpscare jumpscareNormal, jumpscareItsMe;

    public RoamingCris(String name, double secInterval, double secsToKill, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                       boolean globalCameraStalled, List<String> forbiddenCams,
                       float fakeMovementSoundChance, Random rng) throws ResourceException {
        super(name, secInterval, secsToKill, aiDuringNight, 20, cameraStalled, globalCameraStalled,
                "anims/cris/camImg.png", null, forbiddenCams, fakeMovementSoundChance, Color.PINK);

        if (jumpscareNormal == null || jumpscareItsMe == null){
            jumpscareNormal = new Jumpscare("anims/cris/jumpscareNormal.gif", 0,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump1.wav"), 1, JumpscareVisualSetting.STRETCHED);
            jumpscareItsMe = new Jumpscare("anims/cris/jumpscareItsMe.gif", 7,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump2.wav"), 12, JumpscareVisualSetting.CENTERED);
        }
        jumpscare = rng.nextFloat() < 0.9 ? jumpscareNormal : jumpscareItsMe;

        this.sounds.put("move", Resources.loadSound("anims/cris/sounds/move.wav", "crisMove.wav"));
    }

    @Override
    public MoveSuccessRet onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveSuccessRet ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveSuccessRet(ret.moveToCam(), sounds.getOrDefault("move", null));
    }
}
