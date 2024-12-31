package es.cristichi.fnac.gui;

import es.cristichi.fnac.Main;
import es.cristichi.fnac.cnight.CustomNightMenuJC;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.NightProgress;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.JumpscareVisualSetting;
import es.cristichi.fnac.obj.nights.NightFactory;
import es.cristichi.fnac.obj.nights.NightRegistry;
import kuusisto.tinysound.TinySound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Main window, it controls all the thingies.
 */
public class NightsJF extends JFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(NightsJF.class);
    
    public final String GAME_TITLE = "Five Nights at Cris'";
    private final JPanel cardPanel;
    private final SettingsJC settingsPanel;
    private final JPanel nightPanel;
    private final MenuJC mainMenu;
    private final Jumpscare powerOutage;

    private final NightProgress.SaveFile saveFile;
    private final CardLayout cardLayout;
    private Settings settings;

    /**
     * Creates a new window for the game, with a Main Menu and a Custom Night Menu.
     * @param saveFile Save file. This will be modified and saved accordingly when the player completes any Night.
     * @param settings Player's personalized settings.
     * @throws ResourceException If an error occurs when loading any resource.
     */
    public NightsJF(NightProgress.SaveFile saveFile, Settings settings) throws ResourceException {
        super();
        this.saveFile = saveFile;
        this.settings = settings;

        setTitle(getTitleForWindow(null));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(Resources.loadImageResource("icon.jpg"));
        setTitle(getTitleForWindow(null));
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);

        nightPanel = new JPanel(new BorderLayout());
        cardPanel.add(nightPanel, "night");

        MenuJC.Info menuInfo = getUpdatedMenuData();

        powerOutage = new Jumpscare("office/powerOutage.gif", 0, null, -1, JumpscareVisualSetting.STRETCHED);
        mainMenu = new MenuJC(menuInfo.background(), "menu/loading.jpg", menuInfo.menuItems()) {
            @Override
            protected void onMenuItemClick(Item item) throws IOException {
                switch (item.id()) {
                    case "custom" -> {
                        CustomNightMenuJC customNightMenu = new CustomNightMenuJC(settings, powerOutage, NightsJF.this);
                        customNightMenu.addOnExitListener(() -> cardLayout.show(cardPanel, "menu"));
                        cardPanel.add(customNightMenu, "customNightMenu");
                        cardLayout.show(cardPanel, "customNightMenu");
                    }
                    case "settings" -> cardLayout.show(cardPanel, "settings");
                    case "exit" -> {
                        dispose();
                        System.exit(0);
                    }
                    default -> {
                        try {
                            startNightFromFactory(Objects.requireNonNull(NightRegistry.getNight(0)));
                        } catch (NullPointerException e){
                            LOGGER.error("Error trying to load Night: It does not exist.", e);
                        }
                    }
                }
            }
        };
        cardPanel.add(mainMenu, "menu");
        cardLayout.show(cardPanel, "menu");

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

        setMinimumSize(new Dimension(200, 50));
    }

    /**
     * @param subtitle Subtitle. Something like "Night 1" or "Settings menu", or null for nothing.
     * @return A String of the form "{@link NightsJF#GAME_TITLE} - <code>subtitle</code>", or just the game title if
     * there is no subtitle.
     */
    public String getTitleForWindow(@Nullable String subtitle) {
        if (subtitle == null){
            return GAME_TITLE;
        }
        return String.format("%s - %s", GAME_TITLE, subtitle);
    }

    /**
     * @param set <code>true</code> to set window to Fullscreen. <code>false</code> to maximize the window without
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
     * @throws ResourceException If any of the loading screens could not be loaded.
     */
    private @NotNull MenuJC.Info getUpdatedMenuData() throws ResourceException {
        ArrayList<MenuJC.Item> mmItems = new ArrayList<>(2);
        String background;
        List<String> completed = saveFile.completedNights();
        int numCompleted = completed.size();
        
        background = switch (numCompleted) {
            case 0 -> "menu/background0.jpg";
            case 1 -> "menu/background1.jpg";
            case 2 -> "menu/background2.jpg";
            case 3 -> "menu/background3.jpg";
            case 4 -> "menu/background4.jpg";
            case 5 -> "menu/background5.jpg";
            case 6 -> "menu/background6.jpg";
            default -> "menu/backgroundCustom.jpg";
        };
        if (Main.DEBUG) {
            mmItems.add(new MenuJC.Item("custom", "Play with Us!", "Custom Night", "night/custom/loading.jpg"));
            for (NightFactory nightFactory : NightRegistry.getAllNights()){
                mmItems.add(nightFactory.getItem());
            }
        } else {
            NightFactory nightFactory = NightRegistry.getNight(numCompleted);
            if (nightFactory == null) {
                mmItems.add(new MenuJC.Item("custom", "Play with Us!", "Custom Night", "night/custom/loading.jpg"));
            } else {
                mmItems.add(nightFactory.getItem());
            }
        }
        mmItems.add(new MenuJC.Item("settings", "Settings", "Settings", null));
        mmItems.add(new MenuJC.Item("exit", "Run Away", "I'm Sorry", null));
        return new MenuJC.Info(mmItems, background);
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
            NightJC night = nightFactory.createNight(settings, powerOutage, rng);
            night.addOnNightEnd((completed) -> {
                if (completed) {
                    saveFile.addCompletedNight(night.getNightName());
                    try {
                        saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                        MenuJC.Info menuInfo = getUpdatedMenuData();
                        mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                        mainMenu.updateMenuItems(menuInfo.menuItems());
                    } catch (IOException e) {
                        new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e),
                                true, false);
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
            new ExceptionDialog(new NightException("Error creating Night.", e), false, false);
        } catch (NightException e) {
            new ExceptionDialog(e, false, false);
        }
    }
    
    /**
     * Starts a Custom Night, making sure that the menu music is handled and the player goes back to the main menu
     * after dying. I mean after surely completing the Night successfully without incidents.
     * @param night Custom Night to start.
     */
    public void startCustomNight(NightJC night) {
        night.addOnNightEnd((completed) -> {
            if (completed){
                saveFile.addCompletedNight(night.getNightName());
                try {
                    saveFile.saveToFile(NightProgress.SAVE_FILE_NAME);
                    MenuJC.Info menuInfo = getUpdatedMenuData();
                    mainMenu.updateBackground(Resources.loadImageResource(menuInfo.background()));
                    mainMenu.updateMenuItems(menuInfo.menuItems());
                } catch (IOException e) {
                    new ExceptionDialog(new IOException("Progress could not be saved due to an error.", e), true, false);
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
        mainMenu.stopMusic();
        night.startNight();
    }
}
