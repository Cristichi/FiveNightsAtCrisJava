package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronicData;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

@CustomNightAnimatronic(name = "Cris", variant = "final form", portraitPath = "anims/cris/portrait.png", restStart = "storage")
public class PathCris extends PathedMoveAnimatronicDrawing {
    private static Jumpscare jumpscareNormal, jumpscareItsMe;

    public PathCris(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty()?data.name(): data.name()+" ("+data.variant()+")", Map.of(0, data.ai()),
                true, false, switch (data.mapType()){
                    case TUTORIAL -> List.of(
                            List.of("cam1", "cam2", "cam4", "rightDoor"),
                            List.of("cam2", "cam1", "cam3", "leftDoor")
                    );
                    case RESTAURANT -> List.of(
                            List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                            List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor"),
                            List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                            List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
                    );
                }, 0f, data.rng());
    }

    public PathCris(String name, Map<Integer, Integer> aiDuringNight,
                    boolean cameraStalled, boolean globalCameraStalled, List<List<String>> camPaths,
                    float fakeMovementSoundChance, Random rng) throws ResourceException {
        super(name, 5, 4, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/cris/camImg.png",
                null, camPaths, fakeMovementSoundChance, Color.PINK, rng);

        if (jumpscareNormal == null || jumpscareItsMe == null){
            jumpscareNormal = new Jumpscare("anims/cris/jumpscareNormal.gif", 0,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump1.wav"), 1, JumpscareVisualSetting.STRETCHED);
            jumpscareItsMe = new Jumpscare("anims/cris/jumpscareItsMe.gif", 7,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump2.wav"), 12, JumpscareVisualSetting.CENTERED);
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
