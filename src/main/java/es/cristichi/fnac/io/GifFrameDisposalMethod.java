package es.cristichi.fnac.io;

/**
 * Disposal method of a given GIF frame.
 */
public enum GifFrameDisposalMethod {
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
