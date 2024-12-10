package es.cristichi.fnac;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.ExceptionViewer;
import es.cristichi.fnac.gui.Nights;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.SaveFileIO;
import es.cristichi.fnac.io.Settings;
import kuusisto.tinysound.TinySound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;

public class Main {
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_TEST_NIGHT_IS_RESTA = false;

    public static void main(String[] args) {
        // Hardware acceleration op
        System.setProperty("sun.java2d.opengl", "true");

        // Sound system init
        TinySound.init();

        // Making sure our EraserDust font is installed or registered for later used.
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
            new ExceptionViewer(e);
            throw new RuntimeException(e);
        }

        // Save file
        final SaveFileIO.SaveFile saveFile;
        try {
            saveFile = SaveFileIO.loadFromFile(SaveFileIO.SAVE_FILE);
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> new ExceptionViewer(e));

            throw new RuntimeException("Failed to load save file: " + e.getMessage(), e);
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
            SwingUtilities.invokeLater(() -> new ExceptionViewer(e));

            throw new RuntimeException("Failed to load settings file: " + e.getMessage(), e);
        }

        // JFrame in correct Thread
        SwingUtilities.invokeLater(() -> {
            try {
                Nights window = new Nights(saveFile, settings);
                window.setFullScreen(settings.isFullscreen());
                window.setVisible(true);
                {
                    AbstractAction action = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                settings.setFullscreen(!settings.isFullscreen());
                                window.setFullScreen(settings.isFullscreen());
                                settings.saveToFile(Settings.SETTINGS_FILE);
                            } catch (Exception error) {
                                window.dispose();
                                new ExceptionViewer(error);
                            }
                        }
                    };

                    window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                            .put(KeyStroke.getKeyStroke("F11"), "switchFull");
                    window.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK), "switchFull");
                    window.getRootPane().getActionMap().put("switchFull", action);
                }
            } catch (Exception e) {
                new ExceptionViewer(new Exception("Error when trying to prepare the GUI and Nights.", e));
                File log = new File("error.log");
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(log));
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    bw.write(sw.toString());
                } catch (IOException ioException) {
                    new ExceptionViewer(new Exception("Error when trying to write log.", ioException));
                }
            }
        });
    }
}

