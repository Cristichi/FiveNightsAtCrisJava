package es.cristichi.fnac.io;

import es.cristichi.fnac.exception.ResourceException;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

import javax.imageio.ImageIO;
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

    public static Music loadMusic(String path, String tmpFile) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            String[] tempSplit = tmpFile.split("\\.");
            Path tempFile = Files.createTempFile(tempSplit[0], ".".concat(tempSplit[1]));
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            return TinySound.loadMusic(tempFile.toFile(), false);
        } catch (IOException | NullPointerException notFoundE) {
            throw new ResourceException("Audio resource not found at \"" + path + "\". Probably Cristichi forgot to add it.", notFoundE);
        } catch (Exception e){
            throw new ResourceException("Error when trying to load audio at \"" + path + "\".", e);
        }
    }

    public static Sound loadSound(String path, String tmpFile) throws ResourceException {
        try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null){
                throw new NullPointerException("Resource not found.");
            }
            String[] tempSplit = tmpFile.split("\\.");
            Path tempFile = Files.createTempFile(tempSplit[0], ".".concat(tempSplit[1]));
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);

            return TinySound.loadSound(tempFile.toFile());
        } catch (IOException | NullPointerException notFoundE) {
            throw new ResourceException("Audio resource not found at \"" + path + "\". Probably Cristichi forgot to add it.", notFoundE);
        } catch (Exception e){
            throw new ResourceException("Error when trying to load audio at \"" + path + "\".", e);
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