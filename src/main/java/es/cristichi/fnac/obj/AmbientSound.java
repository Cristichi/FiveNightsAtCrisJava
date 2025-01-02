package es.cristichi.fnac.obj;

import es.cristichi.fnac.obj.cams.Camera;
import kuusisto.tinysound.Sound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This represents a Sound, with a chance to being played.
 */
public class AmbientSound {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmbientSound.class);
    /** Sound of this AmbientSound. */
    private final Sound sound;
    /** Weight of this Sound, which is the chances this Sound is picked when an AmbientSound is to play. */
    private final int weight;
    /** Whether this Sound should play on a Camera. Otherwise, it's played at the Office (where the player is). */
    private final boolean playOnCams;
    
    /**
     * Creates an Ambient Sound.
     *
     * @param sound      The Sound.
     * @param weight     Weight of this Sound, which is the chances this Sound is picked when an AmbientSound is to play.
     * @param playOnCams Whether this Sound should play on a Camera. Otherwise, it's played at the Office
     *                   (where the player is).
     */
    public AmbientSound(Sound sound, int weight, boolean playOnCams){
        this.sound = sound;
        this.weight = weight;
        this.playOnCams = playOnCams;
        if (weight <= 0){
            LOGGER.warn("A Sound was registered with a weight ({}) that results in it being never used. " +
                    "If the Sound should be removed, try removing it from the AmbientSoundSystem in use.", weight);
        }
    }
    
    /**
     * @return The Sound.
     */
    public Sound getSound() {
        return sound;
    }
    
    /**
     * @return The weight of this Sound, which should determine the chances of being picked.
     */
    public int getWeight() {
        return weight;
    }
    
    /**
     * @return Whether this Sound should play on a Camera. Otherwise, it's played at the Office (where the player is).
     */
    public boolean shouldPlayOnCams() {
        return playOnCams;
    }
    
    /**
     * Plays the sound normally. This does not check if this {@link AmbientSound} should be
     * played on a Camera.
     */
    public void play(){
        sound.play();
    }
    
    /**
     * Plays this Sound at the given Camera. This does not check if this
     * {@link AmbientSound} should be played on a Camera.
     * @param camera Camera to play the Sound.
     */
    public void play(Camera camera){
        camera.playSoundHere(sound);
    }
}
