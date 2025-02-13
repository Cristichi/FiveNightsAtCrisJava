package es.cristichi.fnac;

import es.cristichi.fnac.anim.*;
import es.cristichi.fnac.cams.CameraMap;
import es.cristichi.fnac.cams.RestaurantCamMapFactory;
import es.cristichi.fnac.cams.TutorialCamMapFactory;
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
import es.cristichi.fnac.loading.LoadRunnable;
import es.cristichi.fnac.loading.LoadRunnableWithSaveFile;
import es.cristichi.fnac.loading.LoadRunnableWithSettings;
import es.cristichi.fnac.nights.*;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Entry point for the main unmodified game. This can be created more than once, which will result in several instances
 * of the game, but please avoid doing so since some windows "exit" the runtime VM which will destroy all windows.
 */
public class FnacMain {
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
     *
     * @param args Arguments, but they are ignored.
     */
    public static void main(String[] args) {
        new FnacMain().run("Five Nights at Cris", null, null, null);
    }
    
    /**
     * Runs the game with the given information and loading steps.
     * @param docFolderName Name of the folder inside the computer user's Documents folder for the Settings and
     *                      Save file.
     * @param loadingSequences List of individual Runnables that must be run during the initial loading, or
     *                         {@code null} for noneª.
     * @param loadingWithSettings List of individual Runnables that depend on the Settings being loaded, or
     *                            {@code null} for none.
     * @param loadingWithSaveFile List of individual Runnables that depend on the Save File being loaded, or
     *                            {@code null} for none.
     */
    public void run(String docFolderName, @Nullable LoadRunnable[] loadingSequences,
                    @Nullable LoadRunnableWithSettings[] loadingWithSettings,
                    @Nullable LoadRunnableWithSaveFile[] loadingWithSaveFile) {
        // Semaphore to make the JFrame wait until everything is loaded.
        Semaphore loadingSem = new Semaphore(-19
                -(loadingSequences==null?0:loadingSequences.length)
                -(loadingWithSettings==null?0:loadingWithSettings.length)
                -(loadingWithSaveFile==null?0:loadingWithSaveFile.length));
        
        // Hardware acceleration op
        System.setProperty("sun.java2d.opengl", "true");
        
        if (loadingSequences != null) {
            for (LoadRunnable loadSequence : loadingSequences) {
                new Thread(() -> {
                    try {
                        loadSequence.run();
                        loadingSem.release();
                    } catch (Exception e) {
                        new ExceptionDialog(e, true, false, LOGGER);
                    }
                }).start();
            }
        }
        
        // Settings and dependands. Together because the Window and other things depend on the Settings.
        final AtomicReference<Settings> settings = new AtomicReference<>();
        AtomicReference<NightsJF> window = new AtomicReference<>();
        SwingUtilities.invokeLater(() -> {
            try {
                Settings.init(docFolderName);
                try {
                    settings.set(Settings.fromFile(Settings.SETTINGS_FILE));
                    settings.get().saveToFile(Settings.SETTINGS_FILE);
                    
                    // Sound system init
                    new Thread(() -> {
                        TinySound.init();
                        TinySound.setGlobalVolume(settings.get().getVolume());
                        loadingSem.release();
                    }).start();
                } catch (Exception e) {
                    new ExceptionDialog(new RuntimeException("Failed to load settings file", e), true, true, LOGGER);
                    return;
                }
                window.set(new NightsJF());
                window.get().startStartingSequence(loadingSem, settings.get());
                
                new Thread(() -> {
                    try {
                        Sound cristichi = Resources.loadSound("startup/cristichi.wav");
                        cristichi.addOnEndListener(loadingSem::release);
                        cristichi.play();
                    } catch (ResourceException e) {
                        LOGGER.error("Error loading cristichi Sound.", e);
                        loadingSem.release();
                    }
                }).start();
                
                AbstractAction action = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            settings.get().setFullscreen(!settings.get().isFullscreen());
                            window.get().setFullScreen(settings.get().isFullscreen());
                            settings.get().saveToFile(Settings.SETTINGS_FILE);
                        } catch (Exception error) {
                            new ExceptionDialog(error, true, false, LOGGER);
                        }
                    }
                };
                window.get().getRootPane().getActionMap().put("switchFull", action);
                
                window.get().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke("F11"), "switchFull");
                window.get().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK), "switchFull");
                
                
                if (loadingWithSettings != null){
                    for (LoadRunnableWithSettings loadSequence : loadingWithSettings){
                        new Thread(() -> {
                            try {
                                loadSequence.run(settings.get());
                                loadingSem.release();
                            } catch (Exception e){
                                new ExceptionDialog(e, true, false, LOGGER);
                            }
                        }).start();
                    }
                }
            } catch (Exception e) {
                new ExceptionDialog(new Exception("Error when trying to prepare the GUI and Nights.", e), true, false,
                        LOGGER);
            }
        });
        
        // Save file
        final AtomicReference<NightProgress.SaveFile> saveFile = new AtomicReference<>();
        new Thread(() -> {
            NightProgress.init(docFolderName);
            try {
                saveFile.set(NightProgress.loadFromFile(NightProgress.SAVE_FILE_NAME));
                
                if (loadingWithSaveFile != null) {
                    for (LoadRunnableWithSaveFile loadSequence : loadingWithSaveFile) {
                        new Thread(() -> {
                            try {
                                loadSequence.run(saveFile.get());
                                loadingSem.release();
                            } catch (Exception e) {
                                new ExceptionDialog(e, true, false, LOGGER);
                            }
                        }).start();
                    }
                }
            } catch (Exception e) {
                new ExceptionDialog(e, true, true, LOGGER);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        // Making sure our EraserDust font is installed or registered for later use.
        new Thread(() -> {
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(Resources.loadFont("fonts/EraserDust.ttf"));
                boolean fontIsLoaded = false;
                for (Font font : ge.getAllFonts()) {
                    if (font.getFamily().equals("Eraser")) {
                        fontIsLoaded = true;
                        break;
                    }
                }
                if (!fontIsLoaded) {
                    throw new ResourceException(
                            "EraserDust Font, which this game uses everywhere, is not installed " + "and could not be" +
                                    " registered.");
                }
            } catch (ResourceException e) {
                new ExceptionDialog(e, true, true, LOGGER);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        // Maps for Custom Night
        TutorialCamMapFactory tutorialCamMapFactory = new TutorialCamMapFactory();
        new Thread(() -> {
            CustomNightMapRegistry.registerMap(new RestaurantCamMapFactory());
            CustomNightMapRegistry.registerMap(tutorialCamMapFactory);
            loadingSem.release();
        }).start();
        
        // Animatronics for Custom Night
        new Thread(() -> {
            try {
                CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<ChatGPT>("ChatGPT", """
                        ChatGPT starts at the Storage or cam2, and decides alternatively whether they want to \
                        move randomly or using a chosen path to either your left or right door.""", 20,
                        Resources.loadImage("anims/chatgpt/portrait.png"), new String[]{"storage", "cam1"}) {
                    @Override
                    public ChatGPT generate(CustomNightAnimData data, Random rng) throws ResourceException {
                        return new ChatGPT(nameId, Map.of(0, data.ai()), false, false, List.of(),
                                List.of(List.of("storage", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                                        List.of("storage", "dining area", "corridor 2", "corridor 4", "rightDoor")),
                                rng);
                    }
                });
            } catch (ResourceException e) {
                LOGGER.error("Error registering ChatGPT for Custom Night.", e);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        new Thread(() -> {
            try {
                CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<Paco>("Paco", """
                        Paco starts his cycle at the kitchen, then moves to the Dining Area, and then chooses whether he \
                        goes to your left side or right side. After waiting at your closed door, he teleports back \
                        to the Kitchen. He never goes into the Staff Lounge, the Bathrooms, the Storage or the Main \
                        Stage.""", 20, Resources.loadImage("anims/paco/portrait.png"),
                        new String[]{"kitchen", "cam1"}) {
                    @Override
                    public Paco generate(CustomNightAnimData data, Random rng) throws ResourceException {
                        return new Paco(nameId, Map.of(0, data.ai()), false, true,
                                List.of(List.of("cam1", "cam2", "cam4", "rightDoor"),
                                        List.of("cam2", "cam1", "cam3", "leftDoor"),
                                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")),
                                rng);
                    }
                });
            } catch (ResourceException e) {
                LOGGER.error("Error registering Paco for Custom Night.", e);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        new Thread(() -> {
            try {
                CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<RoamingMaria>("Maria",
                        "Maria roams looking for the right door to the office.", 20,
                        Resources.loadImage("anims/maria/portrait.png"), new String[]{"main stage", "cam1"}) {
                    @Override
                    public RoamingMaria generate(CustomNightAnimData data, Random rng) throws ResourceException {
                        return new RoamingMaria(nameId, Map.of(0, data.ai()), false, true,
                                List.of("corridor 1", "corridor 3", "staff lounge", "cam3"), rng);
                    }
                });
            } catch (ResourceException e) {
                LOGGER.error("Error registering Maria for Custom Night.", e);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        new Thread(() -> {
            try {
                CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<RoamingBob>("Bob",
                        "Bob roams looking for the left door to the office.", 20,
                        Resources.loadImage("anims/bob/portrait.png"), new String[]{"main stage", "cam1"}) {
                    @Override
                    public RoamingBob generate(CustomNightAnimData data, Random rng) throws ResourceException {
                        return new RoamingBob(nameId, Map.of(0, data.ai()), false, true,
                                List.of("corridor 2", "corridor 4", "bathrooms", "offices", "cam4"), rng);
                    }
                });
            } catch (ResourceException e) {
                LOGGER.error("Error registering Bob for Custom Night.", e);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        new Thread(() -> {
            try {
                CustomNightAnimRegistry.registerAnimatronic(new CustomNightAnimFactory<RoamingCris>("Cris",
                        "Cris roams the entire place to either door, but avoiding roaming too far from the office.", 20,
                        Resources.loadImage("anims/cris/portrait.png"), new String[]{"dining area", "cam2"}) {
                    @Override
                    public RoamingCris generate(CustomNightAnimData data, Random rng) throws ResourceException {
                        return new RoamingCris(nameId, Map.of(0, data.ai()), false, true,
                                List.of("kitchen", "storage", "main stage", "staff lounge", "bathrooms"), rng);
                    }
                });
            } catch (ResourceException e) {
                LOGGER.error("Error registering Cris (final form) for Custom Night.", e);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        new Thread(() -> {
            try {
                CustomNightAnimRegistry.registerAnimatronic(
                        new CustomNightAnimFactory<PathCris>("Cris (final form)", """
                                Cris (final form) starts at the Storage, then goes to the Dining Area. From there, he teleports to \
                                either the Staff Lounge or the Offices. When at the Staff Lounge, he teleports to corridor 3\
                                 or 4 and then he goes to the closest Office door. If at the Offices, he first teleports to \
                                the Bathrooms, and then from there he goes to corridor 3 or 4 and then goes to the closest \
                                Office door.""", 20, Resources.loadImage("anims/cris/portrait.png"),
                                new String[]{"storage", "cam1"}) {
                            @Override
                            public PathCris generate(CustomNightAnimData data, Random rng) throws ResourceException {
                                return new PathCris(nameId, Map.of(0, data.ai()), false, true,
                                        List.of(List.of("cam2", "cam4", "rightDoor"),
                                                List.of("cam1", "cam3", "leftDoor"),
                                                List.of("storage", "dining area", "staff lounge", "corridor 3",
                                                        "leftDoor"),
                                                List.of("storage", "dining area", "staff lounge", "corridor 4",
                                                        "rightDoor"),
                                                List.of("storage", "dining area", "offices", "bathrooms", "corridor 4",
                                                        "rightDoor"),
                                                List.of("storage", "dining area", "offices", "bathrooms", "corridor 3",
                                                        "leftDoor")), rng);
                            }
                        });
            } catch (ResourceException e) {
                LOGGER.error("Error registering Cris for Custom Night.", e);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        new Thread(() -> {
            try {
                CustomNightAnimRegistry.registerAnimatronic(
                        new CustomNightAnimFactory<StatesCris>("Cris (states test)", """
                                Cris (states test) starts at the Main Stage and progresses through several \
                                different states before moving very fast to the right door.""",
                                20, Resources.loadImage("anims/statesCris/portrait.png"),
                                new String[]{"main stage", "cam1"}) {
                            @Override
                            public StatesCris generate(CustomNightAnimData data, Random rng) throws ResourceException {
                                return new StatesCris(nameId, Map.of(0, data.ai()),
                                            List.of(StatesCris.RESTAURANT_PATH,
                                            List.of("cam3", "cam1", "cam2", "cam4", "rightDoor")),
                                        rng);
                            }
                        });
            } catch (ResourceException e) {
                LOGGER.error("Error registering Cris (states test) for Custom Night.", e);
            } finally {
                loadingSem.release();
            }
        }).start();
        
        // Night registering
        new Thread(() -> {
            /* This is an example of creating a NightFactory inline here without having to create a new class for it.
             * This is the Tutorial Night, so it's the first one (it requires having 0 completed Nights).
             */
            NightRegistry.registerNight(
                    new NightFactory(new MenuJC.ItemInfo("n0_tutorial", "New Game", "Tutorial Night", null)) {
                @Override
                public Availability getAvailability(NightProgress.SaveFile saveFile) {
                    return new Availability(saveFile.completedNights().isEmpty(), false);
                }
                
                @Override
                public NightJC createNight(Settings settings, Jumpscare powerOutage,
                                           Random rng) throws IOException, NightException {
                    CameraMap tutorialMap = tutorialCamMapFactory.generate();
                    tutorialMap.addCamAnimatronics("cam1",
                            new RoamingBob("Bob", Map.of(1, 2, 2, 3, 3, 0), false, false, List.of("cam4"), rng));
                    tutorialMap.addCamAnimatronics("cam2",
                            new RoamingMaria("Maria", Map.of(0, 0, 2, 2, 3, 3, 4, 4), false, false, List.of("cam3"),
                                    rng));
                    
                    return new NightJC("Tutorial", settings.getFps(), tutorialMap,
                            Resources.loadImage("night/tutorial/paper.png"), powerOutage, rng, 60, 6, 0.45f,
                            Resources.loadSound("night/tutorial/completed.wav"), null, null);
                }
            });
            loadingSem.release();
        }).start();
            
        // Examples on how to do them by organizing them in classes
        new Thread(() -> {
            try {
                NightRegistry.registerNight(new Night1Factory());
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
            loadingSem.release();
        }).start();
        new Thread(() -> {
            try {
                NightRegistry.registerNight(new Night2Factory());
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
            loadingSem.release();
        }).start();
        new Thread(() -> {
            try {
                NightRegistry.registerNight(new Night3Factory());
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
            loadingSem.release();
        }).start();
        new Thread(() -> {
            try {
                NightRegistry.registerNight(new Night4Factory());
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
            loadingSem.release();
        }).start();
        new Thread(() -> {
            try {
                NightRegistry.registerNight(new Night5Factory());
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
            loadingSem.release();
        }).start();
        new Thread(() -> {
            try {
                NightRegistry.registerNight(new Night6Factory());
            } catch (ResourceException e) {
                throw new RuntimeException(e);
            }
            loadingSem.release();
        }).start();
        
        new Thread(() -> {
            NightRegistry.registerNight(
                    new NightFactory(new MenuJC.ItemInfo("n7", "<u>Full Night</u> Shift", "<b>Full Night</b> Shift", null)) {
                @Override
                public Availability getAvailability(NightProgress.SaveFile saveFile) {
                    return new Availability(saveFile.completedNights().size()>6, true);
                }
                
                @Override
                public NightJC createNight(Settings settings, Jumpscare powerOutage,
                                           Random rng) throws IOException, NightException {
                    AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,10), false, false,
                            List.of("corridor 2", "corridor 4", "bathrooms", "offices", "storage", "kitchen"), rng);
                    
                    AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(0,7, 2,9), false, false,
                            List.of("corridor 1", "corridor 3", "staff lounge", "storage", "kitchen"), rng);
                    
                    AnimatronicDrawing paco = new Paco("Paco", Map.of(0,9, 3,10), false, true,
                            List.of(
                                    List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                                    List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                            ), rng);
                    
                    AnimatronicDrawing crisClon1 = new PathCris("Cris", Map.of(0,5, 2,7), true, false,
                            List.of(
                                    List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                                    List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
                            ), rng);
                    
                    AnimatronicDrawing crisClon2 = new PathCris("Cris?", Map.of(0,7), true, false,
                            List.of(
                                    List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                                    List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor")
                            ), rng);
                    
                    CameraMap nightMap = new RestaurantCamMapFactory().generate();
                    nightMap.addCamAnimatronics("kitchen", paco);
                    nightMap.addCamAnimatronics("storage", bob);
                    nightMap.addCamAnimatronics("offices", maria);
                    nightMap.addCamAnimatronics("dining area", crisClon1, crisClon2);
                    
                    return new NightJC("Full Shift", settings.getFps(), nightMap, null, powerOutage, rng,
                            90, 8, 0.45f, Resources.loadSound("night/general/completed.wav"), null, null);
                }
            });
            loadingSem.release();
        }).start();
        
        // Start main menu.
        try {
            loadingSem.acquire();
            
            window.get().startMenuAndGame(saveFile.get());
        } catch (InterruptedException e) {
            new ExceptionDialog(new IllegalStateException("Interruption.", e), true, false, LOGGER);
        } catch (Exception e) {
            new ExceptionDialog(e, true, false, LOGGER);
        }
    }
}

