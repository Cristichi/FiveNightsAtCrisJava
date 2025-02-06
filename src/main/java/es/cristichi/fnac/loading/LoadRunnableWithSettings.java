package es.cristichi.fnac.loading;

import es.cristichi.fnac.io.Settings;

/**
 * Class to allow mods to load stuff after the player's Settings have been loaded. This allows the method to be run
 * after the loading startup screen is shown to the player, and make sure that it is completed before starting the
 * main menu.
 */
public interface LoadRunnableWithSettings {
    /**
     * Method to run after the player's Settings have been loaded.
     * @param settings Already loaded player's Settings.
     * @throws Exception Any exception that requires the game to cancel loading and show it to the player.
     */
    @SuppressWarnings("RedundantThrows")
    void run(Settings settings) throws Exception;
}
