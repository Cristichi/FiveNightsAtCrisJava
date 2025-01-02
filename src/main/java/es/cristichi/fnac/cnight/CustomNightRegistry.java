package es.cristichi.fnac.cnight;

import es.cristichi.fnac.exception.CustomNightException;
import es.cristichi.fnac.gui.ExceptionDialog;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A registry for managing custom {@link AnimatronicDrawing} for Custom Nights.
 */
public class CustomNightRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomNightRegistry.class);

    /**
     * Map holding registered animatronic drawings by their class names.
     */
    private static final Map<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>> animatronicRegistry = new HashMap<>();

    /**
     * Set of package names to avoid redundant scanning.
     */
    private static final Set<String> packageNames = new HashSet<>();

    /**
     * Registers all {@link AnimatronicDrawing} classes annotated with {@link CustomNightAnimatronic} in
     * the specified package and all its subpackages.<br><br>
     * It logs a warning if the package was already scanned (and skips the scanning).<br>
     * It logs a warning if the package contains no {@link AnimatronicDrawing} classes annotated with
     * {@link CustomNightAnimatronic} or if the package does not exists.
     *
     * @param packageName The name of the package to scan for AnimatronicDrawing classes annotated with
     * {@link CustomNightAnimatronic}.
     */
    public static synchronized void registerPackage(String packageName) {
        if (packageNames.contains(packageName)) {
            LOGGER.warn("Package already registered: {}", packageName);
            return;
        }
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(CustomNightAnimatronic.class);
        if (classes.isEmpty()){
            LOGGER.warn("Package \"{}\" does not exist or contains no AnimatronicDrawings annotated " +
                            "with @CustomNightAnimatronic.", packageName);
        }
        int count = 0;
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(CustomNightAnimatronic.class)
                    && AnimatronicDrawing.class.isAssignableFrom(clazz)) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends AnimatronicDrawing> animatronicClass = (Class<? extends AnimatronicDrawing>) clazz;
                    CustomNightAnimatronic annotation = clazz.getAnnotation(CustomNightAnimatronic.class);
                    animatronicRegistry.put(annotation, animatronicClass);
                    count++;
                } catch (Exception e){
                    new ExceptionDialog(new CustomNightException("Oops! Error trying to create the Animatronic.", e),
                            false, true, null);
                    LOGGER.error("Class {} has an issue with the @CustomNightAnimatronic annotation.",
                            clazz.getName(), e);
                }
            }
        }
        LOGGER.debug("Package \"%s\" registered with \"%d\" found AnimatronicDrawings.".formatted(packageName,  count));
        packageNames.add(packageName);
    }
    
    /**
     * @return Amount of registered {@link AnimatronicDrawing} classes.
     */
    public static int size() {
        return animatronicRegistry.size();
    }

    /**
     * @return An unmodifiable collection of all AnimatronicDrawing classes and their specified
     * information in the annotation {@link CustomNightAnimatronic}.
     */
    public static synchronized Collection<Map.Entry<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>>> getAnimatronics() {
        return Collections.unmodifiableCollection(animatronicRegistry.entrySet());
    }
}
