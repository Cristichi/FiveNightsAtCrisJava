package es.cristichi.fnac.util;

import es.cristichi.fnac.exception.AssetNotFound;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AssetsIO {
    public static BufferedImage loadImage(String path) throws AssetNotFound {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new AssetNotFound("Image not found at \"" + path + "\". Probably Cristichi forgot to add it.", e);
        }
    }
}