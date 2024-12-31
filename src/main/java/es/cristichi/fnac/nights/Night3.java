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

public class Night3 implements NightFactory{
    
    @Override
    public MenuJC.Item getItem() {
        return new MenuJC.Item("n3", "Continue", "Night 3", "night/n3/loading.jpg");
    }
    
    @Override
    public NightJC createNight(Settings settings, NightProgress.SaveFile saveFile, CardLayout cardLayout,
                               Jumpscare powerOutage, JPanel cardPanel, JPanel nightPanel,
                               MenuJC mainMenu, Random rng) throws IOException, NightException {
        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,7), false, false,
            List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 0f, rng);
        
        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(1,3, 4,5), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge"), 0f, rng);
        
        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,5, 4,7), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 0f, rng);
        
        AnimatronicDrawing crisRandomSideAllNight = new RoamingCris("Cris", Map.of(0,1, 4,2, 5,3), true, false,
                List.of("kitchen", "storage", "dining area", "main stage"), 0f, rng);
        
        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisRandomSideAllNight);
        nightMap.get("bathrooms").setBroken(true);
        
        return new NightJC("Night 3", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
    }
}
