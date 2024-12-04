package es.cristichi.fnac;

import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.gui.ExceptionViewer;
import es.cristichi.fnac.gui.Menu;
import es.cristichi.fnac.gui.Night;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.SaveFileIO;
import es.cristichi.fnac.obj.anim.*;
import es.cristichi.fnac.obj.cams.CameraMap;
import es.cristichi.fnac.obj.cams.CrisRestaurantMap;
import es.cristichi.fnac.obj.cams.TutorialMap;
import kuusisto.tinysound.TinySound;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Main {
    public static final String GAME_TITLE = "Five Nights at Cris'";
    private static JPanel cardPanel;
    private static JPanel nightPanel;
    private static Menu mainMenu;
    private static Jumpscare powerOutage;

    public static String getTitleForWindow(String window) {
        if (window == null){
            return String.format("%s", GAME_TITLE);
        }
        return String.format("%s - %s", GAME_TITLE, window);
    }

    public static void main(String[] args) {
        TinySound.init();

        SaveFileIO.SaveFile saveFile;
        try {
            saveFile = SaveFileIO.loadFromFile(SaveFileIO.SAVE_FILE);

            // Initialize the GUI on the EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    initializeGUI(saveFile);
                } catch (Exception e) {
                    new ExceptionViewer(new Exception("Error when trying to prepare the GUI and Nights.", e));
                    File log = new File("error.log");
                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(log));
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        bw.write(sw.toString());
                    } catch (IOException ioException) {
                        new ExceptionViewer(new Exception("Error when trying to write log.", ioException));
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to load save file: " + e.getMessage());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> new ExceptionViewer(e));
        }
    }

    private static void initializeGUI(SaveFileIO.SaveFile saveFile) throws IOException, AWTException {
        JFrame window = new JFrame(GAME_TITLE);
        window.setSize(800, 600);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setIconImage(Resources.loadImageResource("icon.jpg"));
        window.setTitle(getTitleForWindow(null));
        window.setLayout(new BorderLayout());

        CardLayout cards = new CardLayout();
        cardPanel = new JPanel(cards);
        window.add(cardPanel);

        nightPanel = new JPanel(new BorderLayout());
        cardPanel.add(nightPanel, "night");

        Robot robot = new Robot();
        new Timer(10, e -> {
            if (window.isFocused()) {
                Point cursorLocation = MouseInfo.getPointerInfo().getLocation();
                Rectangle bounds = window.getBounds();

                // Check if the cursor is outside the bounds
                if (!bounds.contains(cursorLocation)) {
                    int closestX = Math.max(bounds.x, Math.min(cursorLocation.x, bounds.x + bounds.width - 1));
                    int closestY = Math.max(bounds.y, Math.min(cursorLocation.y, bounds.y + bounds.height - 1));
                    robot.mouseMove(closestX, closestY);
                }
            }
        }).start();

        MenuData menuData = getUpdatedMenuData(saveFile);

        powerOutage = new Jumpscare("office/powerOutage.gif", 40, 0, null, -1);
        mainMenu = createMenu(saveFile, cards, menuData.background(), menuData.mmItems(), window);
        cardPanel.add(mainMenu, "menu");
        cards.show(cardPanel, "menu");

        window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        window.setMinimumSize(new Dimension(600, 400));
        window.setVisible(true);
    }

    private static @NotNull MenuData getUpdatedMenuData(SaveFileIO.SaveFile saveFile) {
        ArrayList<String> mmItems = new ArrayList<>(2);
        String background;
        List<String> completed = saveFile.completedNights();
        int numCompleted = completed.size();

        if (numCompleted == 0) {
            background = "menu/background0.jpg";
            mmItems.add("Tutorial Night");
        } else if (numCompleted == 1) {
            background = "menu/background1.jpg";
            mmItems.add("Night 1");
            mmItems.add("Repeat Tutorial");
        } else if (numCompleted == 2) {
            background = "menu/background2.jpg";
            mmItems.add("Night 2");
            mmItems.add("Repeat Night 1");
            mmItems.add("Repeat Tutorial");
        } else if (numCompleted == 3) {
            background = "menu/background3.jpg";
            mmItems.add("Night 3");
            mmItems.add("Repeat Night 2");
            mmItems.add("Repeat Night 1");
            mmItems.add("Repeat Tutorial");
        } else if (numCompleted == 4) {
            background = "menu/background4.jpg";
            mmItems.add("More Nights don't exist (yet)");
            mmItems.add("Repeat Night 3");
            mmItems.add("Repeat Night 2");
            mmItems.add("Repeat Night 1");
            mmItems.add("Repeat Tutorial");
        } else {
            RuntimeException error = new RuntimeException("Menu is not prepared for "+numCompleted+
                    " nights completed. Cristichi forgot to add it.");
            new ExceptionViewer(error);
            background = "menu/background2.jpg";
            mmItems.add("More Nights don't exist (yet)");
            mmItems.add("Repeat Night 1");
            mmItems.add("Repeat Tutorial");
        }
        //mmItems.add("Testing Night");
        mmItems.add("Exit");
        return new MenuData(mmItems, background);
    }

    private record MenuData(ArrayList<String> mmItems, String background) {
    }

    private static Menu createMenu(SaveFileIO.SaveFile saveFile, CardLayout cards, String background, ArrayList<String> mmItems, JFrame window) throws IOException {
        return new Menu(background, "menu/loading.jpg", mmItems) {
            @Override
            protected Night onMenuItemClick(String item) throws IOException {
                switch (item) {
                    case "Testing Night" -> {
                        return startTESTINGNIGHT(cards, window);
                    }
                    case "Tutorial Night", "Repeat Tutorial" -> {
                        return startTutorialNight(saveFile, cards, window);
                    }
                    case "Night 1", "Repeat Night 1" -> {
                        return startNight1(saveFile, cards, window);
                    }
                    case "Night 2", "Repeat Night 2" -> {
                        return startNight2(saveFile, cards, window);
                    }
                    case "Night 3", "Repeat Night 3" -> {
                        return startNight3(saveFile, cards, window);
                    }
                    case "Exit" -> {
                        window.dispose();
                        System.exit(0);
                        return null;
                    }
                    default -> throw new MenuItemNotFound("Menu item \"" + item + "\" not found in this menu.");
                }
            }
        };
    }

    @SuppressWarnings("all")
    private static Night startTESTINGNIGHT(CardLayout cards, JFrame window) throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);
        boolean newnight = true;
        CameraMap nightMap;
        if (newnight){
            nightMap = new CrisRestaurantMap();
            ((CrisRestaurantMap) nightMap).addCamAnimatronics("dining area",
                    //new Bob(1, Map.of(0, 20), List.of("cam4"), 555),
                    //new Maria(1, Map.of(0,0), List.of(), 5),
                    new RoamingCris(1, Map.of(0,20), List.of("kitchen", "storage", "main stage", "dining area"), 5, rng)
                    ///new Paco(4, Map.of(0,20), List.of("kitchen", "dining area", "corridor 1", "corridor 3"), "kitchen", 1f, 555)
            );
        } else {
            nightMap = new TutorialMap();
            ((TutorialMap) nightMap).addCam1Animatronics(
                    new Bob(1, Map.of(0, 20), List.of("cam4"), 555)
//                        new Maria(1, Map.of(0,0), List.of(), 5),
//                        new Cris(1, Map.of(0,20), List.of(), 5),
//                        new Paco(4, Map.of(0,20), List.of("cam1", "cam2", "cam4"), "cam1", 1f, 555)
            );
        }
        Night night = new Night("Testing", nightMap, "night/tutorial/paper.png",
                powerOutage, rng, 60, 0.45f,
                Resources.loadSound("night/tutorial/completed.wav", "tutorialCom.wav")) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }
        };
        night.addOnNightEnd((completed) -> {
            nightPanel.removeAll();
            cards.show(cardPanel, "menu");
        });
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's testing night is using the seed \"%d\". Have fun!%n", seed);
        night.startNight();
        return night;
    }

    private static Night startTutorialNight(SaveFileIO.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);
        Map<Integer, Integer> aiNightBob = Map.of(0,0, 1,1, 4,2);

        Map<Integer, Integer> aiNightMaria = Map.of(0,0, 2,1, 4,2);

        TutorialMap tutorialMap = new TutorialMap();
        tutorialMap.addCam1Animatronics(new Bob(5, aiNightBob, List.of("cam4"), 8));
        tutorialMap.addCam2Animatronics(new Maria(5, aiNightMaria, List.of("cam3"), 8));

        Night night = new Night("Tutorial", tutorialMap, "night/tutorial/paper.png",
                powerOutage, rng, 60, 0.45f,
                Resources.loadSound("night/tutorial/completed.wav", "tutorialCom.wav")) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }
        };
        night.addOnNightEnd((completed) -> {
            if (completed) {
                saveFile.addCompletedNight(night.getNightName());
                try {
                    SaveFileIO.saveToFile(SaveFileIO.SAVE_FILE, saveFile);
                    MenuData menuData = getUpdatedMenuData(saveFile);
                    mainMenu.updateBackground(menuData.background);
                    mainMenu.updateMenuItems(menuData.mmItems);
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }
            cards.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            System.out.println("You just passed the tutorial! Congratulations, but it was only the beginning.");
        });
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's Tutorial is using the seed \"%d\". Have fun!%n", seed);
        night.startNight();
        return night;
    }

    private static Night startNight1(SaveFileIO.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
        AnimatronicDrawing bob = new Bob(5, Map.of(0,1, 4,2),
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 6);

        AnimatronicDrawing maria = new Maria(5, Map.of(4,1),
                List.of("corridor 1", "corridor 3", "staff lounge"), 6);

        AnimatronicDrawing paco = new Paco(6, Map.of(0,2, 4,3),
                List.of("kitchen", "dining area", "corridor 1", "corridor 3"), "kitchen", 1f, 6);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);

        long seed = new Random().nextLong();
        Night night = new Night("Night 1", nightMap, "night/n1/paper.png",
                powerOutage, new Random(seed), 90, 0.45f,
                Resources.loadSound("night/general/completed.wav", "ngCom.wav")) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
            }
        };
        night.addOnNightEnd((completed) -> {
            if (completed) {
                saveFile.addCompletedNight(night.getNightName());
                try {
                    SaveFileIO.saveToFile(SaveFileIO.SAVE_FILE, saveFile);
                    MenuData menuData = getUpdatedMenuData(saveFile);
                    mainMenu.updateBackground(menuData.background);
                    mainMenu.updateMenuItems(menuData.mmItems);
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }
            cards.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }

    private static Night startNight2(SaveFileIO.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);
        AnimatronicDrawing bob = new Bob(5, Map.of(0,4),
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 5);

        AnimatronicDrawing maria = new Maria(5, Map.of(1,1, 4,2),
                List.of("corridor 1", "corridor 3", "staff lounge"), 5);

        AnimatronicDrawing paco = new Paco(6, Map.of(0,4, 4,5),
                List.of("kitchen", "dining area", "corridor 1", "corridor 3"), "kitchen", 1f, 5);

        AnimatronicDrawing crisIsClose = new RoamingCris(5, Map.of(0,1, 4,2, 5,3),
                List.of("kitchen", "storage", "main stage", "staff lounge", "bathrooms"), 5, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("staff lounge", crisIsClose);

        Night night = new Night("Night 2", nightMap, "night/n2/paper.png",
                powerOutage, rng, 90, 0.45f,
                Resources.loadSound("night/general/completed.wav", "ngCom.wav")) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
            }
        };
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    SaveFileIO.saveToFile(SaveFileIO.SAVE_FILE, saveFile);
                    MenuData menuData = getUpdatedMenuData(saveFile);
                    mainMenu.updateBackground(menuData.background);
                    mainMenu.updateMenuItems(menuData.mmItems);
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }

            cards.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }

    private static Night startNight3(SaveFileIO.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
        long seed = new Random().nextLong();
        Random rng = new Random(seed);

        AnimatronicDrawing bob = new Bob(5, Map.of(0,7),
                List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 5);

        AnimatronicDrawing maria = new Maria(5, Map.of(1,3, 4,5),
                List.of("corridor 1", "corridor 3", "staff lounge"), 5);

        AnimatronicDrawing paco = new Paco(6, Map.of(0,5, 4,7),
                List.of("kitchen", "dining area", "corridor 1", "corridor 3"), "kitchen", 1f, 5);

        AnimatronicDrawing crisChoosesSide = new RoamingCris(5, Map.of(0,1, 4,2, 5,4),
                List.of("kitchen", "storage", "dining area", "main stage"), 5, rng);

        CrisRestaurantMap nightMap = new CrisRestaurantMap();
        nightMap.addCamAnimatronics("kitchen", paco);
        nightMap.addCamAnimatronics("storage", bob);
        nightMap.addCamAnimatronics("offices", maria);
        nightMap.addCamAnimatronics("dining area", crisChoosesSide);
        nightMap.get("bathrooms").setBroken(true);

        Night night = new Night("Night 3", nightMap, null,
                powerOutage, rng, 90, 0.45f,
                Resources.loadSound("night/general/completed.wav", "ngCom.wav")) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
            }
        };
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    SaveFileIO.saveToFile(SaveFileIO.SAVE_FILE, saveFile);
                    MenuData menuData = getUpdatedMenuData(saveFile);
                    mainMenu.updateBackground(menuData.background);
                    mainMenu.updateMenuItems(menuData.mmItems);
                } catch (IOException e) {
                    new ExceptionViewer(new IOException("Progress could not be saved due to an error.", e));
                }
            }

            cards.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
        });
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }
}

