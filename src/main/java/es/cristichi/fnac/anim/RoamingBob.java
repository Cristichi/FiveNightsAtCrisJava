package es.cristichi.fnac.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Bob used to be one of the waiters for the restaurant, that also formed a musical group. He had an additional
 * task to be the walking schedule that tells human employees what they had to do (like going on lunch, their breaks,
 * and any programmed tasks like animatronic maintenance). He moves randomly except towards the right door.
 */
public class RoamingBob extends AvoidCamsAnimatronicDrawing {
    /**
     * Creates a new copy of Bob for use in normal Nights.
     * @param name Unique name. If several copies of Bob will appear, make sure they have different names.
     * @param aiDuringNight Map with the list of AI values. For example: {@code Map.of(0,0, 4,1)} would leave
     *                      Bob unactivated until hour 4.
     * @param cameraStalled Whether Bob should never moved while directly under surveillance.
     * @param globalCameraStalled Whether Bob should never moved while any Camera of this Night is
     *                           under surveillance.
     * @param forbiddenCams Paths that Bob will never visit.
     * @param rng Random for the Night.
     * @throws ResourceException If any images or sounds could not be loaded from disk.
     */
    public RoamingBob(String name, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                      boolean globalCameraStalled, List<String> forbiddenCams, Random rng) throws ResourceException {
        super(name, 5, 6, aiDuringNight, cameraStalled, globalCameraStalled,
                Resources.loadGif("anims/bob/cam.gif", true),
                new Jumpscare(Resources.loadGif("anims/bob/jumpscare.gif", false), 0,
                        Resources.loadSound("anims/bob/sounds/jumpscare.wav"), 0, JumpscareVisualSetting.CENTER_BOTTOM),
                forbiddenCams, Color.RED, rng);

        this.sounds.put("move", Resources.loadSound("anims/bob/sounds/move.wav"));
        this.camPos.put("main stage", new Point2D.Float(0.9f, 0.1f));
    }
}
