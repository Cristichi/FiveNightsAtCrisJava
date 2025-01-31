package es.cristichi.fnac.nights;

import es.cristichi.fnac.anim.*;
import es.cristichi.fnac.cams.CameraMap;
import es.cristichi.fnac.cams.RestaurantCamMapFactory;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * {@link NightFactory} for Night 4.
 */
public class Night4Factory extends NightFactory{
    
    /**
     * Creates a Night 4 Factory and loads the loading screen. Loadingception.
     *
     * @throws ResourceException If menu loading screen could not be loaded.
     */
    public Night4Factory() throws ResourceException {
        super(new MenuJC.ItemInfo("n4", "Continue", "Night 4", Resources.loadImage("night/n4/loading.jpg")));
    }
    
    @Override
    public Availability getAvailability(NightProgress.SaveFile saveFile) {
        return new Availability(saveFile.completedNights().size()==4, false);
    }
    
    @Override
    public NightJC createNight(Settings settings, Jumpscare powerOutage,
                               Random rng) throws IOException, NightException {
        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,8, 4,9), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices", "storage", "kitchen"), rng);
        
        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(0,3, 2,5, 4,6), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge", "storage", "kitchen"), rng);
        
        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,7, 5,8), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), rng);
        
        AnimatronicDrawing crisChoosesPathAndTeleports = new PathCris("Cris", Map.of(0,1, 4,2, 5,4), true, false, List.of(
                List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor")
        ), rng);
        
        CameraMap nightMap = new RestaurantCamMapFactory().generate();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisChoosesPathAndTeleports);
        nightMap.get("main stage").setBroken(true);
        
        return new NightJC("Night 4", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 6, 0.45f, Resources.loadSound("night/general/completed.wav"), null, null);
    }
}
