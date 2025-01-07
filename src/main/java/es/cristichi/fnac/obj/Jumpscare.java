package es.cristichi.fnac.obj;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.GifAnimation;
import es.cristichi.fnac.io.GifFrame;
import es.cristichi.fnac.io.Resources;
import kuusisto.tinysound.Sound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Jumpscare, which is a combination of a GIF file and a Sound, alongside some parameters.
 */
public class Jumpscare {
    /**
     * Default Jumpscare commonly used for when the player runs out of power.
     */
    private static Jumpscare powerOutage;
    
    /**
     * @return The default Jumpscare commonly used for when the player runs out of power.
     * @throws ResourceException If the Jumpscare uses resources that cannot be loaded from the resources.
     */
    public static Jumpscare getPowerOutageJumpscare() throws ResourceException {
        if (powerOutage == null) {
            return setPowerOutageJumpscare(
                    new Jumpscare(Resources.loadGif("office/powerOutage.gif"), 0, null, -1, JumpscareVisualSetting.STRETCHED));
        }
        return powerOutage;
    }
    
    /**
     * Sets a new default Jumpscare that will be used by default.
     *
     * @param jumpscare New default Jumpscare, already loaded.
     * @return The same Jumpscare for chaining.
     */
    public static Jumpscare setPowerOutageJumpscare(Jumpscare jumpscare) {
        powerOutage = jumpscare;
        return jumpscare;
    }
    
    private final Sound sound;
    private final int soundStartFrame;
    private boolean soundDone;
    private final int camsDownFrame;
    private final GifAnimation gifAnimation;
    private final HashMap<Integer, List<GifFrame>> cacheFrames;
    private final JumpscareVisualSetting jumpscareVisual;
    private int currentFrame;
    private int currentFrameStartTick;
    
    private final LinkedList<Runnable> onFinish;
    private Boolean soundFinished;
    
    /**
     * Creates a new {@link Jumpscare} with the given data. This loads the resources specified.
     *
     * @param gifAnimation    Gifanimation. You can use {@link es.cristichi.fnac.io.Resources#loadGif(String)} if the
     *                        GIF file is in the resources.
     * @param camsDownFrame   Index of the frame of the GIF which will force the player to put cams down if they are up.
     *                        In case of doubt, put 0 so the first frame will force cams down for immersion.
     * @param sound           Sound to play during this Jumpscare.
     * @param soundStartFrame Index of the frame of the GIF which will start playing the sound. Use for dramatic effect!
     * @param jumpscareVisual Different ways the Night knows how to draw the GIF on the screen. In case of doubt just
     *                        use {@link JumpscareVisualSetting#STRETCHED}.
     */
    public Jumpscare(GifAnimation gifAnimation, int camsDownFrame, @Nullable Sound sound, int soundStartFrame,
                     JumpscareVisualSetting jumpscareVisual) {
        this.gifAnimation = gifAnimation;
        this.cacheFrames = new HashMap<>(gifAnimation.size());
        this.camsDownFrame = camsDownFrame;
        this.sound = sound;
        this.soundStartFrame = soundStartFrame;
        this.jumpscareVisual = jumpscareVisual;
        
        this.currentFrame = 0;
        this.currentFrameStartTick = 0;
        this.onFinish = new LinkedList<>();
        
        if (sound == null) {
            soundFinished = null;
            soundDone = true;
        } else {
            soundDone = false;
            soundFinished = false;
            sound.addOnEndListener(() -> {
                soundDone = true;
                soundFinished = true;
                if (isFramesFinished()) {
                    for (Runnable onFinished : this.onFinish) {
                        onFinished.run();
                    }
                    this.onFinish.clear();
                }
            });
        }
        
        // Preloading the stuff so they are in the cache
        for (int i = 0; i <= this.gifAnimation.size(); i++) {
            int finalI = i;
            new Thread(() -> getCombinedFrames(finalI), "preload-" + gifAnimation.getFilePath() + ">" + i).start();
        }
    }
    
    /**
     * Adds a {@link Runnable} to run when the Jumpscare is done, like finishing a Night.
     * @param onFinished Code to run when this Jumpscare finishes.
     */
    public void addOnFinishedListener(Runnable onFinished) {
        this.onFinish.add(onFinished);
    }
    
    /**
     * @return The preferred way to draw this Jumpscare's GIF on the screen.
     */
    public JumpscareVisualSetting getVisualSetting() {
        return jumpscareVisual;
    }
    
    private List<GifFrame> getCurrentFrame() {
        return getCombinedFrames(currentFrame);
    }
    
    /**
     * @return The full width of the GIF. For reasons, I'm supposing the last frame is full-sized.
     */
    public int getFullWidth() {
        return gifAnimation.get(gifAnimation.size() - 1).image().getWidth();
    }
    
    /**
     * @return The full height of the GIF. For reasons, I'm supposing the last frame is full-sized.
     */
    public int getFullHeight() {
        return gifAnimation.get(gifAnimation.size() - 1).image().getHeight();
    }
    
    /**
     * @return {@code true} if this Jumpscare's last frame has already passed (with its delay included).
     */
    public boolean isFramesFinished() {
        return currentFrame == gifAnimation.size();
    }
    
    /**
     * @param markAsPlayed Whether or not to mark this Sound as played.
     * @return The Sound that this Jumpscare wants played during it.
     */
    public Sound getSound(boolean markAsPlayed) {
        soundDone = markAsPlayed;
        return sound;
    }
    
    /**
     * @return {@code true} if this is the exact frame when the Sound should be played and that Sound has not
     * been played for this Jumpscare previously.
     */
    public boolean isFrameToPlaySound() {
        if (soundDone) {
            return false;
        }
        return currentFrame == soundStartFrame;
    }
    
    /**
     * @return {@code true} if the player should not be watching Cameras during this frame of the Jumpscare.
     */
    public boolean shouldCamsBeDown() {
        return currentFrame >= camsDownFrame;
    }
    
    /**
     * This method updates the current state of the Jumpscare, such as whether cams should be down or the Sound
     * should be played, then gets the information to draw on the screen.
     * @param tick This Night's current in-game tick.
     * @param fps This Night's current FPS, used to convert seconds to in-game ticks.
     * @return Ordered {@link List<GifFrame>} of images that must be printed on the screen, one on
     *      * top of the previous on the List that must be printed on this tick of the Night.
     */
    public List<GifFrame> updateAndGetFrame(int tick, int fps) {
        GifFrame frame = gifAnimation.get(currentFrame >= gifAnimation.size() ? gifAnimation.size() - 1 : currentFrame);
        if (currentFrameStartTick == 0) {
            currentFrameStartTick = tick;
        } else if (tick >= currentFrameStartTick + frame.delaySecs() * fps) {
            currentFrame++;
            currentFrameStartTick = tick;
            if (isFramesFinished() && (soundFinished == null || soundFinished)) {
                for (Runnable onFinished : onFinish) {
                    onFinished.run();
                }
                onFinish.clear();
            }
        }
        
        return getCombinedFrames(currentFrame);
    }
    
    /**
     * @param frameIndex Current frame.
     * @return Current frame (or the last one if frameIndex is out of the upper bound) plus any frames that should be
     * visible according to the frames' disposal method.
     */
    private List<GifFrame> getCombinedFrames(int frameIndex) {
        if (frameIndex >= gifAnimation.size()) {
            frameIndex = gifAnimation.size() - 1; // Clamp to the last frame
        }
        
        if (cacheFrames.containsKey(frameIndex)) {
            return cacheFrames.get(frameIndex);
        }
        
        List<GifFrame> visibleFrames = new ArrayList<>();
        boolean resetVisibility = false;
        
        for (int i = 0; i < frameIndex; i++) {
            GifFrame frame = gifAnimation.get(i);
            
            // Disposal methods
            switch (frame.disposalMethod()) {
                case RESTORE_TO_BACKGROUND_COLOR -> visibleFrames.clear();
                case UNSPECIFIED, DO_NOT_DISPOSE -> visibleFrames.add(frame);
                default -> throw new IllegalStateException(
                        "Unexpected or unsupported disposal method: " + frame.disposalMethod());
            }
        }
        visibleFrames.add(gifAnimation.get(frameIndex));
        
        cacheFrames.put(frameIndex, visibleFrames);
        return visibleFrames;
    }
}
