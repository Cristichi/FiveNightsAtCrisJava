package es.cristichi.fnac.gui;

import es.cristichi.fnac.exception.AssetNotFound;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;

public abstract class Menu extends JComponent {
	private final List<String> menuItems;
	private Image backgroundImage;
	private Font btnFont;

    public Menu(String background, List<String> menuItems) throws IOException {
		this.menuItems = menuItems;
		loadBackgroundImage(background);
		loadCustomFont("./assets/fonts/EraserRegular.ttf"); // Load the custom font
		setLayout(new GroupLayout(this));
		initializeMenuItems();


        Timer menuTicks = new Timer();
        int fps = 10;
        menuTicks.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				repaint();
			}
		}, 100, 1000 / fps);
	}

	// Load background image from the "assets" folder
	private void loadBackgroundImage(String path) throws AssetNotFound {
		try {
			backgroundImage = ImageIO.read(new File(path));
		} catch (IOException e) {
			throw new AssetNotFound("Background image not found at " + path, e);
		}
	}

	// Load custom font from file
	private void loadCustomFont(String path) {
		try {
			btnFont = Font.createFont(Font.TRUETYPE_FONT, new File(path)).deriveFont(100f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(btnFont);
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
			System.out.println("Font not found at " + path);
			btnFont = new Font("Serif", Font.PLAIN, 24); // Fallback font
		}
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
			JButton menuItemButton = createStyledButton(item);
			horizontalGroup.addComponent(menuItemButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE);
			verticalGroup.addComponent(menuItemButton);
		}

		verticalGroup.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);
	}

	// Create a styled button with transparent background, white text, and custom font
	private JButton createStyledButton(String text) {
		JButton button = new JButton(text);
		button.setFont(btnFont); // Set custom font
		button.setForeground(Color.WHITE); // Set text color to white
		button.setContentAreaFilled(false); // Make background transparent
		button.setBorderPainted(false); // Remove button border
		button.setFocusPainted(false); // Remove focus indicator
		button.setAlignmentX(Component.LEFT_ALIGNMENT);
		button.addActionListener(new MenuActionListener(text));
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				button.setText("<html><u>" + text + "</u></html>");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setText(text);
			}
		});
		return button;
	}

	int errorTicks = 0;
	String[] error = null;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Draw the background image, scaled to fit the component's dimensions
		if (backgroundImage != null) {
			g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
		}

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

	// Abstract method for click actions
	protected abstract void onMenuItemClick(String item) throws IOException;

	// Internal class to handle menu item clicks
	private class MenuActionListener implements ActionListener {
		private final String item;

		public MenuActionListener(String item) {
			this.item = item;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				onMenuItemClick(item);
			} catch (IOException e1) {
				e1.printStackTrace();
				error = new String[] {"Error trying to load "+item, e1.getMessage(), "Check console for full stack trace."};
				errorTicks = 60;
			}
		}
	}
}
