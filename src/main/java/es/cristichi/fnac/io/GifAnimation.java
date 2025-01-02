package es.cristichi.fnac.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data of a GIF file that the game needs.
 */
public class GifAnimation extends ArrayList<GifFrame> {
    /**
     * Creates a new GifAnimation with the given frames.
     * @param frames List of {@link GifFrame}s.
     */
    public GifAnimation(List<GifFrame> frames){
        super(frames);
    }
}
