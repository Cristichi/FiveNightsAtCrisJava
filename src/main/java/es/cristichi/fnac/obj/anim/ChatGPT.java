package es.cristichi.fnac.obj.anim;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;
import es.cristichi.fnac.obj.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.obj.cnight.CustomNightAnimatronicData;
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
    protected boolean usingPathedMove;
    protected BufferedImage buildingCamImg;
    protected boolean building;

    public ChatGPT(CustomNightAnimatronicData data) throws ResourceException {
        this(data.variant().isEmpty() ? data.name() : data.name() + " (" + data.variant() + ")", Map.of(0, data.ai()),
                false, false, List.of(),
                switch (data.mapType()) {
                    case TUTORIAL -> List.of(
                            List.of("cam1", "cam2", "cam4", "rightDoor"),
                            List.of("cam2", "cam1", "cam3", "leftDoor")
                    );
                    case RESTAURANT -> List.of(
                            List.of("storage", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                            List.of("storage", "dining area", "corridor 2", "corridor 4", "rightDoor")
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
                        JumpscareVisualSetting.STRETCHED), Color.BLUE, rng);

        this.forbiddenCameras = new ArrayList<>(forbiddenCameras);
        this.camPaths = new ArrayList<>(camPaths);
        this.sounds.put("move", Resources.loadSound("anims/chatgpt/sounds/move.wav", "chatGptMove.wav"));
        this.sounds.put("moveRoam", Resources.loadSound("anims/chatgpt/sounds/moveRoam.wav", "chatGptMoveRoam.wav"));
        this.sounds.put("building", Resources.loadSound("anims/chatgpt/sounds/building.wav", "chatGptBuild.wav"));
        this.buildingCamImg = Resources.loadImageResource("anims/chatgpt/camImgOnlyBody.png");

        this.usingPathedMove = false;
        this.building = true;
    }

    @Nullable
    @Override
    public ShowOnCamInfo showOnCam(int tick, int fps, boolean openDoor, Camera cam, Random rng) {
        ShowOnCamInfo info = super.showOnCam(tick, fps, openDoor, cam, rng);
        if (building) {
            return new ShowOnCamInfo(buildingCamImg, null);
        }
        return info;
    }

    @Override
    public MoveOppInfo onMoveOppAttempt(Camera currentCam, boolean beingLookedAt, boolean camsUp, boolean isOpenDoor,
                                       Random rng) {
        MoveOppInfo attempt = super.onMoveOppAttempt(currentCam, beingLookedAt, camsUp, isOpenDoor, rng);
        if (building && attempt.move()) {
            building = false;
            return new MoveOppInfo(false, sounds.getOrDefault("building", null));
        }
        return attempt;
    }

    @Override
    public MoveSuccessInfo onMoveOppSuccess(CameraMap map, Camera currentLoc, Random rng) throws AnimatronicException {
        if (usingPathedMove || currentLoc.isRightDoor() ||currentLoc.isLeftDoor()) {
            // We should be doing pathed move, or can't do roaming move
            usingPathedMove = false;
            Collections.shuffle(camPaths, rng);
            for (List<String> path : camPaths) {
                for (int i = 0; i < path.size(); i++) {
                    String cam = path.get(i);
                    if (currentLoc.getNameId().equals(cam)) {
                        if (path.size() > i + 1) {
                            return new MoveSuccessInfo(path.get(i + 1), sounds.getOrDefault("move", null));
                        } else {
                            return new MoveSuccessInfo(path.get(0), sounds.getOrDefault("move", null));
                        }
                    }
                }
            }
        }

        LinkedList<String> connections = currentLoc.getConnections();
        connections.removeIf(forbiddenCameras::contains);
        if (!connections.isEmpty()) {
            // We should be doing roaming move
            usingPathedMove = true;
            int random = rng.nextInt(connections.size());
            return new MoveSuccessInfo(connections.get(random),
                    sounds.getOrDefault("moveRoam", sounds.getOrDefault("move", null)));
        }

        // Oh no, we can't do either
        throw new AnimatronicException(
                "Animatronic " + nameId + " is not at a Camera within any of its paths, and there are no connected " +
                        "Cameras they are not avoiding.");
    }
}
