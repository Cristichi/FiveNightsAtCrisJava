package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.ResourceNotFound;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Paco extends Animatronic{
    private boolean kill = false;
    private Integer startKillTick = null;
    private final double targetPatienceKillSec = 12;
    private final List<String> orderedCamPath;

    public Paco(double secInterval, HashMap<Integer, Integer> aiDuringNight, String retreatCam,
                List<String> orderedCamPath) throws ResourceNotFound {
        super("Paco", secInterval, aiDuringNight, 20, "anims/paco/camImg.png",
                "anims/paco/jumpscare.gif", 1, null);

        this.orderedCamPath = orderedCamPath;
    }

    @Override
    public boolean onMovementOpportunityAttempt(Camera cam, boolean isOpenDoor, Random rng) {
        if (kill || cam.isLeftDoorOfOffice() || cam.isRightDoorOfOffice()){
            return false;
        }
        return super.onMovementOpportunityAttempt(cam, isOpenDoor, rng);
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

    @Override
    public boolean onJumpscareAttempt(int tick, int fps, boolean camsUp, boolean doorOpen, Camera cam, Random rng) {
        if (doorOpen){
            if (startKillTick == null){
                startKillTick = tick;
            } else {
                if (Math.round(targetPatienceKillSec*fps) <= tick-startKillTick){
                    kill = true;
                    return true;
                }
            }
        } else {
            kill = false;
            startKillTick = null;
        }
        return false;
    }

    @Override
    public boolean showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng) {
        return !kill;
    }
}
