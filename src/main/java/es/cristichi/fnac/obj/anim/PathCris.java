package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.obj.cnight.CustomNightAnimatronicData;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Random;

@CustomNightAnimatronic(name = "Cris", variant = "final form", portraitPath = "anims/cris/portrait.png", restStart =
        "storage",
        restDesc = "Cris (final form) starts at the Storage, then goes to the Dining Area. From there, he teleports to " +
                "either the Staff Lounge or the Offices. When at the Staff Lounge, he teleports to corridor 3" +
                " or 4 and then he goes to the closest Office door. If at the Offices, he first teleports to " +
                "the Bathrooms, and then from there he goes to corridor 3 or 4 and then goes to the closest " +
                "Office door.",
        tutDesc = "Cris (final form) moves from cam1 to cam2 and vice-versa until he decides to move to cam4 or 3. " +
                "Nonetheless, he teleports to the opposite side when moving to cam4 or 3. Then he heads to " +
                "your closest Office door and restarts to the same side if he leaves the door.")
public class PathCris extends PathedMoveAnimatronicDrawing {
    private static Jumpscare jumpscareNormal, jumpscareItsMe;

    public PathCris(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty() ? data.name() : data.name() + " (" + data.variant() + ")", Map.of(0, data.ai()),
                true, false, switch (data.mapType()) {
                    case TUTORIAL -> List.of(
                            List.of("cam2", "cam1", "cam4", "rightDoor"),
                            List.of("cam1", "cam2", "cam3", "leftDoor")
                    );
                    case RESTAURANT -> List.of(
                            List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                            List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor"),
                            List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                            List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
                    );
                }, 0f, data.rng());
        this.camPos.put("main stage", new Point2D.Float(0.6f, 0f));
    }

    public PathCris(String name, Map<Integer, Integer> aiDuringNight,
                    boolean cameraStalled, boolean globalCameraStalled, List<List<String>> camPaths,
                    float fakeMovementSoundChance, Random rng) throws ResourceException {
        super(name, 5, 4, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/cris/camImg.png",
                null, camPaths, fakeMovementSoundChance, Color.PINK, rng);

        if (jumpscareNormal == null || jumpscareItsMe == null) {
            jumpscareNormal = new Jumpscare("anims/cris/jumpscareNormal.gif", 0,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump1.wav"), 1,
                    JumpscareVisualSetting.STRETCHED);
            jumpscareItsMe = new Jumpscare("anims/cris/jumpscareItsMe.gif", 7,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav", "crisJump2.wav"), 12,
                    JumpscareVisualSetting.CENTERED);
        }
        jumpscare = rng.nextFloat() < .9 ? jumpscareNormal : jumpscareItsMe;

        this.sounds.put("move", Resources.loadSound("anims/cris/sounds/move.wav", "crisMove.wav"));
    }
}
