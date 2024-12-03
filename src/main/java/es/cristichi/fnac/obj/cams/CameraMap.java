package es.cristichi.fnac.obj.cams;

import es.cristichi.fnac.exception.CameraException;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class CameraMap extends HashMap<String, Camera> {
    private final BufferedImage mapImage;
    private final String defaultSelected;
    private String selected;

    public CameraMap(BufferedImage mapImage, String defaultSelected) {
        super();
        this.mapImage = mapImage;
        this.defaultSelected = defaultSelected;
        this.selected = defaultSelected;
    }

    public BufferedImage getMapImage() {
        return mapImage;
    }

    public String getSelectedName() {
        return selected;
    }

    public Camera getSelectedCam() {
        return super.getOrDefault(selected, get(defaultSelected));
    }

    public void setSelected(String selected) {
        if (containsKey(selected)){
            this.selected = selected;
        } else {
            throw new CameraException("Map "+selected+" does not exist in this CameraMap, therefore it cannot be selected.");
        }
    }

    public void addAll(Camera... cams) {
        for (Camera cam : cams) {
            put(cam.getName(), cam);
        }
    }
}
