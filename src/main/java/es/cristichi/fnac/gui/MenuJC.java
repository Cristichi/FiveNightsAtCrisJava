package es.cristichi.fnac.gui;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import kuusisto.tinysound.Music;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * JComponent that represents the main menu of the game. It displays the available next Night, as well as buttons
 * for Settings and exitting the game.
 */
public abstract class MenuJC extends JComponent {
	private final List<Item> menuItems;
	private Image backgroundImage;
	private final Image defaultLoadingImg;
	private Image currentLoadingImg;
	private boolean loading;
	private final Font btnFont;

	private int errorTicks = 0;
	private String[] error = null;

	private final Music backgroundMusic;
	private int musicCreditsTicks;
	private static final int MUSIC_CREDITS_TICKS = 160;
	private final String[] musicCreditsMsg;

	/**
	 * Creates a new {@link MenuJC} with the given data.
	 * @param info Information of the menu items and background at the moment.
	 * @param defaultLoadingImg Path to default loading image in the resources. It is used for {@link Item}s
	 *                         that don't specify one.
	 * @throws ResourceException If there are any errors loading the images from the resources.
	 */
    public MenuJC(Info info, String defaultLoadingImg) throws ResourceException {
		super();
		this.menuItems = info.menuItems();
		backgroundImage = Resources.loadImageResource(info.background());
		this.defaultLoadingImg = Resources.loadImageResource(defaultLoadingImg);
		loading = false;
		btnFont = new Font("Eraser Dust", Font.PLAIN, 100);

		backgroundMusic = Resources.loadMusic("menu/main.wav", "menuBack.wav");

		initializeMenuItems();

		Timer menuTicks = new Timer();
		int fps = 5;
		menuTicks.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		}, 100, 1000 / fps);

		backgroundMusic.play(true);
		musicCreditsTicks = 160;
		musicCreditsMsg = new String[]{"FNAC Main Theme", "original by Cristichi"};
    }

	/**
	 * @param backgroundImg New background image in the Resources.
	 */
	public void updateBackground(BufferedImage backgroundImg) {
		backgroundImage = backgroundImg;
	}

	/**
	 * @param newMenuItems New menu items. It can be used on the fly, and it will reload the new items.
	 */
	public void updateMenuItems(List<Item> newMenuItems) {
		SwingUtilities.invokeLater(() -> {
			this.menuItems.clear();
			this.menuItems.addAll(newMenuItems);
			initializeMenuItems();
			revalidate();
			repaint();
		});
	}

	/**
	 * Starts the music. If it is already playing, it does nothing.
	 */
	public void startMusic(){
		if (!backgroundMusic.playing()){
			musicCreditsTicks = MUSIC_CREDITS_TICKS;
			backgroundMusic.play(true);
		}
	}

	/**
	 * Stops the music. If it is already stopped, it does nothing.
	 */
	public void stopMusic() {
		if (backgroundMusic.playing()){
			musicCreditsTicks = 0;
			backgroundMusic.stop();
		}
	}

	private void initializeMenuItems() {
		removeAll();
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		for (Item item : menuItems) {
			JButton button = createMenuButton(item);
			horizontalGroup.addComponent(button, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			verticalGroup.addComponent(button);
		}

		verticalGroup.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);
	}

	private JButton createMenuButton(Item item) {
		JButton button = new JButton("<html>" + item.display() + "</html>");
		float fontScale = (float) (btnFont.getSize() * Math.min(getWidth(), getHeight())) / 1000;
		button.setFont(btnFont.deriveFont(fontScale));
		button.setForeground(Color.WHITE);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setAlignmentX(Component.LEFT_ALIGNMENT);
		button.addActionListener(new MenuActionListener(item));
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setText("<html><u>" + item.hoverDisplay() + "</u></html>");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setText("<html>" + item.display() + "</html>");
			}
		});
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				float fontScale = (float) (btnFont.getSize() * Math.min(getWidth(), getHeight())) / 1000;
				button.setFont(btnFont.deriveFont(fontScale));
			}
		});
		return button;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int fontScale = Math.min(getWidth(), getHeight())/1000;

		int fontSize = 40*fontScale;

		// Draw the background image, scaled to fit the component's dimensions
		g.drawImage((loading
					? (currentLoadingImg==null ? defaultLoadingImg : currentLoadingImg)
					: backgroundImage),
				0, 0, getWidth(), getHeight(), this);

		if (errorTicks-- > 0) {
			g.setFont(new Font("Arial", Font.BOLD, fontSize));
			g.setColor(Color.RED);
			int y = fontSize;
			for (String line : error) {
				g.drawString(line, 10, y);
				y+=fontSize;
			}
		}

		if (musicCreditsTicks-- > 0) {
			g.setFont(new Font("Arial", Font.BOLD, fontSize));
			g.setColor(Color.WHITE);
			int y = fontSize* musicCreditsMsg.length;
			int x = (int)(getWidth()*0.99);
			for (String line : musicCreditsMsg) {
				FontMetrics fm = g.getFontMetrics();
				LineMetrics lm = fm.getLineMetrics(line, g);
				g.drawString(line, x-fm.stringWidth(line), getHeight()-y);
				y-=fontSize;
			}
		}
	}

	/**
	 * This is performed after an item is clicked. It also loads a loading screen in case you need time to load resources.
	 * @param item String identifying the item clicked, or null if no Night should start.
	 * @throws IOException To catch errors, so the menu shows them on screen instead of just crashing.
	 */
	protected abstract void onMenuItemClick(Item item) throws Exception;

	private class MenuActionListener implements ActionListener {
		private final Item menuItem;

		public MenuActionListener(Item menuItem) {
			this.menuItem = menuItem;
		}

		public void actionPerformed(ActionEvent e) {
			loading = true;
			if (menuItem.loadingScreenPath() != null) {
				try {
					currentLoadingImg = Resources.loadImageResource(menuItem.loadingScreenPath());
				} catch (ResourceException ex) {
					ex.printStackTrace();
				}
			}
            for (Component component : MenuJC.this.getComponents()) {
				component.setVisible(false);
			}
			new Thread(() -> {
				try {
					error = null;
					errorTicks = 0;

					onMenuItemClick(menuItem);
				} catch (Exception e1) {
					e1.printStackTrace();
					error = new String[] {"Error trying to load "+ menuItem, e1.getMessage(), "Check console for full stack trace."};
					errorTicks = 60;
				}
				for (Component component : MenuJC.this.getComponents()) {
					component.setVisible(true);
				}
				loading = false;
			}, menuItem.id()).start();
		}
	}
	
	/**
	 *
	 * @param id Id of the element. Elements with the same ID will perform the same action, whose code is written in
	 *           {@link NightsJF}.
	 * @param display Text shown for the button when not hovered by the player's mouse.
	 * @param hoverDisplay Text shown for the button when hovered by the player's mouse.
	 * @param loadingScreenPath Path to the resource where the Loading Screen is, or {@code null} to use the
	 *                          default one.
	 */
	public record Item(String id, String display, String hoverDisplay, @Nullable String loadingScreenPath){
	}
	
	/**
	 * Information this menu needs in order to create or update the buttons and background.
	 * @param menuItems List of {@link Item}s. It must not be null or empty.
	 * @param background Path to background image in the resources.
	 */
	public record Info(List<Item> menuItems, String background) {
	}
}