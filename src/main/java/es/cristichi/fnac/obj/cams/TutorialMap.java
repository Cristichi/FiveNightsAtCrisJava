package es.cristichi.fnac.obj.cams;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;

import java.util.List;

public class TutorialMap extends CameraMap {
    public TutorialMap() throws ResourceException {
        super(Resources.loadImageResource("night/tutorial/map.png"), "cam1");
        addAll(
                new Camera.Builder()
                .setName("cam1")
                .setCamBackground("night/tutorial/cam1.jpg")
                .setOnMapLoc(113, 111, 378, 177)
                .setSoundVolume(0.5)
                .setSoundPan(-1)
                .addConnection("cam2", "cam3")
                .build(),
                new Camera.Builder()
                .setName("cam2")
                .setCamBackground("night/tutorial/cam2.jpg")
                .setOnMapLoc(491, 117, 379, 177)
                .setSoundVolume(0.5)
                .setSoundPan(1)
                .addConnection("cam1", "cam4")
                .build(),
                new Camera.Builder()
                .setName("cam3")
                .setCamBackground("night/tutorial/cam3.jpg")
                .setOnMapLoc(134, 287, 167, 571)
                .setSoundVolume(1)
                .setSoundPan(-1)
                .addConnection("cam1")
                .connectToOfficeLeft()
                .build(),
                new Camera.Builder()
                .setName("cam4")
                .setCamBackground("night/tutorial/cam4.jpg")
                .setOnMapLoc(720, 296, 141, 586)
                .setSoundVolume(1)
                .setSoundPan(1)
                .addConnection("cam2")
                .connectToOfficeRight()
                .build()
        );
    }

    public void addCam1Animatronics(AnimatronicDrawing... anims){
        get("cam1").getAnimatronicsHere().addAll(List.of(anims));
    }

    public void addCam2Animatronics(AnimatronicDrawing... anims){
        get("cam2").getAnimatronicsHere().addAll(List.of(anims));
    }

    public void addCam3Animatronics(AnimatronicDrawing... anims){
        get("cam3").getAnimatronicsHere().addAll(List.of(anims));
    }

    public void addCam4Animatronics(AnimatronicDrawing... anims){
        get("cam4").getAnimatronicsHere().addAll(List.of(anims));
    }
}
