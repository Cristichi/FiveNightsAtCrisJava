package es.cristichi.fnac.obj.cnight;

import es.cristichi.fnac.obj.anim.AnimatronicDrawing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to tell the Custom Night data about this Animatronic, and which constructor to use, while
 * {@link es.cristichi.fnac.obj.cnight.CustomNightAnimatronicData} is used to allow the Custom Night to tell
 * the Animatronic the data for a specific user's configured Custom Night.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomNightAnimatronic {
    /** @return Name of the AnimatronicDrawing. */
    String name();
    /** @return Variant of the AnimatronicDrawing. This is to differenciate two Animatronics with the same name
     * but different behaviours. Also for simple clones so their name doesn't match the original or other clones. */
    String variant() default "";
    /** @return Max AI the player should be allowed to configure for this AnimatronicDrawing. */
    int maxAi() default AnimatronicDrawing.GENERIC_MAX_AI;
    String portraitPath();
    /** @return Name of the Tutorial's Camera where this Animatronic starts in Custom Nights. */
    String tutStart() default "cam1";
    /** @return Name of the Restaurant's Camera where this Animatronic starts in Custom Nights. */
    String restStart() default "dining area";
    /** @return Description of how this Animatronic behaves at the Tutorial. */
    String restDesc();
    /** @return Description of how this Animatronic behaves at the Restaurant. */
    String tutDesc();
}
