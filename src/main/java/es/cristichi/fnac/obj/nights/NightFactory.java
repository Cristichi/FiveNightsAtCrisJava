package es.cristichi.fnac.obj.nights;

import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;

import java.io.IOException;
import java.util.Random;

/**
 * A {@link NightFactory} is simply an object capable of creating {@link NightJC} instances. This is needed since
 * creating a Night includes code to create the Animatronics and place them on the 
 * {@link es.cristichi.fnac.obj.cams.CameraMap} for their initial positions. For an example implementation, check
 * the source code of {@link Night1#createNight(Settings, Jumpscare, Random)}.
 */
public interface NightFactory {
    /**
     * @return All the information the instance of {@link MenuJC} must use for the button. Its only requirement is
     * that the ID is not one of the default ones written in the source code of the constructor for
     * {@link es.cristichi.fnac.gui.NightsJF}.*/
    MenuJC.Item getItem();
    
    /**
     *
     * @param settings The player's personal configuration.
     * @param powerOutage Jumpscare that should be thrown when the power is out unless a different one is used.
     * @param rng Random that MUST be used for the creation of the Night and the Night. For instance, if an
     *            {@link es.cristichi.fnac.obj.anim.AnimatronicDrawing} starts at a random location, this Random
     *            should be used to determine that location.
     * @return A Night, without starting it.
     * @throws IOException If an error happens when loading any resources, either when preparing it or during
     *                     its creation.
     * @throws NightException If an error happens that makes this NightJC impossible to create, such as having
     *                        0 seconds per hour.
     */
    NightJC createNight(Settings settings, Jumpscare powerOutage, Random rng) throws IOException, NightException;
}
