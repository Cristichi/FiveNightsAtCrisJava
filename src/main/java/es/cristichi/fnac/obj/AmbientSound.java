package es.cristichi.fnac.obj;

import es.cristichi.fnac.obj.cams.Camera;
import kuusisto.tinysound.Sound;

import java.util.Random;

public class AmbientSound {
    private final float chance;
    private final Sound sound;

    public AmbientSound(float chance, Sound sound){
        this.chance = chance;
        this.sound = sound;
    }

    public void attemptPlayOnCamera(Random rng, Camera camera){
        if (rng.nextFloat() < chance){
            camera.playSoundHere(sound);
        }
    }
}
