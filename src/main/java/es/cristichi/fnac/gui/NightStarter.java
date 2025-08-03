package es.cristichi.fnac.gui;

import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.nights.NightFactory;

import java.io.IOException;

/**
 * Allows different objects to start Nights, and allow methods that only want to start Nights to call only this method
 * from the objects.
 */
public interface NightStarter {
    /**
     * Starts a Night from its Factory.
     * @param nightFactory Night factory that will load the Night.
     * @throws IOException If the Night cannot load a specific resource from disk.
     * @throws NightException If the Night encounters another issue during loading.
     */
    void startNightFromFactory(NightFactory nightFactory) throws IOException, NightException;
    /**
     * Starts a Custom Night.
     * @param night Information about the Night to start.
     */
    void startCustomNight(NightJC night);
}
