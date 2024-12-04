package es.cristichi.fnac.obj;

import es.cristichi.fnac.obj.cams.Camera;
import kuusisto.tinysound.Sound;

import java.util.Random;

public class AmbientSound {
    private final Sound sound;
    private final float chance;
    private final boolean playOnCams;

    public AmbientSound(float chance, Sound sound){
        this.chance = chance;
        this.sound = sound;
        this.playOnCams = false;
    }

    public AmbientSound(float chance, boolean playOnCams, Sound sound){
        this.chance = chance;
        this.sound = sound;
        this.playOnCams = playOnCams;
    }

    public boolean shouldPlayOnCams() {
        return playOnCams;
    }

    public void attemptPlay(Random rng){
        if (rng.nextFloat() < chance){
            sound.play();
        }
    }

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
