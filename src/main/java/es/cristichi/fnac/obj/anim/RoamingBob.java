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

public class RoamingBob extends AvoidCamsAnimatronicDrawing {
    public RoamingBob(double secInterval, double secsToKill, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                      boolean globalCameraStalled, List<String> forbiddenCams, float fakeMovementSoundChance) throws ResourceException {
        super("Bob", secInterval, secsToKill, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/bob/camImg.png",
                new Jumpscare("anims/bob/jumpscare.gif", 0,
                        Resources.loadSound("anims/bob/sounds/jumpscare.wav", "bobJump.wav"), 0, JumpscareVisual.CENTERED),
                forbiddenCams, fakeMovementSoundChance, Color.RED);

        this.sounds.put("move", Resources.loadSound("anims/bob/sounds/move.wav", "bobMove.wav"));
    }

    @Override
    public MoveSuccessRet onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveSuccessRet ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveSuccessRet(ret.moveToCam(), sounds.getOrDefault("move", null));
    }
}
