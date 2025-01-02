package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class implements {@link AnimatronicDrawing#onMoveOppSuccess(CameraMap, Camera, Random)} with a simple
 * movement that avoids a list of Camera names. From the possible Cameras, movement is completely random.
 */
public abstract class AvoidCamsAnimatronicDrawing extends AnimatronicDrawing {
    protected final List<String> forbiddenCameras;

    /**
     * Creates a new {@link AvoidCamsAnimatronicDrawing} with the given data.
     *
     * @param nameId              Name of the Animatronic. This is used as an identifier, and not shown to the
     *                            player in any way during gameplay. Two Animatronics with the same name leads to
     *                            issues.
     * @param secInterval         Seconds between each movement opportunity.
     * @param secsToKill          Seconds the Animatronic needs to kill. Used by the default
     *                            {@link #onTick(int, int, boolean, boolean, Camera, Random)} to determine
     *                            Jumpscares.
     * @param aiDuringNight       Pairs (Hour, AILevel) that define how the Animatronic's AI changes over Night.
     *                            For instance, [(0,0), (5,1)] means that the Animatronic is inactive until 5 AM
     *                            and has an AI of 1 during the last hour. If 0 is not specified, its value is
     *                            defaulted to 0 at the start of the night.
     * @param maxAiLevel          Maximum AI level. This should usually be 20 for consistency, but can be changed
     *                            on weird Animatronics. By default, this is used to determine the chances of
     *                            movement opportunities and also for limiting in Custom Nights.
     * @param cameraStalled       Whether this Animatronic is Camera-stalled. This means that they fail Movement
     *                            Opportunities while being looked at.
     * @param globalCameraStalled Whether this Animatronic is globally Camera-stalled. Same as
     *                            {@code cameraStalled}, except that this Animatronic would fail the Movement
     *                            Opportunity regardless of which Camera the player is looking at. If this is true,
     *                            then {@code cameraStalled} is ignored.
     * @param camImgPath          Path to the image used when the Animatronic is shown on a Camera.
     * @param jumpscare           Jumpscare to play when this Animatronic kills the player.
     * @param forbiddenCameras    List of the names of Cameras this Animatronic should not go to.
     * @param debugColor          Color used for debugging. Not used during normal gameplay. This is used for
     *                            developing purposes only in order to see where all Animatronics are at all
     *                            times without having to switch Cameras.
     * @param rng                 {@link Random} for the Night. Used by default to determine a random delay for each
     *                            Animatronic each Night. Its Movement Opportunities will be delayed that much.
     *                            Specific implementations may make use of this for any other thing they need to
     *                            ranndomize at the time of creating an instance of {@link AnimatronicDrawing}.
     * @throws ResourceException If a resource is not found in the given paths.
     */
    public AvoidCamsAnimatronicDrawing(String nameId, double secInterval, double secsToKill,
                                       Map<Integer, Integer> aiDuringNight, int maxAiLevel, boolean cameraStalled,
                                       boolean globalCameraStalled, String camImgPath, Jumpscare jumpscare,
                                       List<String> forbiddenCameras, Color debugColor,
                                       Random rng) throws ResourceException {
        super(nameId, secInterval, secsToKill, aiDuringNight, maxAiLevel, cameraStalled, globalCameraStalled, camImgPath,
                jumpscare, debugColor, rng);
        this.forbiddenCameras = Objects.requireNonNullElseGet(forbiddenCameras, () -> new ArrayList<>(0));
    }

    @Override
    public MoveSuccessInfo onMoveOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        int random = rng.nextInt(connections.size());
        return new MoveSuccessInfo(connections.get(random), sounds.getOrDefault("move", null));
    }
}
