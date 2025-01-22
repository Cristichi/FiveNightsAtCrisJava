package es.cristichi.fnac.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Cris is a representation of myself, Cristichi, inside the game. He used to be the owner of the restaurant, but
 * after the Cataclysm he "merged" with the suit he was wearing, that is also now alive. Cris has different
 * implementations with different behaviours during Nights.
 */
public class PathCris extends PathedMoveAnimatronicDrawing {
    private static Jumpscare jumpscareNormal, jumpscareItsMe;
    
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
        super(name, 5, 4, aiDuringNight, cameraStalled, globalCameraStalled,
                Resources.loadImage("anims/cris/camImg.png"), null, camPaths, Color.PINK, rng);

        if (jumpscareNormal == null || jumpscareItsMe == null) {
            jumpscareNormal = new Jumpscare(Resources.loadGif("anims/cris/jumpscareNormal.gif"), 0,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav"), 1,
                    JumpscareVisualSetting.FILL_SCREEN);
            jumpscareItsMe = new Jumpscare(Resources.loadGif("anims/cris/jumpscareItsMe.gif"), 7,
                    Resources.loadSound("anims/cris/sounds/jumpscare.wav"), 12,
                    JumpscareVisualSetting.MIDDLE_UP);
        }
        jumpscare = rng.nextFloat() < .9 ? jumpscareNormal : jumpscareItsMe;

        this.sounds.put("move", Resources.loadSound("anims/cris/sounds/move.wav"));
    }
}
