package es.cristichi.fnac.gui;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.io.FNACResources;
import es.cristichi.fnac.obj.Camera;
import es.cristichi.fnac.obj.CameraMap;
import es.cristichi.fnac.obj.OfficeLocation;
import es.cristichi.fnac.obj.anim.Animatronic;
import es.cristichi.fnac.obj.anim.Jumpscare;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.*;

public abstract class Night extends JComponent {
	private static final int FPS = 60;
	private static final int HOUR_INTERVAL = FPS * 90;
	private static final int TOTAL_HOURS = 6;

	private final String nightName;
	private final Random rng;
	private final BufferedImage backgroundImg;
	private float powerLeft;
	private final float powerPerTickPerResource;
	private final Jumpscare powerOutage;
	private int currentTick;
	private int nightHour;
	private final Timer nightTicks;
	private Boolean victoryScreen;

	// Office transition
	private static final int OFFICE_TRANSITION_TICKS = 30;
	private static final int LEFTDOOR_X = 200;
	private static final int MONITOR_X = 1000;
	private static final int RIGHTDOOR_X = 1800;
	private static final int OFFICEWIDTH = 2000; // Important not to change this xD it has to be 2k
	private OfficeLocation officeLoc;
	private int offTransTicks;
	private OfficeLocation offTransFrom;
	private final BufferedImage camMonitorImg, camMonitorStaticImg;
	private final BufferedImage camStaticImg;
	private final float MOUSE_MOVE_THRESHOLD = 0.05f;

	// Cam up/down
	private static final int CAMS_TRANSITION_TICKS = 30;
	private boolean camsUp;
	private int camsUpDownTransTicks;
	private final HashMap<String, Rectangle> camsLocOnScreen;

	// Change cams
	private static final int CHANGE_CAMS_TRANSITION_TICKS = 10;
	private int changeCamsTransTicks;
	private final CameraMap camerasMap;

	// Animatronics moving around cams makes static
	private static final int CAMS_STATIC_MOVE_TICKS = 20;
	private int camsHidingMovementTicks;
	private final List<String> camsHidingMovement;
	private final HashMap<String, Point> animPosInCam;

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
	 * @param nightName Name of the Night. Barely used.
	 * @param mapAndAnimatronics Map of the place.
	 * @param powerOutage Jumpscare that will happen when the player runs out of power.
	 * @param rng Random for the night. You may just use <code>new Random()</code> unless you want a specific seed.
	 * @param powerConsumption A number from 0 to 1, where 0 makes the night impossible to lose by a power outage, and 1 makes it impossible to win even without Animatronics.
	 * @throws IOException If Assets cannot be loaded from disk. Usually it's because they may be missing, so skill issue.
	 */
	public Night(String nightName, CameraMap mapAndAnimatronics, Jumpscare powerOutage, Random rng, float powerConsumption) throws IOException {
		this.rng = rng;
		this.nightName = nightName;
		this.camerasMap = mapAndAnimatronics;
		this.powerOutage = powerOutage;

		powerLeft = 1;
		// So this calculates depending on the fps, which determines the total number of ticks per night, which is
		// important to calibrate that the night cannot be survived by using everything (extremely easy)
		// but also that using nothing does not kill you (extremely hard). The objective is to find a balance
		int totalTicks = HOUR_INTERVAL * TOTAL_HOURS;
		float minPowerPerTickPerResource = 1.0f / totalTicks; // Minimum power consumption per tick. Lower values make the game impossible even with no Animatronics.
		float maxPowerPerTickPerResource = 1.0f / (4 * totalTicks); // Maximum power consumption. Higher values makes the game 100% consistent by closing both doors and not moving.
		powerPerTickPerResource = (minPowerPerTickPerResource + maxPowerPerTickPerResource) * powerConsumption;

		nightHour = 0; // Start at 12 AM = 00:00h
		backgroundImg = FNACResources.loadImageResource("office/background.jpg");
		camMonitorImg = FNACResources.loadImageResource("office/monitor.png");
		camMonitorStaticImg = FNACResources.loadImageResource("office/monitorStatic.png");
		camStaticImg = FNACResources.loadImageResource("office/camTrans.jpg");
		leftDoorOpenImg = FNACResources.loadImageResource("office/leftDoorOpen.png");
		leftDoorTransImg = FNACResources.loadImageResource("office/leftDoorTrans.png");
		leftDoorClosedImg = FNACResources.loadImageResource("office/leftDoorClosed.png");
		rightDoorOpenImg = FNACResources.loadImageResource("office/rightDoorOpen.png");
		rightDoorTransImg = FNACResources.loadImageResource("office/rightDoorTrans.png");
		rightDoorClosedImg = FNACResources.loadImageResource("office/rightDoorClosed.png");

		offTransTicks = 0;
		camsUpDownTransTicks = 0;
		changeCamsTransTicks = 0;
		camsUp = false;
		camsLocOnScreen = new HashMap<>(camerasMap.size());
		camsHidingMovement = new ArrayList<>();
		animPosInCam = new HashMap<>(5);
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

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				if (p.x < getWidth() * MOUSE_MOVE_THRESHOLD) {
					Action leftAction = getActionMap().get("leftAction");
					leftAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "leftAction"));
				} else if (p.x > getWidth() * (1 - MOUSE_MOVE_THRESHOLD)) {
					Action rightAction = getActionMap().get("rightAction");
					rightAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "rightAction"));
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Point click = e.getPoint();
				for (Camera cam : camerasMap.values()) {
					if (camsLocOnScreen.containsKey(cam.getName())) {
						Rectangle camLocScreen = camsLocOnScreen.get(cam.getName());
						if (camLocScreen.contains(click)) {
							camerasMap.setSelected(cam.getName());
							changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
							break;
						}
					}
				}
			}
		});

		nightTicks = new Timer("Night [" + nightName + "]");
	}

	public String getNightName() {
		return nightName;
	}

	public void startNight(){
		nightTicks.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Time never stops. Well sometimes it does, when dying for instance.
				currentTick++;
				if (currentTick % HOUR_INTERVAL == 0) {
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
					for(Camera cam : camerasMap.values()){
						for (Animatronic anim : cam.getAnimatronicsHere()){
							anim.updateIADuringNight(nightHour);
							if (currentTick % (int) Math.round(anim.getSecInterval() * FPS) == 0){
								if (anim.onMovementOpportunityAttempt(rng)){
									moves.put(anim, new AbstractMap.SimpleEntry<>(cam, camerasMap.get(anim.onMovementOppSuccess(camerasMap, cam, rng))));
								}
							}
							boolean openDoor = cam.isLeftDoorOfOffice()&&!leftDoorClosed ||cam.isRightDoorOfOffice()&&!rightDoorClosed;
							if (anim.onJumpscareAttempt(currentTick, camsUp, cam, openDoor, rng, FPS)){
								jumpscare = anim.getJumpscare();
								// In case I want phantom jumpscares in the future.
								jumpscare.reset();
							}
						}
					}
					for (Map.Entry<Animatronic, Map.Entry<Camera, Camera>> move : moves.entrySet()){
						try {
							move.getValue().getKey().move(move.getKey(), move.getValue().getValue());
							camsHidingMovement.add(move.getValue().getKey().getName());
							camsHidingMovement.add(move.getValue().getValue().getName());
							camsHidingMovementTicks = CAMS_STATIC_MOVE_TICKS;
							animPosInCam.remove(move.getKey().getName());
						} catch (AnimatronicException e){
							System.err.println("Prevented crash by cancelling move. Perhaps there is a design flaw in the Animatronic.");
							e.printStackTrace();
						}
					}
				}

				// We repaint da thing
				repaint();
			}
		}, 100, 1000 / FPS);
	}

	private void advanceTime() {
		if (++nightHour == TOTAL_HOURS) {
			nightTicks.cancel();
			jumpscare = null;
			victoryScreen = true;
			Timer end = new Timer("End Thread");
			end.schedule(new TimerTask() {
				@Override
				public void run() {
                    try {
                        onNightPassed();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
			}, 5000);
		}
	}

	protected abstract void onJumpscare();

	protected abstract void onNightPassed() throws IOException;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Most important thing, to fix things being weird depending on the size of this component.
		double scaleX = getWidth() / (double) OFFICEWIDTH;

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
		int leftDoorWidthScaled = (int) ((MONITOR_X-LEFTDOOR_X)*scaleX);
		int rightDoorWidthScaled = (int)((RIGHTDOOR_X-MONITOR_X)*scaleX);

		switch (officeLoc) {
		case LEFTDOOR:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(LEFTDOOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door when watching left door and no transition
				g.drawImage(leftDoor,
						0, 0,
						leftDoorWidthScaled, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(), this);

			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X - ((MONITOR_X - LEFTDOOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Left door when watching left door while transition from MONITOR to LEFTDOOR (center to left)
				double transitionProgress = (double) (OFFICE_TRANSITION_TICKS - offTransTicks) / OFFICE_TRANSITION_TICKS;
				int visibleDoorWidthScaled = (int) (leftDoorWidthScaled * transitionProgress);
				int doorImageStartX = leftDoor.getWidth() - (int) (leftDoor.getWidth() * transitionProgress);
				g.drawImage(leftDoor,
						0, 0,                                     // Screen position (always starts at the left of the screen)
						visibleDoorWidthScaled, getHeight(),      // Draw width matches the visible scaled portion
						doorImageStartX, 0,                       // Source image: start from the right side of the door image
						leftDoor.getWidth(), leftDoor.getHeight(), // Source image: end at the full width
						this);
			}
			break;

		case RIGHTDOOR:
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(RIGHTDOOR_X, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Right door when no transition at RIGHTDOOR
				g.drawImage(rightDoor,
						getWidth()-rightDoorWidthScaled, 0,
						getWidth(), getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(),
						this);

			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X + ((RIGHTDOOR_X - MONITOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Right door while moving from MONITOR to RIGHTDOOR
				double transitionProgress = (double) (OFFICE_TRANSITION_TICKS - offTransTicks) / OFFICE_TRANSITION_TICKS;
				int offsetX = (int) (rightDoorWidthScaled * transitionProgress);
				g.drawImage(rightDoor,
						getWidth() - offsetX, 0,
						getWidth() - offsetX + rightDoorWidthScaled, getHeight(),
						0, 0,
						rightDoor.getWidth(), rightDoor.getHeight(),
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
				double transitionProgress = (double) (OFFICE_TRANSITION_TICKS - offTransTicks) / OFFICE_TRANSITION_TICKS;
				int offsetX = (int) (leftDoorWidthScaled * transitionProgress);
				g.drawImage(leftDoor,
						-offsetX, 0,
						leftDoorWidthScaled - offsetX, getHeight(),
						0, 0,
						leftDoor.getWidth(), leftDoor.getHeight(),
						this);

			} else if (offTransFrom.equals(OfficeLocation.RIGHTDOOR)) {
				int xPosition = RIGHTDOOR_X - ((RIGHTDOOR_X - MONITOR_X) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Right door. PS: Idk why adding 1000 works on fullscreen, but it works, so I'l fix it with the fix below when it happens
				double transitionProgress = (double) (OFFICE_TRANSITION_TICKS - offTransTicks) / OFFICE_TRANSITION_TICKS;
				int offsetX = (int) (rightDoorWidthScaled * transitionProgress);
				g.drawImage(rightDoor,
						getWidth() - rightDoorWidthScaled + offsetX, 0,
						getWidth() + offsetX, getHeight(),
						0, 0,
						rightDoor.getWidth(), rightDoor.getHeight(),
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
			int camImgWidth = camerasMap.getSelectedCam().getCamBackground().getWidth();
			int camImgHeight = camerasMap.getSelectedCam().getCamBackground().getHeight();

			// Monitor's inner area for the camera view, from source
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
						Camera current = camerasMap.getSelectedCam();
						// On current camera, if an Animatronic moved from or to this camera recently, we show static as well
						if (camsHidingMovementTicks-->0 && camsHidingMovement.contains(current.getName())){
							g.drawImage(camStaticImg, camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
									0, 0, camImgWidth, camImgHeight, this);
						} else {
							// Here we draw the camera and the animatronics in there
							g.drawImage(current.getCamBackground(), camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
									0, 0, camImgWidth, camImgHeight, this);

							for (Animatronic an : current.getAnimatronicsHere()){
								boolean openDoor = current.isLeftDoorOfOffice()&&!leftDoorClosed || current.isRightDoorOfOffice()&&!rightDoorClosed;
								if (!an.hideFromCam(currentTick, openDoor, current, rng, FPS)){
									BufferedImage img = an.getCamImg();

									// Calculate scaling factors to fit the image inside camDrawWidth and camDrawHeight
									double anScaleX = camDrawWidth / (double) img.getWidth();
									double anScaleY = camDrawHeight / (double) img.getHeight();
									double anScale = Math.min(anScaleX, anScaleY)*0.3; // Ensure the image fits within both dimensions

									// Calculate the scaled width and height
									int scaledWidth = (int) (img.getWidth() * anScale);
									int scaledHeight = (int) (img.getHeight() * anScale);

									// Calculate a random position within the bounds, ensuring it doesn't overflow
									// Then, we determine if we reuse the last one generated or not depending on
									// whether it is still in-bounds (window resizing) or not.
									int anRandomX = camDrawX + rng.nextInt(camDrawWidth - scaledWidth);
									int anRandomY = camDrawY + rng.nextInt(camDrawHeight - scaledHeight);
									Point p = new Point(anRandomX, anRandomY);
									p = animPosInCam.getOrDefault(an.getName(), p);
									if (p.x<camDrawX || p.x > camDrawX+camDrawWidth-scaledWidth || p.y<camDrawY || p.y > camDrawY+camDrawHeight-scaledHeight){
										p = new Point(anRandomX, anRandomY);
									}
									animPosInCam.put(an.getName(), p);

									// Draw the scaled image
									g.drawImage(img,
											p.x, p.y,
											p.x + scaledWidth, p.y + scaledHeight,
											0, 0, img.getWidth(), img.getHeight(),
											this
									);
								}
							}
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
					double scaleRatioX = (double) scaledMapWidth / camerasMap.getMapImage().getWidth();
					double scaleRatioY = (double) scaledMapHeight / camerasMap.getMapImage().getHeight();

					// Calculate the position of the scaled map on the monitor
					int mapX = monitorXOffset + monitorTargetWidth - scaledMapWidth;
					int mapY = monitorYOffset + monitorTargetHeight - scaledMapHeight;

					// Draw the scaled map
					g.drawImage(camerasMap.getMapImage(), mapX, mapY, scaledMapWidth, scaledMapHeight, this);

                    //for (int i = 0; i < camerasMap.size(); i++) {
                    for (Camera cam : camerasMap.values()) {
						// Scale the current camera’s rectangle position
						Rectangle camMapRec = new Rectangle(cam.getMapLoc());
						int scaledCamMapRecX = mapX + (int) (camMapRec.x * scaleRatioX);
						int scaledCamMapRecY = mapY + (int) (camMapRec.y * scaleRatioY);
						int scaledCamMapRecWidth = (int) (camMapRec.width * scaleRatioX);
						int scaledCamMapRecHeight = (int) (camMapRec.height * scaleRatioY);

						// Draw rectangle
						if (cam.getName().equals(camerasMap.getSelectedName())){
							g.setColor(Color.LIGHT_GRAY);
						} else {
							g.setColor(Color.GRAY);
						}
						g.fillRoundRect(scaledCamMapRecX, scaledCamMapRecY, scaledCamMapRecWidth, scaledCamMapRecHeight, 5, 5);

						// Name of cam in map
						{
							String camName = cam.getName().toUpperCase();
							g.setColor(Color.BLACK);
							int marginX = scaledCamMapRecX/500;
							int marginY = scaledCamMapRecY/500;
							float fontSize = 25;
							Font font = new Font("Tahoma", Font.BOLD, (int) fontSize);
							FontMetrics metrics = g2d.getFontMetrics(font);
							Rectangle2D lineBounds = g2d.getFontMetrics(font).getStringBounds(camName, g);
							while (lineBounds.getWidth()+(marginX*2) >= scaledCamMapRecWidth || lineBounds.getHeight()+(marginY*2) >= scaledCamMapRecHeight) {
								font = font.deriveFont(--fontSize);
								metrics = g2d.getFontMetrics(font);
								lineBounds = metrics.getStringBounds(camName, g);
							}
							g.setFont(font);
							int camNameX = (int) (scaledCamMapRecX + lineBounds.getX() + marginX);
							int camNameY = (int) (scaledCamMapRecY + (scaledCamMapRecHeight - lineBounds.getHeight()) / 2 + metrics.getAscent());
							g2d.drawString(camName, camNameX, camNameY);
						}

						// We update the location of the minimap's cams so that we can check on click if it clicked a camera.
						camsLocOnScreen.put(cam.getName(), new Rectangle(scaledCamMapRecX, scaledCamMapRecY, scaledCamMapRecWidth, scaledCamMapRecHeight));
                    }

					// Cam name in top-left of monitor
					g.setFont(new Font("Times New Roman", Font.PLAIN, 30));
					g.setColor(Color.WHITE);
					g2d.drawString(camerasMap.getSelectedCam().getName(), camDrawX + 30, camDrawY + 40);
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
			int txtMarginX = getWidth()/100;
			int txtMarginY = getHeight()/1000;
            g.setFont(new Font("Arial", Font.BOLD, getWidth()/30));
            String strTime = String.format("%02d:%02d AM", nightHour, (int) (currentTick % HOUR_INTERVAL / (double) HOUR_INTERVAL * 60));
			FontMetrics fm = g.getFontMetrics();
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
				String strPower = String.format("Power: %.0f%% (Usage: %s)", (powerLeft*100), powerUsageStr);
				g2d.drawString(strPower, txtMarginX, getHeight() - fm.getLeading() - fm.getDescent() - txtMarginY);
			}
			{
				g.setColor(Color.WHITE);
				g2d.drawString(strTime, getWidth() - fm.stringWidth(strTime) - txtMarginX, fm.getHeight() + txtMarginY);
			}

        } else {
            g.setFont(new Font("Arial", Font.BOLD, Math.min(getWidth(), getHeight())/5));
			FontMetrics fm = g.getFontMetrics();
			String text = victoryScreen ? "06:00 AM" : "YOU DIED";
			Color color = victoryScreen ? Color.GREEN : Color.RED;

			int textWidth = fm.stringWidth(text);
			int textHeight = fm.getAscent();
			int centerX = (getWidth() - textWidth) / 2;
			int centerY = (getHeight() + textHeight) / 2;

			g.setColor(color);
			g2d.drawString(text, centerX, centerY);
        }

		if (jumpscare != null && camsUpDownTransTicks == 0) {
			if (camsUp){
				Action closeCamsAction = getActionMap().get("camsAction");
				if (closeCamsAction != null) {
					closeCamsAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
				}
			} else {
				g.drawImage(jumpscare.getCurrentFrame(), 0, 0, getWidth(), getHeight(), this);
				jumpscare.update();
				if (jumpscare.isFinished()) {
					nightTicks.cancel();
					victoryScreen = false;
					Timer end = new Timer("End Thread");
					end.schedule(new TimerTask() {

						@Override
						public void run() {
							onJumpscare();
						}
					}, 5000);
				}
			}
		}
    }

	private class LeftAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (camsUpDownTransTicks == 0 && offTransTicks == 0 && changeCamsTransTicks==0 && victoryScreen==null) {
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
				}
				// After moving from an "ordered" list towards selecting by name, it may be best to remove cam switch with keys.
//				else {
//					if (camerasMap.getSelected() > 0) {
//						camerasMap.setSelected(camerasMap.getSelected()-1);
//					} else {
//						camerasMap.setSelected(camerasMap.size()-1);
//					}
//					changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
//				}
			}
		}

	}

	private class RightAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (camsUpDownTransTicks == 0 && offTransTicks == 0 && changeCamsTransTicks==0 && victoryScreen==null) {
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
				}
				// After moving from an "ordered" list towards selecting by name, it may be best to remove cam switch with keys.
//				else {
//					if (camerasMap.getSelected() < camerasMap.size()-1) {
//						camerasMap.setSelected(camerasMap.getSelected()+1);
//					} else {
//						camerasMap.setSelected(0);
//					}
//					changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
//				}
			}
		}
	}

	private class CamsAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && victoryScreen==null){
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
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && !camsUp && victoryScreen==null){
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
