package es.cristichi.fnac.loading;

import es.cristichi.fnac.io.NightProgress;

/**
 * Class to allow mods to load stuff after the player's Save File have been loaded. This allows the method to be run
 * after the loading startup screen is shown to the player, and make sure that it is completed before starting the
 * main menu.
 */
public interface LoadRunnableWithSaveFile {
    /**
     * Method to run after the player's Save File have been loaded.
     * @param saveFile Already loaded player's Save File.
     * @throws Exception Any exception that requires the game to cancel loading and show it to the player.
     */
    @SuppressWarnings("RedundantThrows")
    void run(NightProgress.SaveFile saveFile) throws Exception;
}
