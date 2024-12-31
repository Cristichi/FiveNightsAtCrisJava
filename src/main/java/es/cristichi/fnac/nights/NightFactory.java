package es.cristichi.fnac.nights;

import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.gui.MenuJC;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

/**
 * A {@link NightFactory} is simply an object capable of creating {@link NightJC} instances. This is needed since
 * creating a Night includes code to create the Animatronics and place them on the 
 * {@link es.cristichi.fnac.obj.cams.CameraMap} for their initial positions. For an example implementation, check
 * the source code of {@link
 * TutorialNight#createNight(Settings, NightProgress.SaveFile, CardLayout, Jumpscare, JPanel, JPanel, MenuJC, Random)}.
 */
public interface NightFactory {
    MenuJC.Item getItem();
    NightJC createNight(Settings settings, NightProgress.SaveFile saveFile, CardLayout cardLayout,
                        Jumpscare powerOutage, JPanel cardPanel, JPanel nightPanel,
                        MenuJC mainMenu, Random rng) throws IOException, NightException;
}
