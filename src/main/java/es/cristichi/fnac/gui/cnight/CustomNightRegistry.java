package es.cristichi.fnac.gui.cnight;

import es.cristichi.fnac.exception.CustomNightException;
import es.cristichi.fnac.gui.ExceptionDialog;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronicData;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A registry for managing custom {@link AnimatronicDrawing} for Custom Nights.
 */
public class CustomNightRegistry {
    private static final Logger LOGGER = Logger.getLogger(CustomNightRegistry.class.getName());

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
            LOGGER.warning("Package already registered: " + packageName);
            return;
        }
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(CustomNightAnimatronic.class);
        if (classes.isEmpty()){
            LOGGER.warning(("Package \"%s\" does not exist or contains no AnimatronicDrawings annotated with " +
                    "@CustomNightAnimatronic.").formatted(packageName));
        }
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(CustomNightAnimatronic.class)
                    && AnimatronicDrawing.class.isAssignableFrom(clazz)) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends AnimatronicDrawing> animatronicClass = (Class<? extends AnimatronicDrawing>) clazz;
                    CustomNightAnimatronic annotation = clazz.getAnnotation(CustomNightAnimatronic.class);
                    animatronicRegistry.put(annotation, animatronicClass);
                } catch (ClassCastException e){
                    new ExceptionDialog(new CustomNightException(
                            "Error trying to get the Animatronic class %s with the annotation."
                                    .formatted(clazz.getName()), e), true, false);
                }
            }
        }
        packageNames.add(packageName);
    }

    /**
     * @return An unmodifiable collection of all AnimatronicDrawing classes and their specified
     * information in the annotation {@link CustomNightAnimatronic}.
     */
    public static synchronized Collection<Map.Entry<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>>> getAnimatronics() {
        return Collections.unmodifiableCollection(animatronicRegistry.entrySet());
    }
    
    // Create an instance of a specific animatronic
    protected static AnimatronicDrawing createInstance(CustomNightAnimatronic selected, CustomNightAnimatronicData data)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<? extends AnimatronicDrawing> animatronicClass = animatronicRegistry.get(selected);
        if (animatronicClass != null) {
            try {
                return animatronicClass.getDeclaredConstructor(CustomNightAnimatronicData.class).newInstance(data);
            }catch (NoSuchMethodException e){
                new ExceptionDialog(new CustomNightException(("AnimatronicDrawing %s could not be created because it's " +
                        "missing a constructor with only CustomNightAnimatronicData.").formatted(animatronicClass.getName()), e),
                        true, false);
                return null;
            }
        }
        throw new IllegalArgumentException("No AnimatronicDrawing was found in Registry with the given data: %s (%s)"
                        .formatted(selected.name(), selected.variant().isEmpty() ? "default" : selected.variant()));
    }
    
    public static int size() {
        return animatronicRegistry.size();
    }
}
