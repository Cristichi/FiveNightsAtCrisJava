package es.cristichi.fnac;

import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.gui.ExceptionViewer;
import es.cristichi.fnac.gui.Menu;
import es.cristichi.fnac.gui.Night;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.SaveFileIO;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import es.cristichi.fnac.obj.anim.*;
import kuusisto.tinysound.TinySound;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Main {
    public static final String GAME_TITLE = "Five Nights at Cris'";
    private static JPanel cardPanel;
    private static JPanel nightPanel;
    private static Menu mainMenu;

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

        Timer mouseRestrictTimer = getMouseRestrictTimer(window);
        mouseRestrictTimer.start();

        MenuData menuData = getUpdatedMenuData(saveFile);

        mainMenu = createMenu(saveFile, cards, menuData.background(), menuData.mmItems(), window);
        cardPanel.add(mainMenu, "menu");
        cards.show(cardPanel, "menu");

        window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        window.setMinimumSize(new Dimension(600, 400));
        window.setVisible(true);
    }

    private static Timer getMouseRestrictTimer(JFrame window) throws AWTException {
        Robot robot = new Robot();
        return new Timer(10, e -> {
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
        });
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
            mmItems.add("More Nights don't exist (yet)");
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
        mmItems.add("Testing Night");
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

    private static Night startTESTINGNIGHT(CardLayout cards, JFrame window) throws IOException {
        HashMap<Integer, Integer> aiNightBob = new HashMap<>(4);
        aiNightBob.put(0, 0);

        HashMap<Integer, Integer> aiNightMaria = new HashMap<>(4);
        aiNightMaria.put(0, 0);

        HashMap<Integer, Integer> aiNightPaco = new HashMap<>(4);
        aiNightPaco.put(0, 0);

        HashMap<Integer, Integer> aiNightCris = new HashMap<>(4);
        aiNightCris.put(0, 20);

        CameraMap nightMap = new CameraMap(Resources.loadImageResource("night/tutorial/map.png"), "cam3");
        Camera cam1 = new Camera.Builder()
                .setName("cam1")
                .setCamBackground("night/tutorial/cam1.jpg")
                .setOnMapLoc(113, 111, 378, 177)
                .setSoundVolume(0.5)
                .setSoundPan(-1)
                .addAnimatronics(new Bob(5, aiNightBob, List.of(), 2),
                        new Maria(5, aiNightMaria, List.of(), 2),
                        new Paco(5, aiNightPaco, List.of("cam1", "cam2", "cam4"), "cam1", 1f, 2),
                        new Cris(5, aiNightCris, List.of(), 1)
                )
                .addConnection("cam2", "cam3")
                .build();
        Camera cam2 = new Camera.Builder()
                .setName("cam2")
                .setCamBackground("night/tutorial/cam2.jpg")
                .setOnMapLoc(491, 117, 379, 177)
                .setSoundVolume(0.5)
                .setSoundPan(1)
                .addConnection("cam1", "cam4")
                .build();
        Camera cam3 = new Camera.Builder()
                .setName("cam3")
                .setCamBackground("night/tutorial/cam3.jpg")
                .setOnMapLoc(134, 287, 167, 571)
                .setSoundVolume(1)
                .setSoundPan(-1)
                .addConnection("cam1")
                .connectToOfficeLeft()
                .build();
        Camera cam4 = new Camera.Builder()
                .setName("cam4")
                .setCamBackground("night/tutorial/cam4.jpg")
                .setOnMapLoc(720, 296, 141, 586)
                .setSoundVolume(1)
                .setSoundPan(1)
                .addConnection("cam2")
                .connectToOfficeRight()
                .build();
        nightMap.addAll(cam1, cam2, cam3, cam4);
        long seed = new Random().nextLong();
        Night night = new Night("Testing", nightMap, "night/tutorial/paper.png",
                new Jumpscare("office/powerOutage.gif", 1), new Random(seed), 60, 0.45f,
                Resources.loadSound("night/tutorial/completed.wav", "tutorialCom.wav")) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }
        };
        night.addOnNightCompleted(() -> {
            nightPanel.removeAll();
            cards.show(cardPanel, "menu");
            System.out.println("You just passed the tutorial! Congratulations, but it was only the beginning.");
        });
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's testing night is using the seed \"%d\". Have fun!%n", seed);
        night.startNight();
        return night;
    }

    private static Night startTutorialNight(SaveFileIO.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
        HashMap<Integer, Integer> aiNightBob = new HashMap<>(4);
        aiNightBob.put(0, 0);
        aiNightBob.put(1, 1);
        aiNightBob.put(4, 2);

        HashMap<Integer, Integer> aiNightMaria = new HashMap<>(4);
        aiNightMaria.put(0, 0);
        aiNightMaria.put(3, 1);
        aiNightMaria.put(4, 2);
        aiNightMaria.put(5, 3);

        CameraMap nightMap = new CameraMap(Resources.loadImageResource("night/tutorial/map.png"), "cam3");
        Camera cam1 = new Camera.Builder()
                .setName("cam1")
                .setCamBackground("night/tutorial/cam1.jpg")
                .setOnMapLoc(113, 111, 378, 177)
                .setSoundVolume(0.5)
                .setSoundPan(-1)
                .addAnimatronics(new Bob(5, aiNightBob, List.of("cam4"), 8))
                .addConnection("cam2", "cam3")
                .build();
        Camera cam2 = new Camera.Builder()
                .setName("cam2")
                .setCamBackground("night/tutorial/cam2.jpg")
                .setOnMapLoc(491, 117, 379, 177)
                .setSoundVolume(0.5)
                .setSoundPan(1)
                .addAnimatronics(new Maria(5, aiNightMaria, List.of("cam3"), 8))
                .addConnection("cam1", "cam4")
                .build();
        Camera cam3 = new Camera.Builder()
                .setName("cam3")
                .setCamBackground("night/tutorial/cam3.jpg")
                .setOnMapLoc(134, 287, 167, 571)
                .setSoundVolume(1)
                .setSoundPan(-1)
                .addConnection("cam1")
                .connectToOfficeLeft()
                .build();
        Camera cam4 = new Camera.Builder()
                .setName("cam4")
                .setCamBackground("night/tutorial/cam4.jpg")
                .setOnMapLoc(720, 296, 141, 586)
                .setSoundVolume(1)
                .setSoundPan(1)
                .addConnection("cam2")
                .connectToOfficeRight()
                .build();
        nightMap.addAll(cam1, cam2, cam3, cam4);
        long seed = new Random().nextLong();
        Night night = new Night("Tutorial", nightMap, "night/tutorial/paper.png",
                new Jumpscare("office/powerOutage.gif", 1), new Random(seed), 60, 0.45f,
                Resources.loadSound("night/tutorial/completed.wav", "tutorialCom.wav")) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }
        };
        night.addOnNightCompleted(() -> {
            saveFile.addCompletedNight(night.getNightName());
            try {
                SaveFileIO.saveToFile(SaveFileIO.SAVE_FILE, saveFile);
                MenuData menuData = getUpdatedMenuData(saveFile);
                mainMenu.updateBackground(menuData.background);
                mainMenu.updateMenuItems(menuData.mmItems);
            } catch (IOException e) {
                throw new RuntimeException("Progress could not be saved.", e);
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
        HashMap<Integer, Integer> aiNightBob = new HashMap<>(4);
        aiNightBob.put(0, 3);
        aiNightBob.put(2, 4);
        aiNightBob.put(4, 5);
        aiNightBob.put(5, 6);
        Animatronic bob = new Bob(3, aiNightBob, List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 5);

        HashMap<Integer, Integer> aiNightMaria = new HashMap<>(4);
        aiNightMaria.put(1, 3);
        aiNightMaria.put(3, 4);
        aiNightMaria.put(4, 5);
        aiNightMaria.put(5, 6);
        Animatronic maria = new Maria(3, aiNightMaria, List.of("corridor 1", "corridor 3", "staff lounge"), 5);

        HashMap<Integer, Integer> aiNightPaco = new HashMap<>(4);
        aiNightPaco.put(0, 4);
        aiNightPaco.put(3, 6);
        aiNightPaco.put(4, 7);
        aiNightPaco.put(5, 8);
        Animatronic paco = new Paco(5, aiNightPaco, List.of("kitchen", "dining area", "corridor 1", "corridor 3"),
                "kitchen", 1f, 12);

        CameraMap nightMap = new CameraMap(Resources.loadImageResource("night/n1/map.png"), "storage");
        nightMap.addAll(
                new Camera.Builder()
                        .setName("kitchen")
                        .setCamBackground("night/n1/kitchen.jpg")
                        .setOnMapLoc(187, 45, 140, 70)
                        .setSoundVolume(0.2)
                        .setSoundPan(-1)
                        .addAnimatronics(paco)
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("storage")
                        .setCamBackground("night/n1/storage.jpg")
                        .setOnMapLoc(542, 111, 140, 70)
                        .setSoundVolume(0.2)
                        .setSoundPan(1)
                        .addAnimatronics(bob)
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("dining area")
                        .setCamBackground("night/n1/dining area.jpg")
                        .setOnMapLoc(168, 182, 140, 70)
                        .setSoundVolume(0.4)
                        .setSoundPan(-0.1)
                        .addConnection("kitchen", "storage", "main stage", "corridor 1", "corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("main stage")
                        .setCamBackground("night/n1/main stage.jpg")
                        .setOnMapLoc(537, 399, 140, 70)
                        .setSoundVolume(0.4)
                        .setSoundPan(0.1)
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 1")
                        .setCamBackground("night/n1/corridor 1.jpg")
                        .setOnMapLoc(314, 469, 140, 70)
                        .setSoundVolume(0.6)
                        .setSoundPan(-0.5)
                        .addConnection("dining area", "corridor 3", "staff lounge")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 2")
                        .setCamBackground("night/n1/corridor 2.jpg")
                        .setOnMapLoc(456, 469, 140, 70)
                        .setSoundVolume(0.6)
                        .setSoundPan(0.5)
                        .addConnection("dining area", "corridor 4", "bathrooms")
                        .build(),
                new Camera.Builder()
                        .setName("staff lounge")
                        .setCamBackground("night/n1/staff lounge.jpg")
                        .setOnMapLoc(30, 821, 140, 70)
                        .setSoundVolume(0.6)
                        .setSoundPan(-1)
                        .addConnection("corridor 1")
                        .build(),
                new Camera.Builder()
                        .setName("offices")
                        .setCamBackground("night/n1/offices.jpg")
                        .setOnMapLoc(825, 840, 140, 70)
                        .setSoundVolume(0.6)
                        .setSoundPan(1)
                        .addConnection("corridor 4")  //Offices go to corridor 4, but not vice-versa
                        .addAnimatronics(maria)
                        .build(),
                new Camera.Builder()
                        .setName("bathrooms")
                        .setCamBackground("night/n1/bathrooms.jpg")
                        .setOnMapLoc(560, 734, 140, 51)
                        .setSoundVolume(1)
                        .setSoundPan(0)
                        .addConnection("corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 3")
                        .setCamBackground("night/n1/corridor 3.jpg")
                        .setOnMapLoc(225, 561, 140, 70)
                        .setSoundVolume(1)
                        .setSoundPan(-1)
                        .addConnection("corridor 1")
                        .connectToOfficeLeft()
                        .build(),
                new Camera.Builder()
                        .setName("corridor 4")
                        .setCamBackground("night/n1/corridor 4.jpg")
                        .setOnMapLoc(662, 568, 140, 70)
                        .setSoundVolume(1)
                        .setSoundPan(1)
                        .addConnection("corridor 2") //Offices go to corridor 4, but not vice-versa
                        .connectToOfficeRight()
                        .build()
        );

        long seed = new Random().nextLong();
        Night night = new Night("Night 1", nightMap, "night/n1/paper.png",
                new Jumpscare("office/powerOutage.gif", 1), new Random(seed), 90, 0.45f,
                Resources.loadSound("night/n1/completed.wav", "n1Com.wav")) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }
        };
        night.addOnNightCompleted(() -> {
            saveFile.addCompletedNight(night.getNightName());
            try {
                SaveFileIO.saveToFile(SaveFileIO.SAVE_FILE, saveFile);
                MenuData menuData = getUpdatedMenuData(saveFile);
                mainMenu.updateBackground(menuData.background);
                mainMenu.updateMenuItems(menuData.mmItems);
            } catch (IOException e) {
                throw new RuntimeException("Could not save victory to save file.", e);
            }
            cards.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            System.out.println(
                    "Congratulations! Progressively more challenging experiences do not seem to put a hold on you.\nFor now.");
        });
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();

        return night;
    }
}

