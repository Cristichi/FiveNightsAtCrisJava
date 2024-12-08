package es.cristichi.fnac.gui;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import kuusisto.tinysound.Music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Menu extends JComponent {
	private final List<MenuItem> menuItems;
	private Image backgroundImage;
	private final Image defaultLoadingImg;
	private Image currentLoadingImg;
	private boolean loading;
	private final Font btnFont;

	private int errorTicks = 0;
	private String[] error = null;

	private final Music music;
	private int musicCreditsTicks;
	private final String[] musicCreditsMsg;

	/**
	 * Creates a new {@link Menu} with the given data.
	 * @param backgroundImg Path to background image in the resources.
	 * @param defaultLoadingImg Path to default loading image in the resources. It is used for {@link MenuItem}s
	 *                         that don't specify one.
	 * @param menuItems List of {@link MenuItem}s. It must not be null or empty.
	 * @throws ResourceException If there are any errors loading the images from the resources.
	 */
    public Menu(String backgroundImg, String defaultLoadingImg, List<MenuItem> menuItems) throws ResourceException {
		this.menuItems = menuItems;
		backgroundImage = Resources.loadImageResource(backgroundImg);
		this.defaultLoadingImg = Resources.loadImageResource(defaultLoadingImg);
		loading = false;
		btnFont = new Font("Eraser Dust", Font.PLAIN, 100);

		music = Resources.loadMusic("menu/main.wav", "menuBack.wav");

		initializeMenuItems();

		Timer menuTicks = new Timer();
		int fps = 5;
		menuTicks.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		}, 100, 1000 / fps);

		music.play(true);
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
	public synchronized void updateMenuItems(List<MenuItem> newMenuItems) {
		this.menuItems.clear();
		this.menuItems.addAll(newMenuItems);
		initializeMenuItems();
		revalidate();
		repaint();
	}

	/**
	 * Starts the music. If it is already playing, it does nothing.
	 */
	public void startMusic(){
		if (!music.playing()){
			music.play(true);
		}
	}

	private void initializeMenuItems() {
		removeAll();
		setLayout(new GroupLayout(this));
		GroupLayout layout = (GroupLayout) getLayout();
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		for (MenuItem item : menuItems) {
			JButton button = createMenuButton(item);
			horizontalGroup.addComponent(button, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			verticalGroup.addComponent(button);
		}

		verticalGroup.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);
	}

	private JButton createMenuButton(MenuItem item) {
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
				button.setText("<html><u>" + item.display() + "</u></html>");
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
		g.drawImage((loading?
					(currentLoadingImg ==null?defaultLoadingImg: currentLoadingImg)
					:backgroundImage),
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
	 * It also makes sure to attach a listener to start playing the menu music when Night finishes.
	 * @param item String identifying the item clicked, or null if no Night should start.
	 * @throws IOException To catch errors, so the menu shows them on screen instead of just crashing.
	 */
	protected abstract Night onMenuItemClick(MenuItem item) throws Exception;

	private class MenuActionListener implements ActionListener {
		private final MenuItem menuItem;

		public MenuActionListener(MenuItem menuItem) {
			this.menuItem = menuItem;
		}

		public void actionPerformed(ActionEvent e) {
			loading = true;
			currentLoadingImg = menuItem.loadingScreen();
			for (Component component : Menu.this.getComponents()) {
				component.setVisible(false);
			}
			new Thread(() -> {
				try {
					if (menuItem.stopMusic()){
						music.stop();
					}
					error = null;
					errorTicks = 0;
					Night night = onMenuItemClick(menuItem);
					if (menuItem.stopMusic()){
						if (night != null){
							night.addOnNightEnd((completed) -> {
								music.play(true);
								musicCreditsTicks = 160;
							});
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					error = new String[] {"Error trying to load "+ menuItem, e1.getMessage(), "Check console for full stack trace."};
					errorTicks = 60;
					musicCreditsTicks = 160;
					music.play(true);
				}
				for (Component component : Menu.this.getComponents()) {
					component.setVisible(true);
				}
				loading = false;
			}, menuItem.id()).start();
		}
	}
}