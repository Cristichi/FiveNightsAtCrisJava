package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronicData;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

@CustomNightAnimatronic(name = "Bob", portraitPath = "anims/bob/portrait.png", restStart = "storage",
        restDesc = "Bob starts at the Storage, and moves randomly to any place except corridor 2.",
        tutDesc = "Bob moves randomly, always avoiding cam4 and your right door. He can still move to cam2.")
public class RoamingBob extends AvoidCamsAnimatronicDrawing {
    public RoamingBob(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty()?data.name(): data.name()+" ("+data.variant()+")", Map.of(0, data.ai()),
                true, false, switch (data.mapType()){
                    case TUTORIAL -> List.of("cam4");
                    case RESTAURANT -> List.of("corridor 2", "corridor 4", "bathrooms", "offices");
                }, 0f, data.rng());
    }

    public RoamingBob(String name, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                      boolean globalCameraStalled, List<String> forbiddenCams, float fakeMovementSoundChance, Random rng) throws ResourceException {
        super(name, 5, 6, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/bob/camImg.png",
                new Jumpscare("anims/bob/jumpscare.gif", 0,
                        Resources.loadSound("anims/bob/sounds/jumpscare.wav", "bobJump.wav"), 0, JumpscareVisualSetting.CENTERED),
                forbiddenCams, fakeMovementSoundChance, Color.RED, rng);

        this.sounds.put("move", Resources.loadSound("anims/bob/sounds/move.wav", "bobMove.wav"));
    }
}
