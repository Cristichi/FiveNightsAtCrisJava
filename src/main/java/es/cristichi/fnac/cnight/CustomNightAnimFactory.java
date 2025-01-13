package es.cristichi.fnac.cnight;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Random;

/**
 * Factories that let the Custom Night menu construct instances of the given Animatronic.
 * @param <E> Class that extends {@link AnimatronicDrawing}, which is the class that represents an Animatronic
 *           available to add to Custom Nights.
 */
public abstract class CustomNightAnimFactory<E extends AnimatronicDrawing> {
    /**
     * Name of the Animatronic. Must be unique.
     */
    protected final String nameId;
    /**
     * Player-friendly description of what the Animatronic does.
     */
    protected final String description;
    /**
     * Max AI the player is allowed to set for this Animatronic.
     */
    protected final int maxAi;
    /**
     * Array with all the possible starting positions for all maps.
     */
    protected final String[] startPositions;
    /**
     * Image of the portrait of the Animatronic.
     */
    protected final BufferedImage portrait;
    
    /**
     * Creates a new instance of this factory.
     * @param nameId            Name of the Animatronic. Must be unique.
     * @param description       Player-friendly description of what the Animatronic does.
     * @param maxAi             Max AI the player is allowed to set for this Animatronic.
     * @param portrait          Portrait of the Animatronic, for the Custom Night menu.
     * @param startPositions    Possible starting positions, one for each map.
     */
    public CustomNightAnimFactory(String nameId, String description, int maxAi, BufferedImage portrait, String[] startPositions){
        this.nameId = nameId;
        this.description = description;
        this.maxAi = maxAi;
        this.portrait = portrait;
        this.startPositions = startPositions;
    }
    
    /**
     * @return Name of the Animatronic. Must be unique.
     */
    public String getNameId() {
        return nameId;
    }
    
    /**
     * @return Player-friendly description of what the Animatronic does.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @return Max AI the player is allowed to set for this Animatronic.
     */
    public int getMaxAi() {
        return maxAi;
    }
    
    /**
     * @return Image of the portrait of the Animatronic.
     */
    public BufferedImage getPortrait() {
        return portrait;
    }
    
    /**
     * @return Array with all the possible starting positions for all maps.
     */
    public String[] getStartPositions() {
        return startPositions;
    }
    
    /**
     * Creates an instance of the Animatronic.
     * @param data Data from the Custom Night menu with all the preferences the player set, like the AI value.
     * @param rng Random for the Custom Night.
     * @return An instance of the Animatronic that respects the data.
     * @throws ResourceException If any resources failed to load.
     */
    public abstract E generate(CustomNightAnimData data, Random rng) throws ResourceException;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomNightAnimFactory<?> that)) return false;
        return Objects.equals(nameId, that.nameId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(nameId);
    }
}
