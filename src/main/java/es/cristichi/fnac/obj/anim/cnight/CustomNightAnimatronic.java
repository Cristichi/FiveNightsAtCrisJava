package es.cristichi.fnac.obj.anim.cnight;

import es.cristichi.fnac.obj.anim.AnimatronicDrawing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to tell the Custom Night data about this Animatronic, and which constructor to use, while
 * {@link es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronicData} is used to allow the Custom Night to tell
 * the Animatronic the data for a specific user's configured Custom Night.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomNightAnimatronic {
    String name();
    String variant() default "";
    int maxAi() default AnimatronicDrawing.GENERIC_MAX_AI;
    String portraitPath();
    /** Name of the Tutorial's Camera where this Animatronic starts in Custom Nights. */
    String tutStart() default "cam1";
    /** Name of the Restaurant's Camera where this Animatronic starts in Custom Nights. */
    String restStart() default "dining area";
}
