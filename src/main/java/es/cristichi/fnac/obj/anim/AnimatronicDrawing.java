package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;
import kuusisto.tinysound.Sound;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Represents an Animatronic Drawing in this world, with all the information and code needed for its characteristics
 * and behaviour.
 * <br><br>
 * At least the movement behaviour must be implemented in {@link #onMoveOppSuccess(CameraMap, Camera, Random)} on
 * specific imlpementations of this abstract class. For an example implementation, feel free to check
 * {@link AvoidCamsAnimatronicDrawing#onMoveOppSuccess(CameraMap, Camera, Random)}.
 */
public abstract class AnimatronicDrawing {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnimatronicDrawing.class);
    /** Maximum AI value for most Animatronics. Unless there is a reason to use another value, use this one. */
    public static final int GENERIC_MAX_AI = 20;

    /** Arbitrary maximun delay, in seconds, that AnimatronicDrawings have by default. This is
     *  used to make it harder to keep track of exactly when AnimatronicDrawings will move with
     *  external programs. This is not to fully disencourage the use of external tools to keep
     *  track of information, just to make it harder. Also so that sounds do not overlap too much.
     */
    protected static final double MAX_DELAY_SECS = 11.5;

    /** Name of the Animatronic. This is used as an identifier during Nights, so it must be unique. */
    protected final String nameId;
    /** Debug color for keeping track of its movement on testing buildings when needed. */
    protected final Color debugColor;
    /** Map with the (Hour -> AI Level) so that AnimatronicDrawings can be increasingly easier, or harder,
     * during Nights. It also allows AnimatronicDrawings to be activated or deactivated at arbitrary hours. */
    protected final Map<Integer, Integer> aiDuringNight;
    /** This value determines the chances of moving. On specific implementations it can be used to any
     * other purpose to take into account the difficulty/aggresion of this AnimatronicDrawing. */
    protected int aiLevel;
    /** Interval in seconds for the Movement Opportunities. */
    protected final double secInterval;
    /** Arbitrary number of seconds the AnimatronicDrawing must be at an open door to kill the player in the
     * default implementation of {@link #onTick(int, int, boolean, boolean, Camera, Random)}. */
    protected final double secsToKill;
    /** The delay, calcualted since the first tick of the Night, that all Movement Opportunities must have for
     * this AnimatronicDrawing. See the implementation of
     * {@link #onMoveOppAttempt(Camera, boolean, boolean, boolean, Random)}.*/
    protected final double randomSecDelay;
    /** Whether this Animatronic is Camera-stalled. */
    protected final boolean globalCameraStalled;
    /** Whether this Animatronic is globally Camera-stalled. */
    protected final boolean cameraStalled;
    /** Jumpscare used when this AnimatronicDrawing kills the player. */
    protected Jumpscare jumpscare;
    /** Image normally shown when the Camera where this AnimatronicDrawing is is being watched.*/
    protected final BufferedImage camImg;
    /** Map used to store and access all Sounds. The default implementation only uses a "move" Sound. */
    protected final Map<String, Sound> sounds;
    /** Map used to store and access any specific positions on specific Cameras. The default implementation
     * does not make use of this, but it implements it in case any specuific implementation adds any values. */
    protected final Map<String, Point2D.Float> camPos;
    
    /** Used on the default implementation of {@link #onTick(int, int, boolean, boolean, Camera, Random)} to
     *  determine whether the Animatronic should be killing the player or not. If so, it also makes it fail
     *  Movement Opportunities to avoid them moving out of the door. */
    protected boolean kill = false;
    /** Used on the default implementation of {@link #onTick(int, int, boolean, boolean, Camera, Random)} to
     *  check how long has the AnimatronicDrawing being able to kill without doing so. */
    protected Integer startKillTick = null;

    /**
     * Creating an Animatronic.
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
     * @param camImg              Image used when the Animatronic is shown on a Camera.
     * @param jumpscare           Jumpscare to play when this Animatronic kills the player.
     * @param debugColor          Color used for debugging. Not used during normal gameplay. This is used for
     *                            developing purposes only in order to see where all Animatronics are at all
     *                            times without having to switch Cameras.
     * @param rng                 {@link Random} for the Night. Used by default to determine a random delay for each
     *                            Animatronic each Night. Its Movement Opportunities will be delayed that much.
     *                            Specific implementations may make use of this for any other thing they need to
     *                            ranndomize at the time of creating an instance of {@link AnimatronicDrawing}.
     */
    AnimatronicDrawing(String nameId, double secInterval, double secsToKill, Map<Integer, Integer> aiDuringNight,
                       int maxAiLevel, boolean cameraStalled, boolean globalCameraStalled, BufferedImage camImg,
                       Jumpscare jumpscare, Color debugColor, Random rng) {
        this.nameId = nameId;
        this.secInterval = secInterval;
        this.secsToKill = secsToKill;
        this.aiLevel = aiDuringNight.getOrDefault(0, 0);
        this.aiDuringNight = aiDuringNight;
        this.cameraStalled = cameraStalled;
        this.globalCameraStalled = globalCameraStalled;
        this.camImg = camImg;
        this.jumpscare = jumpscare;
        this.sounds = new HashMap<>(1);
        this.camPos = new HashMap<>(1);
        this.debugColor = debugColor;

        this.randomSecDelay = rng.nextDouble(MAX_DELAY_SECS);
    }
    
    /**
     * @return The name of this Animatronic, which can be used as an unique ID for gameplay and debugging purposes.
     */
    public String getNameId() {
        return nameId;
    }
    
    /**
     * It chances the current {@link #aiLevel}, but only if {@link #aiDuringNight} has a value for the current hour.
     * It is most effective to call this method only once per hour, when the hour is changed.
     * @param hour Current hour during a Night.
     */
    public void updateIADuringNight(int hour) {
        aiLevel = aiDuringNight.getOrDefault(hour, aiLevel);
    }

    /**
     * This method is called on every single tick and is used to allow the Animatronic to decide
     * what to do based on each tick. For the information it must return, check {@link AnimTickInfo}.
     * It serves as a way to tell the Night to start a Jumpscare, and tell the Night that this Animatronic
     * should have a Movement Opportunity at the given tick. Also, it allows specific implementations to play
     * Sounds in any situation (fake movement, knocking on doors, etc). Sounds are always played at the Camera
     * where the Animatronic is at this tick for consistency, before it moves if it is going to move.
     *
     * @param tick     Current tick.
     * @param fps      FPS for the current night. They are a constant throught the Night to convert seconds to ticks
     *                 and vice-versa.
     * @param camsUp   If cams are up on this tick (this changes as soon as the Player clicks, on the first frame of
     *                 the transition)
     * @param openDoor If there is a door to the Office from the current Camera and it is open.
     * @param cam      Current Camera where the Animatronic is.
     * @param rng      Random in charge of today's Night.
     * @return An instance of {@link AnimTickInfo} with the information that the Night requires during this tick from
     * the Animatronic.
     */
    public AnimTickInfo onTick(int tick, int fps, boolean camsUp, boolean openDoor, Camera cam, Random rng) {
        if (openDoor) {
            // Door is open, start counting (doing nothing means we are counting up)
            if (startKillTick == null) {
                startKillTick = tick;
            } else if (tick - startKillTick >= Math.round(secsToKill * fps)) {
                // Boo-arns
                kill = true;
                return new AnimTickInfo(false, jumpscare, null);
            }
            return new AnimTickInfo(false, null, null);
        } else {
            boolean closedDoor = cam.isLeftDoor() || cam.isRightDoor();
            boolean moveOpp = tick % (int) Math.round((secInterval + randomSecDelay) * fps) == 0;
            startKillTick = null;
            // If door is closed but the Animatronic is still at the door, retain the count
            return new AnimTickInfo(moveOpp, null, null);
        }
    }

    /**
     * This is only called at the moment of the defined internal during any given Night. AI = 0 will disable
     * movement unless this method is overriten by an Animatronic to do otherwise.
     *
     * @param currentCam    Current camera from which the Animatronic is trying to move.
     * @param beingLookedAt Whether the player is looking at that Camera on this tick.
     * @param camsUp        Whether the player is looking at any Camera.
     * @param isOpenDoor    If there is a door to the Office from the current Camera and it is open.
     * @param rng           Random in charge of today's night.
     * @return {@code true} if Animatronic should move on this tick. In that case,
     * {@link AnimatronicDrawing#onMoveOppSuccess(CameraMap, Camera, Random)} is called afterwards.
     */
    public MoveOppInfo onMoveOppAttempt(Camera currentCam, boolean beingLookedAt, boolean camsUp, boolean isOpenDoor,
                                        Random rng) {
        boolean itMoves;
        if (kill || startKillTick != null || isOpenDoor || cameraStalled && beingLookedAt
                || !currentCam.isLeftDoor() && !currentCam.isRightDoor() && camsUp && globalCameraStalled) {
            itMoves = false;
        } else if (currentCam.isLeftDoor() || currentCam.isRightDoor()) {
            // isOpenDoor = false here, therefore this is a closed door.
            itMoves = true;
        } else {
            itMoves = rng.nextInt(GENERIC_MAX_AI) < aiLevel;
        }
        return new MoveOppInfo(itMoves, null);
    }

    /**
     * By default, Animatronics should only move to cameras connected with the current one.
     * All implementations of {@link AnimatronicDrawing} must override this method.
     *
     * @param map         Entire map, with all Cams.
     * @param currentLoc  Cam where this Animatronic is.
     * @param rng         Random in charge of today's night.
     * @return The name of the Camera it has to move to. The Night will be in charge of
     * trying to move the Animatronic to the indicated Camera, connected or not. If movement
     * must be cancelled at this step, just return null.
     * @throws AnimatronicException If the Animatronic's logic cannot determine the movement. In this case the movement
     * should be canceled without errors to the player.
     */
    public abstract MoveSuccessInfo onMoveOppSuccess(CameraMap map, Camera currentLoc, Random rng)
            throws AnimatronicException;

    /**
     * On the default implementation, it simply returns {@link #camImg} and {@code null} or the
     * {@link Point2D.Float} saved in {@link #camPos} with the name of {@code cam}. All the other
     * information available if for use on specific implementations to change this behaviour at will.
     * @param tick     Current tick, for accurately counting seconds.
     * @param fps      Current ticks per second, to convert from ticks to seconds for consistency with real time.
     * @param openDoor If there is a door to the Office from the current Camera and it is open.
     * @param cam      Current Camera where the Animatronic is and the player is watching.
     * @param rng      Random in charge of today's night.
     * @return {@link BufferedImage} of the image representing this Animatronic on the Camera on this tick.
     * {@code null} if it should not even appear on the Camera.
     */
    public ShowOnCamInfo showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng) {
        return new ShowOnCamInfo(camImg, camPos.getOrDefault(cam.getNameId(), null));
    }
    
    /** @return Debug color for this {@link AnimatronicDrawing}. */
    public Color getDebugColor() {
        return debugColor;
    }
    
    /**
     * @param o Compared object
     * @return {@code true} if {@code Object o} is an {@link AnimatronicDrawing}
     * with the same name as this one. {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return nameId.equals(((AnimatronicDrawing) o).nameId);
    }
    
    @Override
    public String toString() {
        return "AnimatronicDrawing{'%s'}".formatted(nameId);
    }
    
    /**
     * Information given by each Animatronic at each tick.
     *
     * @param moveOpp   Whether the Animatronic has succeeded a Movement Opportunity on this tick.
     * @param jumpscare A Jumpscare if it must happen, or {@code null} otherwise.
     * @param sound     Sound to play at the current Camera of this Animatronic on this tick,
     *                  or {@code null} otherwise.
     */
    public record AnimTickInfo(boolean moveOpp, @Nullable Jumpscare jumpscare, @Nullable Sound sound) {}

    /**
     * Information given by each Animatronic when the Night gives them a chance to move and they succeed it.
     *
     * @param moveToCam Name of the Camera to move to. Teleporting allowed if desired. It should never be
     *                  {@code null}, as the Animatronic is forced to move. The only way to not move
     *                  is to throw an {@link AnimatronicException}, which should
     *                  be done only for unexpected behaviours.
     * @param moveSound Sound to play because of this movement on the destination Camera, or {@code null}
     *                  if no Sound should play. This is ignored if moveToCam is {@code false}.
     */
    public record MoveSuccessInfo(String moveToCam, @Nullable Sound moveSound) {}

    /**
     * Information given by each Animatronic when the Night gives them a chance to move and they succeed it.
     *
     * @param move  {@code true} if this Animatronic should move on this Movement Opportunity.
     * @param sound Sound to play because of this movement on the origin Camera, {@code null} if no Sound
     *              should play. WARNING regular movement Sounds are implemented on the method
     *              {@link AnimatronicDrawing#onMoveOppSuccess(CameraMap, Camera, Random)}, playing
     *              Sound on the Movement Opportunity is usually for when {@code move} is false for fake
     *              movement Sounds.
     */
    public record MoveOppInfo(boolean move, @Nullable Sound sound) {}

    /**
     * Information given by each Animatronic when the Night wants to show them on Camera.
     * @param camImg A {@link BufferedImage} of what the Animatronic must look like on the Camera on this tick. It
     *               admits {@code null} to indicate that the Animatronic should not appear, then preferredPoint
     *               is ignored.
     * @param preferredPoint A {@link Point2D.Float} with the preferred position on this Camera and tick, or
     *                       {@code null} if Night should handle it. Both {@code x} and {@code y}
     *                       must be between 0 and 1, as a percentage in the available size to remain constant.
     *                       These coordinates represent where the top-left corner of the image will be.
     */
    public record ShowOnCamInfo(@Nullable BufferedImage camImg, @Nullable Point2D.Float preferredPoint) {}
}
