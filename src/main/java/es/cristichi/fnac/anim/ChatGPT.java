package es.cristichi.fnac.anim;

import es.cristichi.fnac.cams.Camera;
import es.cristichi.fnac.cams.CameraMap;
import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.NightDrawableImage;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.StaticNightDrawableImage;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Fun AnimatronicDrawing that ChatGPT designed (based on my questions on how it should behave). The bot had a lot of
 * impossible additions like making the Animatronic change your controls and such. Maybe in the future, but for now
 * they simply choose alternatively between moving towards the Office and moving randomly.
 */
public class ChatGPT extends AnimatronicDrawing {
    /**
     * List with the names of all Cameras that ChatGPT will avoid when moving randomly.
     */
    protected final List<String> forbiddenCameras;
    /**
     * List of paths (which are Lists of Camera names) that ChatGPT will follow when not moving randomly.
     */
    protected final List<List<String>> camPaths;
    /**
     * {@code true} if ChatGPT will follow a path on the next move (if possible). {@code false} if they will move
     * randomly.
     */
    protected boolean usingPathedMove;
    /**
     * Image to show on Cameras when ChatGPT has not built themselves yet.
     */
    protected NightDrawableImage buildingCamImg;
    /**
     * {@code true} if ChatGPT has not built themselves yet. When ChatGPT succeeds a Movement Oppotunity, this
     * becomes {@code false} and the movement is canceled. Then ChatGPT is built and can move.
     */
    protected boolean building;
    
    /**
     * Creates a new copy of ChatGPT for use in normal Nights.
     * @param name Unique name. If several copies of ChatGPT will appear, make sure they have different names.
     * @param aiDuringNight Map with the list of AI values. For example: {@code Map.of(0,0, 4,1)} would leave
     *                      ChatGPT unactivated until hour 4.
     * @param cameraStalled Whether ChatGPT should never moved while directly under surveillance.
     * @param globalCameraStalled Whether ChatGPT should never moved while any Camera of this Night is
     *                           under surveillance.
     * @param forbiddenCameras Cameras ChatGPT will avoid when moving randomly.
     * @param camPaths Paths that ChatGPT will follow when not moving randomly.
     * @param rng Random for the Night.
     * @throws ResourceException If any images or sounds could not be loaded from disk.
     */
    public ChatGPT(String name, Map<Integer, Integer> aiDuringNight, boolean cameraStalled, boolean globalCameraStalled,
                   List<String> forbiddenCameras, List<List<String>> camPaths, Random rng) throws ResourceException {
        super(name, 6, 4, aiDuringNight, cameraStalled, globalCameraStalled,
                new StaticNightDrawableImage("anims/chatgpt/camImg.png"),
                new Jumpscare(Resources.loadGif("anims/chatgpt/jumpscare.gif", false), 0,
                        Resources.loadSound("anims/chatgpt/sounds/jumpscare.wav"), 2,
                        JumpscareVisualSetting.FILL_SCREEN), Color.BLUE, rng);

        this.forbiddenCameras = new ArrayList<>(forbiddenCameras);
        this.camPaths = new ArrayList<>(camPaths);
        this.sounds.put("move", Resources.loadSound("anims/chatgpt/sounds/move.wav"));
        this.sounds.put("moveRoam", Resources.loadSound("anims/chatgpt/sounds/moveRoam.wav"));
        this.sounds.put("building", Resources.loadSound("anims/chatgpt/sounds/building.wav"));
        this.buildingCamImg = Resources.loadGif("anims/chatgpt/building.gif", true);

        this.usingPathedMove = false;
        this.building = true;
    }

    @Nullable
    @Override
    public ShowOnCamInfo showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng) {
        if (building) {
            return new ShowOnCamInfo(buildingCamImg.getImageForTick(tick, fps), null);
        }
        return super.showOnCam(tick, fps, openDoor, cam, rng);
    }

    @Override
    public MoveOppInfo onMoveOppAttempt(Camera currentCam, boolean beingLookedAt, boolean camsUp, boolean isOpenDoor,
                                       Random rng) {
        MoveOppInfo attempt = super.onMoveOppAttempt(currentCam, beingLookedAt, camsUp, isOpenDoor, rng);
        if (building && attempt.move()) {
            building = false;
            return new MoveOppInfo(false, sounds.getOrDefault("building", null));
        }
        return attempt;
    }

    @Override
    public MoveSuccessInfo onMoveOppSuccess(CameraMap map, Camera currentLoc, Random rng) throws AnimatronicException {
        if (usingPathedMove || currentLoc.isRightDoor() ||currentLoc.isLeftDoor()) {
            // We should be doing pathed move, or can't do roaming move
            usingPathedMove = false;
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
        }

        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        if (!connections.isEmpty()) {
            // We should be doing roaming move
            usingPathedMove = true;
            int random = rng.nextInt(connections.size());
            return new MoveSuccessInfo(connections.get(random),
                    sounds.getOrDefault("moveRoam", sounds.getOrDefault("move", null)));
        }

        // Oh no, we can't do either
        throw new AnimatronicException(
                "Animatronic " + nameId + " is not at a Camera within any of its paths, and there are no connected " +
                        "Cameras they are not avoiding.");
    }
}
