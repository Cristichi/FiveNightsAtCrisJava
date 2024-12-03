package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class implements {@link AnimatronicDrawing#onMovementOppSuccess(CameraMap, Camera, Random)} with a simple
 * movement that avoids a list of Camera names. From the possible Cameras, movement is completely random.
 */
public abstract class AvoidCamsAnimatronicDrawing extends AnimatronicDrawing {
    protected final List<String> forbiddenCameras;

    public AvoidCamsAnimatronicDrawing(String name, double secInterval, Map<Integer, Integer> iaDuringNight,
                                       int maxIaLevel, String camImgPath, Jumpscare jumpscare,
                                       List<String> forbiddenCameras, Color debugColor) throws ResourceException {
        super(name, secInterval, iaDuringNight, maxIaLevel, camImgPath, jumpscare, debugColor);
        this.forbiddenCameras = Objects.requireNonNullElseGet(forbiddenCameras, () -> new ArrayList<>(0));
    }

    @Override
    public MoveOppReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        int random = rng.nextInt(connections.size());
        return new MoveOppReturn(connections.get(random), null);
    }
}
