package es.cristichi.fnac.gui;

import kuusisto.tinysound.Music;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * JComponent that represents the main menu of the game. It displays the available next Night, as well as buttons
 * for Settings and exitting the game.
 */
public class MenuJC extends JComponent {
	private static final Logger LOGGER = LoggerFactory.getLogger(MenuJC.class);
	
	/**
	 * List of Items the menu has, which determines the buttons to show.
	 */
	protected final List<Item> menuItems;
	/**
	 * Background Image.
	 */
	protected Image backgroundImage;
	/**
	 * Default loading image, so that not all menu items need a custom background.
	 */
	protected final Image defaultLoadingImg;
	/**
	 * Current loading image to have on display. {@code null} when no loading image should be visible at this time.
	 */
	protected Image currentLoadingImg;
	/**
	 * Whether the game is loading or not.
	 */
	protected boolean loading;
	/**
	 * Font used for the buttons.
	 */
	protected final Font btnFont;

	/**
	 * Background Music, with credits to the artists.
	 */
	protected final List<WeightedCreditedMusic> musicList;
	/**
	 * Index of current music playing.
	 */
	protected int currentMusicIndex = 0;
	/**
	 * Amount of ticks left that the music credits must be shown for. {@code 0} for no credits.
	 */
	protected int musicCreditsTicks = 160;
	/**
	 * Usual number of ticks the Music's credits must be shown in total.
	 */
	protected static final int MUSIC_CREDITS_TICKS = 160;
	
	/**
	 * Creates a new {@link MenuJC} with the given data.
	 * @param info Information of the menu items and background at the moment.
	 * @param defaultLoadingImg Default loading image in the resources. It is used for {@link ItemInfo}s
	 *                         that don't specify one.
	 * @param backgroundMusic Background music list.
	 */
    public MenuJC(Info info, BufferedImage defaultLoadingImg, List<WeightedCreditedMusic> backgroundMusic) {
		super();
		this.menuItems = info.menuItems;
		backgroundImage = info.background;
		this.defaultLoadingImg = defaultLoadingImg;
		loading = false;
		btnFont = new Font("Eraser Dust", Font.PLAIN, 100);

		initializeMenuItems();

		Timer menuTicks = new Timer();
		int fps = 5;
		menuTicks.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		}, 100, 1000 / fps);

		this.musicList = backgroundMusic;

		playRandomMusic();
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
	 * Plays the current background music.
     * @throws IllegalStateException If there is no music available
	 */
	private void playRandomMusic() {
		musicCreditsTicks = MUSIC_CREDITS_TICKS;
        if (musicList.isEmpty()){
            throw new IllegalStateException("There is no music.");
        }

		int totalWeight = 0;
		for (WeightedCreditedMusic wcm : musicList){
			totalWeight+=wcm.weight;
		}
		int r = new Random().nextInt(totalWeight);

        for (int i = 0; i < musicList.size(); i++) {
            WeightedCreditedMusic item = musicList.get(i);
            r -= item.weight();
            if (r < 0) {
				currentMusicIndex = i;
				break;
            }
        }

        // Advance to next track in order
        new Thread(() -> {
            WeightedCreditedMusic current = musicList.get(currentMusicIndex);
            current.music.rewind();
            current.music.play(false);
            current.music.addOnEndListener(this::playRandomMusic);
        }).start();
	}

	/**
	 * Starts the music. If it is already playing, it does nothing.
	 */
	public void startMusic() {
		if (!musicList.get(currentMusicIndex).music.playing()) {
			playRandomMusic();
		}
	}

	/**
	 * Stops the music. If it is already stopped, it does nothing.
	 */
	public void stopMusic() {
		musicList.get(currentMusicIndex).music.stop();
		musicCreditsTicks = 0;
	}
	
	/**
	 * Removes all components, then adds all the necessary buttons to the menu, one for each {@link MenuJC.Item} on
	 * {@link MenuJC#menuItems}.
	 */
	protected void initializeMenuItems() {
		removeAll();
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		for (Item itemInfo : menuItems) {
			JButton button = createMenuButton(itemInfo);
			horizontalGroup.addComponent(button, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			verticalGroup.addComponent(button);
		}

		verticalGroup.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);
	}
	
	/**
	 * Creates a {@link JButton} with the desired style and a hover effect that changes the text as indicated by the
	 * given {@link Item}. Also, an action listener is added that will execute the {@link Item#runnable()} method.
	 * @param item Menu item that will be represented by this button.
	 * @return The newly created JButton.
	 */
	protected JButton createMenuButton(Item item) {
		JButton button = new JButton("<html>" + item.info.display + "</html>");
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
				button.setText("<html><u>" + item.info.hoverDisplay + "</u></html>");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setText("<html>" + item.info.display + "</html>");
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
        int fontSize = 40 * Math.min(getWidth(), getHeight()) / 935;

		// Draw the background image, scaled to fit the component's dimensions
		g.drawImage((loading
					? (currentLoadingImg==null ? defaultLoadingImg : currentLoadingImg)
					: backgroundImage),
				0, 0, getWidth(), getHeight(), this);

		if (musicCreditsTicks-- > 0) {
			g.setFont(new Font("Eraser Dust", Font.BOLD, fontSize));
			g.setColor(Color.WHITE);
			String[] credits = musicList.get(currentMusicIndex).credits;
			int y = fontSize * credits.length;
			int x = (int)(getWidth()*0.99);
			for (String line : credits) {
				FontMetrics fm = g.getFontMetrics();
				g.drawString(line, x-fm.stringWidth(line), getHeight()-y);
				y -= fontSize;
			}
		}
	}
	
	/**
	 * Action listener for the buttons that take an instance of {@link Item} as parameter for the action.
	 */
	protected class MenuActionListener implements ActionListener {
		/** Menu item that waits for this Action to happen. */
		protected final Item menuItem;
		
		/**
		 * Creates a new Menu Action Listener with the given menu item.
		 * @param menuItem Menu item.
		 */
		protected MenuActionListener(Item menuItem) {
			this.menuItem = menuItem;
		}
		
		/**
		 * Prepares a loading screen on this component, then runs the {@link Item#runnable()} method of this menu item.
		 * @param e the event to be processed
		 */
		public void actionPerformed(ActionEvent e) {
			loading = true;
			if (menuItem.info.loadingScreen != null) {
				currentLoadingImg = menuItem.info.loadingScreen;
			}
            for (Component component : MenuJC.this.getComponents()) {
				component.setVisible(false);
			}
			new Thread(() -> {
				try {
					menuItem.runnable.run();
				} catch (Exception e1) {
					new ExceptionDialog(e1, false, true, LOGGER);
				} finally {
					for (Component component : MenuJC.this.getComponents()) {
						component.setVisible(true);
					}
					currentLoadingImg = null;
					loading = false;
				}
			}, menuItem.info.id).start();
		}
	}
	
	/**
	 * Information this menu needs in order to create or update the buttons and background.
	 * @param menuItems List of {@link ItemInfo}s. It must not be null or empty.
	 * @param background Path to background image in the resources.
	 */
	public record Info(List<Item> menuItems, BufferedImage background) {
	}
	
	/**
	 * Information for the menu item, without the {@link Runnable} instance so that instances of
	 * {@link es.cristichi.fnac.nights.NightFactory} can express how to display their menu item at the same time
	 * that they let the {@link MainJFrame} decide how Nights are started.
	 * @param id Id of the element. Elements with the same ID will perform the same action, whose code is written in
	 *           {@link MainJFrame}.
	 * @param display Text shown for the button when not hovered by the player's mouse.
	 * @param hoverDisplay Text shown for the button when hovered by the player's mouse.
	 * @param loadingScreen BufferedImage of the loading screen, or {@code null} to use the default one.
	 */
	public record ItemInfo(String id, String display, String hoverDisplay,
						   @Nullable BufferedImage loadingScreen) implements Comparable<ItemInfo>{
		@Override
		public int compareTo(@NotNull ItemInfo o) {
			return id.compareTo(o.id);
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ItemInfo itemInfo)) return false;
            return Objects.equals(id, itemInfo.id);
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}
	}
	
	/**
	 * Information for the menu item, including the {@link Runnable}.
	 * @param info Information about the clickable menu item, like display text.
	 * @param runnable Method to run when clicked.
	 */
	public record Item(ItemInfo info, Runnable runnable){
	}

	/**
	 * Represents both music and the credits to the artist.
	 * @param music
	 * @param credits List of Strings representing the credits. Each String is drawn in a different line of text.
	 * @param weight When choosing one at random, the weight is taken into account.
	 */
	public record WeightedCreditedMusic(Music music, String[] credits, int weight){
	}

}