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
 * Bob used to be one of the waiters for the restaurant, that also formed a musical group. He had an additional
 * task to be the walking schedule that tells human employees what they had to do (like going on lunch, their breaks,
 * and any programmed tasks like animatronic maintenance). He moves randomly except towards the right door.
 */
@CustomNightAnimatronic(name = "Bob", portraitPath = "anims/bob/portrait.png", starts = {"storage", "cam1"},
        description = "Bob starts at the Storage, and moves randomly to any place except corridor 2.")
public class RoamingBob extends AvoidCamsAnimatronicDrawing {
    /**
     * Creates a copy of Bob for Custom Night.
     * @param data Data that {@link es.cristichi.fnac.cnight.CustomNightMenuJC} has to create the instance.
     * @throws ResourceException If any resources cannot be loaded from disk.
     */
    public RoamingBob(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty()?data.name(): data.name()+" ("+data.variant()+")", Map.of(0, data.ai()),
                true, false, List.of("corridor 2", "corridor 4", "bathrooms", "offices"), data.rng());
    }
    
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
        super(name, 5, 6, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/bob/camImg.png",
                new Jumpscare("anims/bob/jumpscare.gif", 0,
                        Resources.loadSound("anims/bob/sounds/jumpscare.wav", "bobJump.wav"), 0, JumpscareVisualSetting.CENTERED),
                forbiddenCams, Color.RED, rng);

        this.sounds.put("move", Resources.loadSound("anims/bob/sounds/move.wav", "bobMove.wav"));
        this.camPos.put("main stage", new Point2D.Float(0.9f, 0.1f));
    }
}
