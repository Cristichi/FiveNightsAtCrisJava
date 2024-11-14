package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AssetNotFound;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Jumpscare {
    private List<BufferedImage> frames;
    private int repsMax;
    private int currentReps;
    private int currentFrame;

    public Jumpscare(String filepath, int reps) throws AssetNotFound {
        this.repsMax = reps;
        this.currentFrame = 0;
        loadFrames(filepath);
    }

    private void loadFrames(String filePath) throws AssetNotFound {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new FileInputStream(filePath))) {
            if (stream == null){
                throw new AssetNotFound("No suitable reader found for "+filePath+".");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IOException("No reader for: " + filePath);
            }

            ImageReader reader = readers.next();
            reader.setInput(stream);
            int numFrames = reader.getNumImages(true); // Count of frames
            frames = new ArrayList<>(numFrames);

            for (int i = 0; i < numFrames; i++) {
                frames.add(reader.read(i)); // Read each frame as a BufferedImage
            }
            reader.dispose();
        } catch (IOException e) {
            throw new AssetNotFound("Error when reading \""+filePath+"\".", e);
        }
    }

    public void reset() {
        this.currentFrame = 0;
        this.currentReps = 0;
    }

    public int getCurrentIndex() {
        return currentFrame;
    }

    public BufferedImage getCurrentFrame() {
        return frames.get(currentFrame);
    }

    public void update() {
        if (currentFrame < frames.size()-1){
            if (currentReps == repsMax) {
                currentFrame++;
            }
            currentReps++;
            if (currentReps > repsMax){
                currentReps = 0;
            }
        }
    }

    public boolean isFinished() {
        return currentFrame == frames.size() - 1;
    }
}


