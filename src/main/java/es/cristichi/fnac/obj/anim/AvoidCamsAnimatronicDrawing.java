package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class implements {@link AnimatronicDrawing#onMoveOppSuccess(CameraMap, Camera, Random)} with a simple
 * movement that avoids a list of Camera names. From the possible Cameras, movement is completely random.
 */
public abstract class AvoidCamsAnimatronicDrawing extends AnimatronicDrawing {
    protected final List<String> forbiddenCameras;

    public AvoidCamsAnimatronicDrawing(String name, double secInterval, double secsToKill,
                                       Map<Integer, Integer> iaDuringNight, int maxIaLevel, boolean cameraStalled,
                                       boolean globalCameraStalled, String camImgPath, Jumpscare jumpscare,
                                       List<String> forbiddenCameras, float fakeMovementSoundChance, Color debugColor,
                                       Random rng) throws ResourceException {
        super(name, secInterval, secsToKill, iaDuringNight, maxIaLevel, cameraStalled, globalCameraStalled, camImgPath,
                jumpscare, fakeMovementSoundChance, debugColor, rng);
        this.forbiddenCameras = Objects.requireNonNullElseGet(forbiddenCameras, () -> new ArrayList<>(0));
    }

    @Override
    public MoveSuccessRet onMoveOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        int random = rng.nextInt(connections.size());
        return new MoveSuccessRet(connections.get(random), sounds.getOrDefault("move", null));
    }
}
