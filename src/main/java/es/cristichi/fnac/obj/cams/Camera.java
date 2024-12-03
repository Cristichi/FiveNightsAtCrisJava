package es.cristichi.fnac.obj.cams;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.CameraException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import kuusisto.tinysound.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

public class Camera {
    private final String name;
    private final BufferedImage camBackground;
    private final Rectangle onMapLoc;
    private final LinkedList<String> connections;
    private final LinkedList<AnimatronicDrawing> animatronicsHere;
    private final boolean isLeftDoorOfOffice, isRightDoorOfOffice;
    private final double soundVolume, soundPan;

    private Camera(String name, BufferedImage camBackground, Rectangle onMapLoc, LinkedList<String> connections,
                   LinkedList<AnimatronicDrawing> animatronicsHere, boolean isLeftDoorOfOffice, boolean isRightDoorOfOffice,
                   double soundVolume, double soundPan) {
        this.name = name;
        this.camBackground = camBackground;
        this.onMapLoc = onMapLoc;
        this.connections = connections;
        this.animatronicsHere = animatronicsHere;
        this.isLeftDoorOfOffice = isLeftDoorOfOffice;
        this.isRightDoorOfOffice = isRightDoorOfOffice;
        this.soundVolume = soundVolume;
        this.soundPan = soundPan;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getCamBackground() {
        return camBackground;
    }

    public Rectangle getMapLoc() {
        return onMapLoc;
    }

    public LinkedList<String> getConnections() {
        return new LinkedList<>(connections);
    }

    public boolean isLeftDoorOfOffice() {
        return isLeftDoorOfOffice;
    }

    public boolean isRightDoorOfOffice() {
        return isRightDoorOfOffice;
    }

    public LinkedList<AnimatronicDrawing> getAnimatronicsHere() {
        return animatronicsHere;
    }

    /**
     * This method does not check whether the Cameras are connected, but it will fail if
     * {@link LinkedList#remove(Object)} on the List of Animatronics here returns false.
     * @param animatronicDrawing Animatronic that is on this Camera and has to move.
     * @param dest Camera to move to.
     */
    public void move(AnimatronicDrawing animatronicDrawing, Camera dest){
        if (animatronicsHere.remove(animatronicDrawing)){
            dest.animatronicsHere.add(animatronicDrawing);
        } else {
            throw new AnimatronicException("Animatronic "+ animatronicDrawing.getName()+" not found in camera "+name+".");
        }
    }

    public void playSoundHere(Sound sound){
        sound.play(soundVolume, soundPan);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Camera camera)) return false;
        return Objects.equals(name, camera.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return name;
    }

    public static class Builder {
        private String name = null;
        private BufferedImage camBackground = null;
        private Rectangle onMapLoc = null;
        private final LinkedList<String> connections = new LinkedList<>();
        private final LinkedList<AnimatronicDrawing> animatronicsHere = new LinkedList<>();
        private boolean isLeftDoorOfOffice = false;
        private boolean isRightDoorOfOffice = false;
        private double soundVolume = 1;
        private double soundPan = 0;

        public Builder(){}

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCamBackground(String camBackground) throws ResourceException {
            this.camBackground = Resources.loadImageResource(camBackground);
            return this;
        }

        public Builder setOnMapLoc(int x, int y, int width, int height) {
            this.onMapLoc = new Rectangle(x,y,width,height);
            return this;
        }

        public Builder setSoundVolume(double soundVolume) {
            this.soundVolume = soundVolume;
            return this;
        }

        public Builder setSoundPan(double soundPan) {
            this.soundPan = soundPan;
            return this;
        }

        public Builder addAnimatronics(AnimatronicDrawing... animatronicDrawings) {
            Collections.addAll(animatronicsHere, animatronicDrawings);
            return this;
        }

        @SuppressWarnings("unused")
        public Builder addConnection(String... camNames) {
            Collections.addAll(connections, camNames);
            return this;
        }

        public Builder connectToOfficeLeft() {
            this.isLeftDoorOfOffice = true;
            return this;
        }

        public Builder connectToOfficeRight() {
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
            if (onMapLoc == null){
                throw new CameraException("Cameras must have a Location on the map image. It's needed to " +
                        "click on them with the mouse and also highlight when selected.");
            }
            return new Camera(name, camBackground, onMapLoc, connections, animatronicsHere, isLeftDoorOfOffice,
                    isRightDoorOfOffice, soundVolume, soundPan);
        }
    }
}
