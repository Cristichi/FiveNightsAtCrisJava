package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class PathedMoveAnimatronic extends Animatronic{
    protected final List<String> orderedCamPath;
    protected final String returnToCamera;

    public PathedMoveAnimatronic(String name, double secInterval, HashMap<Integer, Integer> iaDuringNight,
                                 int maxIaLevel, String camImgPath, String jumpscareGifPath, int jumpscareRepFrames,
                                 List<String> orderedCamPath, String returnToCamera, Color debugColor) throws ResourceException {
        super(name, secInterval, iaDuringNight, maxIaLevel, camImgPath, jumpscareGifPath, jumpscareRepFrames, debugColor);
        this.orderedCamPath = orderedCamPath;
        this.returnToCamera = returnToCamera;
    }

    @Override
    public String onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        LinkedList<String> connections = currentLoc.getConnections();
        boolean passedCurrCam = false;
        for (String cam : orderedCamPath){
            if (passedCurrCam){
                for (String connection : connections){
                    if (cam.equals(connection)){
                        return cam;
                    }
                }
            } else {
                if (cam.equals(currentLoc.getName())){
                    passedCurrCam = true;
                }
            }
        }
        return returnToCamera;
    }
}
