package es.cristichi.fnac.io;

import es.cristichi.fnac.exception.ResourceException;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Resources {

    public static InputStream loadInputStream(String path) throws ResourceException {
        InputStream in = Resources.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new ResourceException("Resource not found at \"" + path + "\". Probably Cristichi forgot to add it.");
        }
        return in;
    }

    public static BufferedImage loadImageResource(String path) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            return ImageIO.read(in);
        } catch (IOException | NullPointerException e) {
            throw new ResourceException("Image not found at \"" + path + "\". Probably Cristichi forgot to add it.", e);
        }
    }

    public static Clip loadAudioClip(String path, String tmpFile) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            String[] tempSplit = tmpFile.split("\\.");
            Path tempFile = Files.createTempFile(tempSplit[0], ".".concat(tempSplit[1]));
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(tempFile.toFile());
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);
            return clip;
        } catch (IOException | NullPointerException e) {
            throw new ResourceException("Audio resource not found at \"" + path + "\". Probably Cristichi forgot to add it.", e);
        } catch (UnsupportedAudioFileException e) {
            throw new ResourceException("Audio resource "+path+" is not in a supported format.", e);
        } catch (LineUnavailableException e) {
            throw new ResourceException("Audio resource "+path+" already in use by another application.", e);
        }
    }

    public static Font loadCustomFont(String path) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(40f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            return font;
        } catch (IOException | FontFormatException | NullPointerException e) {
            throw new ResourceException("Error when trying to load Font "+path, e);
        }
    }
}