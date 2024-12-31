package es.cristichi.fnac.nights;

import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.anim.RoamingBob;
import es.cristichi.fnac.obj.anim.RoamingMaria;
import es.cristichi.fnac.obj.cams.TutorialMap;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TutorialNight implements NightFactory{
    
    @Override
    public MenuJC.Item getItem() {
        return new MenuJC.Item("tutorial", "Play With Us!", "Tutorial Night", null);
    }
    
    @Override
    public NightJC createNight(Settings settings, NightProgress.SaveFile saveFile, CardLayout cardLayout,
                               Jumpscare powerOutage, JPanel cardPanel, JPanel nightPanel,
                               MenuJC mainMenu, Random rng) throws IOException, NightException {
        Map<Integer, Integer> aiNightBob = Map.of(1,2, 2,3, 3,0);
        
        Map<Integer, Integer> aiNightMaria = Map.of(0,0, 2,2, 3,3, 4,4);
        
        TutorialMap tutorialMap = new TutorialMap();
        tutorialMap.addCamAnimatronics("cam1", new RoamingBob("Bob", aiNightBob, false, false, java.util.List.of("cam4"), 0f, rng));
        tutorialMap.addCamAnimatronics("cam2", new RoamingMaria("Maria", aiNightMaria, false, false, List.of("cam3"), 0f, rng));
        
        return new NightJC("Tutorial", settings.getFps(), tutorialMap, "night/tutorial/paper.png",
                powerOutage, rng, 60, 0.45f, "night/tutorial/completed.wav");
    }
}
