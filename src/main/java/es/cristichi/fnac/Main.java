package es.cristichi.fnac;

import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.gui.Menu;
import es.cristichi.fnac.gui.Night;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.SaveFileIO;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import es.cristichi.fnac.obj.anim.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Main {
    public static final String GAME_TITLE = "Five Nights at Cris's";
    private static JPanel cardPanel;
    private static JPanel nightPanel;

    public static String getTitleForWindow(String window) {
        return GAME_TITLE.concat(" - ").concat(window);
    }

    public static void main(String[] args) {
        SaveFileIO.SaveFile saveFile;
        try {
            saveFile = SaveFileIO.loadFromFile(SaveFileIO.SAVE_FILE);
        } catch (IOException e) {
            System.err.println("Failed to load save file: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Initialize the GUI on the EDT
        SwingUtilities.invokeLater(() -> {
            try {
                initializeGUI(saveFile);
            } catch (IOException e) {
                throw new RuntimeException("Error when trying to prepare the GUI and Nights.", e);
            } catch (AWTException awtException){
                throw new RuntimeException("We could not restrict the mouse due to an error.", awtException);
            }
        });
    }

    private static void initializeGUI(SaveFileIO.SaveFile saveFile) throws IOException, AWTException {
        JFrame window = new JFrame(GAME_TITLE);
        window.setSize(800, 600);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setIconImage(Resources.loadImageResource("icon.jpg"));
        window.setLayout(new BorderLayout());

        CardLayout cards = new CardLayout();
        cardPanel = new JPanel(cards);
        window.add(cardPanel);

        nightPanel = new JPanel(new BorderLayout());
        cardPanel.add(nightPanel, "night");

        Timer mouseRestrictTimer = getMouseRestrictTimer(window);
        mouseRestrictTimer.start();

        ArrayList<String> mmItems = new ArrayList<>(2);
        String background;
        List<String> completed = saveFile.completedNights();
        int numCompleted = completed.size();

        if (numCompleted == 1) {
            background = "menu/background1.jpg";
            mmItems.add("Night 1");
            mmItems.add("Repeat Tutorial");
        } else if (numCompleted == 2) {
            background = "menu/background2.jpg";
            mmItems.add("The other nights are not available yet! :3");
            mmItems.add("Repeat Tutorial");
            mmItems.add("Repeat Night 1");
        } else {
            background = "menu/background.jpg";
            mmItems.add("Tutorial Night");
        }
        mmItems.add("Exit");

        Menu mainMenu = createMenu(saveFile, cards, background, mmItems, window);
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

    private static Menu createMenu(SaveFileIO.SaveFile saveFile, CardLayout cards, String background, ArrayList<String> mmItems, JFrame window) throws IOException {
        return new Menu(background, "menu/loading.jpg", mmItems) {
            @Override
            protected void onMenuItemClick(String item) throws IOException {
                switch (item) {
                    case "Tutorial Night", "Repeat Tutorial" -> startTutorialNight(saveFile, cards, window);
                    case "Night 1", "Repeat Night 1" -> startNight1(saveFile, cards, window);
                    case "Exit" -> {
                        window.dispose();
                        System.exit(0);
                    }
                    default -> throw new MenuItemNotFound("Menu item \"" + item + "\" not found in this menu.");
                }
            }
        };
    }

    private static void startTutorialNight(SaveFileIO.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
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
                .setLoc(113, 111, 378, 177)
                .addAnimatronics(new Bob(5, aiNightBob, List.of("cam4"), 8))
                .addConnection("cam2", "cam3")
                .build();
        Camera cam2 = new Camera.Builder()
                .setName("cam2")
                .setCamBackground("night/tutorial/cam2.jpg")
                .setLoc(491, 117, 379, 177)
                .addAnimatronics(new Maria(5, aiNightMaria, List.of("cam3"), 8))
                .addConnection("cam1", "cam4")
                .build();
        Camera cam3 = new Camera.Builder()
                .setName("cam3")
                .setCamBackground("night/tutorial/cam3.jpg")
                .setLoc(134, 287, 167, 571)
                .addConnection("cam1")
                .connectToOfficeLeft()
                .build();
        Camera cam4 = new Camera.Builder()
                .setName("cam4")
                .setCamBackground("night/tutorial/cam4.jpg")
                .setLoc(720, 296, 141, 586)
                .addConnection("cam2")
                .connectToOfficeRight()
                .build();
        nightMap.addAll(cam1, cam2, cam3, cam4);
        long seed = new Random().nextLong();
        Night night = new Night("Tutorial", nightMap, "night/tutorial/paper.png", new Jumpscare("office/powerOutage.gif", 1), new Random(seed), 60, 0.45f) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }

            @Override
            protected void onNightPassed() throws IOException {
                saveFile.addCompletedNight(getNightName());
                SaveFileIO.saveToFile(SaveFileIO.SAVE_FILE, saveFile);
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("You just passed the tutorial! Congratulations, but it was only the beginning.");
            }
        };
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's Tutorial is using the seed \"%d\". Have fun!%n", seed);
        night.startNight();
    }

    private static void startNight1(SaveFileIO.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
        HashMap<Integer, Integer> aiNightBob = new HashMap<>(4);
        aiNightBob.put(0, 5);
        aiNightBob.put(2, 6);
        aiNightBob.put(4, 7);
        aiNightBob.put(5, 8);
        Animatronic bob = new Bob(3, aiNightBob, List.of("corridor 2", "corridor 4", "bathrooms", "offices"), 5);

        HashMap<Integer, Integer> aiNightMaria = new HashMap<>(4);
        aiNightMaria.put(1, 5);
        aiNightMaria.put(3, 6);
        aiNightMaria.put(4, 7);
        aiNightMaria.put(5, 8);
        Animatronic maria = new Maria(3, aiNightMaria, List.of("corridor 1", "corridor 3", "staff lounge"), 5);

        HashMap<Integer, Integer> aiNightPaco = new HashMap<>(4);
        aiNightPaco.put(0, 4);
        aiNightPaco.put(3, 6);
        aiNightPaco.put(4, 7);
        aiNightPaco.put(5, 8);
        Animatronic paco = new Paco(5, aiNightPaco, List.of("kitchen", "dining area", "corridor 1", "corridor 3"),
                "kitchen", 12);

        CameraMap nightMap = new CameraMap(Resources.loadImageResource("night/n1/map.png"), "storage");
        nightMap.addAll(
                new Camera.Builder()
                        .setName("kitchen")
                        .setCamBackground("night/n1/kitchen.jpg")
                        .setLoc(187, 45, 140, 70)
                        .addAnimatronics(paco)
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("storage")
                        .setCamBackground("night/n1/storage.jpg")
                        .setLoc(542, 111, 140, 70)
                        .addAnimatronics(bob)
                        .addConnection("dining area")
                        .build(),
                new Camera.Builder()
                        .setName("dining area")
                        .setCamBackground("night/n1/dining area.jpg")
                        .setLoc(168, 182, 140, 70)
                        .addConnection("kitchen", "storage", "main stage", "corridor 1", "corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("main stage")
                        .setCamBackground("night/n1/main stage.jpg")
                        .setLoc(537, 399, 140, 70)
                        .addConnection("kitchen", "main stage", "corridor 1", "corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 1")
                        .setCamBackground("night/n1/corridor 1.jpg")
                        .setLoc(314, 469, 140, 70)
                        .addConnection("dining area", "corridor 3", "staff lounge")
                        .build(),
                new Camera.Builder()
                        .setName("staff lounge")
                        .setCamBackground("night/n1/staff lounge.jpg")
                        .setLoc(30, 821, 140, 70)
                        .addConnection("corridor 1")
                        .build(),
                new Camera.Builder()
                        .setName("corridor 2")
                        .setCamBackground("night/n1/corridor 2.jpg")
                        .setLoc(456, 469, 140, 70)
                        .addConnection("dining area", "corridor 4", "bathrooms")
                        .build(),
                new Camera.Builder()
                        .setName("bathrooms")
                        .setCamBackground("night/n1/bathrooms.jpg")
                        .setLoc(560, 734, 140, 51)
                        .addConnection("corridor 2")
                        .build(),
                new Camera.Builder()
                        .setName("offices")
                        .setCamBackground("night/n1/offices.jpg")
                        .setLoc(825, 840, 140, 70)
                        .addConnection("corridor 4")
                        .addAnimatronics(maria)
                        .build(),
                new Camera.Builder()
                        .setName("corridor 3")
                        .setCamBackground("night/n1/corridor 3.jpg")
                        .setLoc(225, 561, 140, 70)
                        .addConnection("corridor 1")
                        .connectToOfficeLeft()
                        .build(),
                new Camera.Builder()
                        .setName("corridor 4")
                        .setCamBackground("night/n1/corridor 4.jpg")
                        .setLoc(662, 568, 140, 70)
                        .addConnection("corridor 3") //Offices go to corridor 4, but not vice-versa
                        .connectToOfficeRight()
                        .build()
        );

        long seed = new Random().nextLong();
        Night night = new Night("Night 1", nightMap, "night/n1/paper.png",
                new Jumpscare("office/powerOutage.gif", 1), new Random(seed), 90, 0.45f) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }

            @Override
            protected void onNightPassed() throws IOException {
                saveFile.addCompletedNight(getNightName());
                SaveFileIO.saveToFile(SaveFileIO.SAVE_FILE, saveFile);
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println(
                        "Congratulations! Progressively more challenging experiences do not seem to put a hold on you.\nFor now.");
            }
        };
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();
    }
}

