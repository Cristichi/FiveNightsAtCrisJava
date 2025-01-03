package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.cnight.CustomNightAnimatronicData;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Paco used to be an automated kitchen animatronic, capable of the best pizzas a robot can make!
 */
@CustomNightAnimatronic(name = "Paco", portraitPath = "anims/paco/portrait.png", starts = {"kitchen", "cam1"},
        description = "Paco starts his cycle at the kitchen, then moves to the Dining Area, and then chooses whether he " +
        "goes to your left side or right side. After waiting at your closed door, he teleports back " +
        "to the Kitchen. He never goes into the Staff Lounge, the Bathrooms, the Storage or the Main " +
        "Stage."
)
public class Paco extends PathedMoveAnimatronicDrawing {
    /**
     * Creates a copy of Paco for Custom Night.
     * @param data Data that {@link es.cristichi.fnac.cnight.CustomNightMenuJC} has to create the instance.
     * @throws ResourceException If any resources cannot be loaded from disk.
     */
    public Paco(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty() ? data.name() : data.name() + " (" + data.variant() + ")", Map.of(0, data.ai()),
            false, true, List.of(
                List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
            ), data.rng());
    }
    
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
        super(name, 6, 4, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/paco/camImg.png",
                new Jumpscare("anims/paco/jumpscare.gif", 0,
                        Resources.loadSound("anims/paco/sounds/jumpscare.wav", "pacoJump.wav"), 4,
                        JumpscareVisualSetting.CENTERED),
                camPaths, Color.BLUE, rng);

        this.sounds.put("move", Resources.loadSound("anims/paco/sounds/move.wav", "pacoMove.wav"));
    }
}
