package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronicData;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Random;

@CustomNightAnimatronic(name = "Maria", portraitPath = "anims/maria/portrait.png",
        restStart = "offices", tutStart = "cam2",
        restDesc = "Maria starts at the Storage, and moves randomly to any place except corridor 1.",
        tutDesc = "Maria moves randomly, always avoiding cam1 and your left door.")
public class RoamingMaria extends AvoidCamsAnimatronicDrawing {
    public RoamingMaria(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty() ? data.name() : data.name() + " (" + data.variant() + ")", Map.of(0, data.ai()),
                true, false, switch (data.mapType()) {
                    case TUTORIAL -> List.of("cam1");
                    case RESTAURANT -> List.of("corridor 1", "corridor 3", "staff lounge");
                }, 0f, data.rng());
    }

    public RoamingMaria(String name, Map<Integer, Integer> aiDuringNight,
                        boolean cameraStalled, boolean globalCameraStalled, List<String> forbiddenCams,
                        float fakeMovementSoundChance, Random rng) throws ResourceException {
        super(name, 5, 6, aiDuringNight, 20, cameraStalled,
                globalCameraStalled, "anims/maria/camImg.png",
                new Jumpscare("anims/maria/jumpscare.gif", 0,
                        Resources.loadSound("anims/maria/sounds/jumpscare.wav", "mariaJump.wav"),
                        0, JumpscareVisualSetting.CENTERED),
                forbiddenCams, fakeMovementSoundChance, Color.YELLOW, rng);

        this.sounds.put("move", Resources.loadSound("anims/maria/sounds/move.wav", "mariaMove.wav"));
        this.camPos.put("main stage", new Point2D.Float(0.8f, 0.1f));
    }
}
