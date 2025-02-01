package es.cristichi.fnac.io;

import es.cristichi.fnac.exception.ResourceException;

import java.awt.image.BufferedImage;

/**
 * Provides a way to use static {@link BufferedImage}s instead of a GIF for Animatronics.
 */
public class StaticNightDrawableImage implements NightDrawableImage {
    protected final BufferedImage img;
    
    /**
     * Saves this image for Animatronics as a simple static image.
     * @param img Image.
     */
    public StaticNightDrawableImage(BufferedImage img){
        this.img = img;
    }
    
    /**
     * Loads the given image from the path (using {@link Resources#loadImage(String)}).
     * @param resourcePath The path to the image in the resources.
     * @throws ResourceException If any issues occur while trying to load the image.
     */
    public StaticNightDrawableImage(String resourcePath) throws ResourceException {
        this(Resources.loadImage(resourcePath));
    }
    
    @Override
    public BufferedImage getImageForTick(int tick, int fps) {
        return img;
    }
}
