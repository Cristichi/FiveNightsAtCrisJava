package es.cristichi.fnac.obj;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Camera {
    private final String name;
    private final BufferedImage camImg;
    private final Rectangle loc;
    private Rectangle locOnScreen;

    public Camera(String name, BufferedImage camImg, Rectangle loc) {
        this.name = name;
        this.camImg = camImg;
        this.loc = loc;
        this.locOnScreen = new Rectangle(loc);
    }

    public String getName() {
        return name;
    }

    public BufferedImage getCamImg() {
        return camImg;
    }

    public Rectangle getMapLoc() {
        return loc;
    }

    public void updateLocOnScreen(Rectangle locOnScreen) {
        this.locOnScreen = locOnScreen;
    }

    public Rectangle getLocOnScreen() {
        return locOnScreen;
    }
}
