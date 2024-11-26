package es.cristichi.fnac.io;

import es.cristichi.fnac.exception.ResourceNotFound;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class Resources {

    public static InputStream loadInputStream(String path) throws ResourceNotFound {
        InputStream in = Resources.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new ResourceNotFound("Resource not found at \"" + path + "\". Probably Cristichi forgot to add it.");
        }
        return in;
    }

    public static BufferedImage loadImageResource(String path) throws ResourceNotFound {
        try {
            try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
                if (in == null){
                    throw new NullPointerException("Resource not found.");
                }
                return ImageIO.read(in);
            }
        } catch (IOException | NullPointerException e) {
            throw new ResourceNotFound("Image not found at \"" + path + "\". Probably Cristichi forgot to add it.", e);
        }
    }

    public static Font loadCustomFont(String path) throws ResourceNotFound {
        try {
            try (InputStream in = Resources.class.getClassLoader().getResourceAsStream(path)) {
                if (in == null){
                    throw new NullPointerException("Resource not found.");
                }
                Font font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(40f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(font);
                return font;
            }
        } catch (IOException | FontFormatException | NullPointerException e) {
            throw new ResourceNotFound("Error when trying to load Font "+path, e);
        }
    }
}