package es.cristichi.fnac.loading;

import es.cristichi.fnac.io.Settings;

/**
 * Class to allow mods to load stuff after the player's Settings have been loaded. This allows the method to be run
 * after the loading startup screen is shown to the player, and make sure that it is completed before starting the
 * main menu.
 */
public interface RunnableWithSettings {
    /**
     * Method to run after the player's Settings have been loaded.
     * @param settings Already loaded player's Settings.
     */
    void run(Settings settings);
}
