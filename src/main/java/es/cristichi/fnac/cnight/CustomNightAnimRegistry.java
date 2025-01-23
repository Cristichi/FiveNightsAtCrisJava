package es.cristichi.fnac.cnight;

import es.cristichi.fnac.anim.AnimatronicDrawing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A registry for managing custom {@link AnimatronicDrawing} for Custom Nights.
 */
public class CustomNightAnimRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomNightAnimRegistry.class);
    /**
     * Shared instance of {@link CustomNightAnimFactComparator} to use to reorder the factories.
     */
    public static final CustomNightAnimFactComparator NAME_COMPARATOR = new CustomNightAnimFactComparator();
    
    /**
     * Map holding the instances of all registered factories.
     */
    private static final ArrayList<CustomNightAnimFactory<? extends AnimatronicDrawing>> customNightAnimRegistry
            = new ArrayList<>(10);
    
    
    /**
     * Registers a map to be available for Custom Nights.
     * @param factory Factory that generates the map.
     */
    public static synchronized void registerAnimatronic(CustomNightAnimFactory<? extends AnimatronicDrawing> factory) {
        if (customNightAnimRegistry.contains(factory)){
            LOGGER.warn("Animatronic factory %s is already registered.".formatted(factory.getNameId()));
        } else {
            LOGGER.debug("Animatronic factory %s registered for Custom Night.".formatted(factory.getNameId()));
            customNightAnimRegistry.add(factory);
        }
    }
    
    /**
     * @return A copy of the currently registered maps.
     */
    public static synchronized List<CustomNightAnimFactory<? extends AnimatronicDrawing>> getEntries() {
        return new ArrayList<>(customNightAnimRegistry);
    }
}
