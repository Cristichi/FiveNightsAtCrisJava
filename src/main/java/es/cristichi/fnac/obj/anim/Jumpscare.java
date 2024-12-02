package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import kuusisto.tinysound.Sound;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class Jumpscare {
    private final Sound sound;
    private final int soundStartFrame;
    private List<BufferedImage> frames;
    private final int repsMax;
    private int currentReps;
    private int currentFrame;

    private final LinkedList<Runnable> onFinish;
    private Boolean soundFinished;

    public Jumpscare(String filepath, int reps, Runnable... onFinish) throws ResourceException {
        this(filepath, reps, null, -1, onFinish);
    }

    public Jumpscare(String filepath, int reps, @Nullable Sound sound, int soundStartFrame, Runnable... onFinish) throws ResourceException {
        this.repsMax = Math.max(1, reps);
        this.currentFrame = 0;
        this.sound = sound;
        this.soundStartFrame = soundStartFrame;
        loadFrames(filepath);

        this.onFinish = new LinkedList<>();
        this.onFinish.addAll(Arrays.asList(onFinish));
        if (sound == null){
            soundFinished = null;
        } else {
            soundFinished = false;
            sound.addOnEndListener(() -> {
                soundFinished = true;
                if (isFramesFinished()){
                    for (Runnable onFinished : this.onFinish){
                        onFinished.run();
                    }
                    this.onFinish.clear();
                }
            });
        }
    }

    public BufferedImage getCurrentFrame() {
        return frames.get(currentFrame<frames.size()?currentFrame:frames.size()-1);
    }

    public Sound getSound() {
        return sound;
    }

    public boolean isFrameToPlaySound(){
        return currentFrame==soundStartFrame;
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

    public void updateFrame() {
        if (currentFrame < frames.size()){
            if (currentReps == repsMax) {
                currentFrame++;
            }
            currentReps++;
            if (currentReps > repsMax){
                currentReps = 0;
            }
        } else if (isFramesFinished() && (soundFinished == null || soundFinished)){
            for (Runnable onFinished : onFinish){
                onFinished.run();
            }
            onFinish.clear();
        }
    }

    public void addOnFinishedListener(Runnable onFinished){
        this.onFinish.add(onFinished);
    }

    public boolean isFramesFinished() {
        return currentFrame == frames.size();
    }
}


