package es.cristichi.fnac.obj.cams;

import es.cristichi.fnac.exception.CameraException;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the list of Cameras, marked by their name.
 */
public class CameraMap extends HashMap<String, Camera> {
    protected final BufferedImage mapImage;
    protected final String defaultSelected;
    protected String selected;
    
    /**
     * @param mapImage        Minimap image, where the buttons to switch Cameras are drawn.
     * @param defaultSelected The name of the first Camera to be selected on this map.
     */
    public CameraMap(BufferedImage mapImage, String defaultSelected) {
        super();
        this.mapImage = mapImage;
        this.defaultSelected = defaultSelected;
        this.selected = defaultSelected;
    }
    
    /**
     * @return Minimap image.
     */
    public BufferedImage getMapImage() {
        return mapImage;
    }
    
    /**
     * @return Camera currently selected, or the default one.
     */
    public Camera getSelectedCam() {
        return super.getOrDefault(selected, get(defaultSelected));
    }
    
    /**
     * Changes the currently selected Camera.
     *
     * @param selected Name of the Camera that should be selected now.
     */
    public void setSelected(String selected) {
        if (containsKey(selected)) {
            this.selected = selected;
        } else {
            throw new CameraException(
                    "Map " + selected + " does not exist in this CameraMap, therefore it cannot be selected.");
        }
    }
    
    /**
     * Adds all the given Cameras to this map. Cameras with the same name will replace previous Cameras with the same
     * name.
     *
     * @param cams Cameras to add.
     */
    public void addAll(Camera... cams) {
        for (Camera cam : cams) {
            put(cam.getNameId(), cam);
        }
    }
    
    /**
     * Adds all the given {@link AnimatronicDrawing} instances to one specific Camera in this map.
     *
     * @param cam   Name of the Camera.
     * @param anims List of {@link AnimatronicDrawing} instances to add.
     */
    public void addCamAnimatronics(String cam, AnimatronicDrawing... anims) {
        if (!containsKey(cam)) {
            throw new CameraException(("AnimatronicDrawings cannot be added to the Camera %s because it does not exist " +
                    "in this CameraMap.").formatted(cam));
        }
        get(cam).getAnimatronicsHere().addAll(List.of(anims));
    }
}
