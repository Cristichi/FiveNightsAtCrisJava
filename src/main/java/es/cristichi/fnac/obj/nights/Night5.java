package es.cristichi.fnac.obj.nights;

import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.anim.*;
import es.cristichi.fnac.obj.cams.CameraMap;
import es.cristichi.fnac.obj.cams.RestaurantCamMapFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * {@link NightFactory} for Night 5.
 */
public class Night5 implements NightFactory{
    
    @Override
    public MenuJC.Item getItem() throws ResourceException {
        return new MenuJC.Item("n5", "Continue", "Night 5", Resources.loadImage("night/n5/loading.jpg"));
    }
    
    @Override
    public NightJC createNight(Settings settings, Jumpscare powerOutage,
                               Random rng) throws IOException, NightException {
        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,9, 4,10), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices", "storage", "kitchen"), rng);
        
        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(0,4, 2,7, 4,9), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge", "storage", "kitchen"), rng);
        
        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,9), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), rng);
        
        AnimatronicDrawing crisChoosesPathAndTeleports =
                new PathCris("Cris", Map.of(0,3, 1,4, 2,5, 3,6, 5,7), true, false, List.of(
                List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
        ), rng);
        
        CameraMap nightMap = new RestaurantCamMapFactory().generate();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisChoosesPathAndTeleports);
        nightMap.get("bathrooms").setBroken(true);
        nightMap.get("main stage").setBroken(true);
        
        return new NightJC("Night 5", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 0.45f, Resources.loadSound("night/general/completed.wav"), null, null);
    }
}
