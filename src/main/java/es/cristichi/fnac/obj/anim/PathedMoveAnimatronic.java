package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class PathedMoveAnimatronic extends Animatronic{
    protected final List<String> orderedCamPath;

    public PathedMoveAnimatronic(String name, double secInterval, HashMap<Integer, Integer> iaDuringNight,
                                 int maxIaLevel, String camImgPath, String jumpscareGifPath, int jumpscareRepFrames,
                                 List<String> orderedCamPath) throws ResourceNotFound {
        super(name, secInterval, iaDuringNight, maxIaLevel, camImgPath, jumpscareGifPath, jumpscareRepFrames);
        this.orderedCamPath = orderedCamPath;
    }

    @Override
    public String onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        LinkedList<String> connections = currentLoc.getConnections();
        for (String cam : orderedCamPath){
            for (String connection : connections){
                if (cam.equals(connection)){
                    return cam;
                }
            }
        }
        throw new AnimatronicException("Paco has no suitable path to follow.");
    }
}
