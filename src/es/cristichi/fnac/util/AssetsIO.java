package es.cristichi.fnac.util;

import es.cristichi.fnac.exception.AssetNotFound;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class AssetsIO {

    public static InputStream loadInputStream(String path) throws AssetNotFound {
        InputStream in = AssetsIO.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new AssetNotFound("Resource not found at \"" + path + "\". Probably Cristichi forgot to add it.");
        }
        return in;
    }

    public static BufferedImage loadImageResource(String path) throws AssetNotFound {
        try {
            try (InputStream in = AssetsIO.class.getClassLoader().getResourceAsStream(path)) {
                if (in == null){
                    throw new NullPointerException("Resource not found.");
                }
                return ImageIO.read(in);
            }
        } catch (IOException | NullPointerException e) {
            throw new AssetNotFound("Image not found at \"" + path + "\". Probably Cristichi forgot to add it.", e);
        }
    }

    public static Font loadCustomFont(String path) throws AssetNotFound {
        try {
            try (InputStream in = AssetsIO.class.getClassLoader().getResourceAsStream(path)) {
                if (in == null){
                    throw new NullPointerException("Resource not found.");
                }
                Font font = Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(40f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(font);
                return font;
            }
        } catch (IOException | FontFormatException | NullPointerException e) {
            throw new AssetNotFound("Error when trying to load Font "+path, e);
        }
    }
}