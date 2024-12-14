package es.cristichi.fnac.gui.cnight;

import es.cristichi.fnac.exception.CustomNightException;
import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.gui.ExceptionDialog;
import es.cristichi.fnac.gui.NightJC;
import es.cristichi.fnac.gui.NightsJFrame;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import es.cristichi.fnac.obj.Jumpscare;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import es.cristichi.fnac.obj.anim.CustomNightAnimatronic;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronicData;
import es.cristichi.fnac.obj.anim.cnight.CustomNightMapType;
import es.cristichi.fnac.obj.cams.CameraMap;
import es.cristichi.fnac.obj.cams.CrisRestaurantMap;
import es.cristichi.fnac.obj.cams.TutorialMap;
import org.reflections.Reflections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class CustomNightMenuJC extends JComponent {
    protected static final Map<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>> customNightAnimatronicRegistry;

    static {
        Set<Class<?>> classes = getClasses("es.cristichi.fnac.obj.anim");
        customNightAnimatronicRegistry = new HashMap<>(classes.size());
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(CustomNightAnimatronic.class)
                    && AnimatronicDrawing.class.isAssignableFrom(clazz)) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends AnimatronicDrawing> animatronicClass = (Class<? extends AnimatronicDrawing>) clazz;
                    CustomNightAnimatronic annotation = clazz.getAnnotation(CustomNightAnimatronic.class);
                    customNightAnimatronicRegistry.put(annotation, animatronicClass);
                } catch (ClassCastException e){
                    new ExceptionDialog(new CustomNightException(
                            "Error trying to get the Animatronic class %s with the Annotation."
                                    .formatted(clazz.getName()), e), true, false);
                }
            }
        }
    }

    // Create an instance of a specific animatronic
    protected static AnimatronicDrawing createInstance(CustomNightAnimatronic selected, CustomNightAnimatronicData data) throws Exception {
        Class<? extends AnimatronicDrawing> animatronicClass = customNightAnimatronicRegistry.get(selected);
        if (animatronicClass != null) {
            try {
                return animatronicClass.getDeclaredConstructor(CustomNightAnimatronicData.class).newInstance(data);
            }catch (NoSuchMethodException e){
                new ExceptionDialog(new CustomNightException(("Animatronic %s could not be created because it's missing " +
                        "a constructor with only CustomNightAnimatronicData.").formatted(animatronicClass.getName()), e),
                        true, false);
                return null;
            }
        }
        throw new IllegalArgumentException("No Animatronic found in Registry with the given data: " +
                selected.name()+" ("+ (selected.variant().isEmpty()?"default":selected.variant())+")");
    }

    protected static Set<Class<?>> getClasses(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getTypesAnnotatedWith(CustomNightAnimatronic.class);
    }


    // GUI
    protected static final int COLUMNS = 4;
    protected static BufferedImage backgroundImg;

    protected Settings settings;
    protected Jumpscare powerOutage;
    protected NightsJFrame nightsJFrame;

    protected final long seed;
    protected final Random rng;
    protected HashMap<Class<? extends AnimatronicDrawing>, CustomNightAnimatronicData> customInputs;
    protected LinkedList<CustomAnimJP> customAnimJPs;

    protected CustomNightMapType mapType;

    protected JScrollPane scrollPaneAnims;
    protected JPanel panelSettings;

    public CustomNightMenuJC(Settings settings, Jumpscare powerOutage, NightsJFrame nightsJFrame) throws ResourceException {
        super();
        if (backgroundImg == null){
            backgroundImg = Resources.loadImageResource("cnight/menuBackground.jpg");
        }
        this.settings = settings;
        this.powerOutage = powerOutage;
        this.nightsJFrame = nightsJFrame;
        {
            Random seedRng = new Random();
            seed = seedRng.nextLong();
        }
        rng = new Random(seed);
        customInputs = new HashMap<>(customNightAnimatronicRegistry.size());
        customAnimJPs = new LinkedList<>();

        mapType = CustomNightMapType.RESTAURANT;

        setLayout(new BorderLayout());
        setFont(new Font("Eraser", Font.PLAIN, 50));

        panelSettings = new JPanel();
        add(panelSettings, BorderLayout.EAST);
        panelSettings.setLayout(new BoxLayout(panelSettings, BoxLayout.Y_AXIS));
        panelSettings.setBorder(new EmptyBorder(10,20,10,20));

        panelSettings.add(Box.createRigidArea(new Dimension(0, 10)));
        {
            JButton setAll0 = new JButton("Set all to 0");
            setAll0.setAlignmentX(Component.CENTER_ALIGNMENT);
            setAll0.addActionListener(event -> {
                for (CustomAnimJP customAnimJP : customAnimJPs){
                    customAnimJP.setAi(0);
                }
            });
            panelSettings.add(setAll0);
        }
        panelSettings.add(Box.createRigidArea(new Dimension(0, 10)));
        {
            JButton setAll10 = new JButton("Set all to 10");
            setAll10.setAlignmentX(Component.CENTER_ALIGNMENT);
            setAll10.addActionListener(event -> {
                for (CustomAnimJP customAnimJP : customAnimJPs){
                    customAnimJP.setAi(10);
                }
            });
            panelSettings.add(setAll10);
        }
        panelSettings.add(Box.createRigidArea(new Dimension(0, 10)));
        {
            JButton setAll20 = new JButton("Set all to 20");
            setAll20.setAlignmentX(Component.CENTER_ALIGNMENT);
            setAll20.addActionListener(event -> {
                for (CustomAnimJP customAnimJP : customAnimJPs){
                    customAnimJP.setAi(20);
                }
            });
            panelSettings.add(setAll20);
        }
        panelSettings.add(Box.createRigidArea(new Dimension(0, 10)));
        {
            JButton startBtn = new JButton("Start Custom Night");
            startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            startBtn.addActionListener(event -> {
                try {
                    nightsJFrame.startCustomNight(createCustomNight());
                    updateComponents();
                } catch (IOException e) {
                    new ExceptionDialog(e, false, false);
                    updateComponents();
                } catch (CustomNightException e) {
                    new ExceptionDialog(e, false, true);
                }
            });
            panelSettings.add(startBtn);
        }

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateComponents();
            }
        });
    }

    private boolean resizingInProgress = false;
    protected void updateComponents() {
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
            for (Map.Entry<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>> entry : customNightAnimatronicRegistry.entrySet()) {
                customInputs.put(entry.getValue(),
                        new CustomNightAnimatronicData(null, entry.getKey().name(), entry.getKey().variant(), 0, rng));

                try {
                    CustomAnimJP animComponent = new CustomAnimJP(getFont(), entry.getKey(), entry.getValue()){
                        @Override
                        public void onAiChanged(int ai) {
                            customInputs.computeIfPresent(entry.getValue(),
                                    (k, cNightData) -> new CustomNightAnimatronicData(mapType, cNightData.name(),
                                            cNightData.variant(), ai, cNightData.rng()));
                        }
                    };

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

    private NightJC createCustomNight() throws IOException, CustomNightException {
        HashMap<String, AnimatronicDrawing> anims = new HashMap<>(customNightAnimatronicRegistry.size());

        CameraMap nightMap = switch (mapType){
            case TUTORIAL -> new TutorialMap();
            case RESTAURANT -> new CrisRestaurantMap();
        };

        boolean ok = false;
        for (Map.Entry<CustomNightAnimatronic, Class<? extends AnimatronicDrawing>> entry : customNightAnimatronicRegistry.entrySet()) {
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
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e){
                    new ExceptionDialog(new CustomNightException("Animatronic is missing the constructor for Custom Night.", e), false, false);
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
