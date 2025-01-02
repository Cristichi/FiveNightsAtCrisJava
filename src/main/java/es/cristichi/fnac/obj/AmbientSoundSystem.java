package es.cristichi.fnac.obj;

import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is a simple object to get random Sounds at random times.
 */
public class AmbientSoundSystem extends ArrayList<AmbientSound> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmbientSoundSystem.class);
    /**
     * Tick interval, ignoring FPS.
     */
    private final int tickInterval;
    /**
     * Each time the interval is met, the chances an ambient Sound should play.
     */
    private final float chancePerOpp;
    /** This value keeps track of whether the last AmbientSound is currently playing. */
    private boolean currentlyPlaying;
    
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
        currentlyPlaying = false;
    }
    
    /**
     * Checks if there should be an attempt to play an ambient sound at this tick and attempts it with the given Random.
     * If it success, it picks one Sound at random with their weights as chances. Then it checks if the chose Sound
     * should be played on a Camera. If it does, it picks one at random and plays it there. Otherwise it just plays
     * it with the default volume and pan.
     * @param rng Random for the Night.
     * @param tick Current tick of the Night.
     * @param map Map of the Night to pick the random Camera if needed.
     */
    public void attemptRandomSound(Random rng, int tick, CameraMap map){
        if (tick%tickInterval==0){
            if (currentlyPlaying){
                LOGGER.debug("AmbientSound attempt failed because Sound is playing.");
                return;
            }
            if (rng.nextFloat()<chancePerOpp){
                LOGGER.debug("AmbientSound attempt successful.");
                // Calculate total weight
                int totalWeight = this.stream()
                        .mapToInt(AmbientSound::getWeight)
                        .sum();
                
                if (totalWeight == 0){
                    LOGGER.warn("Sounds have total weight 0. {}",
                            size()==0?"There are no ambient Sounds.":"All ambient Sounds have weight 0!");
                    return;
                }
                // Generate a random number between 0 (inclusive) and totalWeight (exclusive)
                int randomWeight = rng.nextInt(totalWeight);
                
                // Select an AmbientSound based on the random weight
                AmbientSound selectedAmSound = null;
                int cumulativeWeight = 0;
                
                for (AmbientSound ambientSound : this) {
                    cumulativeWeight += ambientSound.getWeight();
                    if (randomWeight < cumulativeWeight) {
                        selectedAmSound = ambientSound;
                        break;
                    }
                }
                if (selectedAmSound == null){
                    LOGGER.warn("AmbientSoundSystem is using no elegible Sounds!");
                    return;
                }
                selectedAmSound.getSound().addOnEndListener(() -> currentlyPlaying = false);
                currentlyPlaying = true;
                if (selectedAmSound.shouldPlayOnCams()){
                    ArrayList<Camera> values = new ArrayList<>(map.values());
                    selectedAmSound.play(values.get(rng.nextInt(values.size())));
                } else {
                    selectedAmSound.play();
                }
            } else {
                LOGGER.debug("AmbientSound attempt failed.");
            }
        }
    }
}
