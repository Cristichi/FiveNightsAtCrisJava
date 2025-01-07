package es.cristichi.fnac.io;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data of a GIF file that the game needs.
 */
public class GifAnimation extends ArrayList<GifFrame> {
    /**
     * Information of the path to the file, for debugging purposes.
     */
    protected final String filePath;
    /**
     * Size of the canvas in which the image is normally drawn.
     */
    protected final Dimension logicalScreen;
    /**
     * Creates a new GifAnimation with the given frames.
     * @param filePath File path, for debugging purposes only.
     * @param frames List of {@link GifFrame}s.
     * @param logicalScreenWidth Width of the logical screen.
     * @param logicalScreenHeight Height of the logical screen.
     */
    public GifAnimation(String filePath, List<GifFrame> frames, int logicalScreenWidth, int logicalScreenHeight){
        super(frames);
        this.filePath = filePath;
        this.logicalScreen = new Dimension(logicalScreenWidth, logicalScreenHeight);
    }
    
    /**
     * For debugging purposes.
     * @return The filepath from which this GifAnimatrion was loaded.
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * @return The dimension of the logical screen.
     */
    public Dimension getLogicalScreen() {
        return logicalScreen;
    }
}
