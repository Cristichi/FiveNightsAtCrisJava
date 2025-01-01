package es.cristichi.fnac.obj;

import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is a simple object to get random Sounds at random times.
 */
public class AmbientSoundSystem extends ArrayList<AmbientSound> {
    /**
     * Tick interval, ignoring FPS.
     */
    private final int tickInterval;
    /**
     * Each time the interval is met, the chances an ambient Sound should play.
     */
    private final float chancePerOpp;
    
    /**
     * Creates a new System that manages the list of Sounds and checks when one should be played.
     * @param tickInterval Interval between checks. It should keep in mind the FPS for the Night so that different
     *                     FPSs do not result in different real-time intervals.
     * @param sounds List of ambient Sounds.
     */
    public AmbientSoundSystem(int tickInterval, float chancePerOpp, AmbientSound... sounds){
        super(List.of(sounds));
        this.tickInterval = tickInterval;
        this.chancePerOpp = chancePerOpp;
    }
    
    /**
     * Checks if this tick should have an ambient Sound. If it does, it randomly chooses one and plays it. If it
     * should be played on a Camera, it picks one at random (including the left/right doors, since they are Cameras).
     * @param rng Random for the Night.
     * @param tick Current tick of the Night.
     * @param map Map of the Night to pick the random Camera if needed.
     */
    public void attemptRandomSound(Random rng, int tick, CameraMap map){
        if (tick%tickInterval==0 && rng.nextFloat()<chancePerOpp){
            AmbientSound sound = get(rng.nextInt(size()));
            if (sound.shouldPlayOnCams()){
                ArrayList<Camera> values = new ArrayList<>(map.values());
                sound.attemptPlay(rng, values.get(rng.nextInt(values.size())));
            } else {
                sound.attemptPlay(rng);
            }
        }
    }
}
