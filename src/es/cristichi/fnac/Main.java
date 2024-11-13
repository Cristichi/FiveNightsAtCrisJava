package es.cristichi.fnac;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import es.cristichi.fnac.exception.MenuItemNotFound;
import es.cristichi.fnac.gui.Menu;
import es.cristichi.fnac.gui.Night;

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
		Menu mainMenu = new Menu("./assets/imgs/mainmenu_background.jpg", mmItems) {
			@Override
			protected void onMenuItemClick(String item) throws IOException {
				switch (item) {
				case "New Game":
					Night night1 = new Night("Night 1") {
						@Override
						protected void onJumpscare() {
							nightPanel.removeAll();
							System.out.println("Moriste por loco");
							cards.show(cardPanel, "menu");
						}

						@Override
						protected void onNightComplete() {
							nightPanel.removeAll();
							System.out.println("Wiii");
							cards.show(cardPanel, "menu");
						}
					};
					nightPanel.add(night1);
					cards.show(cardPanel, "night");
					window.setTitle(getTitleForWindow("Night 1"));
					break;

				case "Exit":
					window.dispose();
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
