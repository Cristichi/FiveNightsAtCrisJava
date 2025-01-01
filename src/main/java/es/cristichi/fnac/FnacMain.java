package es.cristichi.fnac;

import es.cristichi.fnac.cnight.CustomNightRegistry;
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
import es.cristichi.fnac.obj.anim.RoamingBob;
import es.cristichi.fnac.obj.anim.RoamingMaria;
import es.cristichi.fnac.obj.cams.TutorialMap;
import es.cristichi.fnac.obj.nights.*;
import kuusisto.tinysound.TinySound;

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
    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        new FnacMain().run();
    }
    
    /**
     * Runs the game with the given {@link NightRegistry}'s Nights and the Animatronics inside. Before running this
     * method you may also want to register your custom {@link es.cristichi.fnac.obj.anim.AnimatronicDrawing} classes
     * for the Custom Night by adding the {@link es.cristichi.fnac.obj.cnight.CustomNightAnimatronic} annotation and
     * making a constructor that requests {@link es.cristichi.fnac.obj.cnight.CustomNightAnimatronicData}.
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
                throw new ResourceException(
                        "EraserDust Font, which this game uses everywhere, is not installed and could not be " +
                                "registered.");
            }
        } catch (ResourceException e) {
            new ExceptionDialog(e, true, true);
            throw new RuntimeException(e);
        }
        
        // Save file
        NightProgress.init();
        final NightProgress.SaveFile saveFile;
        if (DEBUG){
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
                SwingUtilities.invokeLater(() -> new ExceptionDialog(error, true, true));
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
            SwingUtilities.invokeLater(() -> new ExceptionDialog(error, true, true));
            throw error;
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
                TutorialMap tutorialMap = new TutorialMap();
                tutorialMap.addCamAnimatronics("cam1",
                        new RoamingBob("Bob", Map.of(1,2, 2,3, 3,0), false, false, java.util.List.of("cam4"), 0f, rng));
                tutorialMap.addCamAnimatronics("cam2",
                        new RoamingMaria("Maria", Map.of(0,0, 2,2, 3,3, 4,4), false, false, List.of("cam3"), 0f, rng));
                
                return new NightJC("Tutorial", settings.getFps(), tutorialMap, "night/tutorial/paper.png",
                        powerOutage, rng, 60, 0.45f, "night/tutorial/completed.wav");
            }
        });
        // Example on how to do them by organizing them in classes
        NightRegistry.registerNight(1, new Night1());
        NightRegistry.registerNight(2, new Night2());
        NightRegistry.registerNight(3, new Night3());
        NightRegistry.registerNight(4, new Night4());
        NightRegistry.registerNight(5, new Night5());
        NightRegistry.registerNight(6, new Night6());
        
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
                            window.dispose();
                            new ExceptionDialog(error, true, false);
                        }
                    }
                };
                window.getRootPane().getActionMap().put("switchFull", action);
                
                window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke("F11"), "switchFull");
                window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK), "switchFull");
            } catch (Exception e) {
                new ExceptionDialog(new Exception("Error when trying to prepare the GUI and Nights.", e), true, false);
            }
        });
        
        // Custom Night's Animatronic registering
        CustomNightRegistry.registerPackage("es.cristichi.fnac.obj.anim");
    }
}

