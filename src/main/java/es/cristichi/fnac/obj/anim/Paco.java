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

@CustomNightAnimatronic(name = "Paco", portraitPath = "anims/paco/portrait.png", restStart = "kitchen")
public class Paco extends PathedMoveAnimatronicDrawing {
    private final float moveSoundChance;

    public Paco(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty()?data.name(): data.name()+" ("+data.variant()+")", 6, 5, Map.of(0, data.ai()), false, true,
                switch (data.mapType()){
                    case TUTORIAL -> List.of(
                            List.of("cam1", "cam2", "cam4", "rightDoor"),
                            List.of("cam2", "cam1", "cam3", "leftDoor")
                    );
                    case RESTAURANT -> List.of(
                            List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                            List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                    );
                },
                1f, 0f);
    }

    public Paco(String name, double secInterval, double secsToKill, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                boolean globalCameraStalled, List<List<String>> camPaths, float moveSoundChance,
                float fakeMovementSoundChance) throws ResourceException {
        super(name, secInterval, secsToKill, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/paco/camImg.png",
                new Jumpscare("anims/paco/jumpscare.gif", 0,
                        Resources.loadSound("anims/paco/sounds/jumpscare.wav", "pacoJump.wav"), 0, JumpscareVisualSetting.CENTERED),
                camPaths, fakeMovementSoundChance, Color.BLUE);

        this.sounds.put("move", Resources.loadSound("anims/paco/sounds/move.wav", "pacoMove.wav"));
        this.moveSoundChance = moveSoundChance;
    }

    @Override
    public MoveSuccessRet onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveSuccessRet ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveSuccessRet(ret.moveToCam(), rng.nextFloat()<moveSoundChance?sounds.getOrDefault("move", null):null);
    }
}
