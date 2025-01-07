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
 * Maria used to be one of the waiters for the restaurant, that also formed a musical group. She had an additional
 * task to forward any complains to the offices or the kitchen. She moves randomly except towards the left door.
 */
@CustomNightAnimatronic(name = "Maria", portraitPath = "anims/maria/portrait.png",
        starts = {"offices", "cam2"},
        description = "Maria starts at the Offices, and moves randomly to any place except corridor 1.")
public class RoamingMaria extends AvoidCamsAnimatronicDrawing {
    /**
     * Creates a copy of Maria for Custom Night.
     * @param data Data that {@link es.cristichi.fnac.cnight.CustomNightMenuJC} has to create the instance.
     * @throws ResourceException If any resources cannot be loaded from disk.
     */
    public RoamingMaria(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty() ? data.name() : data.name() + " (" + data.variant() + ")", Map.of(0, data.ai()),
                true, false, List.of("corridor 1", "corridor 3", "staff lounge"), data.rng());
    }
    
    /**
     * Creates a new copy of Maria for use in normal Nights.
     * @param name Unique name. If several copies of Maria will appear, make sure they have different names.
     * @param aiDuringNight Map with the list of AI values. For example: {@code Map.of(0,0, 4,1)} would leave
     *                      Maria unactivated until hour 4.
     * @param cameraStalled Whether Maria should never moved while directly under surveillance.
     * @param globalCameraStalled Whether Maria should never moved while any Camera of this Night is
     *                           under surveillance.
     * @param forbiddenCams Paths that Maria will never visit.
     * @param rng Random for the Night.
     * @throws ResourceException If any images or sounds could not be loaded from disk.
     */
    public RoamingMaria(String name, Map<Integer, Integer> aiDuringNight,boolean cameraStalled,
                        boolean globalCameraStalled, List<String> forbiddenCams, Random rng) throws ResourceException {
        super(name, 5, 6, aiDuringNight, 20, cameraStalled,
                globalCameraStalled, "anims/maria/camImg.png",
                new Jumpscare(Resources.loadGif("anims/maria/jumpscare.gif"), 0,
                        Resources.loadSound("anims/maria/sounds/jumpscare.wav"),
                        0, JumpscareVisualSetting.CENTERED),
                forbiddenCams, Color.YELLOW, rng);

        this.sounds.put("move", Resources.loadSound("anims/maria/sounds/move.wav"));
        this.camPos.put("main stage", new Point2D.Float(0.8f, 0.1f));
    }
}
