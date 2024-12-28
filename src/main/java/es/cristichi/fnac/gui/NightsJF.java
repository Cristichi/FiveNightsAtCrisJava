package es.cristichi.fnac.gui;

import es.cristichi.fnac.Main;
import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.cnight.CustomNightMenuJC;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.anim.*;
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
public class NightsJF extends JFrame {
    public final String GAME_TITLE = "Five Nights at Cris'";
    private final JPanel cardPanel;
    private final SettingsJC settingsPanel;
    private final JPanel nightPanel;
    private final MenuJC mainMenu;
    private final Jumpscare powerOutage;

    private final NightProgress.SaveFile saveFile;
    private final CardLayout cardLayout;
    private Settings settings;

    /**
     * Creates a new window for the game.
     * @param saveFile Save file.
     * @param settings Settings of the user.
     * @throws ResourceException If an error occurs when loading a Resource.
     */
    public NightsJF(NightProgress.SaveFile saveFile, Settings settings) throws ResourceException {
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

        MenuJC.Info menuInfo = getUpdatedMenuData();

        powerOutage = new Jumpscare("office/powerOutage.gif", 0, null, -1, JumpscareVisualSetting.STRETCHED);
        mainMenu = new MenuJC(menuInfo.background(), "menu/loading.jpg", menuInfo.menuItems()) {
            @Override
            protected void onMenuItemClick(Item item) throws IOException, NightException {
                switch (item.id()) {
                    case "tutorial" -> startTutorialNight();
                    case "n1" -> startNight1();
                    case "n2" -> startNight2();
                    case "n3" -> startNight3();
                    case "n4" -> startNight4();
                    case "n5" -> startNight5();
                    case "n6" -> startNight6();
                    case "custom" -> {
                        CustomNightMenuJC customNightMenu = new CustomNightMenuJC(settings, powerOutage, NightsJF.this);
                        customNightMenu.addOnExitListener(() -> cardLayout.show(cardPanel, "menu"));
                        cardPanel.add(customNightMenu, "customNightMenu");
                        cardLayout.show(cardPanel, "customNightMenu");
                    }
                    case "settings" -> cardLayout.show(cardPanel, "settings");
                    case "exit" -> {
                        dispose();
                        System.exit(0);
                    }
                    default -> throw new MenuItemNotFound("Menu item \"" + item + "\" not found in this menu.");
                }
            }
        };
        cardPanel.add(mainMenu, "menu");
        cardLayout.show(cardPanel, "menu");

        settingsPanel = new SettingsJC(settings) {
            @Override
            public void onSettingsSaved(Settings saved) {
                if (NightsJF.this.settings.isFullscreen() != saved.isFullscreen()){
                    NightsJF.this.setFullScreen(saved.isFullscreen());
                }
                TinySound.setGlobalVolume(saved.getVolume());
                NightsJF.this.settings = new Settings(saved);
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
     * @return A String of the form "{@link NightsJF#GAME_TITLE} - <code>subtitle</code>".
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
     * @return A {@link MenuJC.Info} object with the information needed to build a Menu (items, background, etc).
     * @throws ResourceException If any of the loading screens could not be loaded.
     */
    private @NotNull MenuJC.Info getUpdatedMenuData() throws ResourceException {
        ArrayList<MenuJC.Item> mmItems = new ArrayList<>(2);
        String background;
        java.util.List<String> completed = saveFile.completedNights();
        int numCompleted = completed.size();

        switch (numCompleted) {
            case 0 -> {
                background = "menu/background0.jpg";
                mmItems.add(new MenuJC.Item("tutorial", "Start Game", "Tutorial Night", null));
            }
            case 1 -> {
                background = "menu/background1.jpg";
                mmItems.add(new MenuJC.Item("n1", "Continue", "Night 1", Resources.loadImageResource("night/n1/loading.jpg")));
                if (Main.DEBUG){
                    mmItems.add(new MenuJC.Item("tutorial", "Tutorial", "Repeat Tutorial", null));
                }
            }
            case 2 -> {
                background = "menu/background2.jpg";
                mmItems.add(new MenuJC.Item("n2", "Continue", "Night 2", Resources.loadImageResource("night/n2/loading.jpg")));
                if (Main.DEBUG) {
                    mmItems.add(new MenuJC.Item("n1", "Rep 1", "Repeat Night 1", Resources.loadImageResource("night/n1/loading.jpg")));
                    mmItems.add(new MenuJC.Item("tutorial", "Rep Tut", "Repeat Tutorial", null));
                }
            }
            case 3 -> {
                background = "menu/background3.jpg";
                mmItems.add(new MenuJC.Item("n3", "Continue", "Night 3", Resources.loadImageResource("night/n3/loading.jpg")));
                if (Main.DEBUG) {
                    mmItems.add(new MenuJC.Item("n2", "Rep 2", "Repeat Night 2", Resources.loadImageResource("night/n2/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n1", "Rep 1", "Repeat Night 1", Resources.loadImageResource("night/n1/loading.jpg")));
                    mmItems.add(new MenuJC.Item("tutorial", "Rep Tut", "Repeat Tutorial", null));
                }
            }
            case 4 -> {
                background = "menu/background4.jpg";
                mmItems.add(new MenuJC.Item("n4", "Continue", "Night 4", Resources.loadImageResource("night/n4/loading.jpg")));
                if (Main.DEBUG) {
                    mmItems.add(new MenuJC.Item("n3", "Rep 3", "Repeat Night 3", Resources.loadImageResource("night/n3/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n2", "Rep 2", "Repeat Night 2", Resources.loadImageResource("night/n2/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n1", "Rep 1", "Repeat Night 1", Resources.loadImageResource("night/n1/loading.jpg")));
                    mmItems.add(new MenuJC.Item("tutorial", "Rep Tut", "Repeat Tutorial", null));
                }
            }
            case 5 -> {
                background = "menu/background5.jpg";
                mmItems.add(new MenuJC.Item("n5", "Continue", "Night 5", Resources.loadImageResource("night/n5/loading.jpg")));
                if (Main.DEBUG) {
                    mmItems.add(new MenuJC.Item("n4", "Rep 4", "Repeat Night 4", Resources.loadImageResource("night/n4/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n3", "Rep 3", "Repeat Night 3", Resources.loadImageResource("night/n3/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n2", "Rep 2", "Repeat Night 2", Resources.loadImageResource("night/n2/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n1", "Rep 1", "Repeat Night 1", Resources.loadImageResource("night/n1/loading.jpg")));
                    mmItems.add(new MenuJC.Item("tutorial", "Rep Tut", "Repeat Tutorial", null));
                }
            }
            case 6 -> {
                background = "menu/background6.jpg";
                mmItems.add(new MenuJC.Item("n6", "Help me", "Night 6", Resources.loadImageResource("night/n6/loading.jpg")));
                if (Main.DEBUG) {
                    mmItems.add(new MenuJC.Item("n5", "Rep 5", "Repeat Night 5", Resources.loadImageResource("night/n5/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n4", "Rep 4", "Repeat Night 4", Resources.loadImageResource("night/n4/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n3", "Rep 3", "Repeat Night 3", Resources.loadImageResource("night/n3/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n2", "Rep 2", "Repeat Night 2", Resources.loadImageResource("night/n2/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n1", "Rep 1", "Repeat Night 1", Resources.loadImageResource("night/n1/loading.jpg")));
                    mmItems.add(new MenuJC.Item("tutorial", "Rep Tut", "Repeat Tutorial", null));
                }
            }
            default -> {
                background = "menu/backgroundCustom.jpg";
                mmItems.add(new MenuJC.Item("custom", "Play with Us!", "Custom Night", Resources.loadImageResource("night/custom/loading.jpg")));
                if (Main.DEBUG) {
                    mmItems.add(new MenuJC.Item("n6", "Rep 6", "Repeat Night 6", Resources.loadImageResource("night/n6/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n5", "Rep 5", "Repeat Night 5", Resources.loadImageResource("night/n5/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n4", "Rep 4", "Repeat Night 4", Resources.loadImageResource("night/n4/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n3", "Rep 3", "Repeat Night 3", Resources.loadImageResource("night/n3/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n2", "Rep 2", "Repeat Night 2", Resources.loadImageResource("night/n2/loading.jpg")));
                    mmItems.add(new MenuJC.Item("n1", "Rep 1", "Repeat Night 1", Resources.loadImageResource("night/n1/loading.jpg")));
                    mmItems.add(new MenuJC.Item("tutorial", "Rep Tut", "Repeat Tutorial", null));
                }
            }
        }
        mmItems.add(new MenuJC.Item("settings", "Settings", "Settings", null));
        mmItems.add(new MenuJC.Item("exit", "Run Away", "I'm Sorry", null));
        return new MenuJC.Info(mmItems, background);
    }

    private void startTutorialNight() throws IOException, NightException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);
        Map<Integer, Integer> aiNightBob = Map.of(1,2, 2,3, 3,0);

        Map<Integer, Integer> aiNightMaria = Map.of(0,0, 2,2, 3,3, 4,4);

        TutorialMap tutorialMap = new TutorialMap();
        tutorialMap.addCamAnimatronics("cam1", new RoamingBob("Bob", aiNightBob, false, false, List.of("cam4"), 0f, rng));
        tutorialMap.addCamAnimatronics("cam2", new RoamingMaria("Maria", aiNightMaria, false, false, List.of("cam3"), 0f, rng));

        NightJC night = new NightJC("Tutorial", settings.getFps(), tutorialMap, "night/tutorial/paper.png",
                powerOutage, rng, 60, 0.45f, "night/tutorial/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed) {
                System.out.println("You just passed the tutorial! Congratulations, but it was only the beginning.");
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's Tutorial is using the seed \"%d\". Have fun!%n", seed);
        mainMenu.stopMusic();
        night.startNight();
    }

    private void startNight1() throws IOException, NightException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,1, 4,2), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 0f, rng);

        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(4,1), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge"), 0f, rng);

        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,2, 4,3), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 0f, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);

        NightJC night = new NightJC("Night 1", settings.getFps(), nightMap, "night/n1/paper.png",
                powerOutage, rng, 90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed) {
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        mainMenu.stopMusic();
        night.startNight();
    }

    private void startNight2() throws IOException, NightException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,4), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 0f, rng);

        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(1,1, 4,2), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge"), 0f, rng);

        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,4, 4,5), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 0f, rng);

        AnimatronicDrawing crisIsClose = new RoamingCris("Cris", Map.of(0,1, 4,2, 5,3), true, false,
                List.of("kitchen", "storage", "main stage", "staff lounge", "bathrooms"), 0f, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("staff lounge", crisIsClose);

        NightJC night = new NightJC("Night 2", settings.getFps(), nightMap, "night/n2/paper.png",
                powerOutage, rng, 90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
                }
            }

            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        mainMenu.stopMusic();
        night.startNight();
    }

    private void startNight3() throws IOException, NightException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

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

        NightJC night = new NightJC("Night 3", settings.getFps(), nightMap, "night/n4/paper.png", powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
                }
            }

            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        mainMenu.stopMusic();
        night.startNight();
    }

    private void startNight4() throws IOException, NightException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,8, 4,9), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices", "storage", "kitchen"), 0f, rng);

        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(0,3, 2,5, 4,6), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge", "storage", "kitchen"), 0f, rng);

        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,7, 5,8), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 0f, rng);

        AnimatronicDrawing crisChoosesPathAndTeleports = new PathCris("Cris", Map.of(0,1, 4,2, 5,4), true, false, List.of(
                List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor")
        ),
                0f, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisChoosesPathAndTeleports);
        nightMap.get("main stage").setBroken(true);

        NightJC night = new NightJC("Night 4", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        mainMenu.stopMusic();
        night.startNight();
    }

    private void startNight5() throws IOException, NightException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

        AnimatronicDrawing bob = new RoamingBob("Bob", Map.of(0,9, 4,10), false, false,
                List.of("corridor 2", "corridor 4", "bathrooms", "offices", "storage", "kitchen"), 0f, rng);

        AnimatronicDrawing maria = new RoamingMaria("Maria", Map.of(0,4, 2,7, 4,9), false, false,
                List.of("corridor 1", "corridor 3", "staff lounge", "storage", "kitchen"), 0f, rng);

        AnimatronicDrawing paco = new Paco("Paco", Map.of(0,9), false, true,
                List.of(
                        List.of("kitchen", "dining area", "corridor 1", "corridor 3", "leftDoor"),
                        List.of("kitchen", "dining area", "corridor 2", "corridor 4", "rightDoor")
                ), 0f, rng);

        AnimatronicDrawing crisChoosesPathAndTeleports = new PathCris("Cris", Map.of(0,3, 1,4, 2,5, 3,6, 5,7), true, false, List.of(
                List.of("storage", "dining area", "staff lounge", "corridor 3", "leftDoor"),
                List.of("storage", "dining area", "staff lounge", "corridor 4", "rightDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 4", "rightDoor"),
                List.of("storage", "dining area", "offices", "bathrooms", "corridor 3", "leftDoor")
        ),
                0f, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisChoosesPathAndTeleports);
        nightMap.get("bathrooms").setBroken(true);
        nightMap.get("main stage").setBroken(true);

        NightJC night = new NightJC("Night 5", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        mainMenu.stopMusic();
        night.startNight();
    }

    private void startNight6() throws IOException, NightException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

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

        NightJC night = new NightJC("Night 6", settings.getFps(), nightMap, null, powerOutage, rng,
                90, 0.45f, "night/general/completed.wav");
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        mainMenu.stopMusic();
        night.startNight();
    }

    public void startCustomNight(NightJC night) {
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
                }
            }
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        mainMenu.stopMusic();
        night.startNight();
    }
}
