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
 * {@link NightFactory} for Night 2.
 */
public class Night2 extends NightFactory{
    
    /**
     * Creates a Night 2 Factory and loads the loading screen. Loadingception.
     *
     * @throws ResourceException If menu loading screen could not be loaded.
     */
    public Night2() throws ResourceException {
        super(new MenuJC.ItemInfo("n2", "Continue", "Night 2", Resources.loadImage("night/n2/loading.jpg")));
    }
    
    @Override
    public Availability getAvailability(NightProgress.SaveFile saveFile) {
        return new Availability(saveFile.completedNights().size()==2, false);
    }
    
    @Override
    public NightJC createNight(Settings settings, Jumpscare powerOutage,
                               Random rng) throws IOException, NightException {
        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,4), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), rng);
        
        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(1,1, 4,2), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge"), rng);
        
        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,4, 4,5), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), rng);
        
        AnimatronicDrawing crisIsClose = new RoamingCris("Cris", Map.of(0,1, 4,2, 5,3), true, false,
                List.of("kitchen", "storage", "main stage", "staff lounge", "bathrooms"), rng);
        
        CameraMap nightMap = new RestaurantCamMapFactory().generate();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("staff lounge", crisIsClose);
        
        return new NightJC("Night 2", settings.getFps(), nightMap, Resources.loadImage("night/n2/paper.png"),
                powerOutage, rng, 90, 6, 0.45f, Resources.loadSound("night/general/completed.wav"), null, null);
    }
}
