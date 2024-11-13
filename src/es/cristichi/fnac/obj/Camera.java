package es.cristichi.fnac.obj;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Camera {
    private final String name;
    private final BufferedImage camImg;
    private final Rectangle loc;

    public Camera(String name, BufferedImage camImg, Rectangle loc) {
        this.name = name;
        this.camImg = camImg;
        this.loc = loc;
    }

    public String getName() {
        return name;
    }

    public Rectangle getMapLoc() {
        return loc;
    }

    public BufferedImage getCamImg() {
        return camImg;
    }
}
