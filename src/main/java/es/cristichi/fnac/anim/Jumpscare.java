package es.cristichi.fnac.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.GifAnimation;
import es.cristichi.fnac.io.GifFrame;
import es.cristichi.fnac.io.Resources;
import kuusisto.tinysound.Sound;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
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
                    new Jumpscare(Resources.loadGif("office/powerOutage.gif", false), 0, null, -1, JumpscareVisualSetting.FILL_SCREEN));
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
    private final JumpscareVisualSetting jumpscareVisual;
    
    private final LinkedList<Runnable> onFinish;
    private Boolean soundFinished;
    
    /**
     * Creates a new {@link Jumpscare} with the given data. This loads the resources specified.
     *
     * @param gifAnimation    Gifanimation. You can use {@link Resources#loadGif(String, boolean)} if the
     *                        GIF file is in the resources.
     * @param camsDownFrame   Index of the frame of the GIF which will force the player to put cams down if they are up.
     *                        In case of doubt, put 0 so the first frame will force cams down for immersion.
     * @param sound           Sound to play during this Jumpscare.
     * @param soundStartFrame Index of the frame of the GIF which will start playing the sound. Use for dramatic effect!
     * @param jumpscareVisual Different ways the Night knows how to draw the GIF on the screen. In case of doubt just
     *                        use {@link JumpscareVisualSetting#FILL_SCREEN}.
     */
    public Jumpscare(GifAnimation gifAnimation, int camsDownFrame, @Nullable Sound sound, int soundStartFrame,
                     JumpscareVisualSetting jumpscareVisual) {
        this.gifAnimation = gifAnimation;
        this.camsDownFrame = camsDownFrame;
        this.sound = sound;
        this.soundStartFrame = soundStartFrame;
        this.jumpscareVisual = jumpscareVisual;
        
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
                if (gifAnimation.isFinished()) {
                    for (Runnable onFinished : this.onFinish) {
                        onFinished.run();
                    }
                    this.onFinish.clear();
                }
            });
        }
        
        // Preloading the stuff so they are in the cache
        for (int i = 0; i < this.gifAnimation.size(); i++) {
            int finalI = i;
            new Thread(() -> gifAnimation.getCombinedImage(finalI),
                    "preload-" + gifAnimation.getFilePath() + ">" + i).start();
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
    
    /**
     * @return The full width and height of the GIF as defined in its logical screen.
     */
    public Dimension getLogicalScreen() {
        return gifAnimation.getLogicalScreen();
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
     * @return {@code true} if both the {@link GifAnimation} for this Jumpscare and the {@link Sound} are finished.
     * {@code false} otherwise.
     */
    public boolean isFinished(){
        System.out.printf("%s && %s = %s.%n", gifAnimation.isFinished()?"YEP":"NOP", soundFinished?"YEP":"NOP",
                (gifAnimation.isFinished() && soundFinished)?"YEP":"NOP");
        return gifAnimation.isFinished() && soundFinished;
    }
    
    /**
     * @return {@code true} if this is the exact frame when the Sound should be played and that Sound has not
     * been played for this Jumpscare previously.
     */
    public boolean isFrameToPlaySound() {
        if (soundDone) {
            return false;
        }
        return gifAnimation.getCurrentFrame() == soundStartFrame;
    }
    
    /**
     * @return {@code true} if the player should not be watching Cameras during this frame of the Jumpscare.
     */
    public boolean shouldCamsBeDown() {
        return gifAnimation.getCurrentFrame() >= camsDownFrame;
    }
    
    /**
     * This method updates the current state of the Jumpscare, such as whether cams should be down or the Sound
     * should be played, then gets the information to draw on the screen.
     * @param tick This Night's current in-game tick.
     * @param fps This Night's current FPS, used to convert seconds to in-game ticks.
     * @return Ordered {@link List<GifFrame>} of images that must be printed on the screen, one on
     *      * top of the previous on the List that must be printed on this tick of the Night.
     */
    public BufferedImage updateAndGetFrame(int tick, int fps) {
        BufferedImage frames = gifAnimation.updateAndGetFrame(tick, fps);
        if (gifAnimation.isFinished() && (soundFinished == null || soundFinished)) {
            for (Runnable onFinished : onFinish) {
                onFinished.run();
            }
            onFinish.clear();
        }
        return frames;
    }
}
