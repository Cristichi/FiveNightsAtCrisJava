package es.cristichi.fnac.obj;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.GifAnimation;
import es.cristichi.fnac.io.GifFrame;
import es.cristichi.fnac.io.Resources;
import kuusisto.tinysound.Sound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Jumpscare {
    private final Sound sound;
    private final int soundStartFrame;
    private final int camsDownFrame;
    private final GifAnimation frames;
    private final JumpscareVisualSetting jumpscareVisual;
    private int currentFrame;
    private int currentFrameStartTick;

    private final LinkedList<Runnable> onFinish;
    private Boolean soundFinished;

    public Jumpscare(String filepath, int camsDownFrame, @Nullable Sound sound, int soundStartFrame, JumpscareVisualSetting jumpscareVisual) throws ResourceException {
        this.frames = Resources.loadGif(filepath);
        this.camsDownFrame = camsDownFrame;
        this.sound = sound;
        this.soundStartFrame = soundStartFrame;
        this.jumpscareVisual = jumpscareVisual;

        this.currentFrame = 0;
        this.currentFrameStartTick = 0;
        this.onFinish = new LinkedList<>();

        if (sound == null) {
            soundFinished = null;
        } else {
            soundFinished = false;
            sound.addOnEndListener(() -> {
                soundFinished = true;
                if (isFramesFinished()) {
                    for (Runnable onFinished : this.onFinish) {
                        onFinished.run();
                    }
                    this.onFinish.clear();
                }
            });
        }
    }

    public void reset() {
        this.currentFrame = 0;
        this.currentFrameStartTick = 0;
    }

    public JumpscareVisualSetting getVisualSetting(){
        return jumpscareVisual;
    }

    public List<GifFrame> getCurrentFrame() {
        return getCombinedFrames(currentFrame);
    }

    public List<GifFrame> getSetFrameDEBUG(int frame) {
        return getCombinedFrames(frame);
    }

    public int getFullWidth(){
        return frames.get(frames.size()-1).image().getWidth();
    }

    public int getFullHeight(){
        return frames.get(frames.size()-1).image().getHeight();
    }

    public boolean isFramesFinished() {
        return currentFrame == frames.size();
    }

    public Sound getSound() {
        return sound;
    }

    public boolean isFrameToPlaySound() {
        return currentFrame == soundStartFrame;
    }

    public boolean shouldCamsBeDown() {
        return currentFrame >= camsDownFrame;
    }

    public List<GifFrame> updateAndGetFrame(int tick, int fps) {
        GifFrame frame = frames.get(currentFrame>=frames.size()?frames.size()-1:currentFrame);
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
     * visible according to the frame's disposal methods.
     */
    private List<GifFrame> getCombinedFrames(int frameIndex) {
        if (frameIndex >= frames.size()) {
            frameIndex = frames.size() - 1; // Clamp to the last frame
        }

        List<GifFrame> visibleFrames = new ArrayList<>();
        boolean resetVisibility = false;

        for (int i = 0; i <= frameIndex; i++) {
            GifFrame frame = frames.get(i);

            // Disposal methods
            switch (frame.disposalMethod()) {
                case RESTORE_TO_BACKGROUND_COLOR -> visibleFrames.clear();
                case UNSPECIFIED, DO_NOT_DISPOSE -> visibleFrames.add(frame);
                default -> throw new IllegalStateException("Unexpected or unsupported disposal method: " + frame.disposalMethod());
            }
        }

        return visibleFrames;
    }

    public void addOnFinishedListener(Runnable onFinished) {
        this.onFinish.add(onFinished);
    }
}
