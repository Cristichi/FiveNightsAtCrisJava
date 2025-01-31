package es.cristichi.fnac.nights;

import es.cristichi.fnac.anim.Jumpscare;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;

/**
 * A {@link NightFactory} is simply an object capable of creating {@link NightJC} instances. This is needed since
 * creating a Night includes code to create the Animatronics and place them on the 
 * {@link es.cristichi.fnac.cams.CameraMap} for their initial positions. For an example implementation, check
 * the source code of {@link Night1Factory#createNight(Settings, Jumpscare, Random)}.
 */
public abstract class NightFactory implements Comparable<NightFactory> {
    private final MenuJC.ItemInfo item;
    
    /**
     * Creates a NightFactory with the given menu Item.
     *
     * @param item Instance of {@link MenuJC.ItemInfo} that will be used for this Night in the menu.
     */
    protected NightFactory(MenuJC.ItemInfo item) {
        this.item = item;
    }
    
    /**
     * Determines whether the Night should be available. For instance, exactly when 0 Nights are completed is when
     * the Tutorial Night is available. Bear in mind that the Tutorial Night is also a completeable Night, so
     * Night 1 is available with 1 Night completed, Night 2 for 2, etc.
     *
     * @param saveFile Save file to check completed Nights or any other progress' information.
     * @return {@code true} if this Night should be available in the menu, {@code false} otherwise.
     */
    public abstract Availability getAvailability(NightProgress.SaveFile saveFile);
    
    /**
     * @return All the information the instance of {@link MenuJC} must use for the button. Its only requirement is
     * that the ID is not one of the default ones written in the source code of the constructor for
     * {@link es.cristichi.fnac.gui.NightsJF}.
     */
    public MenuJC.ItemInfo getItem() {
        return item;
    }
    
    /**
     *
     * @param settings The player's personal configuration.
     * @param powerOutage Jumpscare that should be thrown when the power is out unless a different one is used.
     * @param rng Random that MUST be used for the creation of the Night and the Night. For instance, if an
     *            {@link es.cristichi.fnac.anim.AnimatronicDrawing} starts at a random location, this Random
     *            should be used to determine that location.
     * @return A Night, without starting it.
     * @throws IOException If an error happens when loading any resources, either when preparing it or during
     *                     its creation.
     * @throws NightException If an error happens that makes this NightJC impossible to create, such as having
     *                        0 seconds per hour.
     */
    public abstract NightJC createNight(Settings settings, Jumpscare powerOutage, Random rng) throws IOException, NightException;
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NightFactory other){
            return getItem().equals(other.getItem());
        }
        return false;
    }
    
    @Override
    public int compareTo(@NotNull NightFactory o) {
        return getItem().id().compareTo(o.getItem().id());
    }
    
    /**
     * @param playableNow Whether this Night should be playable now.
     * @param allowWithCustomNight Whether this Night should be playable alongside Custom Night. All currently available
     *                             Nights must have this as true (or no Nights be available) in order to show the
     *                             Custom Night access in the menu.
     */
    public record Availability(boolean playableNow, boolean allowWithCustomNight){}
}
