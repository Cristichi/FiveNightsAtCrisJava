package es.cristichi.fnac.io;

import java.awt.image.BufferedImage;

/**
 * Represents a frame of a GIF animation.
 * @param image Image of this frame.
 * @param delaySecs Delay in seconds this frame has.
 * @param disposalMethod Disposal method. Check {@link DisposalMethod}.
 * @param offsetX X coordinate of the top-left corner.
 * @param offsetY Y coordinate of the top-left corner.
 * @param width Width of this frame.
 * @param height Height of this frame.
 */
public record GifFrame(BufferedImage image, double delaySecs, DisposalMethod disposalMethod, int offsetX,
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
    
    /**
     * Disposal method of a given GIF frame.
     */
    public enum DisposalMethod {
        /**
         * Treat as DO_NOT_DISPOSE.
         */
        UNSPECIFIED,
        /**
         * This frame should be kept on screen for the next frame.
         */
        DO_NOT_DISPOSE,
        /**
         * After this frame, the background color should fill and previous frames discarded.
         */
        RESTORE_TO_BACKGROUND_COLOR,
        /**
         * After this frame, the next frame should be the previous unspecified frame or the first frame. Unsupported here.
         */
        RESTORE_TO_PREVIOUS
    }
}
