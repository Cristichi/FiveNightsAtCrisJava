package es.cristichi.fnac.cams;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Factory that generates a CameraMap that resembles Cris' Restaurant. This is the main location.
 */
public class TutorialCamMapFactory extends CameraMapFactory{
    private static final Point OFFICE_LOC_ON_MAP = new Point(497, 804);
    
    @Override
    public String name() {
        return "Tutorial's Map";
    }
    
    @Override
    public CameraMap generate() throws ResourceException {
        BufferedImage mapImage = Resources.loadImage("night/tutorial/map.png"); 
        return new CameraMap(mapImage, "cam1",
                new Camera.Builder()
                        .setName("cam1")
                        .setCamBackground("night/tutorial/cam1.jpg")
                        .setOnMapLocationVolumeAndPan(113, 111, 378, 177, OFFICE_LOC_ON_MAP,
                                mapImage.getWidth(), mapImage.getHeight())
                        .addConnection("cam2", "cam3")
                        .build(),
                new Camera.Builder()
                        .setName("cam2")
                        .setCamBackground("night/tutorial/cam2.jpg")
                        .setOnMapLocationVolumeAndPan(491, 117, 379, 177, OFFICE_LOC_ON_MAP,
                                mapImage.getWidth(), mapImage.getHeight())
                        .addConnection("cam1", "cam4")
                        .build(),
                new Camera.Builder()
                        .setName("cam3")
                        .setCamBackground("night/tutorial/cam3.jpg")
                        .setOnMapLocationVolumeAndPan(134, 287, 167, 371, OFFICE_LOC_ON_MAP,
                                mapImage.getWidth(), mapImage.getHeight())
                        .addConnection("cam1", "leftDoor")
                        .build(),
                new Camera.Builder()
                        .setName("cam4")
                        .setCamBackground("night/tutorial/cam4.jpg")
                        .setOnMapLocationVolumeAndPan(720, 296, 141, 386, OFFICE_LOC_ON_MAP,
                                mapImage.getWidth(), mapImage.getHeight())
                        .addConnection("cam2", "rightDoor")
                        .build(),
                new Camera.Builder()
                        .setName("leftDoor")
                        .setSoundVolumeManually(1.5)
                        .setSoundPanManually(-1)
                        .isLeftDoor()
                        .addConnection("cam3")
                        .isInvisible()
                        .build(),
                new Camera.Builder()
                        .setName("rightDoor")
                        .setSoundVolumeManually(1.5)
                        .setSoundPanManually(1)
                        .isRightDoor()
                        .addConnection("cam4")
                        .isInvisible()
                        .build()
        );
    }
}
