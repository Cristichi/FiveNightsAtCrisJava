package es.cristichi.fnac.obj;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.GifAnimation;
import es.cristichi.fnac.io.GifDisposalMethod;
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
    private final JumpscareVisual jumpscareVisual;
    private int currentFrame;
    private int currentFrameStartTick;

    private final LinkedList<Runnable> onFinish;
    private Boolean soundFinished;

    public Jumpscare(String filepath, int camsDownFrame, @Nullable Sound sound, int soundStartFrame, JumpscareVisual jumpscareVisual) throws ResourceException {
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

    public JumpscareVisual getVisualSetting(){
        return jumpscareVisual;
    }

    public GifFrame[] getCurrentFrame() {
        return getCombinedFrames(currentFrame);
    }

    public GifFrame[] getSetFrameDEBUG(int frame) {
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

    public GifFrame[] updateAndGetFrame(int tick, int fps) {
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

    private GifFrame[] getCombinedFrames(int frameIndex) {
        if (frameIndex >= frames.size()) {
            frameIndex = frames.size()-1;
        }

        List<GifFrame> combinedFrames = new ArrayList<>(frames.size());
        for (int i = 0; i <= frameIndex; i++) {
            GifFrame frame = frames.get(i);
            combinedFrames.add(frame);
            if (frame.disposalMethod().equals(GifDisposalMethod.UNSPECIFIED)
                    || frame.disposalMethod().equals(GifDisposalMethod.RESTORE_TO_BACKGROUND_COLOR)) {
                combinedFrames.clear();
                combinedFrames.add(frame);
            } else if (!frame.disposalMethod().equals(GifDisposalMethod.DO_NOT_DISPOSE)) {
                System.err.printf("%s not supported. Check frame %d of one of the Jumpscares. Defaulting to DO_NOT_DISPOSE.%n",
                        frame.disposalMethod(), frameIndex);
            }
        }

        return combinedFrames.toArray(new GifFrame[0]);
    }

    public void addOnFinishedListener(Runnable onFinished) {
        this.onFinish.add(onFinished);
    }
}
