package es.cristichi.fnac.gui;

import es.cristichi.fnac.exception.AnimatronicException;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.GifFrame;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.AmbientSound;
import es.cristichi.fnac.obj.AmbientSoundSystem;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.OfficeLocation;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;
import kuusisto.tinysound.Sound;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Timer;
import java.util.*;

/**
 * JComponent that runs a Night. This is the most important part of the gameplay.
 */
public class NightJC extends ExitableJComponent {
	/** DEBUG_MODE enables seeing where all AnimatronicDrawings are in the Camera's minimap.
	 * Quite useful for debugging a new AnimatronicDrawing's movement. Or to have everything under control in test
	 * runs if you are bad at the game like me. */
	@SuppressWarnings("CanBeFinal") //DEBUG_MODE is to be modified by mods that depend on this code during development.
    public static boolean DEBUG_MODE = false;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NightJC.class);

	/** Frames per second, used to convert from in-game ticks to seconds and vice-versa. */
	private final int fps;
	/** Objective hour. Reaching this hour results in a win. */
	private static final int TOTAL_HOURS = 6;

	/** Night identifier, used to save the Night after completion and also for the window title. */
	private final String nightName;
	/** It keeps track of whether this Night was already started when starting it, to throw an error and
	 * avoid issues. */
	private boolean nightStarted = false;
	/** RNG for the Night. All randomness must use this object exclusively. */
	private final Random rng;
	/**
	 * How many ticks must happen for an in-game hour to pass. It must be calculated from FPS to translate to the
	 * configured IRL seconds.
	 */
	private final int hourTicksInterval;
	/**
	 * Amount of power left, where 1 is 100% and exactly 0 is a power outage.
	 */
	private float powerLeft;
	/**
	 * This represents the amount of Power lost each second passively, and is multiplied for each resource in use
	 * (Cameras, left door and right door)
	 */
	private final float powerPerTickPerResource;
	/** Jumpscare to play when the Player runs out of Power. */
	private final Jumpscare powerOutageJumpscare;
	
	/**
	 * An {@link AmbientSoundSystem} object that checks for the ambient Sounds to play during the Night at times.
	 */
	private final AmbientSoundSystem ambientSounds;

    /**
     * List of Runnables that must be executed when the Night is finished, either win or lose. It carries the
     * information of whether the player won or not.
     */
	private final LinkedList<NightEndedListener> onNightEndListeners;
	/**
     * List of Runnables that must be executed when the Night is finished, either win or lose. It does not
     * carry the information of whether the player won or not.
     */
    private final LinkedList<Runnable> onExitListeners;
	/** Victory Sound. */
	private final Sound soundOnCompleted;

	/** Keeps track of the exact tick, the first tick of the Night being tick 1. */
	private int currentTick;
	/** Keeps track of the current hour, starting at 0 representing 00:00h. */
	private int currentHour;
	/** Thread that runs on a loop that controls everything that happens on the game, tick by tick. */
	private final Timer nightTicks;

	/** Boolean object that is null when the game is running:
	 * <br>{@code true} when Night is won
	 * <br>{@code false} when Jumpscared. */
	private Boolean victoryScreen;

	/** Background image showing the office! */
	private final BufferedImage backgroundImg;
	/** In the source image of the background, the X for where the left-side of the screen is while at LEFTDOOR. */
	private static final int LEFTDOOR_X_IN_SOURCE = 200;
	/** In the source image of the background, the X for where the left-side of the screen is while at MONITOR. */
	private static final int MONITOR_X_IN_SOURCE = 1000;
	/** In the source image of the background, the X for where the left-side of the screen is while at RIGHTDOOR. */
	private static final int RIGHTDOOR_X_IN_SOURCE = 1800;
	/** Width of the source image taken on each frame. Everything else works around the fact that
	 * this is exactly 2000. Modifying this results in de-centered and stretch things, but it won't crash. */
	private static final int OFFICEWIDTH_OF_SOURCE = 2000;

	/** Image of a paper with information for the night guard. */
	private final BufferedImage paperImg;
	/** In the source image of the background, the X for the top-left Point where the paper must be drawn (if any). */
	private static final int PAPER_X_IN_BACKGROUND_SOURCE = 2405;
	/** In the source image of the background, the Y for the top-left Point where the paper must be drawn (if any). */
	private static final int PAPER_Y_IN_BACKGROUND_SOURCE = 595;
	/** Width the paper must adjust to, on the screen. Height is calculated from this and the source image's values. */
	private static final int PAPER_WIDTH = 246;
	
	/** Honk! */
	private final Sound honkSound;
	/** The hitbox of whatever does the funny sound in relation to the background's source image. */
	private static final Ellipse2D HONK_IN_BACKGROUND_SOURCE = new Ellipse2D.Double(1728,295,36,30);
	/** Where the honk's clickable location was last drawn on the screen. */
	private Ellipse2D honkBtnOnScreen;

	/** Current view of the player. */
	private OfficeLocation officeLoc;
	/** Ticks the "camera" takes to move between the 3 views of your office. */
	private final int OFFICE_TRANSITION_TICKS;
	/** Ticks left for the player to finish a "moving" transition. If 0, player is not moving around at the moment. */
	private int offTransTicks;
	/** During transitions, previous view of the player so that we know how to calculate what they are currently seeing. */
	private OfficeLocation offTransFrom;

	/** Used for mouse control of movement. It indicates the percentage of the screen (on each side) that detects the
	 * mouse to make the movement. */
	private final float MOUSE_MOVE_THRESHOLD = 0.05f;

	/** Image of the frame of the Camera monitor. */
	private final BufferedImage camMonitorImg;
	/** Image showing static, for the Camera to use when not displaying during transitions or Animatronic's movement. */
	private final BufferedImage camStaticImg;
	/** Image showing both the monitor and static, for convenience. */
	private final BufferedImage camMonitorStaticImg;

	/** It controls whether Cameras are up or not. */
	private boolean camsUp;
	/** Usual number of ticks it takes the player to start and stop watching Cameras. */
	private final int CAMS_UPDOWN_TRANSITION_TICKS;
	/** Ticks left until Cameras are either fully up or fully down. */
	private int camsUpDownTransTicks;

	/** Image showing the area that detects the mouse in order to open/close Cams. */
	private final BufferedImage camsUpDownBtnImg;
	/** On screen Rectangle where the camsUp/Down was last drawn. */
	private Rectangle camsUpDownBtnOnScreen;
	/** Usual number of ticks without showing the Cams Up/Down button. */
	private final int CAMS_UPDOWN_BTN_DELAY_TICKS = 25;
	/** Ticks left until Cams Up/Down button must be shown. */
	private int camsUpDownBtnDelayTicks;

	/** name of Camera -> Rectangle where that Camera was last drawn on the map.<br>
	 * This is used for the mouse clicks to know if there is a clickable Camera where the mouse clicked.
	 * Each frame, after the map is drawn, this is updated. */
	private final HashMap<String, Rectangle> camsLocOnMapOnScreen;

	// Change cams
	/** Usual number of ticks it takes the player to start watching another Camera after they clicked on one. */
	private final int CHANGE_CAMS_TRANSITION_TICKS;
	/** Ticks left until Cameras are visible again after switching Camera. */
	private int changeCamsTransTicks;
	/** Map of all Cameras, including their Animatronics. */
	private final CameraMap camerasMap;

	// Animatronics moving around cams makes static
	/** Usual number of ticks it takes for Cameras to be visible again after an Animatronic moved from or to them. */
	private final int CAMS_STATIC_MOVE_TICKS;
	/**
	 * Camera name -> Ticks left until this Camera is visible again after Animatronic move from or to this Camera.<br>
	 * This is used so that Animatronics don't simply "pop" from and to existence,
	 * instead the view is hidden for {@link NightJC#CAMS_STATIC_MOVE_TICKS} ticks. <br>
	 */
	private final HashMap<String, Integer> camsHidingMovementTicks;
	/** Animatronic name -> Point on this JComponent where Animatronic was last drawn on this Camera.<br>
	 * This is used so that they are not randomly moving around each tick. If the position becomes impossible after
	 * window resizing, a new Point is calculated randomly.
	 */
	private final HashMap<String, Point> animPosInCam;

	// Doors
	/** Usual number of ticks it takes for doors to fully open or close. */
	private final int DOOR_TRANSITION_TICKS;
	/** Marks the next tick to play the door transition sound (either open or close depending on whether the door
	 * of the current side is open or closed. */
	private boolean playDoorTransSound;
	/** Whether the left door is effectively closed. */
	private boolean leftDoorClosed;
	/** Ticks left until left door is visually opened or closed. */
	private int leftDoorTransTicks;
	/** Image of the left door closed. */
	private final BufferedImage leftDoorClosedImg;
	/** Image of the left door half closed/open. */
	private final BufferedImage leftDoorTransImg;
	/** Image of the left door open. */
	private final BufferedImage leftDoorOpenImg;
	
	/** Whether the right door is effectively closed. */
	private boolean rightDoorClosed;
	/** Ticks left until right door is visually opened or closed. */
	private int rightDoorTransTicks;
	/** Image of the right door closed. */
	private final BufferedImage rightDoorClosedImg;
	/** Image of the right door half closed/open. */
	private final BufferedImage rightDoorTransImg;
	/** Image of the right door open. */
	private final BufferedImage rightDoorOpenImg;
	
	/** Position no the screen where the button of the left or right door was last drawn.
	 *  Only one object is needed since both buttons cannot be visible at the same time. */
	private Ellipse2D doorBtnOnScreen;
	/** Percentage of the X coordinate in the source image of the top-left corner of the left door's button's hitbox. */
	private static final float LEFT_DOOR_BTN_X = .038f;
	/** Percentage of the Y coordinate in the source image of the top-left corner of the left door's button's hitbox. */
	private static final float LEFT_DOOR_BTN_Y = .49f;
	/** Percentage of the width in the source image of the top-left corner of the left door's button's hitbox. */
	private static final float LEFT_DOOR_BTN_W = .155f;
	/** Percentage of the height in the source image of the top-left corner of the left door's button's hitbox. */
	private static final float LEFT_DOOR_BTN_H = .13f;
	/** Percentage of the X coordinate in the source image of the top-left corner of the right door's button's hitbox. */
	private static final float RIGHT_DOOR_BTN_X = .813f;
	/** Percentage of the Y coordinate in the source image of the top-left corner of the right door's button's hitbox. */
	private static final float RIGHT_DOOR_BTN_Y = .49f;
	/** Percentage of the width in the source image of the top-left corner of the right door's button's hitbox. */
	private static final float RIGHT_DOOR_BTN_W = .149f;
	/** Percentage of the height in the source image of the top-left corner of the right door's button's hitbox. */
	private static final float RIGHT_DOOR_BTN_H = .13f;

	/** Active Jumpscare, or null if player is still alive or won. */
	private Jumpscare jumpscare;
	/** For controlling how load Jumpscares are. */
	private static final float JUMPSCARE_SOUND_MODIFIER = 1.5f;
	
	/** Volume for both the Sound when opening/closing cams and the static Sound while watching cams. */
	private final double camSoundsVolume = 0.3;
	/** Sound to play when cams are opened. */
	private final Sound openedCamsSound;
	/** Static Sound that repeats while watching cams. */
	private final Sound backgroundCamsSound;
	/** Sound to play when cams are closed. */
	private final Sound closeCamsSound;
	/** Sound to play when another camera is closed while watching cams. */
	private final Sound clickCamSound;
	
	/** Volume of the door Sounds when opening and closing. */
	private final double doorsSoundsVolume = 0.8;
	/** Sound to play when a door are opened. */
	private final Sound openDoorSound;
	/** Sound to play when a door are closed. */
	private final Sound closeDoorSound;

	/**
	 * This method loads all the necessary Resources from disk.
	 * @param nightName Name of the Night. Barely used.
	 * @param fps FPS for the Night. This is used as a target FPS and also to convert seconds to FPS.
	 * @param camMap Map of the place. Animatronics present in the Night must start inside
	 *                              their starting Cameras, they are only stored there.
	 * @param paperImgPath Path to the Image that has the paper to put at the office. The paper should be
	 *                     2480x4193px, or keep that same aspect ratio to be printed correctly.
	 * @param secsPerHour Number of seconds per in-game hour. It significantly affects difficulty, as
	 *                    longer Nights will allow the Animatronics more movements per Night, as well as
	 *                    the human aspect of the difficulty like keeping concentration.
	 *                    The recommended baseline value is 90 seconds per hour.
	 * @param rng Random for the night. Use {@code new Random()} unless you want a specific seed.
	 * @param powerOutageJumpscare Jumpscare that will happen when the player runs out of power.
	 * @param passivePowerUsage A float from 0 to 1, where 0 makes the night impossible to lose by
	 *                             a power outage (even if you have both doors closed at all time),
	 *                             and 1 makes it impossible to win even without Animatronics.
	 *                             It must be kept in mind that Cameras also use power.
	 * @param soundOnNightCompletedPath Path to the sound played when Night is completed. Can be null for dev
	 *                                     purposes but having one is encouraged.
	 * @throws ResourceException If any of the resources required for Nights cannot be loaded from the disk.
     * @throws NightException If the Night is not properly set.
	 */
	public NightJC(String nightName, int fps, CameraMap camMap, @Nullable String paperImgPath,
				   Jumpscare powerOutageJumpscare, Random rng, double secsPerHour,
				   float passivePowerUsage, @Nullable String soundOnNightCompletedPath) throws ResourceException, NightException {
		super();
		LOGGER.debug("Loading {} with FPS {}, {} seconds per hour, and passivePowerUsage equal to {}%.",
				nightName, fps, secsPerHour, passivePowerUsage*100);
		this.rng = rng;
		this.fps = fps;
		this.nightName = nightName;
		this.camerasMap = camMap;
		this.powerOutageJumpscare = powerOutageJumpscare;
		this.hourTicksInterval = (int) (this.fps * secsPerHour);
		if (hourTicksInterval == 0){
			throw new NightException("Duration of Night is so low that there are 0 ticks per hour, leading to errors.");
		}

		onNightEndListeners = new LinkedList<>();
		onExitListeners = new LinkedList<>();

		powerLeft = 1;
		// So this is calculated depending on the FPS, which determines the total number of ticks per night, which is
		// important to calibrate that the Night cannot be survived by keeping both doors closed (extremely easy)
		// but also that using nothing does not kill you (extremely hard). The objective is to find a balance.
		int totalTicks = hourTicksInterval * TOTAL_HOURS;
		// Minimum power consumption per tick. Lower values make the game impossible even with no Animatronics.
		float minPowerPerTickPerResource = 1.0f / totalTicks;
		// Maximum power consumption. Higher values makes the game 100% consistent by closing both doors and not moving.
		// Its "4" counts for passive+leftDoor+rightDoor+camsUp
		float maxPowerPerTickPerResource = 1.0f / (4 * totalTicks);
		// Current power used per tick per resource as the given percentage in-between the minimum and maximum.
		powerPerTickPerResource = (minPowerPerTickPerResource + maxPowerPerTickPerResource) * passivePowerUsage;

		currentHour = 0; // Start at 12 AM = 00:00h. Luckily 0h = 0, pog
		backgroundImg = Resources.loadImageResource("office/background.jpg");
		if (paperImgPath == null){
			paperImg = null;
		} else {
			paperImg = Resources.loadImageResource(paperImgPath);
		}
		camsUpDownBtnImg = Resources.loadImageResource("office/camsButton.png");
		camMonitorImg = Resources.loadImageResource("office/monitor.png");
		camMonitorStaticImg = Resources.loadImageResource("office/monitorStatic.png");
		camStaticImg = Resources.loadImageResource("office/camTrans.jpg");
		leftDoorOpenImg = Resources.loadImageResource("office/leftDoorOpen.png");
		leftDoorTransImg = Resources.loadImageResource("office/leftDoorTrans.png");
		leftDoorClosedImg = Resources.loadImageResource("office/leftDoorClosed.png");
		rightDoorOpenImg = Resources.loadImageResource("office/rightDoorOpen.png");
		rightDoorTransImg = Resources.loadImageResource("office/rightDoorTrans.png");
		rightDoorClosedImg = Resources.loadImageResource("office/rightDoorClosed.png");

		this.soundOnCompleted = Resources.loadSound(soundOnNightCompletedPath, "nightPassed.wav");
		ambientSounds = new AmbientSoundSystem((int) (this.fps *3.115), 0.3f,
				new AmbientSound(Resources.loadSound("office/ambient/weird1.wav", "weird1.wav"), 1, true),
				new AmbientSound(Resources.loadSound("office/ambient/waterLeak.wav", "waterLeak.wav"), 3, true),
				new AmbientSound(Resources.loadSound("office/ambient/fakeSteps1.wav", "fakeSteps.wav"), 1, true),
				new AmbientSound(Resources.loadSound("office/ambient/deep-breath-247459.wav", "amBreath.wav"), 4, true)
		);
		openedCamsSound = Resources.loadSound("office/sounds/radio-static-6382.wav", "openCams.wav");
		honkSound = Resources.loadSound("office/sounds/honk.wav", "funnyHonk.wav");
		backgroundCamsSound = Resources.loadSound("office/sounds/radio-static-6382-cut.wav", "keepCams.wav");
		openedCamsSound.addOnEndListener(() -> backgroundCamsSound.play(camSoundsVolume));
		addOnExitListener(openedCamsSound::stop);
		backgroundCamsSound.addOnEndListener(() -> backgroundCamsSound.play(camSoundsVolume));
		addOnExitListener(backgroundCamsSound::stop);
		closeCamsSound = Resources.loadSound("office/sounds/tv-off-91795.wav", "closeCams.wav");
		clickCamSound = Resources.loadSound("office/sounds/spacebar-click-keyboard-199448.wav", "clickCams.wav");

		openDoorSound = Resources.loadSound("office/sounds/opening-metal-door-199581.wav", "openDoor.wav");
		closeDoorSound = Resources.loadSound("office/sounds/metal-door-slam-172172.wav", "closeDoor.wav");

		offTransTicks = 0;
		camsUpDownTransTicks = 0;
		camsUpDownBtnDelayTicks = 0;
		changeCamsTransTicks = 0;
		camsUp = false;
		camsUpDownBtnOnScreen = null;
		camsLocOnMapOnScreen = new HashMap<>(camerasMap.size());
		camsHidingMovementTicks = new HashMap<>(camerasMap.size());
		animPosInCam = new HashMap<>(5);
		leftDoorClosed = false;
		rightDoorClosed = false;
		victoryScreen = null;
		jumpscare = null;

		officeLoc = OfficeLocation.MONITOR;

		DOOR_TRANSITION_TICKS = fps/6;
		playDoorTransSound = false;
		CHANGE_CAMS_TRANSITION_TICKS = fps/12;
		CAMS_UPDOWN_TRANSITION_TICKS = fps/2;
		OFFICE_TRANSITION_TICKS = fps/2;
		CAMS_STATIC_MOVE_TICKS = fps/3;

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

        AbstractAction action = new DoorAction();

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Q"), "doorAction");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("E"), "doorAction");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "doorAction");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), "doorAction");
        getActionMap().put("doorAction", action);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Point click = e.getPoint();
				if (camsUp){
					for (Camera cam : camerasMap.values()) {
						if (camsLocOnMapOnScreen.containsKey(cam.getNameId())) {
							Rectangle camLocScreen = camsLocOnMapOnScreen.get(cam.getNameId());
							if (camLocScreen.contains(click)) {
								camerasMap.setSelected(cam.getNameId());
								changeCamsTransTicks = CHANGE_CAMS_TRANSITION_TICKS;
								clickCamSound.play(camSoundsVolume);
								break;
							}
						}
					}
				} else if (camsUpDownTransTicks == 0 && doorBtnOnScreen != null && doorBtnOnScreen.contains(click)){
					Action camsAction = getActionMap().get("doorAction");
					camsAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "doorAction"));
				} else if (honkBtnOnScreen != null && honkBtnOnScreen.contains(click)){
					honkSound.play();
				}
			}
		});

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
				} else if (camsUpDownBtnOnScreen != null && camsUpDownBtnOnScreen.contains(p)){
					Action camsAction = getActionMap().get("camsAction");
					camsAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "camsAction"));
				}
			}
		});

		nightTicks = new Timer("Night [" + nightName + "]");
		
		LOGGER.debug("Finished loading {}.", nightName);
	}

	/**
	 * @return Name of the Night. It should be unique to this Night.
	 */
	public String getNightName() {
		return nightName;
	}

	/**
	 * Make the Night start. It should never be called twice, even after a Night is finished.
	 */
	public void startNight(){
		if (nightStarted){
			LOGGER.error("Nights should never be started twice.");
			return;
		}
		LOGGER.debug("Started {}.", nightName);
		nightStarted = true;
		nightTicks.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Time never stops. Well sometimes it does, when dying for instance.
				currentTick++;
				if (currentTick % hourTicksInterval == 0) {
					currentHour++;
					if (currentHour == TOTAL_HOURS) {
						jumpscare = null;
						victoryScreen = true;
						nightTicks.cancel();
						soundOnCompleted.addOnEndListener(() -> {
								for(NightEndedListener onCompleted : onNightEndListeners){
									onCompleted.run(true);
								}
								for(Runnable onExit : onExitListeners){
									onExit.run();
								}
							});
						soundOnCompleted.addOnEndListener(soundOnCompleted::unload);
						soundOnCompleted.play();
					}
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
					jumpscare = powerOutageJumpscare;
					jumpscare.addOnFinishedListener(() -> {
                        for(NightEndedListener onCompleted : onNightEndListeners){
                            onCompleted.run(false);
                        }
                        for(Runnable onExit : onExitListeners){
                            onExit.run();
                        }
					});
				}

				// Animatronic movements and their jumpscare opportunities
				if (jumpscare == null){
					HashMap<AnimatronicDrawing, Map.Entry<Camera, AnimatronicDrawing.MoveSuccessInfo>> moves = new HashMap<>(5);
					for(Camera cam : camerasMap.values()){
						for (AnimatronicDrawing anim : cam.getAnimatronicsHere()){
							anim.updateIADuringNight(currentHour);
							boolean openDoor = cam.isLeftDoor() && !leftDoorClosed
												|| cam.isRightDoor() && !rightDoorClosed;

							AnimatronicDrawing.AnimTickInfo animTickInfo =
									anim.onTick(currentTick, fps, camsUp, openDoor, cam, rng);
							if (animTickInfo.moveOpp()){
								AnimatronicDrawing.MoveOppInfo moveOppInfo = anim.onMoveOppAttempt(cam,
										(camsUp && cam.equals(camerasMap.getSelectedCam())), camsUp, openDoor, rng);
								if (moveOppInfo.move()){
									try {
										AnimatronicDrawing.MoveSuccessInfo moveOpp = anim.onMoveOppSuccess(
												camerasMap, cam, rng);
										if (moveOpp.moveToCam() != null && !moveOpp.moveToCam().equals(cam.getNameId())) {
											moves.put(anim, new AbstractMap.SimpleEntry<>(cam, moveOpp));
										}
									} catch (AnimatronicException e){
										System.err.println("Animatronic needs refinement. This should be avoided.");
										e.printStackTrace();
									}
								}
								if (moveOppInfo.sound() != null){
									cam.playSoundHere(moveOppInfo.sound());
								}
							}
							if (animTickInfo.jumpscare() != null){
								jumpscare = animTickInfo.jumpscare();
								jumpscare.addOnFinishedListener(() -> {
									for(NightEndedListener onCompleted : onNightEndListeners){
										onCompleted.run(false);
									}
                                    for(Runnable onExit : onExitListeners){
                                        onExit.run();
                                    }
								});
							}
							if (animTickInfo.sound() != null){
								cam.playSoundHere(animTickInfo.sound());
							}
						}
					}
					for (AnimatronicDrawing anim : moves.keySet()){
						Map.Entry<Camera, AnimatronicDrawing.MoveSuccessInfo> move = moves.get(anim);

						Camera fromCam = move.getKey();
						Camera toCam = camerasMap.get(move.getValue().moveToCam());

						if (!fromCam.equals(toCam)){
							try {
								fromCam.move(anim, toCam);
								if (move.getValue().moveSound() != null){
									toCam.playSoundHere(move.getValue().moveSound());
								}
								animPosInCam.remove(anim.getNameId());
								camsHidingMovementTicks.put(fromCam.getNameId(), CAMS_STATIC_MOVE_TICKS);
								camsHidingMovementTicks.put(toCam.getNameId(), CAMS_STATIC_MOVE_TICKS);
							} catch (Exception e){
								System.err.printf("Prevented crash by cancelling move of Animatronic %s from %s to %s." +
										" Perhaps there is a design flaw in the Animatronic.%n",
										anim.getNameId(), fromCam, toCam);
								e.printStackTrace();
							}
						}
					}
				}

				// Ambient sounds
				ambientSounds.attemptRandomSound(rng, currentTick, camerasMap);

				// Door sounds
				switch (officeLoc){
					case LEFTDOOR -> {
						if (leftDoorTransTicks == 1 && leftDoorClosed){
							closeDoorSound.play(doorsSoundsVolume, -0.7);
						} else if (playDoorTransSound && !leftDoorClosed){
							openDoorSound.play(doorsSoundsVolume, -0.7);
						}
						playDoorTransSound = false;
					}
					case RIGHTDOOR -> {
						if (rightDoorTransTicks == 1 && rightDoorClosed){
							closeDoorSound.play(doorsSoundsVolume, 0.7);
						} else if (playDoorTransSound && !rightDoorClosed){
							openDoorSound.play(doorsSoundsVolume, 0.7);
						}
						playDoorTransSound = false;
					}
					case MONITOR -> {}
				}

				// We repaint da thing
				repaint();
			}
		}, 100, 1000 / fps);
	}
	
	/**
	 * Adds a Runnable to run when the Night ends, with information of whether the Night was successful or not.
	 * @param runnable Code to run, with information of whether the player passed the Night.
	 */
	public void addOnNightEnd(NightEndedListener runnable) {
		onNightEndListeners.add(runnable);
	}

	@Override
	public void addOnExitListener(Runnable onExitListener) {
		onExitListeners.add(onExitListener);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Most important thing, to fix things being weird depending on the size of this component.
		double scaleX = getWidth() / (double) OFFICEWIDTH_OF_SOURCE;
		double scaleY = getHeight() / (double) backgroundImg.getHeight();

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
		int leftDoorWidthScaled = (int) ((MONITOR_X_IN_SOURCE - LEFTDOOR_X_IN_SOURCE)*scaleX);
		int rightDoorWidthScaled = (int)((RIGHTDOOR_X_IN_SOURCE - MONITOR_X_IN_SOURCE)*scaleX);

		switch (officeLoc) {
		case LEFTDOOR:
			if (offTransFrom == null) {
				g.drawImage(
						backgroundImg.getSubimage(LEFTDOOR_X_IN_SOURCE, 0, OFFICEWIDTH_OF_SOURCE, backgroundImg.getHeight()),
						0, 0, getWidth(), getHeight(), this);
				honkBtnOnScreen = new Ellipse2D.Double((HONK_IN_BACKGROUND_SOURCE.getX()-LEFTDOOR_X_IN_SOURCE)*scaleX,
														HONK_IN_BACKGROUND_SOURCE.getY()*scaleY,
														HONK_IN_BACKGROUND_SOURCE.getWidth()*scaleX,
														HONK_IN_BACKGROUND_SOURCE.getHeight()*scaleY);

				// Left door when watching left door and no transition
				g.drawImage(leftDoor, 0, 0, leftDoorWidthScaled, getHeight(),
						0,0, leftDoor.getWidth(), leftDoor.getHeight(), this);
				doorBtnOnScreen = new Ellipse2D.Double(leftDoor.getWidth()*0.5, leftDoor.getHeight()*0.5,
						50, 50);
				doorBtnOnScreen = new Ellipse2D.Double(
						leftDoorWidthScaled*LEFT_DOOR_BTN_X, getHeight()*LEFT_DOOR_BTN_Y,
						leftDoorWidthScaled*LEFT_DOOR_BTN_W, getHeight()*LEFT_DOOR_BTN_H);

			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X_IN_SOURCE -
						((MONITOR_X_IN_SOURCE - LEFTDOOR_X_IN_SOURCE) * (OFFICE_TRANSITION_TICKS - offTransTicks))
								/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH_OF_SOURCE, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Paper
				if (paperImg != null){
					g.drawImage(paperImg,
							(int)((PAPER_X_IN_BACKGROUND_SOURCE -xPosition)*scaleX), (int)(PAPER_Y_IN_BACKGROUND_SOURCE *scaleY),
							(int)(PAPER_WIDTH*scaleX), (int)(paperImg.getHeight() * ((double) PAPER_WIDTH /paperImg.getWidth()) * scaleY),
							this);
				}

				// Left door while transition from MONITOR to LEFTDOOR (center to left)
				double transitionProgress = (double) (OFFICE_TRANSITION_TICKS - offTransTicks) / OFFICE_TRANSITION_TICKS;
				int visibleDoorWidthScaled = (int) (leftDoorWidthScaled * transitionProgress);
				int offsetX = leftDoor.getWidth() - (int) (leftDoor.getWidth() * transitionProgress);
				g.drawImage(leftDoor, 0, 0, visibleDoorWidthScaled, getHeight(),
						offsetX, 0, leftDoor.getWidth(), leftDoor.getHeight(), this);
				
				doorBtnOnScreen = null;
				honkBtnOnScreen = null;
			}
			break;

		case RIGHTDOOR:
			honkBtnOnScreen = null;
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(RIGHTDOOR_X_IN_SOURCE, 0, OFFICEWIDTH_OF_SOURCE, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Paper
				if (paperImg != null){
					g.drawImage(paperImg,
							(int)((PAPER_X_IN_BACKGROUND_SOURCE -RIGHTDOOR_X_IN_SOURCE)*scaleX), (int)(PAPER_Y_IN_BACKGROUND_SOURCE *scaleY),
							(int)(PAPER_WIDTH*scaleX), (int)(paperImg.getHeight()*((double) PAPER_WIDTH /paperImg.getWidth()) * scaleY),
							this);
				}

				// Right door when no transition at RIGHTDOOR
				g.drawImage(rightDoor, getWidth()-rightDoorWidthScaled, 0, getWidth(), getHeight(),
						0, 0, rightDoor.getWidth(), rightDoor.getHeight(), this);
				doorBtnOnScreen = new Ellipse2D.Double(
						getWidth()-rightDoorWidthScaled+rightDoorWidthScaled*RIGHT_DOOR_BTN_X, getHeight()*RIGHT_DOOR_BTN_Y,
						rightDoorWidthScaled*RIGHT_DOOR_BTN_W, getHeight()*RIGHT_DOOR_BTN_H);

			} else if (offTransFrom.equals(OfficeLocation.MONITOR)) {
				int xPosition = MONITOR_X_IN_SOURCE +
						((RIGHTDOOR_X_IN_SOURCE - MONITOR_X_IN_SOURCE) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH_OF_SOURCE, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Paper
				if (paperImg != null){
					g.drawImage(paperImg, (int)((PAPER_X_IN_BACKGROUND_SOURCE -xPosition)*scaleX), (int)(PAPER_Y_IN_BACKGROUND_SOURCE *scaleY),
							(int)(PAPER_WIDTH*scaleX), (int)(paperImg.getHeight()*((double) PAPER_WIDTH /paperImg.getWidth()) * scaleY), this);
				}

				// Right door while moving from MONITOR to RIGHTDOOR
				double transitionProgress = (double) (OFFICE_TRANSITION_TICKS - offTransTicks) / OFFICE_TRANSITION_TICKS;
				int offsetX = (int) (rightDoorWidthScaled * transitionProgress);
				g.drawImage(rightDoor,
						getWidth() - offsetX, 0,
						getWidth() - offsetX + rightDoorWidthScaled, getHeight(),
						0, 0,
						rightDoor.getWidth(), rightDoor.getHeight(),
						this);
				
				doorBtnOnScreen = null;
			}
			break;
		default:
			doorBtnOnScreen = null;
			if (offTransFrom == null) {
				g.drawImage(backgroundImg.getSubimage(MONITOR_X_IN_SOURCE, 0, OFFICEWIDTH_OF_SOURCE, backgroundImg.getHeight()),
						0, 0, getWidth(), getHeight(), this);
				
				honkBtnOnScreen = new Ellipse2D.Double((HONK_IN_BACKGROUND_SOURCE.getX()-MONITOR_X_IN_SOURCE)*scaleX,
														HONK_IN_BACKGROUND_SOURCE.getY()*scaleY,
														HONK_IN_BACKGROUND_SOURCE.getWidth()*scaleX,
														HONK_IN_BACKGROUND_SOURCE.getHeight()*scaleY);
				
				// Paper
				if (paperImg != null){
					g.drawImage(paperImg,
							(int)((PAPER_X_IN_BACKGROUND_SOURCE -MONITOR_X_IN_SOURCE)*scaleX), (int)(PAPER_Y_IN_BACKGROUND_SOURCE *scaleY),
							(int)(PAPER_WIDTH*scaleX), (int)(paperImg.getHeight()*((double) PAPER_WIDTH /paperImg.getWidth()) * scaleY),
							this);
				}
			} else if (offTransFrom.equals(OfficeLocation.LEFTDOOR)) {
				int xPosition = LEFTDOOR_X_IN_SOURCE + ((MONITOR_X_IN_SOURCE - LEFTDOOR_X_IN_SOURCE) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH_OF_SOURCE, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Paper
				if (paperImg != null){
					g.drawImage(paperImg,
							(int)((PAPER_X_IN_BACKGROUND_SOURCE -xPosition)*scaleX), (int)(PAPER_Y_IN_BACKGROUND_SOURCE *scaleY),
							(int)(PAPER_WIDTH*scaleX), (int)(paperImg.getHeight()*((double) PAPER_WIDTH /paperImg.getWidth()) * scaleY),
							this);
				}

				// Left door
				double transitionProgress = (double) (OFFICE_TRANSITION_TICKS - offTransTicks) / OFFICE_TRANSITION_TICKS;
				int offsetX = (int) (leftDoorWidthScaled * transitionProgress);
				g.drawImage(leftDoor, -offsetX, 0, leftDoorWidthScaled - offsetX, getHeight(),
						0, 0, leftDoor.getWidth(), leftDoor.getHeight(), this);
				doorBtnOnScreen = new Ellipse2D.Double(offsetX+leftDoor.getWidth()*0.5, leftDoor.getHeight()*0.5,
						50, 50);
				
				honkBtnOnScreen = null;
			} else if (offTransFrom.equals(OfficeLocation.RIGHTDOOR)) {
				int xPosition = RIGHTDOOR_X_IN_SOURCE -
						((RIGHTDOOR_X_IN_SOURCE - MONITOR_X_IN_SOURCE) * (OFFICE_TRANSITION_TICKS - offTransTicks))
						/ OFFICE_TRANSITION_TICKS;
				g.drawImage(backgroundImg.getSubimage(xPosition, 0, OFFICEWIDTH_OF_SOURCE, backgroundImg.getHeight()), 0, 0,
						getWidth(), getHeight(), this);

				// Paper
				if (paperImg != null){
					g.drawImage(paperImg,
							(int)((PAPER_X_IN_BACKGROUND_SOURCE -xPosition)*scaleX), (int)(PAPER_Y_IN_BACKGROUND_SOURCE *scaleY),
							(int)(PAPER_WIDTH*scaleX), (int)(paperImg.getHeight()*((double) PAPER_WIDTH /paperImg.getWidth()) * scaleY),
							this);
				}

				// Right door.
				double transitionProgress = (double) (OFFICE_TRANSITION_TICKS - offTransTicks) / OFFICE_TRANSITION_TICKS;
				int offsetX = (int) (rightDoorWidthScaled * transitionProgress);
				g.drawImage(rightDoor, getWidth() - rightDoorWidthScaled + offsetX, 0,
						getWidth() + offsetX, getHeight(), 0, 0,
						rightDoor.getWidth(), rightDoor.getHeight(), this);
				
				honkBtnOnScreen = null;
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
					double transitionScale = (double) (CAMS_UPDOWN_TRANSITION_TICKS - camsUpDownTransTicks) / CAMS_UPDOWN_TRANSITION_TICKS;
					int scaledMonitorWidth = (int) (monitorTargetWidth * transitionScale);
					int scaledMonitorHeight = (int) (monitorTargetHeight * transitionScale);
					int transitionXOffset = (windowWidth - scaledMonitorWidth) / 2;
					int transitionYOffset = (windowHeight - scaledMonitorHeight) / 2;

					g.drawImage(camMonitorStaticImg, transitionXOffset, transitionYOffset, transitionXOffset + scaledMonitorWidth,
							transitionYOffset + scaledMonitorHeight, 0, 0, monitorWidth, monitorHeight, this);

					camsUpDownTransTicks--;

				// Watching cams
				} else {
					// Draw static in transition of camera change
					if (changeCamsTransTicks>0){
						g.drawImage(camStaticImg, camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
								0, 0, camImgWidth, camImgHeight, this);
						changeCamsTransTicks--;
					} else {
						Camera current = camerasMap.getSelectedCam();
						// On current camera, if an Animatronic moved from or to this camera recently, we show static instead
						// Also if Camera is broken
						int currentCamHidingTicks = camsHidingMovementTicks.getOrDefault(current.getNameId(), 0);
						if (currentCamHidingTicks>0 || current.isBroken()){
							g.drawImage(camStaticImg, camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
									0, 0, camImgWidth, camImgHeight, this);
							if (currentCamHidingTicks>0) {
								camsHidingMovementTicks.put(current.getNameId(), currentCamHidingTicks - 1);
							}
						} else {
							// Here we draw the camera and the animatronics in there if no static is drawn
							g.drawImage(current.getCamBackground(),
									camDrawX, camDrawY, camDrawX + camDrawWidth, camDrawY + camDrawHeight,
									0, 0, camImgWidth, camImgHeight, this);

							for (AnimatronicDrawing an : current.getAnimatronicsHere()){
								boolean openDoor = current.isLeftDoor()&&!leftDoorClosed
										|| current.isRightDoor()&&!rightDoorClosed;
								AnimatronicDrawing.ShowOnCamInfo info = an.showOnCam(currentTick, fps, openDoor, current, rng);
								BufferedImage anCamImg = info.camImg();
								if (anCamImg != null){
									// Calculate scaling factors to fit the image inside camDrawWidth and camDrawHeight
									double anScaleX = camDrawWidth / (double) anCamImg.getWidth();
									double anScaleY = camDrawHeight / (double) anCamImg.getHeight();
									double anScale = Math.min(anScaleX, anScaleY)*0.3; // Ensure the image fits within both dimensions

									// Calculate the scaled width and height
									int scaledWidth = (int) (anCamImg.getWidth() * anScale);
									int scaledHeight = (int) (anCamImg.getHeight() * anScale);

									// Calculate a random position within the bounds, ensuring it doesn't overflow
									// Then, we determine if we reuse the last one generated or not depending on
									// whether it is still in-bounds (window resizing) or not.
									Point p;
									if (info.preferredPoint() != null) {
										p = new Point(
											(int) (camDrawX + info.preferredPoint().x * (camDrawWidth - scaledWidth)),
											(int) (camDrawY + info.preferredPoint().y * (camDrawHeight - scaledHeight)));
									} else if (animPosInCam.containsKey(an.getNameId())) {
										p = animPosInCam.get(an.getNameId());
									} else {
										int anRandomX = camDrawX + rng.nextInt(camDrawWidth - scaledWidth);
										int anRandomY = camDrawY + rng.nextInt(camDrawHeight - scaledHeight);
										p = new Point(anRandomX, anRandomY);
									}

									if (p.x < camDrawX || p.x > camDrawX + camDrawWidth - scaledWidth
											|| p.y < camDrawY || p.y > camDrawY + camDrawHeight - scaledHeight) {
										int anRandomX = camDrawX + rng.nextInt(camDrawWidth - scaledWidth);
										int anRandomY = camDrawY + rng.nextInt(camDrawHeight - scaledHeight);
										p = new Point(anRandomX, anRandomY);
									}
									animPosInCam.put(an.getNameId(), p);

									// Draw the scaled image
									g.drawImage(anCamImg, p.x, p.y,
											p.x + scaledWidth, p.y + scaledHeight,
											0, 0, anCamImg.getWidth(), anCamImg.getHeight(),
											this);
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
						if (!cam.isInvisible()){
							// Scale the current camera’s rectangle position
							Rectangle camMapRec = new Rectangle(cam.getMapLoc());
							int scaledCamMapRecX = mapX + (int) (camMapRec.x * scaleRatioX);
							int scaledCamMapRecY = mapY + (int) (camMapRec.y * scaleRatioY);
							int scaledCamMapRecWidth = (int) (camMapRec.width * scaleRatioX);
							int scaledCamMapRecHeight = (int) (camMapRec.height * scaleRatioY);

							// Draw rectangle
							if (cam.equals(camerasMap.getSelectedCam())){
								if (cam.isBroken()){
									g.setColor(Color.PINK);
								} else {
									g.setColor(Color.LIGHT_GRAY);
								}
							} else if (cam.isBroken()){
								g.setColor(Color.RED);
							} else {
								g.setColor(Color.GRAY);
							}
							g.fillRoundRect(scaledCamMapRecX, scaledCamMapRecY, scaledCamMapRecWidth, scaledCamMapRecHeight, 5, 5);

							// Name of cam in map
                            String camName = cam.getNameId().toUpperCase();
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

                            if (DEBUG_MODE){
								int debugRecDim = Math.min(scaledCamMapRecWidth, scaledCamMapRecHeight) / 3;
								int debugRecX = scaledCamMapRecX;
								int debugRecY = scaledCamMapRecY;
								for (AnimatronicDrawing anim : cam.getAnimatronicsHere()) {
									g.setColor(anim.getDebugColor());
									g.fillRect(debugRecX, debugRecY, debugRecDim, debugRecDim);
									debugRecX += debugRecDim;
									debugRecY += debugRecDim;
								}
							}

							// We update the location of the minimap's cams so that we can check on click if it clicked a camera.
							camsLocOnMapOnScreen.put(cam.getNameId(),
									new Rectangle(scaledCamMapRecX, scaledCamMapRecY, scaledCamMapRecWidth, scaledCamMapRecHeight));
						} else {
							// If Camera is now invisible but it was previously visible, we remove this to make sure it's not clickable
							camsLocOnMapOnScreen.remove(cam.getNameId());
						}
                    }

					// Cam name in top-left of monitor
					g.setFont(new Font("Times New Roman", Font.PLAIN, 30));
					g.setColor(Color.WHITE);
					g2d.drawString(camerasMap.getSelectedCam().getNameId(), camDrawX + 30, camDrawY + 40);
				}
			} else {
				// Transition cams down
				if (camsUpDownTransTicks > 0) {
					double transitionScale = (double) camsUpDownTransTicks / CAMS_UPDOWN_TRANSITION_TICKS;
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

		// Cams button
		if (offTransTicks == 0 && camsUpDownTransTicks == 0 && victoryScreen == null && jumpscare == null){
			if (camsUpDownBtnDelayTicks > 0){
				camsUpDownBtnDelayTicks--;
				camsUpDownBtnOnScreen = null;
			} else {
				camsUpDownBtnOnScreen = new Rectangle((int) (getWidth()*0.2), (int) (getHeight()*0.9),
                        (int) (getWidth()*0.4), (int) (getHeight()*0.1));
				g.drawImage(camsUpDownBtnImg, camsUpDownBtnOnScreen.x, camsUpDownBtnOnScreen.y,
						camsUpDownBtnOnScreen.width,  camsUpDownBtnOnScreen.height, this);
			}
		}

        if (victoryScreen == null) {
			int txtMarginX = getWidth()/100;
			int txtMarginY = getHeight()/1000;
			g.setFont(new Font("Eraser Dust", Font.BOLD, getWidth()/30));
			FontMetrics fontMetrics = g.getFontMetrics();

			String strTime = String.format("%02d:%02d AM", currentHour,
					(int) (currentTick % hourTicksInterval / (double) hourTicksInterval * 60));
			g.setColor(Color.WHITE);
			g2d.drawString(strTime, getWidth() - fontMetrics.stringWidth(strTime) - txtMarginX, fontMetrics.getHeight() + txtMarginY);

			g.setFont(new Font("Arial", Font.BOLD, getWidth()/30));
			fontMetrics = g.getFontMetrics();
            int powerUsage = 0;
            if (camsUp) {
                powerUsage++;
            }
            if (leftDoorClosed) {
                powerUsage++;
            }
            if (rightDoorClosed) {
                powerUsage++;
            }
            switch (powerUsage) {
                case 0 -> g.setColor(Color.GREEN);
                case 1 -> g.setColor(Color.ORANGE);
                case 2 -> g.setColor(Color.RED.brighter());
                default -> g.setColor(Color.RED.darker());
            }
            String strPower1 = String.format(Locale.US, "%.0f%%", (powerLeft*100));
            g2d.drawString(strPower1,
                    txtMarginX, getHeight() - fontMetrics.getLeading() - fontMetrics.getDescent() - txtMarginY);
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

		if (jumpscare != null) {
			if (camsUp && camsUpDownTransTicks == 0 && jumpscare.shouldCamsBeDown()){
				Action closeCamsAction = getActionMap().get("camsAction");
				if (closeCamsAction instanceof CamsAction camsAction) {
					camsAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "camsAction"));
				}
			} else {
				List<GifFrame> frames = jumpscare.updateAndGetFrame(currentTick, fps);
				if (jumpscare.isFrameToPlaySound()) {
					jumpscare.getSound(true).play(JUMPSCARE_SOUND_MODIFIER);
				}

				// Full dimensions of the final full-sized frame
				int fullWidth = jumpscare.getFullWidth();
				int fullHeight = jumpscare.getFullHeight();

				// Calculate scaling factor for the full frame
				double scaleJumpX = (double) getWidth() / fullWidth;
				double scaleJumpY = (double) getHeight() / fullHeight;
				double scale = switch (jumpscare.getVisualSetting()){
					case CENTERED -> Math.min(scaleJumpX, scaleJumpY);
					case STRETCHED -> Math.max(scaleJumpX, scaleJumpY);
				};

				// Calculate the size of the scaled full frame
				int scaledFullWidth = (int) (fullWidth * scale);
				int scaledFullHeight = (int) (fullHeight * scale);

				// Calculate the top-left corner for the full frame
				int fullDrawX = (getWidth() - scaledFullWidth) / 2;
				int fullDrawY = (getHeight() - scaledFullHeight) / 2;

				// Draw all frames of the jumpscare
				for (GifFrame frame : frames) {
					// Frame's original size
					int frameWidth = frame.image().getWidth();
					int frameHeight = frame.image().getHeight();

					// Frame's offset relative to the full image
					int frameOffsetX = frame.offsetX();
					int frameOffsetY = frame.offsetY();

					switch (jumpscare.getVisualSetting()){
						case CENTERED -> {
							// Standard logic: center frames with offsets and scaling
							int scaledFrameWidth = (int) (frameWidth * scale);
							int scaledFrameHeight = (int) (frameHeight * scale);
							int scaledOffsetX = (int) (frameOffsetX * scale);
							int scaledOffsetY = (int) (frameOffsetY * scale);

							// Position the frame relative to the scaled full frame
							int frameDrawX = fullDrawX + scaledOffsetX;
							int frameDrawY = fullDrawY + scaledOffsetY;

							// Draw the frame without stretching it
							g.drawImage(frame.image(), frameDrawX, frameDrawY, frameDrawX + scaledFrameWidth, frameDrawY + scaledFrameHeight,
									0, 0, frameWidth, frameHeight, this);
						}
						case STRETCHED -> {
							// Stretching logic: scale entire frame to fill the screen
							int stretchFrameWidth = (int) (frameWidth * scaleX);
							int stretchFrameHeight = (int) (frameHeight * scaleY);
							int stretchDrawX = (int) (frameOffsetX * scaleX);
							int stretchDrawY = (int) (frameOffsetY * scaleY);

							g.drawImage(frame.image(), stretchDrawX, stretchDrawY, stretchDrawX + stretchFrameWidth, stretchDrawY + stretchFrameHeight,
									0, 0, frameWidth, frameHeight, this);
						}
					}
				}

				if (jumpscare.isFramesFinished()) {
					nightTicks.cancel();
					victoryScreen = false;
				}
			}
		}
	}
	
	/**
	 * Listener that runs when the Nights end, and carries the information of whether the Night was a success or not
	 * for the player.
	 */
	public interface NightEndedListener {
		/**
		 * @param completed {@code true} if player won, {@code false} otherwise.
		 */
		void run(boolean completed);
	}
	
	/**
	 * Move to the left.
	 */
    private class LeftAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (camsUpDownTransTicks == 0 && offTransTicks == 0
					&& changeCamsTransTicks==0 && victoryScreen==null && jumpscare == null) {
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
			}
		}

	}
	
	/**
	 * Move to the right.
	 */
	private class RightAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (camsUpDownTransTicks == 0 && offTransTicks == 0
					&& changeCamsTransTicks==0 && victoryScreen==null && jumpscare == null) {
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
			}
		}
	}
	
	/**
	 * Open/close the Camera monitor.
	 */
	private class CamsAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0 && victoryScreen==null){
				if (camsUp) {
					camsUpDownTransTicks = CAMS_UPDOWN_TRANSITION_TICKS;
					camsUpDownBtnDelayTicks = CAMS_UPDOWN_BTN_DELAY_TICKS;
					camsUp = false;
					backgroundCamsSound.stop();
					openedCamsSound.stop();
					closeCamsSound.play(camSoundsVolume);
				} else if (jumpscare == null){
					camsUpDownTransTicks = CAMS_UPDOWN_TRANSITION_TICKS;
					camsUpDownBtnDelayTicks = CAMS_UPDOWN_BTN_DELAY_TICKS;
					camsUp = true;
					openedCamsSound.play(camSoundsVolume);
				}
			}
		}
	}
	
	/**
	 * Open/close current door on screen.
	 */
	private class DoorAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (offTransTicks == 0 && camsUpDownTransTicks == 0
					&& !camsUp && victoryScreen==null && jumpscare == null){
				if (rightDoorTransTicks==0 && officeLoc.equals(OfficeLocation.RIGHTDOOR)) {
					rightDoorClosed = !rightDoorClosed;
					rightDoorTransTicks = DOOR_TRANSITION_TICKS;
					playDoorTransSound = true;
				} else if (leftDoorTransTicks==0 && officeLoc.equals(OfficeLocation.LEFTDOOR)) {
					leftDoorClosed = !leftDoorClosed;
					leftDoorTransTicks = DOOR_TRANSITION_TICKS;
					playDoorTransSound = true;
				}
			}
		}
	}
}
