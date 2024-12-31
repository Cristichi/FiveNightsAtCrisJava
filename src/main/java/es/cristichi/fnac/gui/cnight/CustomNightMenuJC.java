package es.cristichi.fnac.gui.cnight;

import es.cristichi.fnac.exception.CustomNightException;
import es.cristichi.fnac.exception.NightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.ExceptionDialog;
import es.cristichi.fnac.gui.ExitableJComponent;
import es.cristichi.fnac.gui.NightsJF;
import es.cristichi.fnac.gui.night.NightJC;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronic;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronicData;
import es.cristichi.fnac.obj.anim.cnight.CustomNightMapType;
import es.cristichi.fnac.obj.cams.CameraMap;
import es.cristichi.fnac.obj.cams.CrisRestaurantMap;
import es.cristichi.fnac.obj.cams.TutorialMap;

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


public class CustomNightMenuJC extends ExitableJComponent {
    protected static final int COLUMNS = 4;
    protected static BufferedImage backgroundImg;

    protected Settings settings;
    protected Jumpscare powerOutage;

    protected final List<Runnable> onExitListeners;

    private boolean resizingInProgress = false;
    private boolean enabledEditing = true;

    protected final long seed;
    protected final Random rng;
    protected HashMap<Class<? extends AnimatronicDrawing>, CustomNightAnimatronicData> customInputs;
    protected LinkedList<CustomAnimJP> customAnimJPs;

    protected CustomNightMapType mapType;

    protected JScrollPane scrollPaneAnims;
    protected JPanel panelSettings;

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

        mapType = CustomNightMapType.RESTAURANT;

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
                    nightsJF.startCustomNight(createCustomNight());
                } catch (IOException e) {
                    new ExceptionDialog(e, false, false);
                } catch (CustomNightException | NightException e) {
                    new ExceptionDialog(e, false, true);
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
                        new CustomNightAnimatronicData(null, entry.getKey().name(), entry.getKey().variant(), AI, rng);
                customInputs.put(entry.getValue(), data);

                try {
                    CustomAnimJP animComponent = new CustomAnimJP(getFont(), entry.getKey(), entry.getValue(), AI){
                        @Override
                        public void onAiChanged(int ai) {
                            customInputs.computeIfPresent(entry.getValue(),
                                    (k, cNightData) -> new CustomNightAnimatronicData(mapType, cNightData.name(),
                                            cNightData.variant(), ai, cNightData.rng()));
                        }
                    };
                    animComponent.setEnabled(enabledEditing);

                    viewportPanel.add(animComponent);
                    customAnimJPs.add(animComponent);
                } catch (ResourceException e) {
                    new ExceptionDialog(new CustomNightException("Error trying to create panel for %s (%s). Skipping it."
                            .formatted(entry.getKey().name(), entry.getKey().variant()), e), false, false);
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

        CameraMap nightMap = switch (mapType){
            case TUTORIAL -> new TutorialMap();
            case RESTAURANT -> new CrisRestaurantMap();
        };

        boolean ok = false;
        for (Map.Entry<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>> entry : CustomNightRegistry.getAnimatronics()) {
                CustomNightAnimatronicData data = customInputs.get(entry.getValue());
            if (data.ai() > 0){
                ok = true;
                try{
                    AnimatronicDrawing anim = entry.getValue().getConstructor(CustomNightAnimatronicData.class).newInstance(
                            new CustomNightAnimatronicData(mapType, entry.getKey().name(), entry.getKey().variant(),
                                    data.ai(), rng));

                    nightMap.get(switch (mapType){
                            case TUTORIAL -> entry.getKey().tutStart();
                            case RESTAURANT-> entry.getKey().restStart();
                        }).getAnimatronicsHere().add(anim);
                } catch (InvocationTargetException e){
                    CustomNightException ex = new CustomNightException("Error trying to create the Animatronic.", e);
                    new ExceptionDialog(ex, false, false);
                    throw ex;
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e){
                    CustomNightException ex = new CustomNightException("Animatronic is missing the constructor for Custom Night.", e);
                    new ExceptionDialog(ex, false, false);
                    throw ex;
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
