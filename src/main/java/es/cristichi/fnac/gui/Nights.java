package es.cristichi.fnac.gui;

import es.cristichi.fnac.Main;
import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.SaveFileIO;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisual;
import es.cristichi.fnac.obj.anim.*;
import es.cristichi.fnac.obj.cams.CameraMap;
import es.cristichi.fnac.obj.cams.CrisRestaurantMap;
import es.cristichi.fnac.obj.cams.TutorialMap;
import kuusisto.tinysound.TinySound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Main window, it controls all the thingies.
 */
public class Nights extends JFrame {
    public final String GAME_TITLE = "Five Nights at Cris'";
    private final JPanel cardPanel;
    private final JSettings settingsPanel;
    private final JPanel nightPanel;
    private final Menu mainMenu;
    private final Jumpscare powerOutage;

    private final SaveFileIO.SaveFile saveFile;
    private final CardLayout cardLayout;
    private Settings settings;

    /**
     * Creates a new window for the game.
     * @param saveFile Save file.
     * @param settings Settings of the user.
     * @throws ResourceException If an error occurs when loading a Resource.
     */
    public Nights(SaveFileIO.SaveFile saveFile, Settings settings) throws ResourceException {
        super();
        this.saveFile = saveFile;
        this.settings = settings;

        setTitle(getTitleForWindow(null));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(Resources.loadImageResource("icon.jpg"));
        setTitle(getTitleForWindow(null));
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);

        nightPanel = new JPanel(new BorderLayout());
        cardPanel.add(nightPanel, "night");

        MenuData menuData = getUpdatedMenuData();

        powerOutage = new Jumpscare("office/powerOutage.gif", 0, null, -1, JumpscareVisual.STRETCHED);
        mainMenu = new Menu(menuData.background(), "menu/loading.jpg", menuData.menuItems()) {
            @Override
            protected Night onMenuItemClick(MenuItem item) throws IOException {
                switch (item.id()) {
                    case "test" -> {
                        return startSandboxNight();
                    }
                    case "tutorial" -> {
                        return startTutorialNight();
                    }
                    case "n1" -> {
                        return startNight1();
                    }
                    case "n2" -> {
                        return startNight2();
                    }
                    case "n3" -> {
                        return startNight3();
                    }
                    case "n4" -> {
                        return startNight4();
                    }
                    case "n5" -> {
                        return startNight5();
                    }
                    case "settings" -> {
                        cardLayout.show(cardPanel, "settings");
                        return null;
                    }
                    case "exit" -> {
                        dispose();
                        System.exit(0);
                        return null;
                    }
                    case "" -> {
                        return null;
                    }
                    default -> throw new MenuItemNotFound("Menu item \"" + item + "\" not found in this menu.");
                }
            }
        };
        cardPanel.add(mainMenu, "menu");
        cardLayout.show(cardPanel, "menu");

        settingsPanel = new JSettings(settings) {
            @Override
            public void onSettingsSaved(Settings saved) {
                if (Nights.this.settings.isFullscreen() != saved.isFullscreen()){
                    Nights.this.setFullScreen(saved.isFullscreen());
                }
                TinySound.setGlobalVolume(saved.getVolume());
                Nights.this.settings = new Settings(saved);
            }

            @Override
            public void onReturnToMenu() {
                cardLayout.show(cardPanel, "menu");
            }
        };
        cardPanel.add(settingsPanel, "settings");

        setMinimumSize(new Dimension(200, 50));
    }

    /**
     * @param subtitle Subtitle. Something like "Night 1" or "Settings menu", or null for nothing.
     * @return A String of the form "{@link Nights#GAME_TITLE} - <code>subtitle</code>".
     */
    public String getTitleForWindow(@Nullable String subtitle) {
        if (subtitle == null){
            return String.format("%s", GAME_TITLE);
        }
        return String.format("%s - %s", GAME_TITLE, subtitle);
    }

    /**
     * @param set <code>true</code> to set window to Fullscreen. <code>false</code> to simply maximize the window.
     */
    public void setFullScreen(boolean set) {
        Rectangle windowBounds = getBounds();

        GraphicsEnvironment graphicsEnvironment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] graphicsDevices =
                graphicsEnvironment.getScreenDevices();

        GraphicsDevice best = null;
        int bestArea = 0;
        for (GraphicsDevice graphicsDevice : graphicsDevices) {
            GraphicsConfiguration[] graphicsConfigurations =
                    graphicsDevice.getConfigurations();

            for (GraphicsConfiguration graphicsConfiguration : graphicsConfigurations) {
                Rectangle graphicsBounds =
                        graphicsConfiguration.getBounds();

                Rectangle intersection = windowBounds.intersection(graphicsBounds);

                int intersectionArea = intersection.width * intersection.height;
                if (intersectionArea > bestArea) {
                    best = graphicsDevice;
                    bestArea = intersectionArea;
                }
            }
        }

        dispose();
        setUndecorated(set);
        if (set) {
            if (best != null && best.isFullScreenSupported()) {
                best.setFullScreenWindow(this);
            } else {
                setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }
        } else {
            if (best != null) {
                best.setFullScreenWindow(null);
            }
            setBounds(windowBounds);
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }

        setVisible(true);
    }

    /**
     * Simply a method that updates the Menu, useful for when the player unlocked a new Night
     * after completing one.
     * @return A {@link MenuData} object with the information needed to build a Menu (items, background, etc).
     * @throws ResourceException If any of the loading screens could not be loaded.
     */
    private @NotNull MenuData getUpdatedMenuData() throws ResourceException {
        ArrayList<MenuItem> mmItems = new ArrayList<>(2);
        String background;
        java.util.List<String> completed = saveFile.completedNights();
        int numCompleted = completed.size();

        switch (numCompleted) {
            case 0 -> {
                background = "menu/background0.jpg";
                mmItems.add(new MenuItem("tutorial", "Tutorial Night", true, null));
            }
            case 1 -> {
                background = "menu/background1.jpg";
                mmItems.add(new MenuItem("n1", "Night 1", true, Resources.loadImageResource("night/n1/loading.jpg")));
                mmItems.add(new MenuItem("tutorial", "Repeat Tutorial", true, null));
            }
            case 2 -> {
                background = "menu/background2.jpg";
                mmItems.add(new MenuItem("n2", "Night 2", true, Resources.loadImageResource("night/n2/loading.jpg")));
                mmItems.add(new MenuItem("n1", "Repeat Night 1", true, Resources.loadImageResource("night/n1/loading.jpg")));
                mmItems.add(new MenuItem("tutorial", "Repeat Tutorial", true, null));
            }
            case 3 -> {
                background = "menu/background3.jpg";
                mmItems.add(new MenuItem("n3", "Night 3", true, Resources.loadImageResource("night/n3/loading.jpg")));
                mmItems.add(new MenuItem("n2", "Repeat Night 2", true, Resources.loadImageResource("night/n2/loading.jpg")));
                mmItems.add(new MenuItem("n1", "Repeat Night 1", true, Resources.loadImageResource("night/n1/loading.jpg")));
                mmItems.add(new MenuItem("tutorial", "Repeat Tutorial", true, null));
            }
            case 4 -> {
                background = "menu/background4.jpg";
                mmItems.add(new MenuItem("n4", "Night 4", true, Resources.loadImageResource("night/n4/loading.jpg")));
                mmItems.add(new MenuItem("n3", "Repeat Night 3", true, Resources.loadImageResource("night/n3/loading.jpg")));
                mmItems.add(new MenuItem("n2", "Repeat Night 2", true, Resources.loadImageResource("night/n2/loading.jpg")));
                mmItems.add(new MenuItem("n1", "Repeat Night 1", true, Resources.loadImageResource("night/n1/loading.jpg")));
                mmItems.add(new MenuItem("tutorial", "Repeat Tutorial", true, null));
            }
            case 5 -> {
                background = "menu/background5.jpg";
                mmItems.add(new MenuItem("n5", "Night 5", true, Resources.loadImageResource("night/n5/loading.jpg")));
                mmItems.add(new MenuItem("n4", "Repeat Night 4", true, Resources.loadImageResource("night/n4/loading.jpg")));
                mmItems.add(new MenuItem("n3", "Repeat Night 3", true, Resources.loadImageResource("night/n3/loading.jpg")));
                mmItems.add(new MenuItem("n2", "Repeat Night 2", true, Resources.loadImageResource("night/n2/loading.jpg")));
                mmItems.add(new MenuItem("n1", "Repeat Night 1", true, Resources.loadImageResource("night/n1/loading.jpg")));
                mmItems.add(new MenuItem("tutorial", "Repeat Tutorial", true, null));
            }
            case 6 -> {
                background = "menu/background6.jpg";
                mmItems.add(new MenuItem("n6", "Help me", true, Resources.loadImageResource("night/n6/loading.jpg")));
                mmItems.add(new MenuItem("n5", "Repeat Night 5", true, Resources.loadImageResource("night/n5/loading.jpg")));
                mmItems.add(new MenuItem("n4", "Repeat Night 4", true, Resources.loadImageResource("night/n4/loading.jpg")));
                mmItems.add(new MenuItem("n3", "Repeat Night 3", true, Resources.loadImageResource("night/n3/loading.jpg")));
                mmItems.add(new MenuItem("n2", "Repeat Night 2", true, Resources.loadImageResource("night/n2/loading.jpg")));
                mmItems.add(new MenuItem("n1", "Repeat Night 1", true, Resources.loadImageResource("night/n1/loading.jpg")));
                mmItems.add(new MenuItem("tutorial", "Repeat Tutorial", true, null));
            }
            default -> {
                RuntimeException error = new RuntimeException("Menu is not prepared for " + numCompleted +
                        " Nights completed. Cristichi forgot to add it.");
                new ExceptionViewer(error);
                dispose();
                throw error;
            }
        }
        if (Main.DEBUG)
            mmItems.add(new MenuItem("test", "TESTING NIGHT (DEBUG ONLY)", true, null));
        mmItems.add(new MenuItem("settings", "Settings", false, null));
        mmItems.add(new MenuItem("exit", "Run away", false, null));
        return new MenuData(mmItems, background);
    }

    /**
     * Just a sandbox Night for me to test. Not intended for gameplay.
     */
    private Night startSandboxNight() throws ResourceException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);
        CameraMap nightMap;
        if (Main.DEBUG_TEST_NIGHT_IS_RESTA){
            nightMap = new CrisRestaurantMap();
            nightMap.addCamAnimatronics("corridor 1",
                    //new Bob(1, Map.of(0, 20), false, false, List.of("cam4"), 555),
                    //new Maria(1, Map.of(0,0), false, false, List.of(), 5),
                    //new RoamingCris(1, Map.of(0,20), true, false, List.of("kitchen", "storage", "main stage", "dining area"), 5, rng)
                    new RoamingCris(3, 3, Map.of(0,20), true, false, List.of(), 0f, rng)
                    ///new Paco(4, Map.of(0,20), false, true, List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"), "kitchen", 1f, 555)
            );
        } else {
            nightMap = new TutorialMap();
            nightMap.setSelected("cam3");
            nightMap.remove("cam1");
            nightMap.remove("cam2");
            nightMap.addCamAnimatronics("cam3",
                    //new Bob(5, Map.of(0, 20), false, false, List.of("cam2", "cam4"), 1)
                    //new Maria(1, Map.of(0,0), false, false, List.of(), 3)
                    new RoamingCris(5, 5, Map.of(0,20), false, false, List.of(), 0f, rng)
                    //new Paco(4, Map.of(0,20), false, true, List.of("cam1", "cam2", "cam4", "rightDoor"), "cam1", 1f, 1)
            );
        }
        Night night = new Night("Testing", settings.getFps(), nightMap, "night/tutorial/paper.png",
                powerOutage, rng, 60, .45f, "night/tutorial/completed.wav");
        night.addOnNightEnd((completed) -> {
            nightPanel.removeAll();
            cardLayout.show(cardPanel, "menu");
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's testing night is using the seed \"%d\". Have fun!%n", seed);
        night.startNight();
        return night;
    }

    private Night startTutorialNight() throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);
        Map<Integer, Integer> aiNightBob = Map.of(1,2, 2,3, 3,0);

        Map<Integer, Integer> aiNightMaria = Map.of(0,0, 2,2, 3,3, 4,4);

        TutorialMap tutorialMap = new TutorialMap();
        tutorialMap.addCamAnimatronics("cam1", new RoamingBob(5, 8, aiNightBob, false, false, List.of("cam4"), 0f));
        tutorialMap.addCamAnimatronics("cam2", new RoamingMaria(5, 8, aiNightMaria, false, false, List.of("cam3"), 0f));

        Night night = new Night("Tutorial", settings.getFps(), tutorialMap, "night/tutorial/paper.png",
                powerOutage, rng, 60, 0.45f, "night/tutorial/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed) {
                System.out.println("You just passed the tutorial! Congratulations, but it was only the beginning.");
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(SaveFileIO.SAVE_FILE);
                    MenuData menuData = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuData.background()));
                    mainMenu.updateMenuItems(menuData.menuItems());
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's Tutorial is using the seed \"%d\". Have fun!%n", seed);
        night.startNight();
        return night;
    }

    private Night startNight1() throws IOException {
        AnimatronicDrawing bob = new RoamingBob(5, 6, Map.of(0,1, 4,2), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 0f);

        AnimatronicDrawing maria = new RoamingMaria(5, 6, Map.of(4,1), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge"), 0f);

        AnimatronicDrawing paco = new Paco(6, 6, Map.of(0,2, 4,3), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 1f, 0f);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);

        long seed = new Random().nextLong();
        Night night = new Night("Night 1", settings.getFps(), nightMap, "night/n1/paper.png",
                powerOutage, new Random(seed), 90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed) {
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(SaveFileIO.SAVE_FILE);
                    MenuData menuData = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuData.background()));
                    mainMenu.updateMenuItems(menuData.menuItems());
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }

    private Night startNight2() throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);
        AnimatronicDrawing bob = new RoamingBob(5, 5, Map.of(0,4), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 0f);

        AnimatronicDrawing maria = new RoamingMaria(5, 5, Map.of(1,1, 4,2), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge"), 0f);

        AnimatronicDrawing paco = new Paco(6, 5, Map.of(0,4, 4,5), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 1f, 0f);

        AnimatronicDrawing crisIsClose = new RoamingCris(5, 5, Map.of(0,1, 4,2, 5,3), true, false,
                List.of("kitchen", "storage", "main stage", "staff lounge", "bathrooms"), 0f, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("staff lounge", crisIsClose);

        Night night = new Night("Night 2", settings.getFps(), nightMap, "night/n2/paper.png",
                powerOutage, rng, 90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(SaveFileIO.SAVE_FILE);
                    MenuData menuData = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuData.background()));
                    mainMenu.updateMenuItems(menuData.menuItems());
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }

            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }

    private Night startNight3() throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

        AnimatronicDrawing bob = new RoamingBob(5, 5, Map.of(0,7), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 0f);

        AnimatronicDrawing maria = new RoamingMaria(5, 5, Map.of(1,3, 4,5), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge"), 0f);

        AnimatronicDrawing paco = new Paco(6, 5, Map.of(0,5, 4,7), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 1f, 0f);

        AnimatronicDrawing crisRandomSideAllNight = new RoamingCris(5, 5, Map.of(0,1, 4,2, 5,3), true, false,
                List.of("kitchen", "storage", "dining area", "main stage"), 0f, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisRandomSideAllNight);
        nightMap.get("bathrooms").setBroken(true);

        Night night = new Night("Night 3", settings.getFps(), nightMap, "night/n4/paper.png", powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(SaveFileIO.SAVE_FILE);
                    MenuData menuData = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuData.background()));
                    mainMenu.updateMenuItems(menuData.menuItems());
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }

            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }

    private Night startNight4() throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

        AnimatronicDrawing bob = new RoamingBob(5, 5, Map.of(0,8, 4,9), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices", "storage", "kitchen"), 0f);

        AnimatronicDrawing maria = new RoamingMaria(5, 5, Map.of(0,3, 2,5, 4,6), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge", "storage", "kitchen"), 0f);

        AnimatronicDrawing paco = new Paco(6, 5, Map.of(0,7, 5,8), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 1f, 0f);

        AnimatronicDrawing crisChoosesPathAndTeleports = new PathCris(5, Map.of(0,1, 4,2, 5,4), true, false,
                List.of(
                        List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                        List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor")
                ), "storage", 5, 0f, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisChoosesPathAndTeleports);
        nightMap.get("main stage").setBroken(true);

        Night night = new Night("Night 4", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(SaveFileIO.SAVE_FILE);
                    MenuData menuData = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuData.background()));
                    mainMenu.updateMenuItems(menuData.menuItems());
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }

    private Night startNight5() throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

        AnimatronicDrawing bob = new RoamingBob(5, 5, Map.of(0,9, 4,10), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices", "storage", "kitchen"), 0f);

        AnimatronicDrawing maria = new RoamingMaria(5, 5, Map.of(0,4, 2,7, 4,9), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge", "storage", "kitchen"), 0f);

        AnimatronicDrawing paco = new Paco(6, 5, Map.of(0,9), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 1f, 0f);

        AnimatronicDrawing crisChoosesPathAndTeleports = new PathCris(5, Map.of(0,3, 1,4, 2,5, 3,6, 5,7), true, false,
                List.of(
                        List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                        List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor"),
                        List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                        List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
                ), "storage", 5, 0f, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisChoosesPathAndTeleports);
        nightMap.get("bathrooms").setBroken(true);
        nightMap.get("main stage").setBroken(true);

        Night night = new Night("Night 5", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(SaveFileIO.SAVE_FILE);
                    MenuData menuData = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuData.background()));
                    mainMenu.updateMenuItems(menuData.menuItems());
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }
}
