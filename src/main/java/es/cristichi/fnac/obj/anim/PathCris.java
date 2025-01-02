package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.cnight.CustomNightAnimatronicData;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Cris is a representation of myself, Cristichi, inside the game. He used to be the owner of the restaurant, but
 * after the Cataclysm he "merged" with the suit he was wearing, that is also now alive. Cris has different
 * implementations with different behaviours during Nights.
 */
@CustomNightAnimatronic(name = "Cris", variant = "final form", portraitPath = "anims/cris/portrait.png", starts =
        {"storage", "cam1"},
        description = "Cris (final form) starts at the Storage, then goes to the Dining Area. From there, he teleports to " +
                "either the Staff Lounge or the Offices. When at the Staff Lounge, he teleports to corridor 3" +
                " or 4 and then he goes to the closest Office door. If at the Offices, he first teleports to " +
                "the Bathrooms, and then from there he goes to corridor 3 or 4 and then goes to the closest " +
                "Office door.")
public class PathCris extends PathedMoveAnimatronicDrawing {
    private static Jumpscare jumpscareNormal, jumpscareItsMe;
    
    /**
     * Creates a copy of Cris for Custom Night.
     * @param data Data that {@link es.cristichi.fnac.cnight.CustomNightMenuJC} has to create the instance.
     * @throws ResourceException If any resources cannot be loaded from disk.
     */
    public PathCris(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty() ? data.name() : data.name() + " (" + data.variant() + ")", Map.of(0, data.ai()),
            true, false, List.of(
                List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
            ), data.rng());
        this.camPos.put("main stage", new Point2D.Float(0.6f, 0f));
    }
    
    /**
     * Creates a new copy of Cris for use in normal Nights.
     * @param name Unique name. If several copies of Cris will appear, make sure they have different names.
     * @param aiDuringNight Map with the list of AI values. For example: {@code Map.of(0,0, 4,1)} would leave
     *                      Cris unactivated until hour 4.
     * @param cameraStalled Whether Cris should never moved while directly under surveillance.
     * @param globalCameraStalled Whether Cris should never moved while any Camera of this Night is
     *                           under surveillance.
     * @param camPaths Paths that Cris will always follow.
     * @param rng Random for the Night.
     * @throws ResourceException If any images or sounds could not be loaded from disk.
     */
    public PathCris(String name, Map<Integer, Integer> aiDuringNight,
                    boolean cameraStalled, boolean globalCameraStalled, List<List<String>> camPaths, Random rng)
            throws ResourceException {
        super(name, 5, 4, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/cris/camImg.png",
                null, camPaths, Color.PINK, rng);

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
