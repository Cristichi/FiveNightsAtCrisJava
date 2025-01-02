package es.cristichi.fnac.io;

import java.awt.image.BufferedImage;

/**
 * Represents a frame of a GIF animation.
 * @param image Image of this frame.
 * @param delaySecs Delay in seconds this frame has.
 * @param disposalMethod Disposal method. Check {@link GifFrameDisposalMethod}.
 * @param offsetX X coordinate of the top-left corner.
 * @param offsetY Y coordinate of the top-left corner.
 * @param width Width of this frame.
 * @param height Height of this frame.
 */
public record GifFrame(BufferedImage image, double delaySecs, GifFrameDisposalMethod disposalMethod, int offsetX,
                       int offsetY, int width, int height) {

    @Override
    public String toString() {
        return "GifFrame{" +
                "image=" + (image==null?"present":"null") +
                ", delaySecs=" + delaySecs +
                ", disposalMethod=" + disposalMethod +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
