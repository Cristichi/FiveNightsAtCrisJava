package es.cristichi.fnac.cnight;

import es.cristichi.fnac.anim.AnimatronicDrawing;
import es.cristichi.fnac.cams.CameraMapFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A registry for managing custom {@link AnimatronicDrawing} for Custom Nights.
 */
public class CustomNightMapRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomNightMapRegistry.class);

    /**
     * Map holding the instances of all registered factories.
     */
    private static final LinkedList<CameraMapFactory> customNightMapRegistry = new LinkedList<>();
    
    /**
     * Registers a map to be available for Custom Nights.
     * @param factory Factory that generates the map.
     */
    public static void registerMap(CameraMapFactory factory) {
        if (customNightMapRegistry.contains(factory)){
            LOGGER.warn("Factory %s is already registered.".formatted(factory.getClass().getName()));
        } else {
            LOGGER.debug("Factory %s registered for Custom Night.".formatted(factory.getClass().getName()));
            customNightMapRegistry.add(factory);
        }
    }
    
    /**
     * @return A copy of the currently registered maps.
     */
    public static List<CameraMapFactory> getCustomNightMapRegistry() {
        return Collections.unmodifiableList(customNightMapRegistry);
    }
}
