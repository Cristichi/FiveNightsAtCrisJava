package es.cristichi.fnac.gui;

import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import es.cristichi.fnac.obj.OfficeLocation;
import es.cristichi.fnac.obj.anim.Animatronic;
import es.cristichi.fnac.obj.anim.Jumpscare;
import es.cristichi.fnac.util.AssetsIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Timer;
import java.util.*;

public abstract class Night extends JComponent {
	private static final int fps = 60;

	private final int hourInterval = fps * 60;
	private final int powerDrainInterval = fps * 5;

	private final Random rng;

	@SuppressWarnings("unused")
	private final String name;
	private final BufferedImage backgroundImg;
	private int powerLevel;
	private int time;
	private int tick;
	private final Timer nightTicks;
	private Boolean victoryScreen;

	// Office transition
	private static final int OFFICE_TRANSITION_TICKS = 30;
	// Theses values are for the size used. Perhaps the background should be resized to be 1080p, but this works and it looks good without too much work on the processing part.
	private static final int LEFTDOOR_X = 200;
	private static final int MONITOR_X = 1000;
	private static final int RIGHTDOOR_X = 1800;
	private static final int OFFICEWIDTH = 2000; // Important not to change this xD it has to be 2k
	private OfficeLocation officeLoc;
	private int offTransTicks;
	private OfficeLocation offTransFrom;
	private final BufferedImage camMonitorImg, camMonitorStaticImg;
	private final BufferedImage camStaticImg;

	// Cam up/down
	private static final int CAMS_TRANSITION_TICKS = 30;
	private boolean camsUp;
	private int camsUpDownTransTicks;

	// Change cams
	private static final int CHANGE_CAMS_TRANSITION_TICKS = 10;
	private int changeCamsTransTicks;
	private final CameraMap map;

	// Doors
	private static final int DOOR_TRANSITION_TICKS = 10;
	private boolean leftDoorClosed;
	private int leftDoorTransTicks;
	private final BufferedImage leftDoorClosedImg;
	private final BufferedImage leftDoorTransImg;
	private final BufferedImage leftDoorOpenImg;
	private boolean rightDoorClosed;
	private int rightDoorTransTicks;
	private final BufferedImage rightDoorClosedImg;
	private final BufferedImage rightDoorTransImg;
	private final BufferedImage rightDoorOpenImg;

	// Jumpscare
	private Jumpscare jumpscare;

	public Night(String name, CameraMap mapAndAnimatronics, Random rng) throws IOException {
		this.rng = rng;
		this.name = name;
		this.map = mapAndAnimatronics;

		powerLevel = 100;
		time = 0; // Start at 12 AM = 00:00h
		backgroundImg = AssetsIO.loadImage("./assets/imgs/night/background.jpg");
		camMonitorImg = AssetsIO.loadImage("./assets/imgs/night/cams/monitor.png");
		camMonitorStaticImg = AssetsIO.loadImage("./assets/imgs/night/cams/monitorStatic.png");
		camStaticImg = AssetsIO.loadImage("./assets/imgs/night/cams/camTrans.jpg");
		leftDoorOpenImg = AssetsIO.loadImage("./assets/imgs/night/leftDoorOpen.jpg");
		leftDoorTransImg = AssetsIO.loadImage("./assets/imgs/night/leftDoorTrans.jpg");
		leftDoorClosedImg = AssetsIO.loadImage("./assets/imgs/night/leftDoorClosed.jpg");
		rightDoorOpenImg = AssetsIO.loadImage("./assets/imgs/night/rightDoorOpen.jpg");
		rightDoorTransImg = AssetsIO.loadImage("./assets/imgs/night/rightDoorTrans.jpg");
		rightDoorClosedImg = AssetsIO.loadImage("./assets/imgs/night/rightDoorClosed.jpg");

		offTransTicks = 0;
		camsUpDownTransTicks = 0;
		changeCamsTransTicks = 0;
		camsUp = false;
		leftDoorClosed = false;
		rightDoorClosed = false;
		victoryScreen = null;
		jumpscare = null;

		officeLoc = OfficeLocation.MONITOR;

		{
			AbstractAction action = new LeftAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "leftAction");
			getActionMap().put("leftAction", action);

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "leftAction");
			getActionMap().put("leftAction", action);
		}

		{
			AbstractAction action = new RightAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "rightAction");
			getActionMap().put("rightAction", action);

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "rightAction");
			getActionMap().put("rightAction", action);
		}

		{
			AbstractAction action = new CamsUpAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "camsUpAction");
			getActionMap().put("camsUpAction", action);

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "camsUpAction");
			getActionMap().put("camsUpAction", action);

		}

		{
			AbstractAction action = new CamsDownAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "camsDownAction");
			getActionMap().put("camsDownAction", action);

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "camsDownAction");
			getActionMap().put("camsDownAction", action);
		}

		{
			AbstractAction action = new LeftDoorAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Q"), "leftDoorAction");
			getActionMap().put("leftDoorAction", action);
		}

		{
			AbstractAction action = new RightDoorAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("E"), "rightDoorAction");
			getActionMap().put("rightDoorAction", action);
		}

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

		nightTicks = new Timer("Night [" + name + "]");
		startNight();
	}

	public void startNight(){
		nightTicks.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				tick++;
				if (tick % hourInterval == 0) {
					advanceTime();
				}
				if (tick % powerDrainInterval == 0) {
					drainPower();
				}

				if (jumpscare == null){
					HashMap<Animatronic, Map.Entry<Camera, Camera>> moves = new HashMap<>(5);
					for(Camera cam : map){
						for (Animatronic anim : cam.getAnimatronicsHere()){
							anim.updateIADuringNight(time);
							if (tick % (int) Math.round(anim.getSecInterval() * fps) == 0){
								System.out.printf("At tick %d, %s tried to move with AI %d.", tick, anim.getName(), anim.getAiLevel());
								if (anim.onMovementOpportunityAttempt(rng)){
									moves.put(anim, new AbstractMap.SimpleEntry<>(cam, anim.onMovementOppSuccess(map, cam, rng)));
									System.out.println(" And succeeded.");
								} else {
									System.out.println(" But did not succeed.");
								}
							}
							boolean openDoor = cam.isLeftDoorOfOffice()&&!leftDoorClosed ||cam.isRightDoorOfOffice()&&!rightDoorClosed;
							if (anim.onJumpscareAttempt(tick, openDoor, camsUp, cam, rng, fps)){
								jumpscare = anim.getJumpscare();
								// In case I want phantom jumpscares in the future.
								jumpscare.reset();
							}
						}
					}
					for (Map.Entry<Animatronic, Map.Entry<Camera, Camera>> move : moves.entrySet()){
						move.getValue().getKey().move(move.getKey(), move.getValue().getValue());
					}
				}

				repaint();
			}
		}, 100, 1000 / fps);
	}

	private void advanceTime() {
		if (++time == 6) {
			nightTicks.cancel();
			jumpscare = null;
			victoryScreen = true;
			Timer end = new Timer("End Thread");
			end.schedule(new TimerTask() {

				@Override
				public void run() {
					onNightPassed();
				}
			}, 5000);
		}
	}

	private void drainPower() {
		if (powerLevel > 0) {
			powerLevel--;
		}
		if (powerLevel > 0 && camsUp) {
			powerLevel--;
		}

		if (powerLevel == 0){
			nightTicks.cancel();
			onJumpscare();
		}
	}

	protected void usePower(int amount) {
		powerLevel -= Math.min(amount, powerLevel);
		repaint();
	}

	protected abstract void onJumpscare();

	protected abstract void onNightPassed();

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

        if (jumpscare != null && camsUpDownTransTicks == 0) {
			g.drawImage(jumpscare.getCurrentFrame(), 0, 0, getWidth(), getHeight(), this);
			jumpscare.update();
			if (jumpscare.isFinished()) {
				nightTicks.cancel();
				victoryScreen = true;
				Timer end = new Timer("End Thread");
				end.schedule(new TimerTask() {

					@Override
					public void run() {
						onNightPassed();
					}
				}, 5000);
			}

			return;
		}

		// Draw background and doors
		BufferedImage leftDoor;
		if (leftDoorTransTicks > 0) {
			leftDoorTransTicks--;
			leftDoor = leftDoorTransImg;
		} else {
			leftDoor = leftDoorClosed?leftDoorClosedImg:leftDoorOpenImg;
		}

		BufferedImage rightDoor;
		if (rightDoorTransTicks > 0) {
			rightDoorTransTicks--;
			rightDoor = rightDoorTransImg;
		} else {
			rightDoor = rightDoorClosed ? rightDoorClosedImg : rightDoorOpenImg;
		}

		switch (officeLoc) {
		case LEFTDOOR:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(LEFTDOOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door
				//TODO: When the window size is smaller than 1080p it shows too much to the left
				g.drawImage(leftDoor, 0, 0, getWidth()/2, getHeight(), 0,0, leftDoor.getWidth(), leftDoor.getHeight(), this);
			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X - ((MONITOR_X - LEFTDOOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door
				//TODO: When the window size is smaller than 1080p it shows too much to the left
				g.drawImage(leftDoor,
                        LEFTDOOR_X-xPosition, 0,
						getWidth()/2 + LEFTDOOR_X-xPosition, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(),
						this);
			}
			break;

		case RIGHTDOOR:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(RIGHTDOOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Right door
				g.drawImage(rightDoor,
						getWidth() / 2, 0,
						getWidth(), getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(),
						this);
			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X + ((RIGHTDOOR_X - MONITOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door shows also at the start of this transition
				//TODO: When the window size is smaller than 1080p it shows too much to the left
				g.drawImage(leftDoor, LEFTDOOR_X-xPosition, 0, getWidth()/2 + LEFTDOOR_X-xPosition, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(), this);

				// Right door
				//TODO: Fix this animation being just entirely wrong
				g.drawImage(rightDoor,
						RIGHTDOOR_X, 0,
						getWidth()/2 + RIGHTDOOR_X, getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(),
						this);
			}
			break;
		default:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(MONITOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door
				g.drawImage(leftDoor,
						LEFTDOOR_X-MONITOR_X, 0,
						getWidth()/2 + LEFTDOOR_X-MONITOR_X, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(),
						this);

				// Right door
				//TODO: When the window size is smaller than 1080p it shows too much to the right
				g.drawImage(rightDoor,
						RIGHTDOOR_X, 0,
						getWidth()/2 + RIGHTDOOR_X, getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(),
						this);
			} else if (offTransFrom.equals(OfficeLocation.LEFTDOOR)) {
				int xPosition = LEFTDOOR_X + ((MONITOR_X - LEFTDOOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door
				g.drawImage(leftDoor, LEFTDOOR_X-xPosition, 0, getWidth()/2 + LEFTDOOR_X-xPosition, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(), this);

				// Right door also shows at the end of this animation
				//TODO: For whatever reason it becomes stuck to the screen in this transition
				g.drawImage(rightDoor,
						RIGHTDOOR_X, 0,
						getWidth()/2 + RIGHTDOOR_X, getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(),
						this);
			} else if (offTransFrom.equals(OfficeLocation.RIGHTDOOR)) {
				int xPosition = RIGHTDOOR_X - ((RIGHTDOOR_X - MONITOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door shows also at the end of this transition
				g.drawImage(leftDoor, LEFTDOOR_X-xPosition, 0, getWidth()/2 + LEFTDOOR_X-xPosition, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(), this);
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
			int camImgWidth = map.getSelectedCam().getCamBackground().getWidth();
			int camImgHeight = map.getSelectedCam().getCamBackground().getHeight();

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
				// Transition cams up
				if (camsUpDownTransTicks > 0) {
					double transitionScale = (double) (CAMS_TRANSITION_TICKS - camsUpDownTransTicks) / CAMS_TRANSITION_TICKS;
					int scaledMonitorWidth = (int) (monitorTargetWidth * transitionScale);
					int scaledMonitorHeight = (int) (monitorTargetHeight * transitionScale);
					int transitionXOffset = (windowWidth - scaledMonitorWidth) / 2;
					int transitionYOffset = (windowHeight - scaledMonitorHeight) / 2;

					g.drawImage(camMonitorStaticImg, transitionXOffset, transitionYOffset, transitionXOffset + scaledMonitorWidth,
							transitionYOffset + scaledMonitorHeight, 0, 0, monitorWidth, monitorHeight, this);

					camsUpDownTransTicks--;

				// Watching cams
				} else {
					// Draw static in transition of camera change or current camera
					if (changeCamsTransTicks>0){
						g.drawImage(camStaticImg, camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
								0, 0, camImgWidth, camImgHeight, this);
						changeCamsTransTicks--;
					} else {
						g.drawImage(map.getSelectedCam().getCamBackground(), camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
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

						int debugRecDim = Math.min(scaledRecWidth, scaledRecHeight)/3;
						int debugRecX = scaledRecX;
						int debugRecY = scaledRecY;
						for (Animatronic anim : cam.getAnimatronicsHere()){
							g.setColor(anim.getDebugColor());
							g.fillRect(debugRecX, debugRecY, debugRecDim, debugRecDim);
							debugRecX+=debugRecDim;
							debugRecY+=debugRecDim;
						}
                    }

					// Reset transparency
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

					// Cam name
					g.setFont(new Font("Times New Roman", Font.PLAIN, 30));
					g.setColor(Color.WHITE);
					g.drawString(map.getSelectedCam().getName(), camDrawX + 30, camDrawY + 40);
				}
			} else {
				// Transition cams down
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
            g.drawString("Left door: " + (leftDoorClosed?"Closed":"Open"), 10, 60);
            g.drawString("Left door trans ticks: " + leftDoorTransTicks, 10, 80);
            g.drawString("Right door: " + (rightDoorClosed?"Closed":"Open"), 10, 100);
			g.drawString("Right door trans ticks: " + leftDoorTransTicks, 10, 120);
			g.drawString("Jumpscare: " + (jumpscare==null?"null":jumpscare.getCurrentIndex()+""), 10, 140);
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

	private class LeftDoorAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && !camsUp && leftDoorTransTicks==0 && officeLoc.equals(OfficeLocation.LEFTDOOR)) {
				leftDoorClosed = !leftDoorClosed;
				leftDoorTransTicks = DOOR_TRANSITION_TICKS;
			}
		}
	}


	private class RightDoorAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && !camsUp && rightDoorTransTicks==0 && officeLoc.equals(OfficeLocation.RIGHTDOOR)) {
				rightDoorClosed = !rightDoorClosed;
				rightDoorTransTicks = DOOR_TRANSITION_TICKS;
			}
		}
	}
}
