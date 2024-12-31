package es.cristichi.fnac.nights;

import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.anim.*;
import es.cristichi.fnac.obj.cams.CrisRestaurantMap;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Night6 implements NightFactory{
    
    @Override
    public MenuJC.Item getItem() {
        return new MenuJC.Item("n6", "Continue", "Night 6", "night/n6/loading.jpg");
    }
    
    @Override
    public NightJC createNight(Settings settings, NightProgress.SaveFile saveFile, CardLayout cardLayout,
                               Jumpscare powerOutage, JPanel cardPanel, JPanel nightPanel,
                               MenuJC mainMenu, Random rng) throws IOException, NightException {
        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,10), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices", "storage", "kitchen"), 0f, rng);
        
        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(0,7, 2,9, 4,11), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge", "storage", "kitchen"), 0f, rng);
        
        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,9, 3,10, 5,12), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 0f, rng);
        
        AnimatronicDrawing crisClon1 = new PathCris("Cris", Map.of(0,5, 2,7, 4,8, 5,9), true, false, List.of(
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
        ),
                0f, rng);
        
        AnimatronicDrawing crisClon2 = new PathCris("Cris?", Map.of(0,5, 1,6, 3,7, 5,9), true, false, List.of(
                List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor")
        ),
                0f, rng);
        
        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisClon1, crisClon2);
        nightMap.get("bathrooms").setBroken(true);
        nightMap.get("main stage").setBroken(true);
        
        return new NightJC("Night 6", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
    }
}
