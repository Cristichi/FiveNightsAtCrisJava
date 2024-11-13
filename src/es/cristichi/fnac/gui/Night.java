package es.cristichi.fnac.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import es.cristichi.fnac.obj.OfficeLocation;

public abstract class Night extends JComponent {
	private static final long serialVersionUID = -8562006473118553625L;
	private static final int fps = 60;

	private final int hourInterval = fps * 60;
	private final int powerDrainInterval = fps * 5;

	private final String name;
	private BufferedImage backgroundImg;
	private BufferedImage camMonitorImg;
	private int powerLevel;
	private int time;
	private int tick;
	private Timer gameTicks;
	private Boolean victoryScreen;

	private static final int OFFICE_TRANSITION_TICKS = 30;
	// Theses values are for the size used. Perhaps the background should be resized to be 1080p, but this works and it looks good without too much work on the processing part.
	private static final int LEFTDOOR_X = 500;
	private static final int MONITOR_X = 1000;
	private static final int RIGHTDOOR_X = 1500;
	private static final int OFFICEWIDTH = 2000;
	private OfficeLocation officeLoc;
	private int offTransTicks;
	private OfficeLocation offTransFrom;

	private static final int CAMS_TRANSITION_TICKS = 30;
	private boolean camsUp;
	private int camsUpDownTransTicks;

	private static final int CHANGE_CAMS_TRANSITION_TICKS = 10;
	private int changeCamsTransTicks;
	private BufferedImage[] camerasImgs;
	private int selectedCam;

	public Night(String name) throws IOException {
		this.name = name;
		powerLevel = 100;
		time = 0; // Start at 12 AM = 00:00h
		backgroundImg = loadImage("./assets/imgs/night/background.jpg");
		camMonitorImg = loadImage("./assets/imgs/night/cams/monitor.png");
		camerasImgs = new BufferedImage[] { loadImage("./assets/imgs/night/cams/cam1.jpg"),
				loadImage("./assets/imgs/night/cams/cam2.jpg") };

		offTransTicks = 0;
		camsUpDownTransTicks = 0;
		changeCamsTransTicks = 0;
		selectedCam = 0;
		camsUp = false;
		victoryScreen = null;

		gameTicks = new Timer("Night [" + name + "]");
		gameTicks.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				tick++;
				if (tick % hourInterval == 0) {
					advanceTime();
				}
				if (tick % powerDrainInterval == 0) {
					drainPower();
				}
				repaint();
			}
		}, 100, 1000 / fps);
		officeLoc = OfficeLocation.MONITOR;

		LeftAction left = new LeftAction();

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "leftAction");
		getActionMap().put("leftAction", left);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "leftAction");
		getActionMap().put("leftAction", left);

		RightAction right = new RightAction();

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "rightAction");
		getActionMap().put("rightAction", right);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "rightAction");
		getActionMap().put("rightAction", right);

		CamsUpAction camU = new CamsUpAction();

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "camsUpAction");
		getActionMap().put("camsUpAction", camU);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "camsUpAction");
		getActionMap().put("camsUpAction", camU);

		CamsDownAction camD = new CamsDownAction();

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "camsDownAction");
		getActionMap().put("camsDownAction", camD);

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "camsDownAction");
		getActionMap().put("camsDownAction", camD);
	}

	public String getName() {
		return name;
	}

	// Load the background image for the night
	private BufferedImage loadImage(String path) throws IOException {
		try {
			return ImageIO.read(new File(path));
		} catch (IOException e) {
			throw new IOException("Image not found at \"" + path + "\". Probably Cristichi forgot to add it.", e);
		}
	}

	// Handle time progression
	private void advanceTime() {
		if (++time == 6) {
			gameTicks.cancel();
			victoryScreen = true;
			Timer end = new Timer("End Thread");
			end.schedule(new TimerTask() {

				@Override
				public void run() {
					onNightComplete();
				}
			}, 5000);
		}
	}

	// Drain power over time
	private void drainPower() {
		if (powerLevel > 0) {
			powerLevel--;
		} else {
			// powerDrainTimer.cancel();
			gameTicks.cancel();
			onPowerOutage(); // Trigger when power runs out
		}
	}

	// Example method to simulate player interactions
	protected void usePower(int amount) {
		powerLevel -= Math.max(amount, powerLevel);
		repaint();
	}

	// Abstract methods to define night-specific behaviors
	protected abstract void onJumpscare();

	protected abstract void onNightComplete();

	protected abstract void onPowerOutage();

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Draw background
		switch (officeLoc) {
		case LEFTDOOR:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(LEFTDOOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);
			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X - ((MONITOR_X - LEFTDOOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);
			}
			break;

		case RIGHTDOOR:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(RIGHTDOOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);
			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X + ((RIGHTDOOR_X - MONITOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);
			}
			break;
		default:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(MONITOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);
			} else if (offTransFrom.equals(OfficeLocation.LEFTDOOR)) {
				int xPosition = LEFTDOOR_X + ((MONITOR_X - LEFTDOOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);
			} else if (offTransFrom.equals(OfficeLocation.RIGHTDOOR)) {
				int xPosition = RIGHTDOOR_X - ((RIGHTDOOR_X - MONITOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);
			}
			break;
		}

		if (offTransTicks-- <= 0) {
			offTransTicks = 0;
			offTransFrom = null;
		}

		if (camsUp || camsUpDownTransTicks > 0) {
			int windowWidth = getWidth();
			int windowHeight = getHeight();
			int imageWidth = camMonitorImg.getWidth(null);
			int imageHeight = camMonitorImg.getHeight(null);
			int camImgWidth = camerasImgs[selectedCam].getWidth();
			int camImgHeight = camerasImgs[selectedCam].getHeight();

			// Calculate aspect ratio-based dimensions to fit the monitor image without stretching
			double monitorAspectRatio = (double) imageWidth / imageHeight;
			double windowAspectRatio = (double) windowWidth / windowHeight;

			int monitorTargetWidth, monitorTargetHeight;
			if (windowAspectRatio > monitorAspectRatio) {
				monitorTargetHeight = windowHeight;
				monitorTargetWidth = (int) (windowHeight * monitorAspectRatio);
			} else {
				monitorTargetWidth = windowWidth;
				monitorTargetHeight = (int) (windowWidth / monitorAspectRatio);
			}

			double camScale = Math.min((double) monitorTargetWidth / camImgWidth,
					(double) monitorTargetHeight / camImgHeight);
			int camDrawWidth = (int) (camImgWidth * camScale);
			int camDrawHeight = (int) (camImgHeight * camScale);
			int camDrawX = (windowWidth - camDrawWidth) / 2;
			int camDrawY = (windowHeight - camDrawHeight) / 2;

			if (camsUp) {
				if (camsUpDownTransTicks > 0) {
					double scale = (double) (CAMS_TRANSITION_TICKS - camsUpDownTransTicks) / CAMS_TRANSITION_TICKS;
					int scaledMonitorWidth = (int) (monitorTargetWidth * scale);
					int scaledMonitorHeight = (int) (monitorTargetHeight * scale);
					int monitorXOffset = (windowWidth - scaledMonitorWidth) / 2;
					int monitorYOffset = (windowHeight - scaledMonitorHeight) / 2;

					// Draw camera view in position first
					g.drawImage(camerasImgs[selectedCam], camDrawX, camDrawY, camDrawX + camDrawWidth,
							camDrawY + camDrawHeight, 0, 0, camImgWidth, camImgHeight, this);

					// Draw the scaling monitor frame over the camera view
					g.drawImage(camMonitorImg, monitorXOffset, monitorYOffset, monitorXOffset + scaledMonitorWidth,
							monitorYOffset + scaledMonitorHeight, 0, 0, imageWidth, imageHeight, this);

					camsUpDownTransTicks--;
				} else {

					// Fully up state: Draw the camera view and monitor frame at full scale
					g.drawImage(camerasImgs[selectedCam], camDrawX, camDrawY, camDrawX + camDrawWidth,
							camDrawY + camDrawHeight, 0, 0, camImgWidth, camImgHeight, this);
					g.drawImage(camMonitorImg, (windowWidth - monitorTargetWidth) / 2,
							(windowHeight - monitorTargetHeight) / 2, (windowWidth + monitorTargetWidth) / 2,
							(windowHeight + monitorTargetHeight) / 2, 0, 0, imageWidth, imageHeight, this);

					g.setFont(new Font("Times New Roman", Font.PLAIN, 30));
					g.setColor(Color.WHITE);
					g.drawString("Cam" + (selectedCam + 1), camDrawX + 30, camDrawY + 40);
				}
			} else {
				if (camsUpDownTransTicks > 0) {
					double scale = (double) camsUpDownTransTicks / CAMS_TRANSITION_TICKS;
					int scaledMonitorWidth = (int) (monitorTargetWidth * scale);
					int scaledMonitorHeight = (int) (monitorTargetHeight * scale);
					int monitorXOffset = (windowWidth - scaledMonitorWidth) / 2;
					int monitorYOffset = (windowHeight - scaledMonitorHeight) / 2;

					// Draw camera view behind the shrinking monitor frame
					g.drawImage(camerasImgs[selectedCam], camDrawX, camDrawY, camDrawX + camDrawWidth,
							camDrawY + camDrawHeight, 0, 0, camImgWidth, camImgHeight, this);
					g.drawImage(camMonitorImg, monitorXOffset, monitorYOffset, monitorXOffset + scaledMonitorWidth,
							monitorYOffset + scaledMonitorHeight, 0, 0, imageWidth, imageHeight, this);

					camsUpDownTransTicks--;
				}
			}
		}

		// Display time and power level
		g.setFont(new Font("Arial", Font.BOLD, 18));
		g.setColor(Color.WHITE);
		g.drawString("Power: " + powerLevel + "%", 10, 20);
		g.drawString("Time: " + time + " AM", getWidth() - 100, 40);
		g.drawString("Debug Tick: " + tick, 0, 60);
		g.drawString("Debug Loc: " + officeLoc.name(), 0, 80);
		g.drawString("Debug trans: " + offTransTicks, 0, 100);
		g.drawString("Debug cams up: " + camsUp, 0, 120);
		g.drawString("Debug cams trans: " + camsUpDownTransTicks, 0, 140);

		if (victoryScreen != null) {
			g.setFont(new Font("Arial", Font.BOLD, 200));
			if (victoryScreen) {
				g.setColor(Color.GREEN);
				g.drawString("06:00 AM", 100, 200);
			} else {
				g.setColor(Color.RED);
				g.drawString("OH NO", 100, 200);
			}
		}
	}

	private class LeftAction extends AbstractAction {
		private static final long serialVersionUID = 4399005171704551504L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (camsUpDownTransTicks == 0 && offTransTicks == 0) {
				if (!camsUp) {
					if (!officeLoc.equals(OfficeLocation.LEFTDOOR)) {
						if (officeLoc.equals(OfficeLocation.RIGHTDOOR)) {
							offTransFrom = OfficeLocation.RIGHTDOOR;
							officeLoc = OfficeLocation.MONITOR;
							offTransTicks = OFFICE_TRANSITION_TICKS;
						} else {
							offTransFrom = OfficeLocation.MONITOR;
							officeLoc = OfficeLocation.LEFTDOOR;
							offTransTicks = OFFICE_TRANSITION_TICKS;
						}
					}
				} else {
					if (selectedCam > 0) {
						selectedCam--;
						changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
					}
				}
			}
		}

	}

	private class RightAction extends AbstractAction {
		private static final long serialVersionUID = -8464841812192761050L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (camsUpDownTransTicks == 0 && offTransTicks == 0) {
				if (!camsUp) {
					if (!officeLoc.equals(OfficeLocation.RIGHTDOOR)) {
						if (officeLoc.equals(OfficeLocation.LEFTDOOR)) {
							offTransFrom = OfficeLocation.LEFTDOOR;
							officeLoc = OfficeLocation.MONITOR;
							offTransTicks = OFFICE_TRANSITION_TICKS;
						} else {
							offTransFrom = OfficeLocation.MONITOR;
							officeLoc = OfficeLocation.RIGHTDOOR;
							offTransTicks = OFFICE_TRANSITION_TICKS;
						}
					}
				} else {
					if (selectedCam < camerasImgs.length - 1) {
						selectedCam++;
						changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
					}
				}
			}
		}
	}

	private class CamsUpAction extends AbstractAction {
		private static final long serialVersionUID = -8464841812192761050L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && !camsUp) {
				camsUpDownTransTicks = CAMS_TRANSITION_TICKS;
				camsUp = true;
			}
		}
	}

	private class CamsDownAction extends AbstractAction {
		private static final long serialVersionUID = -8464841812192761050L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && camsUp) {
				camsUpDownTransTicks = CAMS_TRANSITION_TICKS;
				camsUp = false;
			}
		}
	}
}
