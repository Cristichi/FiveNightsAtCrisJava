package es.cristichi.fnac.cams;

import es.cristichi.fnac.anim.AnimatronicDrawing;
import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.CameraException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import kuusisto.tinysound.Sound;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

/**
 * A Camera represents a location inside the Restaurant where {@link AnimatronicDrawing} can be. As its name
 * indicates, it features
 * a surveillance camera that allows the player to see the AnimatronicDrawings that are here (and can be seen).
 */
public class Camera {
    /**
     * Name of the Camera. Works as an identifier.
     */
    private final String nameId;
    /**
     * Background that the Camera has while being watched by the player.
     */
    private final BufferedImage camBackground;
    /**
     * Rectangle of the map where the clickable button to start watching this Camera should be. The coordinates
     * and size must be relative to the source of the minimap image.
     */
    private final Rectangle onMapLoc;
    /**
     * List of names of the other Cameras this Camera is connected. Usually used by Animatronics to check where
     * to move.
     */
    private final LinkedList<String> connections;
    /**
     * List of Animatronics that are here. This is how Animatronics are stored in memory.
     */
    private final LinkedList<AnimatronicDrawing> animatronicsHere;
    /**
     * Whether this Camera is the Camera directly connected to the office via the left side.
     */
    private final boolean isLeftDoorOfOffice;
    /**
     * Whether this Camera is the Camera directly connected to the office via the right side.
     */
    private final boolean isRightDoorOfOffice;
    /**
     * Volume for Sounds played on this Camera.
     */
    private final double soundVolume;
    /**
     * Pan (how much it sounds on the left and right sides of headphones) for Sounds played on this Camera.
     */
    private final double soundPan;
    /**
     * This indicates if the Camera is broken, therefor it's there but its image and Animatronics should
     * not be visible by the player. This still allows the player to see that there is a Camera there.
     */
    private boolean broken;
    /**
     * This indicates if this Camera should not be visible. Usually, the Cameras at left and right to the Office are
     * invisible for gameplay purposes. These Cameras do not need a {@link #camBackground} or an {@link #onMapLoc}.
     */
    private final boolean invisible;
    
    /**
     * Creates a new Camera. This method is private as to use {@link Camera.Builder} instead.
     *
     * @param nameId              Name of the Camera. Works as an identifier.
     * @param camBackground       Background that the Camera has while being watched by the player.
     * @param onMapLoc            Rectangle of the map where the clickable button to start watching this Camera
     *                            should be. The coordinates and size must be relative to the source of the minimap
     *                            image.
     * @param connections         List of names of the other Cameras this Camera is connected. Usually used by
     *                            Animatronics to check where to move.
     * @param animatronicsHere    List of Animatronics that are here. This is how Animatronics are stored in memory.
     * @param isLeftDoorOfOffice  Whether this Camera is the Camera directly connected to the office via the left side.
     * @param isRightDoorOfOffice Whether this Camera is the Camera directly connected to the office via the right side.
     * @param soundVolume         Volume for Sounds played on this Camera.
     * @param soundPan            Pan (how much it sounds on the left and right sides of headphones) for Sounds
     *                            played on this Camera.
     * @param broken              This indicates if the Camera is broken, therefor it's there but its image and
     *                            Animatronics should not be visible by the player. This still allows the player to
     *                            see that there is a Camera there.
     * @param invisible           This indicates if this Camera should not be visible. Usually, the Cameras at left
     *                            and right to the Office are
     *                            invisible for gameplay purposes. These Cameras do not need a {@link #camBackground}
     *                            or an {@link #onMapLoc}.
     */
    private Camera(String nameId, BufferedImage camBackground, Rectangle onMapLoc, LinkedList<String> connections,
                   LinkedList<AnimatronicDrawing> animatronicsHere, boolean isLeftDoorOfOffice,
                   boolean isRightDoorOfOffice, double soundVolume, double soundPan, boolean broken,
                   boolean invisible) {
        this.nameId = nameId;
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
    
    /**
     * @return Name of the Camera. It should be treated as unique for each instance.
     */
    public String getNameId() {
        return nameId;
    }
    
    /**
     * @return Image that shows "what the Camera is seeing" (but without Animatronics or any additional images added
     * by {@link es.cristichi.fnac.gui.NightJC}).
     */
    public BufferedImage getCamBackground() {
        return camBackground;
    }
    
    /**
     * @return Rectangle relative to the source image of the minimap where this Camera has the button to switch
     * to this one.
     */
    public Rectangle getMapLoc() {
        return onMapLoc;
    }
    
    /**
     * @return Names of the Cameras that this Camera is "physically" connected to (by a door/entrance).
     */
    public LinkedList<String> getConnections() {
        return new LinkedList<>(connections);
    }
    
    /**
     * @return {@code true} if this door is connected to the Office (which is not a Camera itself) by the left door.
     */
    public boolean isLeftDoor() {
        return isLeftDoorOfOffice;
    }
    
    /**
     * @return {@code true} if this door is connected to the Office (which is not a Camera itself) by the right door.
     */
    public boolean isRightDoor() {
        return isRightDoorOfOffice;
    }
    
    /**
     * @return List of all {@link AnimatronicDrawing} instances that are at this Camera at the moment.
     */
    public LinkedList<AnimatronicDrawing> getAnimatronicsHere() {
        return animatronicsHere;
    }
    
    /**
     * @return {@code true} if this Camera should be clickable but show nothing.
     */
    public boolean isBroken() {
        return broken;
    }
    
    /**
     * @param broken {@code true} if this Camera should be clickable but show nothing.
     */
    public void setBroken(boolean broken) {
        this.broken = broken;
    }
    
    /**
     * @return {@code true} if this Camera should not be visible at all, even in the minimap.
     */
    public boolean isInvisible() {
        return invisible;
    }
    
    /**
     * This method does not check whether the Cameras are connected, but it will fail if
     * {@link LinkedList#remove(Object)} on the List of Animatronics here returns false.
     *
     * @param animatronicDrawing Animatronic that is on this Camera and has to move.
     * @param dest               Camera to move to.
     * @throws AnimatronicException If the Animatronic does not exist in this origin Camera.
     */
    public void move(AnimatronicDrawing animatronicDrawing, Camera dest) throws AnimatronicException {
        if (animatronicsHere.remove(animatronicDrawing)) {
            dest.animatronicsHere.add(animatronicDrawing);
        } else {
            throw new AnimatronicException(
                    "Animatronic " + animatronicDrawing.getNameId() + " not found in camera " + nameId + ".");
        }
    }
    
    /**
     * Plays the given Sound at this Camera, meaning that the volume and pan are taken from this Camera.
     * @param sound Sound to play here.
     */
    public void playSoundHere(Sound sound) {
        sound.play(soundVolume, soundPan);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Camera camera)) return false;
        return Objects.equals(nameId, camera.nameId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(nameId);
    }
    
    @Override
    public String toString() {
        return nameId;
    }
    
    /**
     * Builder for Cameras that makes sure that everything is ok to generate the Camera. It applies default values
     * to unspecified things, and will throw a {@link CameraException} if anything is not a valid configuration
     * for a Camera (like missing background on a non-invisible Camera).
     */
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
        
        /**
         * Starts a new {@link Camera.Builder}. After you set everything this Camera has, call {@link #build()} to
         * get the Camera.
         */
        public Builder() {
        }
        
        /**
         * @param name Name of the Camera. Works as an identifier. This is mandatory.
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        /**
         * @param camBackground        Background that the Camera has while being watched by the player.
         *                             This is mandatory unless the Camera is marked as invisible
         *                             {@link #isInvisible()}.
         * @return This Builder so that you can configure the Camera on one line.
         */
        @SuppressWarnings("unused") // For Custom Cameras in case they load the image from elsewhere.
        public Builder setCamBackground(BufferedImage camBackground) {
            this.camBackground = camBackground;
            return this;
        }
        
        /**
         * @param pathCamBackgResource Path in the resources to the image for the background that the Camera has
         *                             while being watched by the player.
         *                             This is mandatory unless the Camera is marked as invisible
         *                             {@link #isInvisible()}.
         * @return This Builder so that you can configure the Camera on one line.
         * @throws ResourceException If the image specified is not found in the resources during execution.
         */
        public Builder setCamBackground(String pathCamBackgResource) throws ResourceException {
            this.camBackground = Resources.loadImage(pathCamBackgResource);
            return this;
        }
        
        /**
         * Rectangle of the map where the clickable button to start watching this Camera should be. The coordinates
         * and size must be relative to the source of the minimap image. This or
         * {@link #setOnMapLocationVolumeAndPan(int, int, int, int, Point, int, int)} are mandatory unless the
         * Camera is marked as invisible {@link #isInvisible()}.
         * @param x Top-left X coordinate.
         * @param y Top-left Y coordinate.
         * @param width Width of the Rectangle.
         * @param height Height of the Rectangle.
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder setOnMapLocManually(int x, int y, int width, int height) {
            this.onMapLoc = new Rectangle(x, y, width, height);
            return this;
        }
        
        /**
         * @param soundVolume Volume for Sounds played on this Camera. Default is 1 (100% volume).
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder setSoundVolumeManually(double soundVolume) {
            this.soundVolume = soundVolume;
            return this;
        }
        
        /**
         * @param soundPan Pan (how much it sounds on the left and right sides of headphones) for Sounds
         *                 played on this Camera. Valid from -1 to 1. Default is 0 (same on both ears).
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder setSoundPanManually(double soundPan) {
            this.soundPan = soundPan;
            return this;
        }
        
        /**
         * Sets the location on the map, just like {@link #setOnMapLocManually(int, int, int, int)}, except that
         * this will also calculate on the fly the correct values for volume and pan for Sounds played there with
         * the location of the office and the size of the map. This or
         * {@link #setOnMapLocManually(int, int, int, int)} are mandatory unless the Camera is marked as
         * invisible {@link #isInvisible()}.
         * @param x Top-left X coordinate.
         * @param y Top-left Y coordinate.
         * @param width Width of the Rectangle.
         * @param height Height of the Rectangle.
         * @param officeLoc Location of the office, in the source image of the minimap.
         * @param mapWidth Width of the minimap image source.
         * @param mapHeight Height of the minimap image source.
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder setOnMapLocationVolumeAndPan(int x, int y, int width, int height, Point officeLoc, int mapWidth,
                                                    int mapHeight) {
            this.onMapLoc = new Rectangle(x, y, width, height);
            int centerX = x + (width / 2);
            int centerY = y + (height / 2);
            
            this.soundPan = Math.max(-1, Math.min(1, (double) (centerX - officeLoc.x) / ((double) mapWidth / 2)));
            
            double distance = Math.sqrt(Math.pow(centerX - officeLoc.x, 2) + Math.pow(centerY - officeLoc.y, 2));
            double maxDistance = Math.sqrt(mapWidth * mapWidth + mapHeight * mapHeight);
            
            double minVolume = 0.3;
            this.soundVolume = Math.max(minVolume,
                                        Math.min(1, minVolume + (1 - minVolume) * (1 - (distance / maxDistance))));
            
            return this;
        }
        
        /**
         * @param camNames List of names of the other Cameras this Camera is connected. Usually used by Animatronics
         *                 to check where to move. Default is an empty list, but it is encouraged that the Camera is
         *                 not disconnected as it may lead to useless Cameras.
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder addConnection(String... camNames) {
            Collections.addAll(connections, camNames);
            return this;
        }
        
        /**
         * Marks this door as a door that is connected to the office on the left.
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder isLeftDoor() {
            this.isLeftDoorOfOffice = true;
            return this;
        }
        
        /**
         * Marks this door as a door that is connected to the office on the right.
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder isRightDoor() {
            this.isRightDoorOfOffice = true;
            return this;
        }
        
        /**
         * Marks this Camera as broken. It indicates that it is there but its image and Animatronics should not
         * be visible by the player. This still allows the player to see that there is a Camera there and click it.
         * @return This Builder so that you can configure the Camera on one line.
         */
        @SuppressWarnings("unused") // For custom maps to use.
        public Builder isBroken() {
            this.broken = true;
            return this;
        }
        
        /**
         * Marks this Camera as invisible. Usually, the Cameras at left and right to the Office are invisible for
         * gameplay purposes. These Cameras do not exist form the player's perspective, and are used to allow
         * Animatronics to go there and therefore do not need a {@link #camBackground} or an {@link #onMapLoc}.
         * @return This Builder so that you can configure the Camera on one line.
         */
        public Builder isInvisible() {
            this.invisible = true;
            return this;
        }
        
        /**
         * Builds the Camera.
         * @return A Camera with the proposed configuration, if possible.
         * @throws CameraException If the Camera is impossible, in the following scenarios:
         * -Name is not set.
         * -It's not invisible and a background image or location on map are not set.
         */
        public Camera build() throws CameraException{
            if (name == null) {
                throw new CameraException("Name of Camera cannot be unset.");
            }
            if (!invisible) {
                if (camBackground == null) {
                    throw new CameraException("Cameras must have a background or be invisible.");
                }
                if (onMapLoc == null) {
                    throw new CameraException(
                            "Cameras must have a Location on the map image. It's needed to " + "click on them with " + "the mouse and also highlight when selected.");
                }
            }
            return new Camera(name, camBackground, onMapLoc, connections, animatronicsHere, isLeftDoorOfOffice,
                    isRightDoorOfOffice, soundVolume, soundPan, broken, invisible);
        }
    }
}
