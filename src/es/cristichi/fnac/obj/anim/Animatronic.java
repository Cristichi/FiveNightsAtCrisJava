package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AssetNotFound;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;

import java.awt.image.BufferedImage;
import java.util.*;

// Supressing unused warnings since some parts of this are for future Animatronics to use, even if the currently
// existing ones do not make use of it.
@SuppressWarnings("unused")
public abstract class Animatronic {
    private final String name;
    private final HashMap<Integer, Integer> iaDuringNight;
    private int aiLevel;
    private final int maxIaLevel;
    private final double secInterval;
    private final Jumpscare jumpscare;
    private final BufferedImage camImg;
    private final List<String> forbiddenCameras;

    public Animatronic(String name, double secInterval, HashMap<Integer, Integer> iaDuringNight, int maxIaLevel, BufferedImage img, String jumpscareGif, int jumpscareRepFrames, List<String> forbiddenCameras) throws AssetNotFound {
        this.name = name;
        this.aiLevel = iaDuringNight.getOrDefault(0, 0);
        this.iaDuringNight = iaDuringNight;
        this.secInterval = secInterval;
        this.maxIaLevel = maxIaLevel;
        this.camImg = img;
        this.forbiddenCameras = forbiddenCameras;
        jumpscare = new Jumpscare(jumpscareGif, jumpscareRepFrames);
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
     * @return The name of the Camera it has to move to. The Night will be in charge of trying to move the Animatronic.
     */
    public String onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng){
        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        return connections.get(rng.nextInt(connections.size()));
    }

    /**
     * This is only called at the moment of the defined internal during any given Night. AI = 0 will disable movement unless this method is overriten by an Animatronic to do so.
     * @param rng Random in charge of today's night.
     * @return True if Animatronic should move at the end of this tick.
     */
    public boolean onMovementOpportunityAttempt(Random rng){
        return rng.nextInt(maxIaLevel) < aiLevel;
    }

    /**
     * This method should be implemented per animatronic. This is called on every single tick, even if Animatronic is not at a door. For an example implementation see {@link Bob} where he waits some time at the door and if it is open after some time it kills on next cams down.
     * @param tick Current tick.
     * @param camsUp If cams are up on this tick (this changes as soon as the Player clicks, on the first frame of the transition)
     * @param cam Current Camera where the Animatronic is
     * @param openDoor If there is a door to the Office from the current Camera and it is open
     * @param rng Random in charge of today's night.
     * @param fps FPS for the current night. They are a constant throught the night.
     * @return true if Animatronic will kill Player on this tick. false otherwise.
     */
    public abstract boolean onJumpscareAttempt(int tick, boolean camsUp, Camera cam, boolean openDoor, Random rng, int fps);

    public abstract boolean hideFromCam(int tick, boolean openDoor, Camera cam, Random rng, int fps);

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
        Animatronic that = (Animatronic) o;
        return aiLevel == that.aiLevel && Objects.equals(name, that.name) && Objects.equals(camImg, that.camImg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, aiLevel, camImg);
    }
}
