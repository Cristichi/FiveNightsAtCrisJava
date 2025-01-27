package es.cristichi.fnac.io;

import es.cristichi.fnac.gui.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An instance of Settings has all the player preferences that are required by the game.
 */
public class Settings {
    private static String pathToFnacFolder;
    private static Yaml yaml;
    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);
    
    /**
     * This method must be called in order to find the Documents folder of the computer's user.
     * Also, it sets some options to make the generated YAML files pretty and ordered for humans to
     * look and modify manually if needed.
     * @param folderName Name of the folder inside the Documents folder.
     */
    public static void init(String folderName){
        pathToFnacFolder = "%s/Documents/%s/".formatted(System.getProperty("user.home"), folderName);
        new File(pathToFnacFolder).mkdirs();

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer orderedRepresenter = new Representer(options);
        orderedRepresenter.setPropertyUtils(new OrderedPropertyUtils());
        yaml = new Yaml(orderedRepresenter, options);
    }
    
    /**
     * Usual name for Settings files.
     */
    public static final String SETTINGS_FILE = "settings.yaml";

    /**
     * You need to call {@link Settings#init(String)} one anywhere else before this method can work.
     * Retrieves the Settings saved on the specifiled file the filePath is pointing to. If it does not
     * exist, it will create a new one with the default values.
     * @param filePath Path to file, from the user's Documents folder, and name.
     * @return Either a new {@link Settings}, or one filled with the values saved on the specified file.
     * @throws IOException If file exists but an error happens trying to read it.
     */
    public static Settings fromFile(String filePath) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(pathToFnacFolder+filePath)) {
            Settings settings = new Settings();
            Map<String, Object> config = yaml.load(inputStream);
            settings.setFullscreen((Boolean) config.get("fullscreen"));
            settings.setFps((Integer) config.get("fps"));
            settings.setVolume((Double)config.get("volume"));

            return settings;
        } catch (FileNotFoundException notFound){
            return new Settings();
        } catch (Exception exception){
            throw new IOException("Error trying to read the configuration filePath \""+filePath+"\".", exception);
        }
    }

    private boolean fullscreen = true;
    private int fps = 60;
    private double volume = 1;

    /**
     * Creates a new {@link Settings} with the default values (fullscreen=true, fps=60, volume=1).
     */
    public Settings() {
    }

    /**
     * Creates a new {@link Settings} copying the values of another one.
     * @param copy Instance to copy.
     */
    public Settings(Settings copy) {
        this();
        setFullscreen(copy.fullscreen);
        setFps(copy.fps);
        setVolume(copy.volume);
    }

    /**
     * @return Whether or not screen should be fullscreen.
     */
    public boolean isFullscreen() {
        return fullscreen;
    }

    /**
     * @return Number of target frames per second. It is used for converting real time to in-game frames.
     *            Not recommended to have more than the system can handler or less than 5.
     */
    public int getFps() {
        return fps;
    }

    /**
     * @return Desired global volume, where 1 = 100% and 0 = 0%.
     */
    public double getVolume() {
        return volume;
    }

    /**
     * @param fullscreen Whether or not screen should be fullscreen.
     */
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    /**
     * @param fps Number of target frames per second. It is used for converting real time to in-game frames.
     *            Not recommended to have more than the system can handler or less than 5.
     */
    public void setFps(int fps) {
        this.fps = fps;
    }

    /**
     * @param volume Desired global volume, where 1 = 100% and 0 = 0%.
     */
    public void setVolume(double volume) {
        this.volume = volume;
    }

    /**
     * You need to call {@link Settings#init(String)} one anywhere else before this method can work.
     * Saves the current values of this {@link Settings} object into the file in YAML format.
     * @param filePath Path to file, from the user's Documents folder, and name.
     */
    public void saveToFile(String filePath){
        try (Writer writer = new FileWriter(pathToFnacFolder+filePath)) {
            LinkedHashMap<String, Object> config = new LinkedHashMap<>(Map.of(
                    "fullscreen", fullscreen,
                    "fps", fps,
                    "volume", volume
            ));
            yaml.dump(config, writer);
        } catch (IOException e) {
            new ExceptionDialog(new IOException("Settings save filePath could not be saved.", e), false, false, LOGGER);
        }
    }
}
