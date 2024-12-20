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

@CustomNightAnimatronic(name = "Paco", portraitPath = "anims/paco/portrait.png", restStart = "kitchen",
        restDesc = "Paco starts his cycle at the kitchen, then moves to the Dining Area, and then chooses whether he " +
        "goes to your left side or right side. After waiting at your closed door, he teleports back " +
        "to the Kitchen. He never goes into the Staff Lounge, the Bathrooms, the Storage or the Main " +
        "Stage.",
        tutDesc = "Paco moves from cam1 to cam2 and vice-versa until he decides to move to cam4 or 3. Then he heads to " +
                "your closest Office door and restarts to the opposite side if he leaves the door."
)
public class Paco extends PathedMoveAnimatronicDrawing {
    public Paco(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty() ? data.name() : data.name() + " (" + data.variant() + ")", Map.of(0, data.ai()),
                false, true,
                switch (data.mapType()) {
                    case TUTORIAL -> List.of(
                            List.of("cam1", "cam2", "cam4", "rightDoor"),
                            List.of("cam2", "cam1", "cam3", "leftDoor")
                    );
                    case RESTAURANT -> List.of(
                            List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                            List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                    );
                },
                0f, data.rng());
    }

    public Paco(String name, Map<Integer, Integer> aiDuringNight, boolean cameraStalled, boolean globalCameraStalled,
                List<List<String>> camPaths, float fakeMovementSoundChance, Random rng) throws ResourceException {
        super(name, 6, 4, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/paco/camImg.png",
                new Jumpscare("anims/paco/jumpscare.gif", 0,
                        Resources.loadSound("anims/paco/sounds/jumpscare.wav", "pacoJump.wav"), 4,
                        JumpscareVisualSetting.CENTERED),
                camPaths, fakeMovementSoundChance, Color.BLUE, rng);

        this.sounds.put("move", Resources.loadSound("anims/paco/sounds/move.wav", "pacoMove.wav"));
    }
}
