package es.cristichi.fnac.nights;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This registry registers each Night in the order they must appear. The index of each Night represents the number of
 * previous Nights the player must have completed (exactly: no more no less) in order to play that one. <br>
 * This registry never contains Custom Night, which is a special kind of Night that appears when no Night here meets
 * the requisite of completed Nights.
 */
public class NightRegistry {
    private static final ArrayList<NightFactory> nightRegistry = new ArrayList<>(7);
    
    public static void registerNight(int requiredCompletedNights, NightFactory nightFactory){
        nightRegistry.add(requiredCompletedNights, nightFactory);
    }
    
    public static @Nullable NightFactory getNight(int completedNights){
        if (completedNights < 0 || completedNights>=nightRegistry.size()){
            return null;
        }
        return nightRegistry.get(completedNights);
    }
    
    public static List<NightFactory> getAllNights(){
        return Collections.unmodifiableList(nightRegistry);
    }
}
