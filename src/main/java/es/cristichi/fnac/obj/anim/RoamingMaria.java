package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisual;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RoamingMaria extends AvoidCamsAnimatronicDrawing {
    private final double secsToKill;

    public RoamingMaria(double secInterval, Map<Integer, Integer> aiDuringNight, boolean cameraStalled,
                        boolean globalCameraStalled, List<String> forbiddenCams,
                        double secsToKill) throws ResourceException {
        super("María", secInterval, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/maria/camImg.png",
                new Jumpscare("anims/maria/jumpscare.gif", 0,
                        Resources.loadSound("anims/maria/sounds/jumpscare.wav", "mariaJump.wav"),
                        0, JumpscareVisual.CENTERED),
                        forbiddenCams, Color.YELLOW);
        this.secsToKill = secsToKill;

        this.sounds.put("move", Resources.loadSound("anims/maria/sounds/move.wav", "mariaMove.wav"));
    }

    @Override
    public MoveOppReturn onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) {
        MoveOppReturn ret = super.onMovementOppSuccess(map, currentLoc, rng);
        return new MoveOppReturn(ret.moveToCam(), sounds.getOrDefault("move", null));
    }

    @Override
    public TickReturn onTick(int tick, int fps, boolean camsUp, boolean doorOpen, Camera cam, Random rng) {
        if (doorOpen){
            if (startKillTick == null){
                startKillTick = tick;
            } else {
                if (Math.round(secsToKill *fps) <= tick-startKillTick){
                    kill = true;
                    return new TickReturn(true, null, 0, 0);
                }
            }
        } else {
            kill = false;
            startKillTick = null;
            if (cam.isLeftDoor()){
                return new TickReturn(false, sounds.getOrDefault("knock", null), 1, -1);
            } else if (cam.isRightDoor()){
                return new TickReturn(false, sounds.getOrDefault("knock", null), 1, 1);
            }
        }
        return new TickReturn(false, null, 0, 0);
    }
}
