package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AssetNotFound;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public abstract class Animatronic {
    private final Jumpscare jumpscare;
    private final String name;
    private final int maxIaLevel;
    private int aiLevel;
    private final HashMap<Integer, Integer> iaDuringNight;
    private final BufferedImage camImg;
    private final double secInterval;
    private final Color debugColor;

    public Animatronic(String name, double secInterval, HashMap<Integer, Integer> iaDuringNight, int maxIaLevel, BufferedImage img, String jumpscareGif, Color debugColor) throws AssetNotFound {
        this.name = name;
        this.aiLevel = iaDuringNight.getOrDefault(0, 0);
        this.iaDuringNight = iaDuringNight;
        this.secInterval = secInterval;
        this.maxIaLevel = maxIaLevel;
        this.camImg = img;
        this.debugColor = debugColor;
        jumpscare = new Jumpscare(jumpscareGif, 20);
    }

    public String getName() {
        return name;
    }

    public double getSecInterval() {
        return secInterval;
    }

    public Color getDebugColor() {
        return debugColor;
    }

    public void updateIADuringNight(int time){
        aiLevel = iaDuringNight.getOrDefault(time, aiLevel);
    }

    /**
     * Animatronics may override this method if they move on specific paths.
     * @param map Entire map, with all Cams
     * @param currentLoc, Cam where this Animatronic is
     * @param rng Random ni charge of today's night
     * @return The Camera it has to move to.
     */
    public Camera onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng){
        return currentLoc.getConnections().get(rng.nextInt(currentLoc.getConnections().size()));
    }

    /**
     * This is only called at the moment of the defined internal during any given Night. AI = 0 will disable movement unless this method is overriten by an Animatronic to do so.
     * @param rng Random in charge of today's night.
     * @return True if Animatronic should move at the end of this tick.
     */
    public boolean onMovementOpportunityAttempt(Random rng){
        return rng.nextInt(maxIaLevel) <= aiLevel;
    }

    /**
     * This method should be implemented per animatronic. This is called on every single tick, even if Animatronic is not at a door. For an example implementation see {@link Bob} where he waits some time at the door and if it is open after some time it kills on next cams down.
     * @param tick Current tick.
     * @param openDoor If there is a door to the Office and it's open
     * @param camsUp If cams are up on this tick (this changes as soon as the Player clicks, on the first frame of the transition)
     * @param cam Current Camera where the Animatronic is
     * @param rng Random in charge of today's night.
     * @param fps FPS for the current night. They are a constant throught the night.
     * @return true if Animatronic will kill Player on this tick. false otherwise.
     */
    public abstract boolean onJumpscareAttempt(int tick, boolean openDoor, boolean camsUp, Camera cam, Random rng, int fps);

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
