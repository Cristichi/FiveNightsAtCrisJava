package es.cristichi.fnac.gui;

import es.cristichi.fnac.io.Resources;
import kuusisto.tinysound.Music;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.LineMetrics;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Menu extends JComponent {
	private final List<String> menuItems;
	private final Image backgroundImage;
	private final Image loadingImage;
	private boolean loading;
	private final Font btnFont;

	private int errorTicks = 0;
	private String[] error = null;

	private final Music music;
	private int musicCreditsTicks;
	private final String[] musicCreditsMsg;

    public Menu(String backgroundImg, String loadingImg, List<String> menuItems) throws IOException {
		this.menuItems = menuItems;
		backgroundImage = Resources.loadImageResource(backgroundImg);
		loadingImage = Resources.loadImageResource(loadingImg);
		loading = false;
		btnFont = Resources.loadCustomFont("fonts/EraserRegular.ttf").deriveFont(140f);

		music = Resources.loadMusic("menu/main.wav", "menuBack.wav");

        setLayout(new GroupLayout(this));
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

	// Initialize and position the menu items
	private void initializeMenuItems() {
		GroupLayout layout = (GroupLayout) getLayout();
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.ParallelGroup horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		GroupLayout.SequentialGroup verticalGroup = layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		for (String item : menuItems) {
			float fontScale = (float) (btnFont.getSize() * Math.min(getWidth(), getHeight())) /1000;
            JButton button = new JButton("<html>"+item+"</html>");
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
                    button.setText("<html><u>" + item + "</u></html>");
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setText("<html>"+item+"</html>");
                }
            });
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					float fontScale = (float) (btnFont.getSize() * Math.min(getWidth(), getHeight())) /1000;
					button.setFont(btnFont.deriveFont(fontScale));
				}
			});
            horizontalGroup.addComponent(button, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE);
			verticalGroup.addComponent(button);
		}

		verticalGroup.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int fontScale = Math.min(getWidth(), getHeight())/1000;

		int fontSize = 40*fontScale;

		// Draw the background image, scaled to fit the component's dimensions
		g.drawImage(loading?loadingImage:backgroundImage, 0, 0, getWidth(), getHeight(), this);

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
	 * @param item String identifying the item clicked.
	 * @throws IOException To catch errors, so the menu shows them on screen instead of just crashing.
	 */
	protected abstract Night onMenuItemClick(String item) throws Exception;

	private class MenuActionListener implements ActionListener {
		private final String item;

		public MenuActionListener(String item) {
			this.item = item;
		}

		public void actionPerformed(ActionEvent e) {
			loading = true;
			for (Component component : Menu.this.getComponents()) {
				component.setVisible(false);
			}
			new Thread(() -> {
				try {
					music.stop();
					error = null;
					errorTicks = 0;
					Night night = onMenuItemClick(item);
					if (night != null){
						night.addOnNightCompleted(() -> {
                            music.play(true);
							musicCreditsTicks = 160;
                        });
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					error = new String[] {"Error trying to load "+item, e1.getMessage(), "Check console for full stack trace."};
					errorTicks = 60;
					musicCreditsTicks = 160;
					music.play(true);
				}
				for (Component component : Menu.this.getComponents()) {
					component.setVisible(true);
				}
				loading = false;
			}, item).start();
		}
	}
}