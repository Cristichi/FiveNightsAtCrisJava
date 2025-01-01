package es.cristichi.fnac.obj;

import es.cristichi.fnac.obj.cams.Camera;
import kuusisto.tinysound.Sound;

import java.util.Random;

/**
 * This represents a Sound, with a chance to being played.
 */
public class AmbientSound {
    /** Sound of this AmbientSound. */
    private final Sound sound;
    /** When this Sound is selected, chance it will play. */
    private final float chance;
    /** Whether this Sound should play on a Camera. Otherwise, it's played at the Office (where the player is). */
    private final boolean playOnCams;
    
     /**
     * Creates an Ambient Sound.
     * @param chance When this Sound is selected, chance it will play.
     * @param playOnCams Whether this Sound should play on a Camera. Otherwise, it's played at the Office
      *                   (where the player is).
     * @param sound The Sound.
     */
    public AmbientSound(float chance, boolean playOnCams, Sound sound){
        this.chance = chance;
        this.sound = sound;
        this.playOnCams = playOnCams;
    }
    
    /**
     * @return Whether this Sound should play on a Camera. Otherwise, it's played at the Office (where the player is).
     */
    public boolean shouldPlayOnCams() {
        return playOnCams;
    }
    
    /**
     * Checks the chance and plays the Sound if so. This does not check if this {@link AmbientSound} should be
     * played on a Camera.
     * @param rng Random for the Night.
     */
    public void attemptPlay(Random rng){
        if (rng.nextFloat() < chance){
            sound.play();
        }
    }
    
    /**
     * Checks the chance and plays the Sound at the given Camera if so. This does not check if this
     * {@link AmbientSound} should be played on a Camera.
     * @param rng Random for the Night.
     * @param camera Camera to play the Sound.
     */
    public void attemptPlay(Random rng, Camera camera){
        if (rng.nextFloat() < chance){
            if (playOnCams){
                camera.playSoundHere(sound);
            } else {
                sound.play();
            }
        }
    }
}
