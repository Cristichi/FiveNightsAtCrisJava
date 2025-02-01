package es.cristichi.fnac.io;

import java.awt.image.BufferedImage;

/**
 * Provides a way to specify how an image is drawn. Check {@link StaticNightDrawableImage}
 * and {@link es.cristichi.fnac.io.GifAnimation}.
 */
public interface NightDrawableImage {
    /**
     * Returns the image that must be drawn on this tick with these FPS.
     * @param tick Current tick.
     * @param fps Frames per second. It is constant throughout the entire Night.
     * @return The image to draw on this frame.
     */
    BufferedImage getImageForTick(int tick, int fps);
}
