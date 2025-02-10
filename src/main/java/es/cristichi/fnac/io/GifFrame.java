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
    /**
     * Disposal method of a given GIF frame.
     */
    public enum DisposalMethod {
        /**
         * Treat as {@link DisposalMethod#DO_NOT_DISPOSE}.
         */
        UNSPECIFIED,
        /**
         * This frame should be kept on screen for the next frame.
         */
        DO_NOT_DISPOSE,
        /**
         * After this frame, discard previous frames and fill with background color to draw the next one on top of it.
         * Usually the background color should be transparent, in the case of this game.
         */
        RESTORE_TO_BACKGROUND_COLOR,
        /**
         * After this frame, the next frame should be the previous unspecified frame or the first frame. Unsupported here.
         */
        RESTORE_TO_PREVIOUS
    }
}
