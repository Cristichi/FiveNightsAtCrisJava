package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class implements {@link Animatronic} with a default movement that simply avoids a list of Camera names.
 */
public abstract class AvoidCamsAnimatronic extends Animatronic {
    protected final List<String> forbiddenCameras;

    public AvoidCamsAnimatronic(String name, double secInterval, HashMap<Integer, Integer> iaDuringNight,
                                int maxIaLevel, String camImgPath, String jumpscareGifPath, int jumpscareRepFrames,
                                @Nullable List<String> forbiddenCameras, Color debugColor) throws ResourceNotFound {
        super(name, secInterval, iaDuringNight, maxIaLevel, camImgPath, jumpscareGifPath, jumpscareRepFrames, debugColor);
        this.forbiddenCameras = Objects.requireNonNullElseGet(forbiddenCameras, () -> new ArrayList<>(0));
    }

    @Override
    public String onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        return connections.get(rng.nextInt(connections.size()));
    }
}
