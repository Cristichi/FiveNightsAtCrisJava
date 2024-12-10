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
    protected static final int EXTRA_AI_FOR_LEAVING = 5;
    protected static final double DOOR_OPENED_TOO_SOON_SECS = 0.5;

    protected final String name;
    protected final Color debugColor;
    protected final Map<Integer, Integer> iaDuringNight;
    protected int aiLevel;
    protected final int maxIaLevel;
    protected final double secInterval;
    protected final boolean cameraStalled;
    protected final boolean globalCameraStalled;
    protected Jumpscare jumpscare;
    protected final BufferedImage camImg;
    protected boolean kill = false;
    protected Integer startKillTick = null;
    protected double secsToKill;
    protected final Map<String, Sound> sounds;

    /**
     * Creating an Animatronic.
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
     * @param globalCameraStalled     Whether this Animatronic is Camera-stalled.
     * @param camImgPath              Path to the image used when the Animatronic is shown on a Camera.
     * @param jumpscare               Jumpscare to play when this Animatronic kills the player.
     * @param debugColor              Color used for debugging. Not used during normal executions.
     * @throws ResourceException If a resource is not found in the given paths.
     */
    AnimatronicDrawing(String name, double secInterval, double secsToKill, Map<Integer, Integer> iaDuringNight, int maxIaLevel,
                       boolean cameraStalled, boolean globalCameraStalled, String camImgPath,
                       Jumpscare jumpscare, Color debugColor) throws ResourceException {
        this.name = name;
        this.secInterval = secInterval;
        this.secsToKill = secsToKill;
        this.aiLevel = iaDuringNight.getOrDefault(0, 0);
        this.iaDuringNight = iaDuringNight;
        this.maxIaLevel = maxIaLevel;
        this.cameraStalled = cameraStalled;
        this.globalCameraStalled = globalCameraStalled;
        this.camImg = Resources.loadImageResource(camImgPath);
        this.sounds = new HashMap<>(1);
        this.jumpscare = jumpscare;
        this.debugColor = debugColor;
    }

    public String getName() {
        return name;
    }

    public double getSecInterval() {
        return secInterval;
    }

    public void updateIADuringNight(int time){
        aiLevel = iaDuringNight.getOrDefault(time, aiLevel);
    }

    /**
     * This is only called at the moment of the defined internal during any given Night.
     * AI = 0 will disable movement unless this method is overriten by an Animatronic to do so.
     *
     * @param currentCam    Current camera from which the Animatronic is trying to move.
     * @param beingLookedAt Whether the player is looking at that Camera on this tick.
     * @param camsUp Whether the player is looking at any Camera.
     * @param isOpenDoor    If there is a door to the Office from the current Camera and it is open.
     * @param rng           Random in charge of today's night.
     * @return <code>true</code> if Animatronic should move on this tick. In that case,
     * {@link AnimatronicDrawing#onMovementOppSuccess(CameraMap, Camera, Random)} is called afterwards.
     */
    public boolean onMovementOpportunityAttempt(Camera currentCam, boolean beingLookedAt, boolean camsUp, boolean isOpenDoor, Random rng){
        if (kill || startKillTick != null || isOpenDoor || cameraStalled && beingLookedAt || camsUp && globalCameraStalled){
            return false;
        }
        if ((currentCam.isLeftDoor() || currentCam.isRightDoor())){
            return rng.nextInt(maxIaLevel) < aiLevel + EXTRA_AI_FOR_LEAVING;
        }
        return rng.nextInt(maxIaLevel) < aiLevel;
    }

    /**
     * By default, Animatronics should only move to cameras connected with the current one.
     * Animatronics must override this method.
     * @param map Entire map, with all Cams.
     * @param currentLoc, Cam where this Animatronic is.
     * @param rng Random in charge of today's night.
     * @return The name of the Camera it has to move to. The Night will be in charge of
     * trying to move the Animatronic to the indicated Camera, connected or not. If movement
     * must be cancelled at this step, just return null.
     */
    public abstract MovementSuccessReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng);

    /**
     * This method is called on every single tick and is used to allow the Animatronic to decide
     * what to do based on each tick. It serves as a way to tell the Night to start a Jumpscare, as
     * well as to play Sounds in any situation (fake movement, knocking on doors, etc). Soudns are always played
     * At the Camera where the Animatronic is for consistency.
     *
     * @param tick     Current tick.
     * @param fps      FPS for the current night. They are a constant throught the night.
     * @param camsUp   If cams are up on this tick (this changes as soon as the Player clicks,
     *                 on the first frame of the transition)
     * @param openDoor If there is a door to the Office from the current Camera and it is open.
     * @param cam      Current Camera where the Animatronic is.
     * @param rng      Random in charge of today's night.
     * @return An instance of {@link TickReturn} that defines if there is a Jumpscare,
     * along other potential data that is calculated each tick on each Animatronic.
     */
    public TickReturn onTick(int tick, int fps, boolean camsUp, boolean openDoor, Camera cam, Random rng) {
        if (openDoor) {
            // Door is open, start counting (doing nothing means we are counting up)
            if (startKillTick == null) {
                startKillTick = tick;
            } else if (tick - startKillTick >= Math.round(secsToKill * fps)) {
                // Boo-arns
                kill = true;
                return new TickReturn(true, null);
            }
        } else if (!cam.isRightDoor() && !cam.isLeftDoor()) {
            // Reset counter if the Animatronic is not at the door
            startKillTick = null;
        }

        // If door is closed but the Animatronic is still at the door, retain the count
        return new TickReturn(false, null);
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
    public boolean showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng){
        return !kill;
    }

    public Jumpscare getJumpscare() {
        return jumpscare;
    }

    public BufferedImage getCamImg() {
        return camImg;
    }

    public Color getDebugColor(){
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
     * @param jumpscare Whether a Jumpscare is confirmed.
     * @param sound <code>null</code> for no Sound to play on this tick, or the Sound to play.
     */
    public record TickReturn(boolean jumpscare, @Nullable Sound sound){
    }

    /**
     * Information given by each Animatronic when the Night gives them a chance to move and they succeed it.
     * @param moveToCam Name of the Camera to move to. Teleporting allowed if desired. <code>null</code> to indicate
     *                  that the Animatronic cancels the move for any reason.
     * @param sound Sound to play because of this movement on the destination Camera
     *              , <code>null</code> if no Sound should play. This is ignored if moveToCam is <code>null</code>.
     */
    public record MovementSuccessReturn(@Nullable String moveToCam, @Nullable Sound sound){
    }
}
