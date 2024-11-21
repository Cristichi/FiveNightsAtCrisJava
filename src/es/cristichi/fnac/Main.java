package es.cristichi.fnac;

import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.gui.Menu;
import es.cristichi.fnac.gui.Night;
import es.cristichi.fnac.io.FNACResources;
import es.cristichi.fnac.io.FNACSave;
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

	public static void main(String[] args) throws IOException {
		JFrame window = new JFrame(GAME_TITLE);
		window.setSize(800, 600);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());

		CardLayout cards = new CardLayout();
		cardPanel = new JPanel(cards);
		window.add(cardPanel);

		nightPanel = new JPanel(new BorderLayout());
		cardPanel.add(nightPanel, "night");

		FNACSave.SaveFile saveFile = FNACSave.loadFromFile(FNACSave.SAVE_FILE);

		ArrayList<String> mmItems = new ArrayList<>(2);
        List<String> completed = saveFile.completedNights();
        int numCompleted = completed.size();
		String background;
		if (numCompleted == 1) {
			background = "menu/background1.jpg";
            mmItems.add("Night 1 is unplayable WIP");
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
		Menu mainMenu = new Menu(background, "menu/loading.jpg", mmItems) {
			@Override
			protected void onMenuItemClick(String item) throws IOException {
                switch (item) {
                    case "Tutorial Night", "Repeat Tutorial" -> {
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
                                .build();
                        Camera cam2 = new Camera.CameraBuilder()
                                .setName("cam2")
                                .setCamBackground("night/tutorial/cam2.jpg")
                                .setLoc(491, 117, 379, 177)
                                .addAnimatronics(new Maria(5, aiNightMaria, List.of("cam3")))
                                .build();
                        Camera cam3 = new Camera.CameraBuilder()
                                .setName("cam3")
                                .setCamBackground("night/tutorial/cam3.jpg")
                                .setLoc(134, 287, 167, 571)
                                .connectToOfficeLeft()
                                .build();
                        Camera cam4 = new Camera.CameraBuilder()
                                .setName("cam4")
                                .setCamBackground("night/tutorial/cam4.jpg")
                                .setLoc(720, 296, 141, 586)
                                .connectToOfficeRight()
                                .build();
                        cam1.addMutualConnection(cam2);
                        cam1.addMutualConnection(cam3);
                        cam2.addMutualConnection(cam4);
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
                    case "Night 1", "Repeat Night 1" -> {
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

                        HashMap<Integer, Integer> aiNightPaco = new HashMap<>(4);
                        aiNightPaco.put(1, 5);
                        aiNightPaco.put(3, 6);
                        aiNightPaco.put(4, 7);
                        aiNightPaco.put(5, 8);
                        Animatronic paco = new Paco(3, aiNightPaco, List.of());

                        CameraMap nightMap = new CameraMap(FNACResources.loadImageResource("night/n1/map.png"), "cam3");
                        Camera kitchen = new Camera.CameraBuilder()
                                .setName("kitchen")
                                .setCamBackground("night/n1/kitchen.jpg")
                                .setLoc(176, 33, 283, 149)
                                .addAnimatronics(paco)
                                .build();
                        Camera storage = new Camera.CameraBuilder()
                                .setName("storage")
                                .setCamBackground("night/n1/storage.jpg")
                                .setLoc(176, 33, 283, 149)
                                .addAnimatronics(bob)
                                .build();
                        Camera mainStages = new Camera.CameraBuilder()
                                .setName("main stages")
                                .setCamBackground("night/n1/mainStages.jpg")
                                .setLoc(176, 33, 283, 149)
                                .addAnimatronics(maria)
                                .build();

                        nightMap.addAll(kitchen, storage, mainStages);
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
                    case "Exit" -> {
                        window.dispose();
                        System.exit(0);
                    }
                    default -> throw new MenuItemNotFound("Menu item \"" + item + "\" not found in this menu.");
                }
			}
		};
		cardPanel.add(mainMenu, "menu");
        cards.show(cardPanel, "menu");

		window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		window.setVisible(true);
	}
}
