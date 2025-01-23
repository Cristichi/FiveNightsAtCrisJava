package es.cristichi.fnac.gui;

import es.cristichi.fnac.FnacMain;
import es.cristichi.fnac.anim.Jumpscare;
import es.cristichi.fnac.cnight.CustomNightMenuJC;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.nights.NightFactory;
import es.cristichi.fnac.nights.NightRegistry;
import kuusisto.tinysound.TinySound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Main window, it controls all the thingies.
 */
public class NightsJF extends JFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(NightsJF.class);
    
    /**
     * Card Panel that holds what can be on screen at any time (main menu, submenu, or NightJC).
     */
    private final JPanel cardPanel;
    /**
     * Card layout that controls which panel inside {@link #cardPanel} is on screen.
     */
    private final CardLayout cardLayout;
    /**
     * Settings panel that allows the player to customize settings.
     */
    private SettingsJC settingsPanel;
    /**
     * Panel where the NightJC is.
     */
    private final JPanel nightPanel;
    /**
     * Main menu panel.
     */
    private MenuJC mainMenu;
    
    /**
     * Save file currently in use.
     */
    private NightProgress.SaveFile saveFile;
    /**
     * Player's settings.
     */
    private Settings settings;
    
    private MenuJC.Item customNightItem;

    /**
     * Creates a new window for the game, with a Main Menu and a Custom Night Menu.
     * @throws ResourceException If an error occurs when loading any resource.
     */
    public NightsJF() throws ResourceException {
        super();
        setTitle(getTitleForWindow(null));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(Resources.loadImage("icon.jpg"));
        setTitle(getTitleForWindow(null));
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);

        nightPanel = new JPanel(new BorderLayout());
        cardPanel.add(nightPanel, "night");
        
        setMinimumSize(new Dimension(200, 50));
    }
    
    /**
     * Starts the starting sequence, which shows some information while the game loads.
     * @param semaphore Semaphore to release when the animation is done. {@code null} to not use.
     * @param settings Player's personalized settings.
     * @throws ResourceException If any resources could not be loaded.
     */
    public void startStartingSequence(@Nullable Semaphore semaphore, Settings settings) throws ResourceException {
        this.settings = settings;
        try {
            setFullScreen(settings.isFullscreen());
            settingsPanel = new SettingsJC(settings) {
                @Override
                public void onSettingsSaved(Settings saved) {
                    if (NightsJF.this.settings.isFullscreen() != saved.isFullscreen()){
                        NightsJF.this.setFullScreen(saved.isFullscreen());
                    }
                    TinySound.setGlobalVolume(saved.getVolume());
                    NightsJF.this.settings = new Settings(saved);
                }
                
                @Override
                public void onReturnToMenu() {
                    cardLayout.show(cardPanel, "menu");
                }
            };
            cardPanel.add(settingsPanel, "settings");
            
            final BufferedImage backgroundImg = Resources.loadImage("startup/loading1.jpg");
            final BufferedImage aGameByImg = Resources.loadImage("startup/loading2.png");
            final BufferedImage myNameImg = Resources.loadImage("startup/loading3.png");
            final float[] trans = new float[]{0,0};
            final float step = 0.1f;
            final boolean[] released = {false};
            JPanel panel = new JPanel(){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (g instanceof Graphics2D g2d){
                        g2d.drawImage(backgroundImg, 0,0, getWidth(), getHeight(), this);
                        if (trans[0] > 0f){
                            g2d.setComposite(AlphaComposite.SrcOver.derive(trans[0]));
                            g2d.drawImage(aGameByImg, 0,0, getWidth(), getHeight(), this);
                        }
                        if (trans[1] > 0f){
                            g2d.setComposite(AlphaComposite.SrcOver.derive(trans[1]));
                            g2d.drawImage(myNameImg, 0,0, getWidth(), getHeight(), this);
                        }
                    }
                }
            };
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (trans[0] < 1){
                        trans[0] += step;
                        if (trans[0] > 1){
                            trans[0] = 1;
                        }
                    } else if (trans[1] < 1){
                        trans[1] += step;
                        if (trans[1] > 1){
                            trans[1] = 1;
                        }
                    } else {
                        if (!released[0] && semaphore != null){
                            released[0] = true;
                        }
                        cancel();
                    }
                    panel.repaint();
                }
            }, 100, 100);
            
            cardPanel.add("starting", panel);
            cardLayout.show(cardPanel, "starting");
            setVisible(true);
        } catch (ResourceException e){
            LOGGER.error("Error during starting sequence.", e);
            throw e;
        } catch (Exception e){
            LOGGER.error("Error during starting sequence.", e);
        }
    }
    
    /**
     * Starts the menu.
     * @param saveFile Save file. This will be modified and saved accordingly when the player completes any Night.
     * @throws ResourceException If any resources could not load.
     */
    public void startMenuAndGame(NightProgress.SaveFile saveFile) throws ResourceException {
        this.saveFile = saveFile;
        MenuJC.Info menuInfo = getUpdatedMenuData();
        
        mainMenu = new MenuJC(menuInfo, Resources.loadImage("menu/loading.jpg"), Resources.loadMusic("menu/main.wav"));
        cardPanel.add(mainMenu, "menu");
        cardLayout.show(cardPanel, "menu");
    }

    /**
     * @param subtitle Subtitle. Something like "Night 1" or "Settings menu", or null for nothing.
     * @return A String of the form "{@link FnacMain#GAME_TITLE} - {@code subtitle}", or just the game title if
     * there is no subtitle.
     */
    public String getTitleForWindow(@Nullable String subtitle) {
        if (subtitle == null){
            return FnacMain.GAME_TITLE;
        }
        return String.format("%s - %s", FnacMain.GAME_TITLE, subtitle);
    }

    /**
     * @param set {@code true} to set window to Fullscreen. {@code false} to maximize the window without
     *            Fullscreen.
     */
    public void setFullScreen(boolean set) {
        Rectangle windowBounds = getBounds();

        GraphicsEnvironment graphicsEnvironment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] graphicsDevices =
                graphicsEnvironment.getScreenDevices();

        GraphicsDevice best = null;
        int bestArea = 0;
        for (GraphicsDevice graphicsDevice : graphicsDevices) {
            GraphicsConfiguration[] graphicsConfigurations =
                    graphicsDevice.getConfigurations();

            for (GraphicsConfiguration graphicsConfiguration : graphicsConfigurations) {
                Rectangle graphicsBounds =
                        graphicsConfiguration.getBounds();

                Rectangle intersection = windowBounds.intersection(graphicsBounds);

                int intersectionArea = intersection.width * intersection.height;
                if (intersectionArea > bestArea) {
                    best = graphicsDevice;
                    bestArea = intersectionArea;
                }
            }
        }

        dispose();
        setUndecorated(set);
        if (set) {
            if (best != null && best.isFullScreenSupported()) {
                best.setFullScreenWindow(this);
            } else {
                setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }
        } else {
            if (best != null) {
                best.setFullScreenWindow(null);
            }
            setBounds(windowBounds);
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }

        setVisible(true);
    }

    /**
     * Simply a method that updates the Menu, useful for when the player unlocked a new Night
     * after completing one.
     * @return A {@link MenuJC.Info} object with the information needed to build a Menu (items, background, etc).
     */
    private @NotNull MenuJC.Info getUpdatedMenuData() throws ResourceException {
        ArrayList<MenuJC.Item> mmItems = new ArrayList<>(2);
        if (customNightItem == null){
            customNightItem = new MenuJC.Item(new MenuJC.ItemInfo("custom", "Play with Us!", "Custom Night",
                    Resources.loadImage("night/custom/loading.jpg")), () -> {
                try {
                    CustomNightMenuJC customNightMenu = new CustomNightMenuJC(settings,
                            Jumpscare.getPowerOutageJumpscare(), NightsJF.this);
                    customNightMenu.addOnExitListener(() -> cardLayout.show(cardPanel, "menu"));
                    cardPanel.add(customNightMenu, "customNightMenu");
                    cardLayout.show(cardPanel, "customNightMenu");
                } catch (ResourceException e) {
                    new ExceptionDialog(e, false, true, LOGGER);
                }
            });
        }
        String backgrResPath;
        List<String> completed = saveFile.completedNights();
        int numCompleted = completed.size();
        
        backgrResPath = switch (numCompleted) {
            case 0 -> "menu/background0.jpg";
            case 1 -> "menu/background1.jpg";
            case 2 -> "menu/background2.jpg";
            case 3 -> "menu/background3.jpg";
            case 4 -> "menu/background4.jpg";
            case 5 -> "menu/background5.jpg";
            case 6 -> "menu/background6.jpg";
            default -> "menu/backgroundCustom.jpg";
        };
        if (FnacMain.DEBUG_ALLNIGHTS) {
            mmItems.add(customNightItem);
            for (NightFactory nightFactory : NightRegistry.getAllNights().values()){
                mmItems.add(new MenuJC.Item(nightFactory.getItem(), () -> {
                    try {
                        startNightFromFactory(nightFactory);
                    } catch (Exception e){
                        new ExceptionDialog(e, false, true, LOGGER);
                    }
                }));
            }
        } else {
            NightFactory nightFactory = NightRegistry.getNight(numCompleted);
            if (nightFactory == null) {
                mmItems.add(customNightItem);
            } else {
                mmItems.add(new MenuJC.Item(nightFactory.getItem(), () -> {
                    try {
                        startNightFromFactory(nightFactory);
                    } catch (Exception e){
                        new ExceptionDialog(e, false, true, LOGGER);
                    }
                }));
            }
        }
        mmItems.add(new MenuJC.Item(new MenuJC.ItemInfo("settings", "Settings", "Settings", null),
                () -> cardLayout.show(cardPanel, "settings")));
        mmItems.add(new MenuJC.Item(new MenuJC.ItemInfo("exit", "Run Away", "I'm Sorry", null),
                () -> System.exit(1)));
        return new MenuJC.Info(mmItems, Resources.loadImage(backgrResPath));
    }
    
    /**
     * Starts a Night from its Factory. It makes sure to attach everything needed so that the main menu is handled
     * when the Night finishes.
     * @param nightFactory Factory that can create the desired NightJC to play.
     */
    private void startNightFromFactory(NightFactory nightFactory){
        long seed = new Random().nextLong();
        Random rng = new Random(seed);
        try {
            NightJC night = nightFactory.createNight(settings, Jumpscare.getPowerOutageJumpscare(), rng);
            night.addOnNightEnd((completed) -> {
                if (completed) {
                    saveFile.addCompletedNight(night.getNightName());
                    try {
                        saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                        MenuJC.Info menuInfo = getUpdatedMenuData();
                        mainMenu.updateBackground(menuInfo.background());
                        mainMenu.updateMenuItems(menuInfo.menuItems());
                    } catch (IOException e) {
                        new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e),
                                true, false, LOGGER);
                    }
                }
                cardLayout.show(cardPanel, "menu");
                nightPanel.remove(night);
                nightPanel.removeAll();
                nightPanel.revalidate();
                mainMenu.startMusic();
            });
            nightPanel.add(night);
            setTitle(getTitleForWindow(night.getNightName()));
            cardLayout.show(cardPanel, "night");
            LOGGER.info("Today's {} is using the seed \"{}\". Have fun!", night.getNightName(), seed);
            mainMenu.stopMusic();
            night.startNight();
        } catch (IOException e) {
            new ExceptionDialog(new NightException("Error creating Night.", e), false, false, LOGGER);
        } catch (NightException e) {
            new ExceptionDialog(e, false, false, LOGGER);
        }
    }
    
    /**
     * Starts a Custom Night, making sure that the menu music is handled and the player goes back to the main menu
     * after dying. I mean after surely completing the Night successfully without incidents.
     * @param night Custom Night to start.
     */
    public void startCustomNight(NightJC night) {
        night.addOnNightEnd((completed) -> {
            cardLayout.show(cardPanel, "menu");
            nightPanel.remove(night);
            nightPanel.removeAll();
            nightPanel.revalidate();
            mainMenu.startMusic();
        });
        nightPanel.add(night);
        setTitle(getTitleForWindow(night.getNightName()));
        cardLayout.show(cardPanel, "night");
        mainMenu.stopMusic();
        night.startNight();
    }
}
