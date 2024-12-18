package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;
import kuusisto.tinysound.Sound;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public abstract class AnimatronicDrawing {
    public static final int GENERIC_MAX_AI = 20;

    protected static final int AI_FOR_LEAVING_DOOR = 18;
    protected static final double DOOR_OPENED_TOO_SOON_SECS = 0.5;
    protected static final double MAX_DELAY_SECS = 11.5;

    protected final String name;
    protected final Color debugColor;
    protected final Map<Integer, Integer> iaDuringNight;
    protected int aiLevel;
    protected final double secInterval;
    protected final double randomSecDelay;
    protected final boolean cameraStalled;
    protected final boolean globalCameraStalled;
    protected Jumpscare jumpscare;
    protected final BufferedImage camImg;
    protected boolean kill = false;
    protected Integer startKillTick = null;
    protected double secsToKill;
    protected final Map<String, Sound> sounds;
    protected final float fakeMovementSoundChance;

    /**
     * Creating an Animatronic.
     *
     * @param name                    Name of the Animatronic. This is used as an identifier.
     * @param secInterval             Seconds between each movement opportunity.
     * @param secsToKill              Seconds the Animatronic needs to kill. Used by the default
     *                                {@link #onTick(int, int, boolean, boolean, Camera, Random)} to determine
     *                                Jumpscares.
     * @param iaDuringNight           Pairs (Hour, AILevel) that define how the Animatronic's AI changes over Night.
     *                                For instance, [(0,0), (5,1)] means that the Animatronic is inactive until 5 AM
     *                                and has an AI of 1 during the last hour. If 0 is not specified, its value is
     *                                defaulted to 0 at the start of the night.
     * @param maxAiLevel              Maximum AI level. This should usually be 20 for consistency, but can be changed
     *                                on weird Animatronics. By default, this is used to determine the chances of
     *                                movement opportunities and also for Custom Nights.
     * @param cameraStalled           Whether this Animatronic is Camera-stalled.
     * @param globalCameraStalled     Whether this Animatronic is Camera-stalled.
     * @param camImgPath              Path to the image used when the Animatronic is shown on a Camera.
     * @param jumpscare               Jumpscare to play when this Animatronic kills the player.
     * @param fakeMovementSoundChance It determines the chance of failed Movement Opportunities playing the "move"
     *                                Sound regardless as a fake Movement Opportunity.
     * @param debugColor              Color used for debugging. Not used during normal executions.
     * @param rng                     Random for the Night. Used only to determine a random delay for each
     *                                Animatronic each Night. Its Movement Opportunities will be delayed that much.
     *                                Specific implementations may make use of this for any other thing they need to
     *                                ranndomize at the time of creating the instance.
     * @throws ResourceException If a resource is not found in the given paths.
     */
    AnimatronicDrawing(String name, double secInterval, double secsToKill, Map<Integer, Integer> iaDuringNight,
                       int maxAiLevel, boolean cameraStalled, boolean globalCameraStalled, String camImgPath,
                       Jumpscare jumpscare, float fakeMovementSoundChance, Color debugColor,
                       Random rng) throws ResourceException {
        this.name = name;
        this.secInterval = secInterval;
        this.secsToKill = secsToKill;
        this.aiLevel = iaDuringNight.getOrDefault(0, 0);
        this.iaDuringNight = iaDuringNight;
        this.cameraStalled = cameraStalled;
        this.globalCameraStalled = globalCameraStalled;
        this.camImg = Resources.loadImageResource(camImgPath);
        this.sounds = new HashMap<>(1);
        this.jumpscare = jumpscare;
        this.fakeMovementSoundChance = fakeMovementSoundChance;
        this.debugColor = debugColor;

        this.randomSecDelay = rng.nextDouble(MAX_DELAY_SECS);
    }

    public String getName() {
        return name;
    }

    public double getSecInterval() {
        return secInterval;
    }

    public void updateIADuringNight(int time) {
        aiLevel = iaDuringNight.getOrDefault(time, aiLevel);
    }

    /**
     * This is only called at the moment of the defined internal during any given Night.
     * AI = 0 will disable movement unless this method is overriten by an Animatronic to do so.
     *
     * @param currentCam    Current camera from which the Animatronic is trying to move.
     * @param beingLookedAt Whether the player is looking at that Camera on this tick.
     * @param camsUp        Whether the player is looking at any Camera.
     * @param isOpenDoor    If there is a door to the Office from the current Camera and it is open.
     * @param rng           Random in charge of today's night.
     * @return <code>true</code> if Animatronic should move on this tick. In that case,
     * {@link AnimatronicDrawing#onMovementOppSuccess(CameraMap, Camera, Random)} is called afterwards.
     */
    public MoveOppRet onMovementOpportunityAttempt(
            Camera currentCam, boolean beingLookedAt, boolean camsUp, boolean isOpenDoor, Random rng) {
        boolean itMoves;
        if (kill || startKillTick != null || isOpenDoor || cameraStalled && beingLookedAt
                || !currentCam.isLeftDoor() && !currentCam.isRightDoor() && camsUp && globalCameraStalled) {
            itMoves = false;
        } else if ((currentCam.isLeftDoor() || currentCam.isRightDoor())) {
            itMoves = rng.nextInt(GENERIC_MAX_AI) < AI_FOR_LEAVING_DOOR;
        } else {
            itMoves = rng.nextInt(GENERIC_MAX_AI) < aiLevel;
        }
        return new MoveOppRet(itMoves,
                !itMoves && rng.nextFloat() < fakeMovementSoundChance ? sounds.getOrDefault("move", null) : null);
    }

    /**
     * By default, Animatronics should only move to cameras connected with the current one.
     * Animatronics must override this method.
     *
     * @param map         Entire map, with all Cams.
     * @param currentLoc, Cam where this Animatronic is.
     * @param rng         Random in charge of today's night.
     * @return The name of the Camera it has to move to. The Night will be in charge of
     * trying to move the Animatronic to the indicated Camera, connected or not. If movement
     * must be cancelled at this step, just return null.
     */
    public abstract MoveSuccessRet onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng);

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
     * @return An instance of {@link AnimTickInfo} with the information that the Night requires for this tick from
     *         the Animatronic.
     */
    public AnimTickInfo onTick(int tick, int fps, boolean camsUp, boolean openDoor, Camera cam, Random rng) {
        boolean moveOpp = tick % (int) Math.round((secInterval + randomSecDelay) * fps) == 0;

        if (openDoor) {
            // Door is open, start counting (doing nothing means we are counting up)
            if (startKillTick == null) {
                startKillTick = tick;
            } else if (tick - startKillTick >= Math.round(secsToKill * fps)) {
                // Boo-arns
                kill = true;
                return new AnimTickInfo(moveOpp, true, null);
            }
        } else if (!cam.isRightDoor() && !cam.isLeftDoor()) {
            // Reset counter if the Animatronic is not at the door
            startKillTick = null;
        }

        // If door is closed but the Animatronic is still at the door, retain the count
        return new AnimTickInfo(moveOpp, false, null);
    }

    /**
     * This determines whether the Animatronic should appear on a camera or not. Just some flavor.
     * This is called per tick, but there is a counter that is unique to each instance of cams, which
     * increases each time the player changes cams an each time they open cams.
     *
     * @param tick     Current tick, for accurately counting seconds.
     * @param fps      Current ticks per second, to convert from ticks to seconds for consistency with real time.
     * @param openDoor If there is a door to the Office from the current Camera and it is open.
     * @param cam      Current Camera where the Animatronic is and the player is watching.
     * @param rng      Random in charge of today's night.
     * @return True if this Animatronic should not be drawn in the Camera. False otherwise
     */
    public boolean showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng) {
        return !kill;
    }

    public Jumpscare getJumpscare() {
        return jumpscare;
    }

    public BufferedImage getCamImg() {
        return camImg;
    }

    public Color getDebugColor() {
        return debugColor;
    }

    public Map<String, Sound> getSounds() {
        return sounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return name.equals(((AnimatronicDrawing) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, aiLevel, camImg);
    }

    /**
     * Information given by each Animatronic at the start of each tick.
     *
     * @param jumpscare Whether a Jumpscare is confirmed.
     * @param sound     <code>null</code> for no Sound to play on this tick, or the Sound to play.
     */
    public record AnimTickInfo(boolean moveOpp, boolean jumpscare, @Nullable Sound sound) {
    }

    /**
     * Information given by each Animatronic when the Night gives them a chance to move and they succeed it.
     *
     * @param moveToCam Name of the Camera to move to. Teleporting allowed if desired. It should not be
     *                  <code>null</code>, as the Animatronic is forced to move. The only way to not move
     *                  is to throw an {@link es.cristichi.fnac.exception.AnimatronicException}.
     * @param sound     Sound to play because of this movement on the destination Camera
     *                  , <code>null</code> if no Sound should play. This is ignored if moveToCam is <code>null</code>.
     */
    public record MoveSuccessRet(String moveToCam, @Nullable Sound sound) {
    }

    /**
     * Information given by each Animatronic when the Night gives them a chance to move and they succeed it.
     *
     * @param move  <true>true</true> if this Animatronic should move on this Movement Opportunity.
     * @param sound Sound to play because of this movement on the origin Camera, <code>null</code> if no Sound
     *              should play. WARNING regular movement Sounds are implemented on the method
     *              {@link AnimatronicDrawing#onMovementOppSuccess(CameraMap, Camera, Random)}, playing
     *              Sound on the Movement Opportunity is usually for when <code>move</code> is false for fake
     *              movement Sounds.
     */
    public record MoveOppRet(boolean move, @Nullable Sound sound) {
    }
}
