package es.cristichi.fnac.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.StaticNightDrawableImage;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Paco used to be an automated kitchen animatronic, capable of the best pizzas a robot can make!
 */
public class Paco extends PathedMoveAnimatronicDrawing {
    /**
     * Creates a new copy of Paco for use in normal Nights.
     * @param name Unique name. If several copies of Paco will appear, make sure they have different names.
     * @param aiDuringNight Map with the list of AI values. For example: {@code Map.of(0,0, 4,1)} would leave
     *                      Paco unactivated until hour 4.
     * @param cameraStalled Whether Paco should never moved while directly under surveillance.
     * @param globalCameraStalled Whether Paco should never moved while any Camera of this Night is
     *                           under surveillance.
     * @param camPaths Paths that Paco will always follow.
     * @param rng Random for the Night.
     * @throws ResourceException If any images or sounds could not be loaded from disk.
     */
    public Paco(String name, Map<Integer, Integer> aiDuringNight, boolean cameraStalled, boolean globalCameraStalled,
                List<List<String>> camPaths, Random rng) throws ResourceException {
        super(name, 6, 4, aiDuringNight, cameraStalled, globalCameraStalled,
                new StaticNightDrawableImage("anims/paco/camImg.png"),
                new Jumpscare(Resources.loadGif("anims/paco/jumpscare.gif", false), 0,
                        Resources.loadSound("anims/paco/sounds/jumpscare.wav"), 4,
                        JumpscareVisualSetting.CENTER_BOTTOM),
                camPaths, Color.BLUE, rng);

        this.sounds.put("move", Resources.loadSound("anims/paco/sounds/move.wav"));
    }
}
