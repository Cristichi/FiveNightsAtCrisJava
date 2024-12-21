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
    private boolean broken, invisible;
    private final double soundVolume, soundPan;

    private Camera(String name, BufferedImage camBackground, Rectangle onMapLoc, LinkedList<String> connections,
                   LinkedList<AnimatronicDrawing> animatronicsHere, boolean isLeftDoorOfOffice, boolean isRightDoorOfOffice,
                   double soundVolume, double soundPan, boolean broken, boolean invisible) {
        this.name = name;
        this.camBackground = camBackground;
        this.onMapLoc = onMapLoc;
        this.connections = connections;
        this.animatronicsHere = animatronicsHere;
        this.isLeftDoorOfOffice = isLeftDoorOfOffice;
        this.isRightDoorOfOffice = isRightDoorOfOffice;
        this.soundVolume = soundVolume;
        this.soundPan = soundPan;
        this.broken = broken;
        this.invisible = invisible;
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

    public boolean isLeftDoor() {
        return isLeftDoorOfOffice;
    }

    public boolean isRightDoor() {
        return isRightDoorOfOffice;
    }

    public LinkedList<AnimatronicDrawing> getAnimatronicsHere() {
        return animatronicsHere;
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        if (!invisible){
            if (camBackground == null){
                throw new CameraException("Cameras must have a background or be invisible.");
            }
            if (onMapLoc == null){
                throw new CameraException("Cameras must have a Location on the map image. It's needed to " +
                        "click on them with the mouse and also highlight when selected.");
            }
        }
        this.invisible = invisible;
    }

    /**
     * This method does not check whether the Cameras are connected, but it will fail if
     * {@link LinkedList#remove(Object)} on the List of Animatronics here returns false.
     * @param animatronicDrawing Animatronic that is on this Camera and has to move.
     * @param dest Camera to move to.
     */
    public void move(AnimatronicDrawing animatronicDrawing, Camera dest) throws AnimatronicException {
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
        private boolean broken = false;
        private boolean invisible = false;

        public Builder(){}

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCamBackground(String camBackground) throws ResourceException {
            this.camBackground = Resources.loadImageResource(camBackground);
            return this;
        }

        public Builder setOnMapLocManually(int x, int y, int width, int height) {
            this.onMapLoc = new Rectangle(x,y,width,height);
            return this;
        }

        public Builder setSoundVolumeManually(double soundVolume) {
            this.soundVolume = soundVolume;
            return this;
        }

        public Builder setSoundPanManually(double soundPan) {
            this.soundPan = soundPan;
            return this;
        }

        public Builder setOnMapLocationVolumeAndPan(int x, int y, int width, int height, Point officeLoc, int mapWidth, int mapHeight) {
            this.onMapLoc = new Rectangle(x, y, width, height);

            // Calculate the center of the rectangle
            int centerX = x + (width / 2);
            int centerY = y + (height / 2);

            // Calculate pan based on horizontal position
            double pan = (double) (centerX - officeLoc.x) / ((double) mapWidth / 2);
            pan = Math.max(-1, Math.min(1, pan)); // Clamp to range [-1, 1]
            this.soundPan = pan;

            // Calculate distance from the office
            double distance = Math.sqrt(
                    Math.pow(centerX - officeLoc.x, 2) +
                            Math.pow(centerY - officeLoc.y, 2)
            );

            // Calculate maximum distance on the map
            double maxDistance = Math.sqrt(mapWidth * mapWidth + mapHeight * mapHeight);

            // Calculate volume based on distance
            double minVolume = 0.2; // Minimum volume level
            double volume = minVolume + (1 - minVolume) * (1 - (distance / maxDistance));
            volume = Math.max(minVolume, Math.min(1, volume));
            this.soundVolume = volume;

            return this;
        }


        public Builder addAnimatronics(AnimatronicDrawing... animatronicDrawings) {
            Collections.addAll(animatronicsHere, animatronicDrawings);
            return this;
        }

        public Builder addConnection(String... camNames) {
            Collections.addAll(connections, camNames);
            return this;
        }

        public Builder isLeftDoor() {
            this.isLeftDoorOfOffice = true;
            return this;
        }

        public Builder isRightDoor() {
            this.isRightDoorOfOffice = true;
            return this;
        }

        public Builder isBroken() {
            this.broken = true;
            return this;
        }

        public Builder isInvisible() {
            this.invisible = true;
            return this;
        }

        public Camera build(){
            if (name == null){
                throw new CameraException("Name of Camera cannot be unset.");
            }
            if (!invisible){
                if (camBackground == null){
                    throw new CameraException("Cameras must have a background or be invisible.");
                }
                if (onMapLoc == null){
                    throw new CameraException("Cameras must have a Location on the map image. It's needed to " +
                            "click on them with the mouse and also highlight when selected.");
                }
            }
            return new Camera(name, camBackground, onMapLoc, connections, animatronicsHere, isLeftDoorOfOffice,
                    isRightDoorOfOffice, soundVolume, soundPan, broken, invisible);
        }
    }
}
