package es.cristichi.fnac;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.ExceptionDialog;
import es.cristichi.fnac.gui.NightsJF;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import kuusisto.tinysound.TinySound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class Main {
    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        // Hardware acceleration op
        System.setProperty("sun.java2d.opengl", "true");

        // Sound system init
        TinySound.init();

        // Making sure our EraserDust font is installed or registered for later use.
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Resources.loadCustomFont("fonts/EraserDust.ttf"));
            boolean fontIsLoaded = false;
            for (Font font : ge.getAllFonts()){
                if (font.getFamily().equals("Eraser")){
                    fontIsLoaded = true;
                    break;
                }
            }
            if (!fontIsLoaded){
                throw new ResourceException("EraserDust Font was not registered.");
            }
        } catch (ResourceException e) {
            new ExceptionDialog(e, true, true);
            throw new RuntimeException(e);
        }

        // Save file
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

                window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke("F11"), "switchFull");
                window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK), "switchFull");
                window.getRootPane().getActionMap().put("switchFull", action);
            } catch (Exception e) {
                new ExceptionDialog(new Exception("Error when trying to prepare the GUI and Nights.", e), true, false);
            }
        });
    }
}

