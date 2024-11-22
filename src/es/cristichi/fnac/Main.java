package es.cristichi.fnac;

import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.gui.Menu;
import es.cristichi.fnac.gui.Night;
import es.cristichi.fnac.io.FNACResources;
import es.cristichi.fnac.io.FNACSave;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import es.cristichi.fnac.obj.anim.Animatronic;
import es.cristichi.fnac.obj.anim.Bob;
import es.cristichi.fnac.obj.anim.Jumpscare;
import es.cristichi.fnac.obj.anim.Maria;

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
        FNACSave.SaveFile saveFile;
        try {
            saveFile = FNACSave.loadFromFile(FNACSave.SAVE_FILE);
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

    private static void initializeGUI(FNACSave.SaveFile saveFile) throws IOException, AWTException {
        JFrame window = new JFrame(GAME_TITLE);
        window.setSize(800, 600);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setIconImage(FNACResources.loadImageResource("icon.jpg"));
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

    private static Menu createMenu(FNACSave.SaveFile saveFile, CardLayout cards, String background, ArrayList<String> mmItems, JFrame window) throws IOException {
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

    private static void startTutorialNight(FNACSave.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
        HashMap<Integer, Integer> aiNightBob = new HashMap<>(4);
        aiNightBob.put(0, 0);
        aiNightBob.put(1, 1);
        aiNightBob.put(4, 2);

        HashMap<Integer, Integer> aiNightMaria = new HashMap<>(4);
        aiNightMaria.put(0, 0);
        aiNightMaria.put(3, 1);
        aiNightMaria.put(4, 2);
        aiNightMaria.put(5, 3);

        CameraMap nightMap = new CameraMap(FNACResources.loadImageResource("night/tutorial/map.png"), "cam3");
        Camera cam1 = new Camera.CameraBuilder()
                .setName("cam1")
                .setCamBackground("night/tutorial/cam1.jpg")
                .setLoc(113, 111, 378, 177)
                .addAnimatronics(new Bob(5, aiNightBob, List.of("cam4")))
                .addConnection("cam2", "cam3")
                .build();
        Camera cam2 = new Camera.CameraBuilder()
                .setName("cam2")
                .setCamBackground("night/tutorial/cam2.jpg")
                .setLoc(491, 117, 379, 177)
                .addAnimatronics(new Maria(5, aiNightMaria, List.of("cam3")))
                .addConnection("cam1", "cam4")
                .build();
        Camera cam3 = new Camera.CameraBuilder()
                .setName("cam3")
                .setCamBackground("night/tutorial/cam3.jpg")
                .setLoc(134, 287, 167, 571)
                .addConnection("cam1")
                .connectToOfficeLeft()
                .build();
        Camera cam4 = new Camera.CameraBuilder()
                .setName("cam4")
                .setCamBackground("night/tutorial/cam4.jpg")
                .setLoc(720, 296, 141, 586)
                .addConnection("cam2")
                .connectToOfficeRight()
                .build();
        nightMap.addAll(cam1, cam2, cam3, cam4);
        long seed = new Random().nextLong();
        Night night = new Night("Tutorial", nightMap, new Jumpscare("office/powerOutage.gif", 1), new Random(seed), 0.45f) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }

            @Override
            protected void onNightPassed() throws IOException {
                saveFile.addCompletedNight(getNightName());
                FNACSave.saveToFile(FNACSave.SAVE_FILE, saveFile);
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

    private static void startNight1(FNACSave.SaveFile saveFile, CardLayout cards, JFrame window) throws IOException {
        HashMap<Integer, Integer> aiNightBob = new HashMap<>(4);
        aiNightBob.put(0, 5);
        aiNightBob.put(2, 6);
        aiNightBob.put(4, 7);
        aiNightBob.put(5, 8);
        Animatronic bob = new Bob(5, aiNightBob, List.of());

        HashMap<Integer, Integer> aiNightMaria = new HashMap<>(4);
        aiNightMaria.put(1, 5);
        aiNightMaria.put(3, 6);
        aiNightMaria.put(4, 7);
        aiNightMaria.put(5, 8);
        Animatronic maria = new Maria(5, aiNightMaria, List.of());

// TODO: Paco will be work on after I complete this map. I also didn't decode on how he will move or behave to be honest.
//        HashMap<Integer, Integer> aiNightPaco = new HashMap<>(4);
//        aiNightPaco.put(1, 5);
//        aiNightPaco.put(3, 6);
//        aiNightPaco.put(4, 7);
//        aiNightPaco.put(5, 8);
//        Animatronic paco = new Paco(3, aiNightPaco, List.of());

        CameraMap nightMap = new CameraMap(FNACResources.loadImageResource("night/n1/map.png"), "storage");
        Camera kitchen = new Camera.CameraBuilder()
                .setName("kitchen")
                .setCamBackground("night/n1/kitchen.jpg")
                .setLoc(187, 45, 140, 70)
                //.addAnimatronics(paco)
                .addConnection("storage")
                .build();
        Camera storage = new Camera.CameraBuilder()
                .setName("storage")
                .setCamBackground("night/n1/storage.jpg")
                .setLoc(542, 111, 140, 70)
                .addAnimatronics(bob, maria)
                .addConnection("kitchen")
                .build();

        nightMap.addAll(kitchen, storage);
        long seed = new Random().nextLong();
        Night night = new Night("Night 1", nightMap, new Jumpscare("office/powerOutage.gif", 1), new Random(seed), 0.45f) {
            @Override
            protected void onJumpscare() {
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Player died.");
            }

            @Override
            protected void onNightPassed() throws IOException {
                saveFile.addCompletedNight(getNightName());
                FNACSave.saveToFile(FNACSave.SAVE_FILE, saveFile);
                nightPanel.removeAll();
                cards.show(cardPanel, "menu");
                System.out.println("Congratulations! Progressively more challenging experiences do not seem to put a hold on you.\nFor now.");
            }
        };
        nightPanel.add(night);
        window.setTitle(getTitleForWindow(night.getNightName()));
        cards.show(cardPanel, "night");
        System.out.printf("Today's %s is using the seed \"%d\". Good luck.%n", night.getNightName(), seed);
        night.startNight();
    }
}

