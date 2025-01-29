package es.cristichi.fnac.nights;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(NightRegistry.class);
    
    private static final ArrayList<NightFactory> registry = new ArrayList<>(7);
    
    /**
     * It registers the {@link NightFactory} for the Night that is available as indicated by itself. Its menu item
     * must have a unique ID, otherwise an error is thrown to allow "replacing" the default Nights by mods.
     *
     * @param nightFactory            The NightFactory that can create the correct next Night for the player to play.
     *                                A {@code null} value indicates Custom Night.
     *
     * @throws IllegalArgumentException If a NightFactory with the same menu item ID already exists.
     */
    public static synchronized void registerNight(NightFactory nightFactory){
        for (NightFactory factory : registry){
            if (factory.equals(nightFactory)){
                throw new IllegalArgumentException("There cannot be more than one Night with the menu id \"%s\"."
                        .formatted(nightFactory.getItem().id()));
            }
        }
        registry.add(nightFactory);
        LOGGER.debug("Night with menu item id \"{}\" has been registered.", nightFactory.getItem().id());
    }
    
    /**
     * @return An ordered, unmodifiable {@link List<NightFactory>} of all Nights registered.
     */
    public static List<NightFactory> getAllNights(){
        return Collections.unmodifiableList(registry);
    }
}
