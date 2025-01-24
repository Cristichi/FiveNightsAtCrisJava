package es.cristichi.fnac.anim;

import es.cristichi.fnac.cams.Camera;
import es.cristichi.fnac.cams.CameraMap;
import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Cris is a representation of myself, Cristichi, inside the game. He used to be the owner of the restaurant, but
 * after the Cataclysm he "merged" with the suit he was wearing, that is also now alive. Cris has different
 * implementations with different behaviours during Nights.
 */
public class StatesCris extends PathedMoveAnimatronicDrawing {
    /**
     * A path that is usually followed by this Animatronic while at the Restaurant.
     */
    public static final List<String> RESTAURANT_PATH = List.of("main stage", "dining area", "corridor 2", "corridor 4", "rightDoor");
    
    /** Image of StatesCris crawling on the floor. */
    protected BufferedImage camImgFloor;
    /** Image of StatesCris waking up from the floor. */
    protected BufferedImage camImgWaking;
    /** Image of StatesCris starring at the playuer. */
    protected BufferedImage camImgStare;
    /** Current state. */
    protected State state;
    
    /**
     * Creates a new copy of Cris for use in normal Nights.
     * @param name Unique name. If several copies of Cris will appear, make sure they have different names.
     * @param aiDuringNight Map with the list of AI values. For example: {@code Map.of(0,0, 4,1)} would leave
     *                      Cris unactivated until hour 4.
     * @param camPaths Paths that Cris will always follow.
     * @param rng Random for the Night.
     * @throws ResourceException If any images or sounds could not be loaded from disk.
     */
    public StatesCris(String name, Map<Integer, Integer> aiDuringNight, List<List<String>> camPaths, Random rng)
            throws ResourceException {
        super(name, 1, 6, aiDuringNight, true, false,
                Resources.loadImage("anims/statesCris/camImg.png"),
                        new Jumpscare(Resources.loadGif("anims/cris/jumpscareItsMe.gif"), 0,
                                Resources.loadSound("anims/cris/sounds/jumpscare.wav"), 1,
                                JumpscareVisualSetting.FILL_SCREEN),
                camPaths, Color.PINK, rng);

        this.sounds.put("move", Resources.loadSound("anims/cris/sounds/move.wav"));
        this.state = State.FLOOR;
        this.camImgFloor = Resources.loadImage("anims/statesCris/camImgFloor.png");
        this.camImgWaking = Resources.loadImage("anims/statesCris/camImgWaking.png");
        this.camImgStare = Resources.loadImage("anims/statesCris/camImgStare.png");
    }
    
    @Override
    public MoveOppInfo onMoveOppAttempt(Camera currentCam, boolean beingLookedAt, boolean camsUp, boolean isOpenDoor,
                                        Random rng) {
        if (cameraStalled && beingLookedAt) {
            return new MoveOppInfo(false, null);
        }
        switch (state){
            case FLOOR -> {
                state = State.WAKING_UP;
                return new MoveOppInfo(false, null);
            }
            case WAKING_UP -> {
                state = State.STARE;
                return new MoveOppInfo(false, null);
            }
            case STARE -> {
                state = State.MOVING;
                return new MoveOppInfo(false, null);
            }
        }
        return super.onMoveOppAttempt(currentCam, beingLookedAt, camsUp, isOpenDoor, rng);
    }
    
    @Override
    public MoveSuccessInfo onMoveOppSuccess(CameraMap map, Camera currentLoc, Random rng) throws AnimatronicException {
        Collections.shuffle(camPaths, rng);
        for (List<String> path : camPaths) {
            for (int i = 0; i < path.size(); i++) {
                String cam = path.get(i);
                if (currentLoc.getNameId().equals(cam)) {
                    if (path.size() > i + 1) {
                        return new MoveSuccessInfo(path.get(i + 1), sounds.getOrDefault("move", null));
                    } else {
                        state = State.FLOOR;
                        return new MoveSuccessInfo(path.get(0), sounds.getOrDefault("move", null));
                    }
                }
            }
        }
        throw new AnimatronicException("Animatronic StatesCris is not at a Camera within any of its paths (%s).".formatted(currentLoc.getNameId()));
    }
    
    @Override
    public ShowOnCamInfo showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng) {
        switch (state){
            case FLOOR -> {
                return new ShowOnCamInfo(camImgFloor, camPos.getOrDefault(cam.getNameId(), null));
            }
            case WAKING_UP -> {
                return new ShowOnCamInfo(camImgWaking, camPos.getOrDefault(cam.getNameId(), null));
            }
            case STARE -> {
                return new ShowOnCamInfo(camImgStare, camPos.getOrDefault(cam.getNameId(), null));
            }
        }
        return super.showOnCam(tick, fps, openDoor, cam, rng);
    }
    
    /**
     * StatesCris' states. They indicate his current position.
     */
    protected enum State {
        /** Apparently unconscious on the floor. */
        FLOOR,
        /** Starting to wake up. */
        WAKING_UP,
        /** Starring at the player. */
        STARE,
        /** Normal states, meaning Cris can move. */
        MOVING
    }
}
