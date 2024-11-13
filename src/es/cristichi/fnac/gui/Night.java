package es.cristichi.fnac.gui;

import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import es.cristichi.fnac.obj.OfficeLocation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Night extends JComponent {
	private static final int fps = 60;

	private final int hourInterval = fps * 60;
	private final int powerDrainInterval = fps * 5;

	private final String name;
	private final BufferedImage backgroundImg;
	private final BufferedImage camMonitorImg, camMonitorStaticImg;
	private final BufferedImage camStaticImg;
	private int powerLevel;
	private int time;
	private int tick;
	private final Timer gameTicks;
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
	private final CameraMap map;

	public Night(String name) throws IOException {
		this.name = name;
		powerLevel = 100;
		time = 0; // Start at 12 AM = 00:00h
		backgroundImg = loadImage("./assets/imgs/night/background.jpg");
		camMonitorImg = loadImage("./assets/imgs/night/cams/monitor.png");
		camMonitorStaticImg = loadImage("./assets/imgs/night/cams/monitorStatic.png");
		camStaticImg = loadImage("./assets/imgs/night/cams/camTrans.jpg");

		map = new CameraMap("test1", loadImage("./assets/imgs/night/cams/map.png"));
		map.add(new Camera("cam1", loadImage("./assets/imgs/night/cams/cam1.jpg"), new Rectangle(113,111,378,177)));
		map.add(new Camera("cam2", loadImage("./assets/imgs/night/cams/cam2.jpg"), new Rectangle(491,117,379,177)));
		map.add(new Camera("cam3", loadImage("./assets/imgs/night/cams/cam3.jpg"), new Rectangle(134,287,167,571)));
		map.add(new Camera("cam4", loadImage("./assets/imgs/night/cams/cam4.jpg"), new Rectangle(720,296,141,586)));

		offTransTicks = 0;
		camsUpDownTransTicks = 0;
		changeCamsTransTicks = 0;
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

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
                for (int i = 0; i < map.size(); i++) {
                    Camera camera = map.get(i);
                    if (camera.getLocOnScreen().contains(e.getLocationOnScreen())) {
						map.setSelected(i);
						changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
						break;
					}
                }
			}
		});
	}

	public String getName() {
		return name;
	}

	private BufferedImage loadImage(String path) throws IOException {
		try {
			return ImageIO.read(new File(path));
		} catch (IOException e) {
			throw new IOException("Image not found at \"" + path + "\". Probably Cristichi forgot to add it.", e);
		}
	}

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

	private void drainPower() {
		if (powerLevel > 0) {
			powerLevel--;
		} else {
			// powerDrainTimer.cancel();
			gameTicks.cancel();
			onJumpscare();
		}
	}

	protected void usePower(int amount) {
		powerLevel -= Math.min(amount, powerLevel);
		repaint();
	}

	protected abstract void onJumpscare();

	protected abstract void onNightComplete();

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

		if (camsUp || camsUpDownTransTicks > 0) {int windowWidth = getWidth();
			int windowHeight = getHeight();
			int monitorWidth = camMonitorImg.getWidth(null);
			int monitorHeight = camMonitorImg.getHeight(null);
			int camImgWidth = map.getSelectedCam().getCamImg().getWidth();
			int camImgHeight = map.getSelectedCam().getCamImg().getHeight();

			// Monitor's inner area for the camera view, from original image
			int innerX = 176;
			int innerY = 195;
			int innerWidth = 1695;
			int innerHeight = 1082;

			// Calculate aspect ratio-based dimensions to fit the monitor image without stretching
			double monitorAspectRatio = (double) monitorWidth / monitorHeight;
			double windowAspectRatio = (double) windowWidth / windowHeight;

			int monitorTargetWidth, monitorTargetHeight;
			if (windowAspectRatio > monitorAspectRatio) {
				monitorTargetHeight = windowHeight;
				monitorTargetWidth = (int) (windowHeight * monitorAspectRatio);
			} else {
				monitorTargetWidth = windowWidth;
				monitorTargetHeight = (int) (windowWidth / monitorAspectRatio);
			}

			// Calculate scaling for the monitor image and inner camera area
			double monitorScale = (double) monitorTargetWidth / monitorWidth;
			int scaledInnerWidth = (int) (innerWidth * monitorScale);
			int scaledInnerHeight = (int) (innerHeight * monitorScale);
			int scaledInnerX = (int) (innerX * monitorScale);
			int scaledInnerY = (int) (innerY * monitorScale);

			// Calculate scale for the camera image to fit within the scaled inner area
			double camScale = Math.min((double) scaledInnerWidth / camImgWidth, (double) scaledInnerHeight / camImgHeight);
			int camDrawWidth = (int) (camImgWidth * camScale);
			int camDrawHeight = (int) (camImgHeight * camScale);

			// Center the scaled inner area of the monitor on the screen
			int monitorXOffset = (windowWidth - monitorTargetWidth) / 2;
			int monitorYOffset = (windowHeight - monitorTargetHeight) / 2;
			int camDrawX = monitorXOffset + scaledInnerX + (scaledInnerWidth - camDrawWidth) / 2;
			int camDrawY = monitorYOffset + scaledInnerY + (scaledInnerHeight - camDrawHeight) / 2;

			if (camsUp) {
				if (camsUpDownTransTicks > 0) {
					double transitionScale = (double) (CAMS_TRANSITION_TICKS - camsUpDownTransTicks) / CAMS_TRANSITION_TICKS;
					int scaledMonitorWidth = (int) (monitorTargetWidth * transitionScale);
					int scaledMonitorHeight = (int) (monitorTargetHeight * transitionScale);
					int transitionXOffset = (windowWidth - scaledMonitorWidth) / 2;
					int transitionYOffset = (windowHeight - scaledMonitorHeight) / 2;

					g.drawImage(camMonitorStaticImg, transitionXOffset, transitionYOffset, transitionXOffset + scaledMonitorWidth,
							transitionYOffset + scaledMonitorHeight, 0, 0, monitorWidth, monitorHeight, this);

					camsUpDownTransTicks--;
				} else {
					// Draw static in transition of camera change or current camera
					if (changeCamsTransTicks>0){
						g.drawImage(camStaticImg, camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
								0, 0, camImgWidth, camImgHeight, this);
						changeCamsTransTicks--;
					} else {
						g.drawImage(map.getSelectedCam().getCamImg(), camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
								0, 0, camImgWidth, camImgHeight, this);
					}

					// Draw monitor frame
					g.drawImage(camMonitorImg, monitorXOffset, monitorYOffset,
							monitorXOffset + monitorTargetWidth, monitorYOffset + monitorTargetHeight,
							0, 0, monitorWidth, monitorHeight, this);

					// Draw map
					// Define target scaled map size based on monitor dimensions
					int scaledMapWidth = (int) (monitorTargetWidth * 0.3);
					int scaledMapHeight = (int) (monitorTargetHeight * 0.3);

					// Calculate the scale ratio between the original and scaled map
					double scaleRatioX = (double) scaledMapWidth / map.getImage().getWidth();
					double scaleRatioY = (double) scaledMapHeight / map.getImage().getHeight();

					// Calculate the position of the scaled map on the monitor
					int mapX = monitorXOffset + monitorTargetWidth - scaledMapWidth;
					int mapY = monitorYOffset + monitorTargetHeight - scaledMapHeight;

					// Draw the scaled map
					g.drawImage(map.getImage(), mapX, mapY, scaledMapWidth, scaledMapHeight, this);

					// Draw the transparent rectangle to highlight the current camera
					Graphics2D g2d = (Graphics2D) g;
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                    for (int i = 0; i < map.size(); i++) {
                        Camera cam = map.get(i);
						// Scale the current camera’s rectangle position
						Rectangle rec = new Rectangle(cam.getMapLoc());
						int scaledRecX = mapX + (int) (rec.x * scaleRatioX);
						int scaledRecY = mapY + (int) (rec.y * scaleRatioY);
						int scaledRecWidth = (int) (rec.width * scaleRatioX);
						int scaledRecHeight = (int) (rec.height * scaleRatioY);
						cam.updateLocOnScreen(new Rectangle(scaledRecX, scaledRecY, scaledRecWidth, scaledRecHeight));

						if (i == map.getSelected()){
							// Draw the scaled rectangle on the map
							g.setColor(Color.WHITE);
						} else {
							g.setColor(Color.GRAY);
						}
						g.fillRect(scaledRecX, scaledRecY, scaledRecWidth, scaledRecHeight);
                    }

					// Reset transparency
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

					// Cam name
					g.setFont(new Font("Times New Roman", Font.PLAIN, 30));
					g.setColor(Color.WHITE);
					g.drawString(map.getSelectedCam().getName(), camDrawX + 30, camDrawY + 40);
				}
			} else {
				if (camsUpDownTransTicks > 0) {
					double transitionScale = (double) camsUpDownTransTicks / CAMS_TRANSITION_TICKS;
					int scaledMonitorWidth = (int) (monitorTargetWidth * transitionScale);
					int scaledMonitorHeight = (int) (monitorTargetHeight * transitionScale);
					int transitionXOffset = (windowWidth - scaledMonitorWidth) / 2;
					int transitionYOffset = (windowHeight - scaledMonitorHeight) / 2;

					g.drawImage(camMonitorStaticImg, transitionXOffset, transitionYOffset, transitionXOffset + scaledMonitorWidth,
							transitionYOffset + scaledMonitorHeight, 0, 0, monitorWidth, monitorHeight, this);

					camsUpDownTransTicks--;
				}
			}
		}


        if (victoryScreen == null) {
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.setColor(Color.WHITE);
            g.drawString("Power: " + powerLevel + "%", 10, 20);
            g.drawString(String.format("%02d:?? AM", time), getWidth() - 100, 20);
			g.setColor(Color.GREEN);
            g.drawString("Debug Tick: " + tick, 10, 40);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 200));
            if (victoryScreen) {
                g.setColor(Color.GREEN);
                g.drawString("06:00 AM", 100, 200);
            } else {
                g.setColor(Color.RED);
                g.drawString("YOU DIED", 100, 200);
            }
        }
    }

	private class LeftAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (camsUpDownTransTicks == 0 && offTransTicks == 0 && changeCamsTransTicks==0) {
				if (!camsUp) {
					if (!officeLoc.equals(OfficeLocation.LEFTDOOR)) {
						if (officeLoc.equals(OfficeLocation.RIGHTDOOR)) {
							offTransFrom = OfficeLocation.RIGHTDOOR;
							officeLoc = OfficeLocation.MONITOR;
                        } else {
							offTransFrom = OfficeLocation.MONITOR;
							officeLoc = OfficeLocation.LEFTDOOR;
                        }
                        offTransTicks = OFFICE_TRANSITION_TICKS;
                    }
				} else {
					if (map.getSelected() > 0) {
						map.setSelected(map.getSelected()-1);
					} else {
						map.setSelected(map.size()-1);
					}
					changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
				}
			}
		}

	}

	private class RightAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (camsUpDownTransTicks == 0 && offTransTicks == 0 && changeCamsTransTicks==0) {
				if (!camsUp) {
					if (!officeLoc.equals(OfficeLocation.RIGHTDOOR)) {
						if (officeLoc.equals(OfficeLocation.LEFTDOOR)) {
							offTransFrom = OfficeLocation.LEFTDOOR;
							officeLoc = OfficeLocation.MONITOR;
                        } else {
							offTransFrom = OfficeLocation.MONITOR;
							officeLoc = OfficeLocation.RIGHTDOOR;
                        }
                        offTransTicks = OFFICE_TRANSITION_TICKS;
                    }
				} else {
					if (map.getSelected() < map.size()-1) {
						map.setSelected(map.getSelected()+1);
					} else {
						map.setSelected(0);
					}
					changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
				}
			}
		}
	}

	private class CamsUpAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && !camsUp) {
				camsUpDownTransTicks = CAMS_TRANSITION_TICKS;
				camsUp = true;
				usePower(1);
			}
		}
	}

	private class CamsDownAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && camsUp) {
				camsUpDownTransTicks = CAMS_TRANSITION_TICKS;
				camsUp = false;
			}
		}
	}
}
