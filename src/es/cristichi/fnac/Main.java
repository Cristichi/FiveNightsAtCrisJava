package es.cristichi.fnac;

import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.gui.Menu;
import es.cristichi.fnac.gui.Night;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import es.cristichi.fnac.obj.anim.Bob;
import es.cristichi.fnac.obj.anim.Jumpscare;
import es.cristichi.fnac.obj.anim.Maria;
import es.cristichi.fnac.util.AssetsIO;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Main {
	public static final String GAME_TITLE = "Five Nights at Crist's";
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

		List<String> mmItems = List.of("New Game", "Exit");
		Menu mainMenu = new Menu("imgs/menu/background.jpg", "imgs/menu/loading.jpg", mmItems) {
			@Override
			protected void onMenuItemClick(String item) throws IOException {
				switch (item) {
				case "New Game":
					HashMap<Integer, Integer> aiNightBob = new HashMap<>(4);
					aiNightBob.put(0, 5);
					aiNightBob.put(3, 6);
					aiNightBob.put(4, 7);
					aiNightBob.put(5, 8);

					HashMap<Integer, Integer> aiNightMaria = new HashMap<>(4);
					aiNightMaria.put(2, 7);
					aiNightMaria.put(4, 8);
					aiNightMaria.put(5, 9);

					CameraMap night1Map = new CameraMap("test1", AssetsIO.loadImageResource("imgs/night/cams/map.png"));
					Camera cam1 = new Camera.CameraBuilder()
							.setName("cam1")
							.setCamBackground("imgs/night/cams/cam1.jpg")
							.setLoc(113, 111, 378, 177)
							.addAnimatronics(new Bob(5, aiNightBob))
							.build();
					Camera cam2 = new Camera.CameraBuilder()
							.setName("cam2")
							.setCamBackground("imgs/night/cams/cam2.jpg")
							.setLoc(491, 117, 379, 177)
							.addAnimatronics(new Maria(5, aiNightMaria))
							.build();
					Camera cam3 = new Camera.CameraBuilder()
							.setName("cam3")
							.setCamBackground("imgs/night/cams/cam3.jpg")
							.setLoc(134, 287, 167, 571)
							.connectToOfficeLeft()
							.build();
					Camera cam4 = new Camera.CameraBuilder()
							.setName("cam4")
							.setCamBackground("imgs/night/cams/cam4.jpg")
							.setLoc(720, 296, 141, 586)
							.connectToOfficeRight()
							.build();
					cam1.addMutualConnection(cam2);
					cam1.addMutualConnection(cam3);
					cam2.addMutualConnection(cam4);
					night1Map.add(cam1);
					night1Map.add(cam2);
					night1Map.add(cam3);
					night1Map.add(cam4);
					long seed = new Random().nextLong();
					Night night1 = new Night("Night 1", night1Map, new Jumpscare("imgs/night/powerOutage.gif", 1), new Random(seed), 0.45f) {
						@Override
						protected void onJumpscare() {
							nightPanel.removeAll();
							System.out.println("Moriste por loco");
							cards.show(cardPanel, "menu");
						}

						@Override
						protected void onNightPassed() {
							nightPanel.removeAll();
							System.out.println("Wiii");
							cards.show(cardPanel, "menu");
						}
					};
					nightPanel.add(night1);
					cards.show(cardPanel, "night");
					window.setTitle(getTitleForWindow("Night 1"));
					System.out.printf("Today's Night 1 is using the seed %d.%n", seed);
					night1.startNight();
					break;

				case "Exit":
					window.dispose();
					System.exit(0);
					break;

				default:
					throw new MenuItemNotFound("Menu item \"" + item + "\" not found in this menu.");
				}
			}
		};
		cardPanel.add(mainMenu, "menu");
		cards.show(cardPanel, "menu");

		window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		window.setVisible(true);
	}
}
