package es.cristichi.fnac.nights;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This registry registers each Night in the order they must appear. The index of each Night represents the number of
 * previous Nights the player must have completed (exactly: no more no less) in order to play that one. <br>
 * This registry never contains Custom Night, which is a special kind of Night that appears when no Night here meets
 * the requisite of completed Nights.
 */
public class NightRegistry {
    private static final Logger logger = LoggerFactory.getLogger(NightRegistry.class);
    
    private static final ArrayList<NightFactory> registry = new ArrayList<>(7);
    
    /**
     * It registers athe {@link NightFactory} for the Night that is available with the given exact
     * number of completed Nights. If a Night with the same requirement of completed Nights exists, this does nothing.
     * @param requiredCompletedNights Exact number of completed Nights the player must have in their save file to
     *                                have this Night be the next one to play.
     * @param nightFactory The NightFactory that can create the correct next Night for the player to play.
     */
    public static void registerNight(int requiredCompletedNights, NightFactory nightFactory){
        if (requiredCompletedNights < 0){
            throw new IllegalArgumentException("There cannot be less than 0 completed Nights.");
        }
        if (requiredCompletedNights < registry.size()){
            logger.info("Night for {} completed Nights has been registered.", requiredCompletedNights);
            registry.add(requiredCompletedNights, nightFactory);
        } else {
            logger.info("Night for {} completed Nights was already set.", requiredCompletedNights);
        }
    }
    
    /**
     * @param completedNights Number of completed Nights.
     * @return The factory for this number of completed Nights, or <code>null</code> if the player completed all Nights.
     */
    public static @Nullable NightFactory getNight(int completedNights){
        if (completedNights < 0 || completedNights>= registry.size()){
            return null;
        }
        return registry.get(completedNights);
    }
    
    /**
     * @return An ordered, unmodifiable {@link List<NightFactory>} of all Nights registered.
     */
    public static List<NightFactory> getAllNights(){
        return Collections.unmodifiableList(registry);
    }
}
