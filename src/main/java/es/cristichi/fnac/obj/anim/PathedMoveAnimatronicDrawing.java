package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class implements {@link AnimatronicDrawing#onMovementOppSuccess(CameraMap, Camera, Random)} with a series
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
     * @param name                    Name of the Animatronic. This is used as an identifier.
     * @param secInterval             Seconds between each movement opportunity.
     * @param iaDuringNight           Pairs (Hour, AILevel) that define how the Animatronic's AI changes over Night.
     *                                For instance, [(0,0), (5,1)] means that the Animatronic is inactive until 5 AM
     *                                and has an AI of 1 during the last hour. If 0 is not specified, its value is
     *                                defaulted to 0 at the start of the night.
     * @param maxIaLevel              Maximum AI level. This should usually be 20 for consistency, but can be changed on
     *                                weird Animatronics. By default, this is only used to determine the chances of
     *                                movement opportunities.
     * @param cameraStalled           Whether this Animatronic is Camera-stalled.
     * @param camImgPath              Path to the image used when the Animatronic is shown on a Camera.
     * @param jumpscare               Jumpscare to play when this Animatronic kills the player.
     * @param camPaths                List of paths the Animatronic can take. Each path is another List of Strings with the names
     *                                of the Cameras in that path, ordered from first to last.
     * @param fakeMovementSoundChance It determines the chance of failed Movement Opportunities playing the "move"
     *                                Sound regardless as a fake Movement Opportunity.
     * @param debugColor              Color used for debugging. Not used during normal executions.
     * @throws ResourceException If a given Resource's path does not exist.
     */
    public PathedMoveAnimatronicDrawing(String name, double secInterval, double secsToKill, Map<Integer, Integer> iaDuringNight,
                                        int maxIaLevel, boolean cameraStalled, boolean globalCameraStalled,
                                        String camImgPath, Jumpscare jumpscare, List<List<String>> camPaths,
                                        float fakeMovementSoundChance, Color debugColor) throws ResourceException {
        super(name, secInterval, secsToKill, iaDuringNight, maxIaLevel, cameraStalled, globalCameraStalled, camImgPath,
                jumpscare, fakeMovementSoundChance, debugColor);
        this.camPaths = new LinkedList<>(camPaths);
    }

    @Override
    public MovementSuccessReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        Collections.shuffle(camPaths, rng);
        for (List<String> path : camPaths) {
            for (int i = 0; i < path.size(); i++) {
                String cam = path.get(i);
                if (currentLoc.getName().equals(cam)) {
                    if (path.size() > i + 1) {
                        return new MovementSuccessReturn(path.get(i + 1), sounds.getOrDefault("move", null));
                    } else {
                        return new MovementSuccessReturn(path.get(0), sounds.getOrDefault("move", null));
                    }
                }
            }
        }
        throw new AnimatronicException("Animatronic " + name + " is not at a Camera within any of its paths.");
    }
}
