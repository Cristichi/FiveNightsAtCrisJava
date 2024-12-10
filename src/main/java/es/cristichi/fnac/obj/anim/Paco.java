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

public class Paco extends PathedMoveAnimatronicDrawing {
    private final float moveSoundChance;

    public Paco(double secInterval, double secsToKill, Map<Integer, Integer> aiDuringNight, boolean cameraStalled, boolean globalCameraStalled,
                List<List<String>> camPaths, float moveSoundChance) throws ResourceException {
        super("Paco", secInterval, secsToKill, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/paco/camImg.png",
                new Jumpscare("anims/paco/jumpscare.gif", 0,
                        Resources.loadSound("anims/paco/sounds/jumpscare.wav", "pacoJump.wav"), 0, JumpscareVisual.CENTERED),
                camPaths, Color.BLUE);

        this.sounds.put("move", Resources.loadSound("anims/paco/sounds/move.wav", "pacoMove.wav"));
        this.moveSoundChance = moveSoundChance;
    }

    @Override
    public MovementSuccessReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MovementSuccessReturn ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MovementSuccessReturn(ret.moveToCam(), rng.nextFloat()<moveSoundChance?sounds.getOrDefault("move", null):null);
    }
}
