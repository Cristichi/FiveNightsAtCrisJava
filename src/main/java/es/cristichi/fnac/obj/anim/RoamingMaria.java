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

public class RoamingMaria extends AvoidCamsAnimatronicDrawing {
    public RoamingMaria(String name, double secInterval, double secsToKill, Map<Integer, Integer> aiDuringNight,
                        boolean cameraStalled, boolean globalCameraStalled, List<String> forbiddenCams,
                        float fakeMovementSoundChance) throws ResourceException {
        super(name, secInterval, secsToKill, aiDuringNight, 20, cameraStalled,
                globalCameraStalled, "anims/maria/camImg.png",
                new Jumpscare("anims/maria/jumpscare.gif", 0,
                        Resources.loadSound("anims/maria/sounds/jumpscare.wav", "mariaJump.wav"),
                        0, JumpscareVisualSetting.CENTERED),
                        forbiddenCams, fakeMovementSoundChance, Color.YELLOW);

        this.sounds.put("move", Resources.loadSound("anims/maria/sounds/move.wav", "mariaMove.wav"));
    }

    @Override
    public MoveSuccessRet onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveSuccessRet ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveSuccessRet(ret.moveToCam(), sounds.getOrDefault("move", null));
    }
}
