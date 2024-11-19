package es.cristichi.fnac.obj;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class CameraMap extends HashMap<String, Camera> {
    private final BufferedImage image;
    private int selected;

    public CameraMap(BufferedImage image) {
        super();
        this.image = image;
        selected = 0;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public Camera getSelectedCam() {
        return super.get(selected);
    }

    public void addAll(Camera... cams) {
        for (Camera cam : cams) {
            put(cam.getName(), cam);
        }
    }
}
