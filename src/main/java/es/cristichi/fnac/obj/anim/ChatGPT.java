package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronicData;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

@CustomNightAnimatronic(name = "ChatGPT", portraitPath = "anims/chatgpt/portrait.png",
        restStart = "storage", tutStart = "cam2",
        restDesc = "ChatGPT starts at the Storage, and decides alternatively whether they want to move randomly or " +
                "using a chosen path to either your left or right door.",
        tutDesc = "ChatGPT starts at cam2, and decides alternatively whether they want to move randomly or " +
                "using a chosen path to either your left or right door."
)
public class ChatGPT extends AnimatronicDrawing {
    protected final List<String> forbiddenCameras;
    protected final List<List<String>> camPaths;
    protected boolean usingRoamingMove;
    protected BufferedImage buildingCamImg;
    protected boolean building;

    public ChatGPT(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty() ? data.name() : data.name() + " (" + data.variant() + ")", Map.of(0, data.ai()),
                false, false,
                switch (data.mapType()) {
                    case TUTORIAL -> List.of();
                    case RESTAURANT -> List.of("offices");
                },
                switch (data.mapType()) {
                    case TUTORIAL -> List.of(
                            List.of("cam1", "cam2", "cam4", "rightDoor"),
                            List.of("cam2", "cam1", "cam3", "leftDoor")
                    );
                    case RESTAURANT -> List.of(
                            List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                            List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                    );
                },
                0f, data.rng());
    }

    public ChatGPT(String name, Map<Integer, Integer> aiDuringNight, boolean cameraStalled, boolean globalCameraStalled,
                   List<String> forbiddenCameras, List<List<String>> camPaths, float fakeMovementSoundChance,
                   Random rng) throws ResourceException {
        super(name, 6, 4, aiDuringNight, 20, cameraStalled, globalCameraStalled, "anims/chatgpt/camImg.png",
                new Jumpscare("anims/chatgpt/jumpscare.gif", 0,
                        Resources.loadSound("anims/chatgpt/sounds/jumpscare.wav", "chatGptJump.wav"), 2,
                        JumpscareVisualSetting.STRETCHED), fakeMovementSoundChance, Color.BLUE, rng);

        this.forbiddenCameras = new ArrayList<>(forbiddenCameras);
        this.camPaths = new ArrayList<>(camPaths);
        this.sounds.put("move", Resources.loadSound("anims/chatgpt/sounds/move.wav", "chatGptMove.wav"));
        this.sounds.put("moveRoam", Resources.loadSound("anims/chatgpt/sounds/moveRoam.wav", "chatGptMoveRoam.wav"));
        this.sounds.put("building", Resources.loadSound("anims/chatgpt/sounds/building.wav", "chatGptBuild.wav"));
        this.buildingCamImg = Resources.loadImageResource("anims/chatgpt/camImgOnlyBody.png");

        this.usingRoamingMove = true;
        this.building = true;
    }

    @Nullable
    @Override
    public BufferedImage showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng) {
        if (building){
            return buildingCamImg;
        }
        return super.showOnCam(tick, fps, openDoor, cam, rng);
    }

    @Override
    public MoveOppRet onMovementOpportunityAttempt(Camera currentCam, boolean beingLookedAt, boolean camsUp,
                                                   boolean isOpenDoor, Random rng) {
        MoveOppRet attempt = super.onMovementOpportunityAttempt(currentCam, beingLookedAt, camsUp, isOpenDoor, rng);
        if (building && attempt.move()){
            building = false;
            return new MoveOppRet(false, sounds.getOrDefault("building", null));
        }
        return attempt;
    }

    @Override
    public MoveSuccessRet onMovementOppSuccess(CameraMap map, Camera currentLoc, Random rng) throws AnimatronicException {
        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        if (usingRoamingMove && !connections.isEmpty()) {
            int random = rng.nextInt(connections.size());
            usingRoamingMove = false;
            return new MoveSuccessRet(connections.get(random),
                    sounds.getOrDefault("moveRoam", sounds.getOrDefault("move", null)));
        }
        usingRoamingMove = true;
        Collections.shuffle(camPaths, rng);
        for (List<String> path : camPaths) {
            for (int i = 0; i < path.size(); i++) {
                String cam = path.get(i);
                if (currentLoc.getName().equals(cam)) {
                    if (path.size() > i + 1) {
                        return new MoveSuccessRet(path.get(i + 1), sounds.getOrDefault("move", null));
                    } else {
                        return new MoveSuccessRet(path.get(0), sounds.getOrDefault("move", null));
                    }
                }
            }
        }
        throw new AnimatronicException(
                "Animatronic " + name + " is not at a Camera within any of its paths, and there are no connected " +
                        "Cameras they are not avoiding.");
    }
}
