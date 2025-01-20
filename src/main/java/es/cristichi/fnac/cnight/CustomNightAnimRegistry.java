package es.cristichi.fnac.cnight;

import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A registry for managing custom {@link AnimatronicDrawing} for Custom Nights.
 */
public class CustomNightAnimRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomNightAnimRegistry.class);

    /**
     * Map holding the instances of all registered factories.
     */
    private static final ArrayList<CustomNightAnimFactory<? extends AnimatronicDrawing>> customNightAnimRegistry
            = new ArrayList<>(10);
    
    private static final CustomNightAnimFactComparator comparator = new CustomNightAnimFactComparator();
    
    /**
     * Registers a map to be available for Custom Nights.
     * @param factory Factory that generates the map.
     */
    public static void registerAnimatronic(CustomNightAnimFactory<? extends AnimatronicDrawing> factory) {
        if (customNightAnimRegistry.contains(factory)){
            LOGGER.warn("Animatronic factory %s is already registered.".formatted(factory.getClass().getName()));
        } else {
            LOGGER.debug("Animatronic factory %s registered for Custom Night.".formatted(factory.getClass().getName()));
            customNightAnimRegistry.add(factory);
            customNightAnimRegistry.sort(comparator);
        }
    }
    
    /**
     * @return A copy of the currently registered maps.
     */
    public static List<CustomNightAnimFactory<? extends AnimatronicDrawing>> getEntries() {
        return Collections.unmodifiableList(customNightAnimRegistry);
    }
}
