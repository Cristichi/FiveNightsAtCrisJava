package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.*;

// Supressing unused warnings since some parts of this are for future Animatronics to use,
// even if the currently existing ones do not make use of it.
@SuppressWarnings("unused")
public abstract class Animatronic {
    protected final String name;
    protected final HashMap<Integer, Integer> iaDuringNight;
    protected int aiLevel;
    protected final int maxIaLevel;
    protected final double secInterval;
    protected final Jumpscare jumpscare;
    protected final BufferedImage camImg;
    protected final List<String> forbiddenCameras;

    /**
     * Creating an Animatronic.
     * @param name Name of the Animatronic. This is used as an identifier.
     * @param secInterval Seconds between each movement opportunity.
     * @param iaDuringNight Pairs (Hour, AILevel) that define how the Animatronic's AI changes over Night.
     *                      For instance, [(0,0), (5,1)] means that the Animatronic is inactive until 5 AM
     *                      and has an AI of 1 during the last hour.
     * @param maxIaLevel Maximum AI level. This should usually be 20 for consistency, but can be changed on
     *                   weird Animatronics. By default, this is only used to determine the chances of 
     *                   movement opportunities.
     * @param camImgPath Path to the image used when the Animatronic is shown on a Camera.
     * @param jumpscareGifPath Path in resources to the gif containing the jumpscare of this Animatronic.
     * @param jumpscareRepFrames Number of times each frame of the jumpscare is kept on screen before 
     *                           showing the next one.
     * @param forbiddenCameras Only used by the default {@link #onMovementOppSuccess(CameraMap, Camera, Random)}
     *                         so that it avoids those cameras. Useful for most Animatronics, but feel free to
     *                         set it to null if you will not use it.
     * @throws ResourceNotFound If a resource is not found in the given paths.
     */
    Animatronic(String name, double secInterval, HashMap<Integer, Integer> iaDuringNight,
                       int maxIaLevel, String camImgPath, String jumpscareGifPath, int jumpscareRepFrames,
                       @Nullable List<String> forbiddenCameras) throws ResourceNotFound {
        this.name = name;
        this.aiLevel = iaDuringNight.getOrDefault(0, 0);
        this.iaDuringNight = iaDuringNight;
        this.secInterval = secInterval;
        this.maxIaLevel = maxIaLevel;
        this.camImg = Resources.loadImageResource(camImgPath);
        this.forbiddenCameras = Objects.requireNonNullElseGet(forbiddenCameras, () -> new ArrayList<>(0));
        jumpscare = new Jumpscare(jumpscareGifPath, jumpscareRepFrames);
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
     * By default, Animatronics will only move to cameras connected with the current one,
     * and to a randomly decided one between them with equal probability.
     * Animatronics must override this method if they move differently.
     * @param map Entire map, with all Cams.
     * @param currentLoc, Cam where this Animatronic is.
     * @param rng Random in charge of today's night.
     * @return The name of the Camera it has to move to. The Night will be in charge of
     * trying to move the Animatronic to the indicated Camera, connected or not.
     */
    public String onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng){
        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        return connections.get(rng.nextInt(connections.size()));
    }

    /**
     * This is only called at the moment of the defined internal during any given Night.
     * AI = 0 will disable movement unless this method is overriten by an Animatronic to do so.
     *
     * @param cam        Current camera from which the Animatronic is trying to move.
     * @param isOpenDoor If there is a door to the Office from the current Camera and it is open.
     * @param rng        Random in charge of today's night.
     * @return True if Animatronic should move at the end of this tick.
     */
    public boolean onMovementOpportunityAttempt(Camera cam, boolean isOpenDoor, Random rng){
        return rng.nextInt(maxIaLevel) < aiLevel;
    }

    /**
     * This method should be implemented per animatronic. This is called on every single tick,
     * even if Animatronic is not at a door. For an example implementation see
     * {@link Bob#onJumpscareAttempt(int, int, boolean, boolean, Camera, Random)} where
     * he waits some time at the door and if it is open after some time it kills on next cams down.
     *
     * @param tick     Current tick.
     * @param fps      FPS for the current night. They are a constant throught the night.
     * @param camsUp   If cams are up on this tick (this changes as soon as the Player clicks,
     *                on the first frame of the transition)
     * @param openDoor If there is a door to the Office from the current Camera and it is open.
     * @param cam      Current Camera where the Animatronic is.
     * @param rng      Random in charge of today's night.
     * @return true if Animatronic will kill Player on this tick. false otherwise.
     */
    public abstract boolean onJumpscareAttempt(int tick, int fps, boolean camsUp,
                                               boolean openDoor, Camera cam, Random rng);

    /**
     * This determines whether the Animatronic should appear on a camera or not. Just some flavor.
     * This is called per tick, but there is a counter that is unique to each instance of cams, which
     * increases each time the player changes cams an each time they open cams.
     *
     * @param tick     Current tick, for accuraetly counting seconds.
     * @param fps      Current ticks per second, to convert from ticks to seconds for consistency with real time.
     * @param openDoor If there is a door to the Office from the current Camera and it is open.
     * @param cam      Current Camera where the Animatronic is and the player is watching.
     * @param rng      Random in charge of today's night.
     * @return True if this Animatronic should not be drawn in the Camera. False otherwise
     */
    public abstract boolean showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng);

    public Jumpscare getJumpscare() {
        return jumpscare;
    }

    public BufferedImage getCamImg() {
        return camImg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return name.equals(((Animatronic) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, aiLevel, camImg);
    }
}
