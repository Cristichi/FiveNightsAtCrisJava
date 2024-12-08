package es.cristichi.fnac.io;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Settings {
    private static Yaml yaml;
    public static void init(){
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer orderedRepresenter = new Representer(options);
        orderedRepresenter.setPropertyUtils(new OrderedPropertyUtils());
        yaml = new Yaml(orderedRepresenter, options);
    }

    public static final String SETTINGS_FILE = "settings.yaml";

    public static Settings fromFile(String file) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            Settings settings = new Settings();
            Map<String, Object> config = yaml.load(inputStream);
            settings.setFullscreen((Boolean) config.get("fullscreen"));
            settings.setFps((Integer) config.get("fps"));
            settings.setVolume((Double)config.get("volume"));

            return settings;
        } catch (FileNotFoundException notFound){
            return new Settings();
        } catch (Exception exception){
            throw new IOException("Error trying to read the configuration file \""+file+"\".", exception);
        }
    }

    private boolean fullscreen = true;
    private int fps = 60;
    private double volume = 1f;

    public Settings() {
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public int getFps() {
        return fps;
    }

    public double getVolume() {
        return volume;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public void saveToFile(String file){
        try (Writer writer = new FileWriter(file)) {
            LinkedHashMap<String, Object> config = new LinkedHashMap<>(Map.of(
                    "fullscreen", fullscreen,
                    "fps", fps,
                    "volume", volume
            ));
            yaml.dump(config, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
