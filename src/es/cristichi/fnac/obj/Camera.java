package es.cristichi.fnac.obj;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.AssetNotFound;
import es.cristichi.fnac.exception.CameraException;
import es.cristichi.fnac.io.FNACResources;
import es.cristichi.fnac.obj.anim.Animatronic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Camera {
    private final String name;
    private final BufferedImage camBackground;
    private final Rectangle loc;
    private final LinkedList<Camera> connections;
    private final LinkedList<Animatronic> animatronicsHere;
    private final boolean isLeftDoorOfOffice;
    private final boolean isRightDoorOfOffice;

    private Camera(String name, BufferedImage camBackground, Rectangle loc, LinkedList<Camera> connections, LinkedList<Animatronic> animatronicsHere, boolean isLeftDoorOfOffice, boolean isRightDoorOfOffice) {
        this.name = name;
        this.camBackground = camBackground;
        this.loc = loc;
        this.connections = connections;
        this.animatronicsHere = animatronicsHere;
        this.isLeftDoorOfOffice = isLeftDoorOfOffice;
        this.isRightDoorOfOffice = isRightDoorOfOffice;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getCamBackground() {
        return camBackground;
    }

    public Rectangle getMapLoc() {
        return loc;
    }

    public LinkedList<Camera> getConnections() {
        return connections;
    }

    public boolean isLeftDoorOfOffice() {
        return isLeftDoorOfOffice;
    }

    public boolean isRightDoorOfOffice() {
        return isRightDoorOfOffice;
    }

    // TODO: I think this might be overkill. I should instead save only the names
    //  and the night will work it out by looping throughout the map.
    public void addMutualConnection(Camera cam) {
        cam.connections.add(this);
        connections.add(cam);
    }

    public LinkedList<Animatronic> getAnimatronicsHere() {
        return animatronicsHere;
    }

    public void move(Animatronic animatronic, Camera dest){
        if (connections.contains(dest)){
            if (animatronicsHere.remove(animatronic)){
                dest.animatronicsHere.add(animatronic);
            } else {
                throw new AnimatronicException("Animatronic "+animatronic.getName()+" not found in camera "+name+".");
            }
        } else {
            throw new AnimatronicException("Animatronic "+animatronic.getName()+" cannot move from "+name+" to "+dest.name+".");
        }
    }

    public static class CameraBuilder{
        private String name = null;
        private BufferedImage camBackground = null;
        private Rectangle loc = null;
        private final LinkedList<Camera> connections = new LinkedList<>();
        private final LinkedList<Animatronic> animatronicsHere = new LinkedList<>();
        private boolean isLeftDoorOfOffice = false;
        private boolean isRightDoorOfOffice = false;

        public CameraBuilder(){}

        public CameraBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public CameraBuilder setCamBackground(String camBackground) throws AssetNotFound {
            this.camBackground = FNACResources.loadImageResource(camBackground);
            return this;
        }

        public CameraBuilder setLoc(int x, int y, int width, int height) {
            this.loc = new Rectangle(x,y,width,height);
            return this;
        }

        public CameraBuilder addAnimatronics(Animatronic animatronic) {
            this.animatronicsHere.add(animatronic);
            return this;
        }

        public CameraBuilder connectToOfficeLeft() {
            this.isLeftDoorOfOffice = true;
            return this;
        }

        public CameraBuilder connectToOfficeRight() {
            this.isRightDoorOfOffice = true;
            return this;
        }

        public Camera build(){
            if (name == null){
                throw new CameraException("Name of Camera cannot be unset.");
            }
            if (camBackground == null){
                throw new CameraException("Cameras must have a background.");
            }
            if (loc == null){
                throw new CameraException("Cameras must have a Location on the map image. It's needed to click on them with the mouse and also highlight when selected.");
            }
            return new Camera(name, camBackground, loc, connections, animatronicsHere, isLeftDoorOfOffice, isRightDoorOfOffice);
        }
    }
}
