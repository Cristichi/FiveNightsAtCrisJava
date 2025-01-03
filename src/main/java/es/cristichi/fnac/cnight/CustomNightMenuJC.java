package es.cristichi.fnac.cnight;

import es.cristichi.fnac.exception.CustomNightException;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.ExceptionDialog;
import es.cristichi.fnac.gui.ExitableJComponent;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.gui.NightsJF;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import es.cristichi.fnac.obj.cams.CameraMap;
import es.cristichi.fnac.obj.cams.CrisRestaurantMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;

/**
 * JComponent of the Menu that allows players to customize their own Night.
 */
public class CustomNightMenuJC extends ExitableJComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomNightMenuJC.class);
    
    /**
     * Background image for the viewpoer of the scroll panel.
     */
    protected static BufferedImage backgroundImg;
    
    /**
     * Current Settings for the player.
     */
    protected Settings settings;
    /**
     * Configured power outage Jumpscare for the Custom NightJC that the player is creating.
     */
    protected Jumpscare powerOutage;
    
    /**
     * List of Runnables to run when the player wishes to return to the main menu.
     */
    protected final List<Runnable> onExitListeners;
    
    /**
     * {@code true} while resizing is already in progress, to avoid doing the work twice when the player resizes the
     * window for a long time.
     */
    protected boolean resizingInProgress = false;
    /**
     * {@code false} while the Custom Night is being created, since it takes some time to load the resources. This
     * disallows the player from editing the AI values during this time.
     */
    protected boolean enabledEditing = true;
    
    /**
     * Seed of the Random for the Custom Night.
     */
    protected final long seed;
    /**
     * Random for the Custom Night.
     */
    protected final Random rng;
    /**
     * Map of AnimatronicDrawing classes and their CustomNightAnimatronicData needed to call their
     * Custom Night constructor.
     */
    protected HashMap<Class<? extends AnimatronicDrawing>, CustomNightAnimatronicData> customInputs;
    /**
     * List of CustomAnimJP components so that they can be mass edited.
     */
    protected LinkedList<CustomAnimJP> customAnimJPs;
    
    /**
     * Scroll panel. Its viewport contains the CustomAnimJPs.
     */
    protected JScrollPane scrollPaneAnims;
    /**
     * Panel for the buttons to set-up diferent things or exit to main menu.
     */
    protected JPanel panelSettings;
    
    /**
     * Creates a new CustomNightMenuJC.
     * @param settings User Settings, used to create the Custom Night.
     * @param powerOutage Jumpscare that the generated Custom {@link NightJC} will use for the power outage.
     * @param nightsJF Main frame, used to start the custom Night and handling the menu music.
     * @throws ResourceException If any images or sounds could not load from disk.
     */
    public CustomNightMenuJC(Settings settings, Jumpscare powerOutage, NightsJF nightsJF) throws ResourceException {
        super();
        if (backgroundImg == null){
            backgroundImg = Resources.loadImageResource("cnight/menuBackground.jpg");
        }
        this.settings = settings;
        this.powerOutage = powerOutage;
        Random seedRng = new Random();
        seed = seedRng.nextLong();
        rng = new Random(seed);
        customInputs = new HashMap<>();
        customAnimJPs = new LinkedList<>();

        onExitListeners = new ArrayList<>(1);

        setLayout(new BorderLayout());
        setFont(new Font("Eraser", Font.PLAIN, 50));

        panelSettings = new JPanel();
        add(panelSettings, BorderLayout.EAST);
        panelSettings.setLayout(new BoxLayout(panelSettings, BoxLayout.Y_AXIS));
        panelSettings.setBorder(new EmptyBorder(10,20,10,20));
        panelSettings.setBackground(Color.BLACK);


        createSettingButton("Return to Main Menu", event -> {
            for (Runnable onExit : onExitListeners){
                onExit.run();
            }
        });

        createSettingButton("Set all to 0", event -> {
            for (CustomAnimJP customAnimJP : customAnimJPs) {
                customAnimJP.setAi(0);
            }
        });

        createSettingButton("Set all to 10", event -> {
            for (CustomAnimJP customAnimJP : customAnimJPs){
                customAnimJP.setAi(10);
            }
        });

        createSettingButton("Set all to 20", event -> {
            for (CustomAnimJP customAnimJP : customAnimJPs){
                customAnimJP.setAi(20);
            }
        });

        createSettingButton("Start Custom Night", event -> {
            panelSettings.setVisible(false);
            enabledEditing = false;
            updateAnimatronicGrid();
            new Thread(() -> {
                try {
                    NightJC night = createCustomNight();
                    nightsJF.startCustomNight(night);
                    LOGGER.info("Today's {} is using the seed \"{}\". Have fun!", night.getNightName(), seed);
                } catch (IOException e) {
                    new ExceptionDialog(e, false, false, LOGGER);
                } catch (CustomNightException | NightException e) {
                    new ExceptionDialog(e, false, true, LOGGER);
                }
                panelSettings.setVisible(true);
                enabledEditing = true;
                updateAnimatronicGrid();
            }, "cnight_t").start();
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateAnimatronicGrid();
            }
        });
    }

    private void createSettingButton(String txt, ActionListener action){
        JButton btn = new JButton(txt);
        btn.setFont(getFont());
        btn.setForeground(Color.YELLOW);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.addActionListener(action);

        panelSettings.add(Box.createRigidArea(new Dimension(0, 10)));
        panelSettings.add(btn);
    }

    @Override
    public void addOnExitListener(Runnable onExitListener) {
        onExitListeners.add(onExitListener);
    }

    protected void updateAnimatronicGrid() {
        if (resizingInProgress) return;
        resizingInProgress = true;

        SwingUtilities.invokeLater(() -> {
            if (scrollPaneAnims != null) {
                remove(scrollPaneAnims);
                scrollPaneAnims = null;
            }

            JPanel viewportPanel = new JPanel(new CustomGridLayout(10, 10));
            viewportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            viewportPanel.setOpaque(false);

            customAnimJPs.clear();
            for (Map.Entry<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>> entry
                    : CustomNightRegistry.getAnimatronics()) {
                int AI = 0;
                if (customInputs.containsKey(entry.getValue())){
                    CustomNightAnimatronicData previous = customInputs.get(entry.getValue());
                    AI = previous.ai();
                }
                CustomNightAnimatronicData data =
                        new CustomNightAnimatronicData(entry.getKey().name(), entry.getKey().variant(), AI, rng);
                customInputs.put(entry.getValue(), data);

                try {
                    CustomAnimJP animComponent = new CustomAnimJP(getFont(), entry.getKey(), entry.getValue(), AI){
                        @Override
                        public void onAiChanged(int ai) {
                            customInputs.computeIfPresent(entry.getValue(),
                                    (k, cNightData) -> new CustomNightAnimatronicData(cNightData.name(),
                                            cNightData.variant(), ai, cNightData.rng()));
                        }
                    };
                    animComponent.setEnabled(enabledEditing);

                    viewportPanel.add(animComponent);
                    customAnimJPs.add(animComponent);
                } catch (ResourceException e) {
                    new ExceptionDialog(new CustomNightException("Error trying to create panel for %s (%s). Skipping it."
                            .formatted(entry.getKey().name(), entry.getKey().variant()), e), false, false, LOGGER);
                }
            }

            scrollPaneAnims = new JScrollPane(viewportPanel){
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(backgroundImg, 0,0, scrollPaneAnims.getWidth(), scrollPaneAnims.getHeight(), this);
                }
            };
            scrollPaneAnims.getViewport().setOpaque(false);
            scrollPaneAnims.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPaneAnims.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            add(scrollPaneAnims, BorderLayout.CENTER);

            revalidate();
            repaint();

            resizingInProgress = false;
        });
    }

    private NightJC createCustomNight() throws IOException, CustomNightException, NightException {
        HashMap<String, AnimatronicDrawing> anims = new HashMap<>(CustomNightRegistry.size());

        CameraMap nightMap = new CrisRestaurantMap();

        boolean ok = false;
        for (Map.Entry<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>> entry : CustomNightRegistry.getAnimatronics()) {
                CustomNightAnimatronicData data = customInputs.get(entry.getValue());
            if (data.ai() > 0){
                ok = true;
                try{
                    AnimatronicDrawing anim = entry.getValue().getConstructor(CustomNightAnimatronicData.class).newInstance(
                            new CustomNightAnimatronicData(entry.getKey().name(), entry.getKey().variant(),
                                    data.ai(), rng));

                    nightMap.get(entry.getKey().description()).getAnimatronicsHere().add(anim);
                } catch (InvocationTargetException e){
                    throw new CustomNightException("Error trying to create the Animatronic.", e);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e){
                    throw new CustomNightException("Animatronic is missing the constructor for Custom Night.", e);
                }
            }
        }

        if (!ok){
            throw new CustomNightException("This Custom Night has no Animatronics. Try increasing the AI of a few of them!");
        }
        return new NightJC("Custom Night", settings.getFps(), nightMap, null, powerOutage, rng, 90, 0.45f,
                "night/general/completed.wav");
    }
}
