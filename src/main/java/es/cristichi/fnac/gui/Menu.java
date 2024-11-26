package es.cristichi.fnac.gui;

import es.cristichi.fnac.io.FNACResources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    public Menu(String backgroundImg, String loadingImg, List<String> menuItems) throws IOException {
		this.menuItems = menuItems;
		backgroundImage = FNACResources.loadImageResource(backgroundImg);
		loadingImage = FNACResources.loadImageResource(loadingImg);
		loading = false;
		btnFont = FNACResources.loadCustomFont("fonts/EraserRegular.ttf").deriveFont(140f);
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
            JButton button = new JButton("<html>"+item+"</html>");
            button.setFont(btnFont); // Set custom font
            button.setForeground(Color.WHITE); // Set text color to white
            button.setContentAreaFilled(false); // Make background transparent
            button.setBorderPainted(false); // Remove button border
            button.setFocusPainted(false); // Remove focus indicator
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

		// Draw the background image, scaled to fit the component's dimensions
		g.drawImage(loading?loadingImage:backgroundImage, 0, 0, getWidth(), getHeight(), this);

		if (errorTicks-- > 0) {
			int yOg = 40;
			g.setFont(new Font("Arial", Font.BOLD, yOg));
			g.setColor(Color.RED);
			int y = yOg;
			for (String line : error) {
				g.drawString(line, 10, y);
				y+=yOg;
			}
		}
	}

	/**
	 * This is performed after an item is closed, alongside a loading screen in case you need time to load assets.
	 * @param item String identifying the item clicked.
	 * @throws IOException To catch errors, so the menu shows them on screen instead of just crashing.
	 */
	protected abstract void onMenuItemClick(String item) throws Exception;

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
                        onMenuItemClick(item);
						error = null;
						errorTicks = 0;
					} catch (Exception e1) {
                        e1.printStackTrace();
                        error = new String[] {"Error trying to load "+item, e1.getMessage(), "Check console for full stack trace."};
                        errorTicks = 60;
                    }
					for (Component component : Menu.this.getComponents()) {
						component.setVisible(true);
					}
					loading = false;
                }, item).start();
		}
	}
}