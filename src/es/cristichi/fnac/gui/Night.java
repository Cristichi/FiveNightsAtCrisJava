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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.*;

public abstract class Night extends JComponent {
	private static final int FPS = 60;
	private static final int HOUR_INTERVAL = FPS * 60;
	private static final int TOTAL_HOURS = 6;

	private final Random rng;

	@SuppressWarnings("unused")
	private final String name;
	private final BufferedImage backgroundImg;
	/**
	 * Power in percentage, where 0 is 0%, 0.5 is 50% and 1 is 100%.
	 */
	private float powerLeft;
	private final float powerPerTickPerResource;
	private final Jumpscare powerOutage;
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

	// Animatronics moving around cams makes static
	private static final int CAMS_STATIC_MOVE_TICKS = 20;
	private static int camsHidingMovementTicks;
	private static List<String> camsHidingMovement;

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

	/**
	 *
	 * @param name Name of the Night. Barely used.
	 * @param mapAndAnimatronics Map of the place.
	 * @param powerOutage Jumpscare that will happen when the player runs out of power.
	 * @param rng Random for the night. You may just use <code>new Random()</code> unless you want a specific seed.
	 * @param powerConsumption A number from 0 to 1, where 0 makes the night impossible to lose by a power outage, and 1 makes it impossible to win even without Animatronics.
	 * @throws IOException If Assets cannot be loaded from disk. Usually it's because they may be missing, so skill issue.
	 */
	public Night(String name, CameraMap mapAndAnimatronics, Jumpscare powerOutage, Random rng, float powerConsumption) throws IOException {
		this.rng = rng;
		this.name = name;
		this.map = mapAndAnimatronics;
		this.powerOutage = powerOutage;

		powerLeft = 1;
		// So this calculates depending on the fps, which determines the total number of ticks per night, which is
		// important to calibrate that the night cannot be survived by using everything (extremely easy)
		// but also that using nothing does not kill you (extremely hard). The objective is to find a balance
		int totalTicks = HOUR_INTERVAL * TOTAL_HOURS;
		float minPowerPerTickPerResource = 1.0f / totalTicks; // Minimum power consumption per tick. Lower values make the game impossible even with no Animatronics.
		float maxPowerPerTickPerResource = 1.0f / (4 * totalTicks); // Maximum power consumption. Higher values makes the game 100% consistent by closing both doors and not moving.
		powerPerTickPerResource = (minPowerPerTickPerResource + maxPowerPerTickPerResource) * powerConsumption;

		time = 0; // Start at 12 AM = 00:00h
		backgroundImg = AssetsIO.loadImage("./assets/imgs/night/background.jpg");
		camMonitorImg = AssetsIO.loadImage("./assets/imgs/night/cams/monitor.png");
		camMonitorStaticImg = AssetsIO.loadImage("./assets/imgs/night/cams/monitorStatic.png");
		camStaticImg = AssetsIO.loadImage("./assets/imgs/night/cams/camTrans.jpg");
		leftDoorOpenImg = AssetsIO.loadImage("./assets/imgs/night/leftDoorOpen.png");
		leftDoorTransImg = AssetsIO.loadImage("./assets/imgs/night/leftDoorTrans.png");
		leftDoorClosedImg = AssetsIO.loadImage("./assets/imgs/night/leftDoorClosed.png");
		rightDoorOpenImg = AssetsIO.loadImage("./assets/imgs/night/rightDoorOpen.png");
		rightDoorTransImg = AssetsIO.loadImage("./assets/imgs/night/rightDoorTrans.png");
		rightDoorClosedImg = AssetsIO.loadImage("./assets/imgs/night/rightDoorClosed.png");

		offTransTicks = 0;
		camsUpDownTransTicks = 0;
		changeCamsTransTicks = 0;
		camsUp = false;
		camsHidingMovement = new ArrayList<>();
		leftDoorClosed = false;
		rightDoorClosed = false;
		victoryScreen = null;
		jumpscare = null;

		officeLoc = OfficeLocation.MONITOR;

		{
			AbstractAction action = new LeftAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "leftAction");
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "leftAction");
			getActionMap().put("leftAction", action);
		}

		{
			AbstractAction action = new RightAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "rightAction");
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "rightAction");
			getActionMap().put("rightAction", action);
		}

		{
			AbstractAction action = new CamsAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "camsAction");
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "camsAction");
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "camsAction");
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "camsAction");
			getActionMap().put("camsAction", action);

		}

		{
			AbstractAction action = new DoorAction();

			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Q"), "doorAction");
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("E"), "doorAction");
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), "doorAction");
			getActionMap().put("doorAction", action);
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
	}

	public void startNight(){
		nightTicks.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Time never stops. Well sometimes it does, when dying for instance.
				tick++;
				if (tick % HOUR_INTERVAL == 0) {
					advanceTime();
				}

				// Power drain
				if (powerLeft > 0) {
					powerLeft-=powerPerTickPerResource;
				}
				if (powerLeft > 0 && camsUp) {
					powerLeft-=powerPerTickPerResource;
				}
				if (powerLeft > 0 && leftDoorClosed) {
					powerLeft-=powerPerTickPerResource;
				}
				if (powerLeft > 0 && rightDoorClosed) {
					powerLeft-=powerPerTickPerResource;
				}

				if (powerLeft <= 0){
					jumpscare = powerOutage;
				}

				// Animatronic movements and jumpscare opportunities
				if (jumpscare == null){
					HashMap<Animatronic, Map.Entry<Camera, Camera>> moves = new HashMap<>(5);
					for(Camera cam : map){
						for (Animatronic anim : cam.getAnimatronicsHere()){
							anim.updateIADuringNight(time);
							if (tick % (int) Math.round(anim.getSecInterval() * FPS) == 0){
								if (anim.onMovementOpportunityAttempt(rng)){
									moves.put(anim, new AbstractMap.SimpleEntry<>(cam, anim.onMovementOppSuccess(map, cam, rng)));
								}
							}
							boolean openDoor = cam.isLeftDoorOfOffice()&&!leftDoorClosed ||cam.isRightDoorOfOffice()&&!rightDoorClosed;
							if (anim.onJumpscareAttempt(tick, openDoor, camsUp, cam, rng, FPS)){
								jumpscare = anim.getJumpscare();
								// In case I want phantom jumpscares in the future.
								jumpscare.reset();
							}
						}
					}
					for (Map.Entry<Animatronic, Map.Entry<Camera, Camera>> move : moves.entrySet()){
						move.getValue().getKey().move(move.getKey(), move.getValue().getValue());
						camsHidingMovement.add(move.getValue().getKey().getName());
						camsHidingMovement.add(move.getValue().getValue().getName());
						camsHidingMovementTicks = CAMS_STATIC_MOVE_TICKS;
					}
				}

				// We repaint da thing
				repaint();
			}
		}, 100, 1000 / FPS);
	}

	private void advanceTime() {
		if (++time == TOTAL_HOURS) {
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
						onJumpscare();
					}
				}, 5000);
			}

			return;
		}

		// Draw background and doors. Oh boy.
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
		int leftDoorWidth = (MONITOR_X-LEFTDOOR_X);
		int rightDoorWidth = RIGHTDOOR_X-MONITOR_X;

		switch (officeLoc) {
		case LEFTDOOR:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(LEFTDOOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door
				// TODO: Fix for non-1920 width
				g.drawImage(leftDoor,
						0, 0,
						leftDoorWidth, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(), this);
			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X - ((MONITOR_X - LEFTDOOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door
				// TODO: Fix for non-1920 width
				g.drawImage(leftDoor,
                        LEFTDOOR_X-xPosition, 0,
						leftDoorWidth+LEFTDOOR_X-xPosition, getHeight(),
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
						getWidth()-rightDoorWidth, 0,
						getWidth(), getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(),
						this);

			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X + ((RIGHTDOOR_X - MONITOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door shows also at the start of this transition
				// TODO: Fix for non-1920 width
				g.drawImage(leftDoor,
						LEFTDOOR_X-xPosition, 0,
						leftDoorWidth - xPosition, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(),
						this);

				// Right door. PS: Idk why adding 1000 works on fullscreen, but it works, so I'l fix it with the fix below when it happens
				// TODO: Fix for non-1920 width
				g.drawImage(rightDoor,
						getWidth()-xPosition+1000, 0,
						getWidth()+rightDoorWidth-xPosition+1000, getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(),
						this);
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

				// Left door
				g.drawImage(leftDoor,
						LEFTDOOR_X-xPosition, 0,
						leftDoorWidth + LEFTDOOR_X - xPosition, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(),
						this);
			} else if (offTransFrom.equals(OfficeLocation.RIGHTDOOR)) {
				int xPosition = RIGHTDOOR_X - ((RIGHTDOOR_X - MONITOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Right door. PS: Idk why adding 1000 works on fullscreen, but it works, so I'l fix it with the fix below when it happens
				// TODO: Fix for non-1920 width
				g.drawImage(rightDoor,
						getWidth()-xPosition+1000, 0,
						getWidth()+rightDoorWidth-xPosition+1000, getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(),
						this);
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
						Camera current = map.getSelectedCam();
						// On current camera, if an Animatronic moved from or to this camera recently, we show static as well
						if (camsHidingMovementTicks-->0 && camsHidingMovement.contains(current.getName())){
							g.drawImage(camStaticImg, camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
									0, 0, camImgWidth, camImgHeight, this);
						} else {
							g.drawImage(current.getCamBackground(), camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
									0, 0, camImgWidth, camImgHeight, this);
						}
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
			g.drawString(String.format("%02d:?? AM", time), getWidth() - 100, 20);

			{
				int powerUsage = 0;
				String powerUsageStr = "█";
				if (camsUp) {
					powerUsage++;
					powerUsageStr = powerUsageStr.concat("█");
				}
				if (leftDoorClosed) {
					powerUsage++;
					powerUsageStr = powerUsageStr.concat("█");
				}
				if (rightDoorClosed) {
					powerUsage++;
					powerUsageStr = powerUsageStr.concat("█");
				}
				switch (powerUsage) {
					case 0:
						g.setColor(Color.GREEN);
						break;
					case 1:
						g.setColor(Color.ORANGE);
						break;
					case 2:
						g.setColor(Color.RED);
						break;
					case 3:
						g.setColor(Color.RED.darker());
						break;
				}
				g.drawString(String.format("Power: %.0f%% (Usage: %s)", (powerLeft*100), powerUsageStr), 10, 20);
			}

//			g.setColor(Color.GREEN);
//          g.drawString("Debug Tick: " + tick, 10, 40);
//          g.drawString("Left door: " + (leftDoorClosed?"Closed":"Open"), 10, 60);
//          g.drawString("Left door trans ticks: " + leftDoorTransTicks, 10, 80);
//          g.drawString("Right door: " + (rightDoorClosed?"Closed":"Open"), 10, 100);
//			g.drawString("Right door trans ticks: " + leftDoorTransTicks, 10, 120);
//			g.drawString("Jumpscare: " + (jumpscare==null?"null":jumpscare.getCurrentIndex()+""), 10, 140);
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

	private class CamsAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0){
				if (camsUp) {
					camsUpDownTransTicks = CAMS_TRANSITION_TICKS;
					camsUp = false;
				} else {
					camsUpDownTransTicks = CAMS_TRANSITION_TICKS;
					camsUp = true;
				}
			}
		}
	}

	private class DoorAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && !camsUp){
				if (rightDoorTransTicks==0 && officeLoc.equals(OfficeLocation.RIGHTDOOR)) {
					rightDoorClosed = !rightDoorClosed;
					rightDoorTransTicks = DOOR_TRANSITION_TICKS;
				} else if (leftDoorTransTicks==0 && officeLoc.equals(OfficeLocation.LEFTDOOR)) {
					leftDoorClosed = !leftDoorClosed;
					leftDoorTransTicks = DOOR_TRANSITION_TICKS;
				}
			}
		}
	}
}
