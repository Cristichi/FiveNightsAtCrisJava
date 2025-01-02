package es.cristichi.fnac.obj.cams;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;

import java.awt.*;

/**
 * Map of the Tutorial, which features less cameras. It must be used only for the Tutorial.
 */
public class TutorialMap extends CameraMap {
    private static final Point OFFICE_LOC_ON_MAP = new Point(497, 804);
    
    /**
     * Creates a new copy of this map for use on a Night. A new one must be created for each new Night if used on
     * more than one.
     * @throws ResourceException If any images or sounds cannot be read from disk.
     */
    public TutorialMap() throws ResourceException {
        super(Resources.loadImageResource("night/tutorial/map.png"), "cam1");
        addAll(
                new Camera.Builder()
                    .setName("cam1")
                    .setCamBackground("night/tutorial/cam1.jpg")
                    .setOnMapLocationVolumeAndPan(113, 111, 378, 177, OFFICE_LOC_ON_MAP,
                            getMapImage().getWidth(), getMapImage().getHeight())
                    .addConnection("cam2", "cam3")
                    .build(),
                new Camera.Builder()
                    .setName("cam2")
                    .setCamBackground("night/tutorial/cam2.jpg")
                    .setOnMapLocationVolumeAndPan(491, 117, 379, 177, OFFICE_LOC_ON_MAP,
                            getMapImage().getWidth(), getMapImage().getHeight())
                    .addConnection("cam1", "cam4")
                    .build(),
                new Camera.Builder()
                    .setName("cam3")
                    .setCamBackground("night/tutorial/cam3.jpg")
                    .setOnMapLocationVolumeAndPan(134, 287, 167, 371, OFFICE_LOC_ON_MAP,
                            getMapImage().getWidth(), getMapImage().getHeight())
                    .addConnection("cam1", "leftDoor")
                    .build(),
                new Camera.Builder()
                    .setName("cam4")
                    .setCamBackground("night/tutorial/cam4.jpg")
                    .setOnMapLocationVolumeAndPan(720, 296, 141, 386, OFFICE_LOC_ON_MAP,
                            getMapImage().getWidth(), getMapImage().getHeight())
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
