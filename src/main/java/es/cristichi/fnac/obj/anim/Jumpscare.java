package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Jumpscare {
    private List<BufferedImage> frames;
    private final int repsMax;
    private int currentReps;
    private int currentFrame;

    public Jumpscare(String filepath, int reps) throws ResourceException {
        this.repsMax = Math.max(1, reps);
        this.currentFrame = 0;
        loadFrames(filepath);
    }

    private void loadFrames(String resourcePath) throws ResourceException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(Resources.loadInputStream(resourcePath))) {
            if (stream == null) {
                throw new ResourceException("No suitable reader found for " + resourcePath + ".");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IOException("No reader for: " + resourcePath);
            }

            ImageReader reader = readers.next();
            reader.setInput(stream);

            int numFrames = reader.getNumImages(true); // Count of frames
            frames = new ArrayList<>(numFrames);

            for (int i = 0; i < numFrames; i++) {
                // Read the frame
                BufferedImage rawFrame = reader.read(i);

                // Create a new ARGB image for transparency
                BufferedImage transparentFrame = new BufferedImage(
                        rawFrame.getWidth(), rawFrame.getHeight(), BufferedImage.TYPE_INT_ARGB);

                // Draw the raw frame onto the transparent canvas
                Graphics2D g2d = transparentFrame.createGraphics();
                g2d.setComposite(AlphaComposite.Src);
                g2d.drawImage(rawFrame, 0, 0, null);
                g2d.dispose();

                // Add the processed frame to the list
                frames.add(transparentFrame);
            }
            reader.dispose();
        } catch (IOException | NullPointerException e) {
            throw new ResourceException("Error when reading \"" + resourcePath + "\".", e);
        }
    }

    public void reset() {
        this.currentFrame = 0;
        this.currentReps = 0;
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
        return currentFrame == frames.size();
    }
}


