package es.cristichi.fnac.io;

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
     * Creates a new GifAnimation with the given frames.
     * @param filePath File path, for debugging purposes only.
     * @param frames List of {@link GifFrame}s.
     */
    public GifAnimation(String filePath, List<GifFrame> frames){
        super(frames);
        this.filePath = filePath;
    }
    
    /**
     * For debugging purposes.
     * @return The filepath from which this GifAnimatrion was loaded.
     */
    public String getFilePath() {
        return filePath;
    }
}
