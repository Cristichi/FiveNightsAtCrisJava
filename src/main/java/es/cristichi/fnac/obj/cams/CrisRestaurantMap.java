package es.cristichi.fnac.obj.cams;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;

import java.awt.*;

public class CrisRestaurantMap extends CameraMap {
    private static final Point OFFICE_LOC_ON_MAP = new Point(497, 843);

    public CrisRestaurantMap() throws ResourceException {
        super(Resources.loadImageResource("night/general/map.png"), "dining area");
        addAll(
                new Camera.Builder()
                        .setName("kitchen")
                        .setCamBackground("night/general/kitchen.jpg")
                        .setOnMapLocationVolumeAndPan(187, 45, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("storage")
                        .setCamBackground("night/general/storage.jpg")
                        .setOnMapLocationVolumeAndPan(542, 111, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("dining area")
                        .setCamBackground("night/general/dining area.jpg")
                        .setOnMapLocationVolumeAndPan(168, 182, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("main stage", "corridor 1", "corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("main stage")
                        .setCamBackground("night/general/main stage.jpg")
                        .setOnMapLocationVolumeAndPan(537, 399, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 1")
                        .setCamBackground("night/general/corridor 1.jpg")
                        .setOnMapLocationVolumeAndPan(314, 469, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("dining area", "corridor 3", "staff lounge")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 2")
                        .setCamBackground("night/general/corridor 2.jpg")
                        .setOnMapLocationVolumeAndPan(456, 469, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("dining area", "corridor 4", "bathrooms")
                        .build(),
                new Camera.Builder()
                        .setName("staff lounge")
                        .setCamBackground("night/general/staff lounge.jpg")
                        .setOnMapLocationVolumeAndPan(30, 821, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("corridor 1")
                        .build(),
                new Camera.Builder()
                        .setName("offices")
                        .setCamBackground("night/general/offices.jpg")
                        .setOnMapLocationVolumeAndPan(825, 840, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("corridor 4")  //Offices go to corridor 4, but not vice-versa
                        .build(),
                new Camera.Builder()
                        .setName("bathrooms")
                        .setCamBackground("night/general/bathrooms.jpg")
                        .setOnMapLocationVolumeAndPan(560, 734, 140, 51, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 3")
                        .setCamBackground("night/general/corridor 3.jpg")
                        .setOnMapLocationVolumeAndPan(225, 561, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("corridor 1", "leftDoor")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 4")
                        .setCamBackground("night/general/corridor 4.jpg")
                        .setOnMapLocationVolumeAndPan(662, 568, 140, 70, OFFICE_LOC_ON_MAP,
                                getMapImage().getWidth(), getMapImage().getHeight())
                        .addConnection("corridor 2", "rightDoor") //Offices go to corridor 4, but not vice-versa
                        .build(),
                new Camera.Builder()
                        .setName("leftDoor")
                        .setSoundVolumeManually(1)
                        .setSoundPanManually(-1)
                        .addConnection("corridor 3")
                        .isLeftDoor()
                        .isInvisible()
                        .build(),
                new Camera.Builder()
                        .setName("rightDoor")
                        .setSoundVolumeManually(1)
                        .setSoundPanManually(1)
                        .addConnection("corridor 4")
                        .isRightDoor()
                        .isInvisible()
                        .build()
        );
    }
}
