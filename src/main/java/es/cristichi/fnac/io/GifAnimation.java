package es.cristichi.fnac.io;

import java.util.ArrayList;
import java.util.List;

public class GifAnimation extends ArrayList<GifFrame> {
    public GifAnimation(List<GifFrame> frames){
        super(frames);
    }

    public GifAnimation(int size){
        super(size);
    }
}
