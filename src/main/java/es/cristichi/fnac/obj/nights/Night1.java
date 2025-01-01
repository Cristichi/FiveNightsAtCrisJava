package es.cristichi.fnac.obj.nights;

import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import es.cristichi.fnac.obj.anim.Paco;
import es.cristichi.fnac.obj.anim.RoamingBob;
import es.cristichi.fnac.obj.anim.RoamingMaria;
import es.cristichi.fnac.obj.cams.CrisRestaurantMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Night1 implements NightFactory {
    
    @Override
    public MenuJC.Item getItem() {
        return new MenuJC.Item("n1", "Continue", "Night 1", "night/n1/loading.jpg");
    }
    
    @Override
    public NightJC createNight(Settings settings, Jumpscare powerOutage,
                               Random rng) throws IOException, NightException {
        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0, 1, 4, 2), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 0f, rng);
        
        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(4, 1), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge"), 0f, rng);
        
        AnimatronicDrawing paco = new Paco("Paco", Map.of(0, 2, 4, 3), false, true,
                List.of(List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")), rng);
        
        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        
        return new NightJC("Night 1", settings.getFps(), nightMap, "night/n1/paper.png", powerOutage, rng, 90, 0.45f,
                "night/general/completed.wav");
    }
}
