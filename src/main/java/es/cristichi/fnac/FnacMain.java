package es.cristichi.fnac;

import es.cristichi.fnac.cnight.CustomNightAnimData;
import es.cristichi.fnac.cnight.CustomNightAnimFactory;
import es.cristichi.fnac.cnight.CustomNightAnimRegistry;
import es.cristichi.fnac.cnight.CustomNightMapRegistry;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.ExceptionDialog;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.gui.NightsJF;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.anim.*;
import es.cristichi.fnac.obj.cams.CameraMap;
import es.cristichi.fnac.obj.cams.RestaurantCamMapFactory;
import es.cristichi.fnac.obj.cams.TutorialCamMapFactory;
import es.cristichi.fnac.obj.nights.*;
import kuusisto.tinysound.TinySound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Entry point for the main unmodified game. This can be created more than once, which will result in several instances
 * of the game, but please avoid doing so since some windows "exit" the runtime VM which will destroy all windows.
 */
public class FnacMain implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FnacMain.class);
    /**
     * When enabled, all Nights are available in the menu.
     */
    @SuppressWarnings("CanBeFinal") //This is to be modified by modders using this as dependency.
    public static boolean DEBUG_ALLNIGHTS = false;
    /**
     * Name of the game.
     */
    @SuppressWarnings("CanBeFinal") //This is to be modified by modders using this as dependency.
    public static String GAME_TITLE = "Five Nights at Cris'";
    
    /**
     * Entry point.
     * @param args Arguments, but they are ignored.
     */
    public static void main(String[] args) {
        new FnacMain().run();
    }
    
    /**
     * Runs the game with the given {@link NightRegistry}'s Nights and the Animatronics inside. Before running this
     * method you may also want to register anything you need on the registries.
     */
    @Override
    public void run() {
        // Hardware acceleration op
        System.setProperty("sun.java2d.opengl", "true");
        
        // Sound system init
        TinySound.init();
        
        // Making sure our EraserDust font is installed or registered for later use.
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Resources.loadCustomFont("fonts/EraserDust.ttf"));
            boolean fontIsLoaded = false;
            for (Font font : ge.getAllFonts()) {
                if (font.getFamily().equals("Eraser")) {
                    fontIsLoaded = true;
                    break;
                }
            }
            if (!fontIsLoaded) {
                throw new ResourceException("EraserDust Font, which this game uses everywhere, is not installed " +
                        "and could not be registered.");
            }
        } catch (ResourceException e) {
            new ExceptionDialog(e, true, true, LOGGER);
            return;
        }
        
        // Save file
        NightProgress.init();
        final NightProgress.SaveFile saveFile;
        if (DEBUG_ALLNIGHTS){
            ArrayList<String> nights = new ArrayList<>(6);
            nights.add("Tutorial");
            nights.add("Night 1");
//            nights.add("Night 2");
//            nights.add("Night 3");
//            nights.add("Night 4");
//            nights.add("Night 5");
//            nights.add("Night 6");
            saveFile = new NightProgress.SaveFile(nights);
//            try {
//                saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        } else {
            try {
                saveFile = NightProgress.loadFromFile(NightProgress.SAVE_FILE_NAME);
            } catch (Exception e) {
                e.printStackTrace();
                RuntimeException error = new RuntimeException("Failed to load save file: " + e.getMessage(), e);
                SwingUtilities.invokeLater(() -> new ExceptionDialog(error, true, true, LOGGER));
                throw error;
            }
        }
        
        // Settings
        Settings.init();
        final Settings settings;
        try {
            settings = Settings.fromFile(Settings.SETTINGS_FILE);
            settings.saveToFile(Settings.SETTINGS_FILE);
            TinySound.setGlobalVolume(settings.getVolume());
        } catch (Exception e) {
            e.printStackTrace();
            RuntimeException error = new RuntimeException("Failed to load settings file: " + e.getMessage(), e);
            SwingUtilities.invokeLater(() -> new ExceptionDialog(error, true, true, LOGGER));
            return;
        }
        
        // JFrame in correct Thread
        SwingUtilities.invokeLater(() -> {
            try {
                NightsJF window = new NightsJF(saveFile, settings);
                window.setFullScreen(settings.isFullscreen());
                window.setVisible(true);
                AbstractAction action = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            settings.setFullscreen(!settings.isFullscreen());
                            window.setFullScreen(settings.isFullscreen());
                            settings.saveToFile(Settings.SETTINGS_FILE);
                        } catch (Exception error) {
                            new ExceptionDialog(error, true, false, LOGGER);
                            window.dispose();
                        }
                    }
                };
                window.getRootPane().getActionMap().put("switchFull", action);
                
                window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke("F11"), "switchFull");
                window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK), "switchFull");
            } catch (Exception e) {
                new ExceptionDialog(new Exception("Error when trying to prepare the GUI and Nights.", e), true, false,
                        LOGGER);
            }
        });
        
        // Maps for Custom Night
        TutorialCamMapFactory tutorialCamMapFactory = new TutorialCamMapFactory();
        CustomNightMapRegistry.registerMap(new RestaurantCamMapFactory());
        CustomNightMapRegistry.registerMap(tutorialCamMapFactory);
        
        // Animatronics for Custom Night
        try {
            CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<ChatGPT>("ChatGPT", """
                    ChatGPT starts at the Storage or cam2, and decides alternatively whether they want to \
                    move randomly or using a chosen path to either your left or right door.""",
                    20, Resources.loadImage("anims/chatgpt/portrait.png"), new String[]{"storage", "cam1"}) {
                @Override
                public ChatGPT generate(CustomNightAnimData data, Random rng) throws ResourceException {
                    return new ChatGPT(nameId, Map.of(0, data.ai()), false, false, List.of(),
                            List.of(List.of("storage", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                                    List.of("storage", "dining area", "corridor 2", "corridor 4", "rightDoor")),
                            rng);
                }
            });
        } catch (ResourceException e){
            LOGGER.error("Error registering ChatGPT for Custom Night.", e);
        }
        try {
            CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<Paco>("Paco", """
                    Paco starts his cycle at the kitchen, then moves to the Dining Area, and then chooses whether he \
                    goes to your left side or right side. After waiting at your closed door, he teleports back \
                    to the Kitchen. He never goes into the Staff Lounge, the Bathrooms, the Storage or the Main \
                    Stage.""",
                    20, Resources.loadImage("anims/paco/portrait.png"), new String[]{"kitchen", "cam1"}) {
                @Override
                public Paco generate(CustomNightAnimData data, Random rng) throws ResourceException {
                    return new Paco(nameId, Map.of(0, data.ai()), false, true,
                            List.of(
                                    List.of("cam1", "cam2", "cam4", "rightDoor"),
                                    List.of("cam2", "cam1", "cam3", "leftDoor"),
                                    List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                                    List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                            ), rng);
                }
            });
        } catch (ResourceException e){
            LOGGER.error("Error registering Paco for Custom Night.", e);
        }
        try {
            CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<RoamingMaria>("Maria",
                    "Maria roams looking for the right door to the office.",
                    20, Resources.loadImage("anims/maria/portrait.png"), new String[]{"main stage", "cam1"}) {
                @Override
                public RoamingMaria generate(CustomNightAnimData data, Random rng) throws ResourceException {
                    return new RoamingMaria(nameId, Map.of(0, data.ai()), false, true,
                            List.of("corridor 1", "corridor 3", "staff lounge", "cam3"), rng);
                }
            });
        } catch (ResourceException e){
            LOGGER.error("Error registering Maria for Custom Night.", e);
        }
        try {
            CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<RoamingBob>("Bob",
                    "Bob roams looking for the left door to the office.",
                    20, Resources.loadImage("anims/bob/portrait.png"), new String[]{"main stage", "cam1"}) {
                @Override
                public RoamingBob generate(CustomNightAnimData data, Random rng) throws ResourceException {
                    return new RoamingBob(nameId, Map.of(0, data.ai()), false, true,
                            List.of("corridor 2", "corridor 4", "bathrooms", "offices", "cam4"), rng);
                }
            });
        } catch (ResourceException e){
            LOGGER.error("Error registering Bob for Custom Night.", e);
        }
        try {
            CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<RoamingCris>("Cris",
                    "Cris roams the entire place to either door, but avoiding roaming too far from the office.",
                    20, Resources.loadImage("anims/cris/portrait.png"), new String[]{"dining area", "cam2"}) {
                @Override
                public RoamingCris generate(CustomNightAnimData data, Random rng) throws ResourceException {
                    return new RoamingCris(nameId, Map.of(0, data.ai()), false, true,
                            List.of("kitchen", "storage", "main stage", "staff lounge", "bathrooms"), rng);
                }
            });
        } catch (ResourceException e){
            LOGGER.error("Error registering Cris (final form) for Custom Night.", e);
        }
        try {
            CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<PathCris>("Cris (final form)", """
                    Cris (final form) starts at the Storage, then goes to the Dining Area. From there, he teleports to \
                    either the Staff Lounge or the Offices. When at the Staff Lounge, he teleports to corridor 3\
                     or 4 and then he goes to the closest Office door. If at the Offices, he first teleports to \
                    the Bathrooms, and then from there he goes to corridor 3 or 4 and then goes to the closest \
                    Office door.""",
                    20, Resources.loadImage("anims/cris/portrait.png"), new String[]{"storage", "cam1"}) {
                @Override
                public PathCris generate(CustomNightAnimData data, Random rng) throws ResourceException {
                    return new PathCris(nameId, Map.of(0, data.ai()), false, true,
                            List.of(
                                    List.of("cam2", "cam4", "rightDoor"),
                                    List.of("cam1", "cam3", "leftDoor"),
                                    List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                                    List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor"),
                                    List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                                    List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
                            ), rng);
                }
            });
        } catch (ResourceException e){
            LOGGER.error("Error registering Cris for Custom Night.", e);
        }
        
        // Nights
        
        /* This is an example of creating a NightFactory inline here without having to create a new class for it.
         * This is the Tutorial Night, so it's the first one (it requires having 0 completed Nights).
         */
        NightRegistry.registerNight(0, new NightFactory() {
            @Override
            public MenuJC.Item getItem() {
                return new MenuJC.Item("tutorial", "New Game", "Tutorial Night", null);
            }
            @Override
            public NightJC createNight(Settings settings, Jumpscare powerOutage,
                                       Random rng) throws IOException, NightException {
                CameraMap tutorialMap = tutorialCamMapFactory.generate();
                tutorialMap.addCamAnimatronics("cam1",
                        new RoamingBob("Bob", Map.of(1,2, 2,3, 3,0), false, false, java.util.List.of("cam4"), rng));
                tutorialMap.addCamAnimatronics("cam2",
                        new RoamingMaria("Maria", Map.of(0,0, 2,2, 3,3, 4,4), false, false, List.of("cam3"), rng));
                
                return new NightJC("Tutorial", settings.getFps(), tutorialMap,
                        Resources.loadImage("night/tutorial/paper.png"), powerOutage, rng, 60, 0.45f,
                        Resources.loadSound("night/tutorial/completed.wav"));
            }
        });
        // Example on how to do them by organizing them in classes
        NightRegistry.registerNight(1, new Night1());
        NightRegistry.registerNight(2, new Night2());
        NightRegistry.registerNight(3, new Night3());
        NightRegistry.registerNight(4, new Night4());
        NightRegistry.registerNight(5, new Night5());
        NightRegistry.registerNight(6, new Night6());
    }
}

