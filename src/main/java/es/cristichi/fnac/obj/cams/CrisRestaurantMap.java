package es.cristichi.fnac.obj.cams;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;

import java.util.List;

public class CrisRestaurantMap extends CameraMap {
    public CrisRestaurantMap() throws ResourceException {
        super(Resources.loadImageResource("night/general/map.png"), "dining area");
        addAll(
                new Camera.Builder()
                        .setName("kitchen")
                        .setCamBackground("night/general/kitchen.jpg")
                        .setOnMapLoc(187, 45, 140, 70)
                        .setSoundVolume(0.2)
                        .setSoundPan(-1)
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("storage")
                        .setCamBackground("night/general/storage.jpg")
                        .setOnMapLoc(542, 111, 140, 70)
                        .setSoundVolume(0.2)
                        .setSoundPan(1)
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("dining area")
                        .setCamBackground("night/general/dining area.jpg")
                        .setOnMapLoc(168, 182, 140, 70)
                        .setSoundVolume(0.4)
                        .setSoundPan(-0.1)
                        .addConnection("main stage", "corridor 1", "corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("main stage")
                        .setCamBackground("night/general/main stage.jpg")
                        .setOnMapLoc(537, 399, 140, 70)
                        .setSoundVolume(0.4)
                        .setSoundPan(0.1)
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 1")
                        .setCamBackground("night/general/corridor 1.jpg")
                        .setOnMapLoc(314, 469, 140, 70)
                        .setSoundVolume(0.6)
                        .setSoundPan(-0.5)
                        .addConnection("dining area", "corridor 3", "staff lounge")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 2")
                        .setCamBackground("night/general/corridor 2.jpg")
                        .setOnMapLoc(456, 469, 140, 70)
                        .setSoundVolume(0.6)
                        .setSoundPan(0.5)
                        .addConnection("dining area", "corridor 4", "bathrooms")
                        .build(),
                new Camera.Builder()
                        .setName("staff lounge")
                        .setCamBackground("night/general/staff lounge.jpg")
                        .setOnMapLoc(30, 821, 140, 70)
                        .setSoundVolume(0.6)
                        .setSoundPan(-1)
                        .addConnection("corridor 1")
                        .build(),
                new Camera.Builder()
                        .setName("offices")
                        .setCamBackground("night/general/offices.jpg")
                        .setOnMapLoc(825, 840, 140, 70)
                        .setSoundVolume(0.6)
                        .setSoundPan(1)
                        .addConnection("corridor 4")  //Offices go to corridor 4, but not vice-versa
                        .build(),
                new Camera.Builder()
                        .setName("bathrooms")
                        .setCamBackground("night/general/bathrooms.jpg")
                        .setOnMapLoc(560, 734, 140, 51)
                        .setSoundVolume(1)
                        .setSoundPan(0)
                        .addConnection("corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 3")
                        .setCamBackground("night/general/corridor 3.jpg")
                        .setOnMapLoc(225, 561, 140, 70)
                        .setSoundVolume(1)
                        .setSoundPan(-1)
                        .addConnection("corridor 1")
                        .connectToOfficeLeft()
                        .build(),
                new Camera.Builder()
                        .setName("corridor 4")
                        .setCamBackground("night/general/corridor 4.jpg")
                        .setOnMapLoc(662, 568, 140, 70)
                        .setSoundVolume(1)
                        .setSoundPan(1)
                        .addConnection("corridor 2") //Offices go to corridor 4, but not vice-versa
                        .connectToOfficeRight()
                        .build()
        );
    }

    public void addCamAnimatronics(String cam, AnimatronicDrawing... anims){
        get(cam).getAnimatronicsHere().addAll(List.of(anims));
    }
}
