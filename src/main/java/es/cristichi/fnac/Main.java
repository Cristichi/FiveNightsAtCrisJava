package es.cristichi.fnac;

import es.cristichi.fnac.cnight.CustomNightRegistry;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.ExceptionDialog;
import es.cristichi.fnac.gui.NightsJF;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.nights.*;
import kuusisto.tinysound.TinySound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class Main implements Runnable {
    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        new Main().run();
    }
    
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
        try {
            saveFile = NightProgress.loadFromFile(NightProgress.SAVE_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            RuntimeException error = new RuntimeException("Failed to load save file: " + e.getMessage(), e);
            SwingUtilities.invokeLater(() -> new ExceptionDialog(error, true, true));
            throw error;
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
        
        // Nights parametrized
        NightRegistry.registerNight(0, new TutorialNight());
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

