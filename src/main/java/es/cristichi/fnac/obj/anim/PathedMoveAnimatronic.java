package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class implements {@link Animatronic#onMovementOppSuccess(CameraMap, Camera, Random)} with a simple
 * movement that makes the Animatronic move on a set path. When the end is reached, it goes back to a set Camera.
 */
public abstract class PathedMoveAnimatronic extends Animatronic{
    protected final List<String> orderedCamPath;
    protected final String returnToCamera;

    public PathedMoveAnimatronic(String name, double secInterval, Map<Integer, Integer> iaDuringNight,
                                 int maxIaLevel, String camImgPath, Jumpscare jumpscare,
                                 List<String> orderedCamPath, String returnToCamera, Color debugColor) throws ResourceException {
        super(name, secInterval, iaDuringNight, maxIaLevel, camImgPath, jumpscare, debugColor);
        this.orderedCamPath = orderedCamPath;
        this.returnToCamera = returnToCamera;
    }

    @Override
    public MoveOppReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        LinkedList<String> connections = currentLoc.getConnections();
        boolean passedCurrCam = false;
        for (String cam : orderedCamPath){
            if (passedCurrCam){
                for (String connection : connections){
                    if (cam.equals(connection)){
                        return new MoveOppReturn(cam, null);
                    }
                }
            } else {
                if (cam.equals(currentLoc.getName())){
                    passedCurrCam = true;
                }
            }
        }
        return new MoveOppReturn(returnToCamera, null);
    }
}
