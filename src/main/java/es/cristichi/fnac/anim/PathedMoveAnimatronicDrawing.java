package es.cristichi.fnac.anim;

import es.cristichi.fnac.cams.Camera;
import es.cristichi.fnac.cams.CameraMap;
import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.io.NightDrawableImage;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class implements {@link AnimatronicDrawing#onMoveOppSuccess(CameraMap, Camera, Random)} with a series
 * of paths. Each time the Animatronic can move, they will check each given path in a random order and move to the
 * next Camera of the first randomly chosen path that contains the Camera they are at. If that path has no next
 * Camera, meaning they are at the end of the path, the return to the first Camera of that path.
 * If the Animatronic is not at a Camera in any of its paths, it throws {@link AnimatronicException}.
 */
public abstract class PathedMoveAnimatronicDrawing extends AnimatronicDrawing {
    /**
     * This represents a List of paths, each path being an ordered List of Strings that contains
     * the name of the Cameras. When the cycle is completed, the Animatronic returns to a random Camera
     * chosen amonst the first of each path.
     */
    protected final List<List<String>> camPaths;

    /**
     * Creates a new {@link PathedMoveAnimatronicDrawing} with the given data.
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
     * @param cameraStalled       Whether this Animatronic is Camera-stalled. This means that they fail Movement
     *                            Opportunities while being looked at.
     * @param globalCameraStalled Whether this Animatronic is globally Camera-stalled. Same as
     *                            {@code cameraStalled}, except that this Animatronic would fail the Movement
     *                            Opportunity regardless of which Camera the player is looking at. If this is true,
     *                            then {@code cameraStalled} is ignored.
     * @param camImg              Image used when the Animatronic is shown on a Camera.
     * @param jumpscare           Jumpscare to play when this Animatronic kills the player.
     * @param camPaths            List of the paths, each one being a List of Camera names, that this Animatronic
     *                            should stick to when trying to move. Usually they start far from the office and
     *                            end at either "leftDoor" or "rightDoor".
     * @param debugColor          Color used for debugging. Not used during normal gameplay. This is used for
     *                            developing purposes only in order to see where all Animatronics are at all
     *                            times without having to switch Cameras.
     * @param rng                 {@link Random} for the Night. Used by default to determine a random delay for each
     *                            Animatronic each Night. Its Movement Opportunities will be delayed that much.
     *                            Specific implementations may make use of this for any other thing they need to
     *                            ranndomize at the time of creating an instance of {@link AnimatronicDrawing}.
     */
    public PathedMoveAnimatronicDrawing(String nameId, double secInterval, double secsToKill,
                                        Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                                        boolean globalCameraStalled, NightDrawableImage camImg, Jumpscare jumpscare,
                                        List<List<String>> camPaths, Color debugColor,
                                        Random rng) {
        super(nameId, secInterval, secsToKill, aiDuringNight, cameraStalled, globalCameraStalled, camImg,
                jumpscare, debugColor, rng);
        this.camPaths = new LinkedList<>(camPaths);
    }

    @Override
    public MoveSuccessInfo onMoveOppSuccess(CameraMap map, Camera currentLoc, Random rng) throws AnimatronicException{
        Collections.shuffle(camPaths, rng);
        for (List<String> path : camPaths) {
            for (int i = 0; i < path.size(); i++) {
                String cam = path.get(i);
                if (currentLoc.getNameId().equals(cam)) {
                    if (path.size() > i + 1) {
                        return new MoveSuccessInfo(path.get(i + 1), sounds.getOrDefault("move", null));
                    } else {
                        return new MoveSuccessInfo(path.get(0), sounds.getOrDefault("move", null));
                    }
                }
            }
        }
        throw new AnimatronicException("Animatronic " + nameId + " is not at a Camera within any of its paths.");
    }
}
