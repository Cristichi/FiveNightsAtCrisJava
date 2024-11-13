package es.cristichi.fnac.obj;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

public class CameraMap extends ArrayList<Camera> {
    private final String name;
    private final BufferedImage image;
    private int selected;

    public CameraMap(String name, BufferedImage image) {
        super();
        this.name = name;
        this.image = image;
        selected = 0;
    }

    public CameraMap(Collection<? extends Camera> c, String name, BufferedImage image) {
        super(c);
        this.name = name;
        this.image = image;
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
}
