package es.cristichi.fnac.io;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the data of a GIF file that the game needs.
 */
public class GifAnimation extends ArrayList<GifFrame> implements NightDrawableImage {
    /**
     * Information of the path to the file, for debugging purposes.
     */
    protected final String filePath;
    /**
     * Size of the canvas in which the image is normally drawn.
     */
    protected final Dimension logicalScreen;
    /**
     * Determines whether this GIF should be looping.
     */
    protected final boolean loop;
    /**
     * Determines whether this GIF is finished. If it loops, this is never false.
     */
    protected boolean finished;
    /**
     * Provides a cache, assuming each given frame is always the same (which it is). Increases RAM usage but also
     * performance significantly for looping GIFs.
     */
    protected final HashMap<Integer, BufferedImage> cacheFrameImages;
    /**
     * Index of the current frame being displayed.
     */
    protected int currentFrame;
    /**
     * Tick when the current frame was first printed.
     */
    protected int currentFrameStartTick;
    
    /**
     * Creates a new GifAnimation with the given frames.
     * @param filePath File path, for debugging purposes only.
     * @param frames List of {@link GifFrame}s.
     * @param logicalScreenWidth Width of the logical screen.
     * @param logicalScreenHeight Height of the logical screen.
     * @param loop Whether this GIF should be looping.
     */
    public GifAnimation(String filePath, List<GifFrame> frames, int logicalScreenWidth, int logicalScreenHeight, boolean loop){
        super(frames);
        this.filePath = filePath;
        this.logicalScreen = new Dimension(logicalScreenWidth, logicalScreenHeight);
        this.loop = loop;
        cacheFrameImages = new HashMap<>(frames.size());
        currentFrame = 0;
        currentFrameStartTick = 0;
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
    
    /**
     * @return Index of the current frame being displayed.
     */
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    /**
     * @return {@code true} if this Jumpscare's last frame has already passed (with its delay included).
     */
    public boolean isFinished() {
        return finished;
    }
    
    /**
     * This method updates the current state of the GifAnimation by checking if the current frame should loop back
     * or stick to the last frame of the animation. Then, if the frame should be moved on the next tick, it increases
     * the current frame so that the next call to this method prints the next one.
     * @param tick This Night's current in-game tick.
     * @param fps This Night's current FPS, used to convert seconds to in-game ticks.
     * @return A {@link BufferedImage} of the image to show, or {@code null} if no image should be shown.
     */
    @Nullable
    public BufferedImage getImageForTick(int tick, int fps) {
        GifFrame frame = get(currentFrame);
        if (currentFrameStartTick == 0) {
            currentFrameStartTick = tick;
        } else if (tick >= currentFrameStartTick + frame.delaySecs() * fps) {
            currentFrame++;
            if (currentFrame >= size()){
                if (loop){
                    currentFrame = 0;
                } else {
                    currentFrame = size()-1;
                    finished = true;
                }
            }
            currentFrameStartTick = tick;
        }
        
        return getCombinedImage(currentFrame);
    }
    
    /**
     * Returns a fully composed BufferedImage for the given frame index using the {@link #getLogicalScreen()}
     * Dimension. This image is built by drawing all the visible GifFrames on top of each other. It also caches
     * the resulting image related to this frame index so that it allows preloading, as well as to increase
     * performance significantly on looping images.
     *
     * @param frameIndex the index of the frame to combine
     * @return a BufferedImage of size equal to the logical screen with the composed image.
     * @throws IndexOutOfBoundsException If the frameIndex given is not a valid frame of this GifAnimation.
     */
    public BufferedImage getCombinedImage(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= size()){
            throw new IndexOutOfBoundsException(
                    "You cannot get the combined image of frame %d. The number of frames is %d."
                            .formatted(frameIndex, size()));
        }
        if (cacheFrameImages.containsKey(frameIndex)) {
            return cacheFrameImages.get(frameIndex);
        }
        
        List<GifFrame> visibleFrames = new ArrayList<>();
        
        for (int i = 0; i < frameIndex; i++) {
            GifFrame frame1 = get(i);
            
            switch (frame1.disposalMethod()) {
                case RESTORE_TO_BACKGROUND_COLOR -> visibleFrames.clear();
                case UNSPECIFIED, DO_NOT_DISPOSE -> visibleFrames.add(frame1);
                default -> throw new IllegalStateException(
                        "Unexpected or unsupported disposal method: " + frame1.disposalMethod());
            }
        }
        visibleFrames.add(get(frameIndex));
        
        BufferedImage combinedImage = new BufferedImage(logicalScreen.width,logicalScreen.height,
                BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = combinedImage.createGraphics();
        
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, logicalScreen.width, logicalScreen.height);
        g2d.setComposite(AlphaComposite.SrcOver);
        
        for (GifFrame frame : visibleFrames) {
            g2d.drawImage(frame.image(), frame.offsetX(), frame.offsetY(), null);
        }
        
        g2d.dispose();
        
        cacheFrameImages.put(frameIndex, combinedImage);
        return combinedImage;
    }
}
