package es.cristichi.fnac.cnight;

import es.cristichi.fnac.obj.anim.AnimatronicDrawing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to tell the Custom Night data about this Animatronic, and which constructor to use, while
 * {@link CustomNightAnimatronicData} is used to allow the Custom Night to tell
 * the Animatronic the data for a specific user's configured Custom Night.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomNightAnimatronic {
    /**
     * @return Name of the AnimatronicDrawing.
     */
    String name();
    
    /**
     * @return Variant of the AnimatronicDrawing. This is to differenciate two Animatronics with the same name
     * but different behaviours. Also for simple clones so their name doesn't match the original or other clones.
     */
    String variant() default "";
    
    /**
     * @return Max AI the player should be allowed to configure for this AnimatronicDrawing.
     */
    int maxAi() default AnimatronicDrawing.GENERIC_MAX_AI;
    
    /**
     * @return The path to the resource where the portrait for the Custom Night menu is.
     */
    String portraitPath();
    
    /**
     * @return Array of starting positions. A Night will check the first one, and if a Camera does not exists with that
     * name it will check the next one.
     */
    String[] starts() default {"dining area", "cam1"};
    
    /**
     * @return Description of how this Animatronic behaves at the Tutorial.
     */
    String description();
}
