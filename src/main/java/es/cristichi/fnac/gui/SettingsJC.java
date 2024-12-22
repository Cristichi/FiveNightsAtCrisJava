package es.cristichi.fnac.gui;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

public abstract class SettingsJC extends JComponent {
    private static final Color foreground = Color.YELLOW;

    private Settings editingSettings;
    private Settings ogSettings;

    private final Font font;
    private final BufferedImage background;
    private final BufferedImage checkSel;
    private final BufferedImage checkNotSel;

    private final JLabel fullscreenLabel;
    private final JCheckBox fullscreenCheckbox;
    private final JLabel fpsLabel;
    private final JSpinner fpsSpinner;
    private final JLabel volumeLabel;
    private final JSlider volumeSlider;
    private final JButton saveButton;
    private final JButton returnButton;

    public SettingsJC(Settings settings) throws ResourceException {
        super();
        this.background = Resources.loadImageResource("settings/background.jpg");
        this.checkSel = Resources.loadImageResource("settings/checkSel.jpg");
        this.checkNotSel = Resources.loadImageResource("settings/checkNot.jpg");
        this.editingSettings = new Settings(settings);
        this.ogSettings = new Settings(settings);
        font = new Font("Eraser Dust", Font.PLAIN, 100);

        BufferedImage btnUp = Resources.loadImageResource("settings/spinnerUp.jpg");
        BufferedImage btnDown = Resources.loadImageResource("settings/spinnerDown.jpg");

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Fullscreen Toggle
        JLabel label4 = new JLabel("<html>"+ "Fullscreen:" +"</html>");
        label4.setForeground(foreground);
        fullscreenLabel = label4;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(fullscreenLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        fullscreenCheckbox = new JCheckBox();
        fullscreenCheckbox.setIcon(new ImageIcon(checkNotSel));
        fullscreenCheckbox.setSelectedIcon(new ImageIcon(checkSel));
        fullscreenCheckbox.setSelected(editingSettings.isFullscreen());
        fullscreenCheckbox.setFont(font);
        fullscreenCheckbox.setForeground(foreground);
        fullscreenCheckbox.setOpaque(true);
        fullscreenCheckbox.setBorderPainted(false);
        fullscreenCheckbox.setContentAreaFilled(false);
        fullscreenCheckbox.addActionListener(e -> editingSettings.setFullscreen(fullscreenCheckbox.isSelected()));
        add(fullscreenCheckbox, gbc);

        // FPS Selector
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.LINE_END;
        fpsLabel = new JLabel("<html>FPS:</html>");
        fpsLabel.setForeground(foreground);
        fpsLabel.setHorizontalTextPosition(JLabel.TRAILING);
        add(fpsLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        fpsSpinner = new JSpinner(new SpinnerNumberModel(editingSettings.getFps(), 30, 240, 1));
        fpsSpinner.setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                JButton upButton = createCustomSpinnerButton(btnUp);
                installNextButtonListeners(upButton);
                return upButton;
            }

            @Override
            protected Component createPreviousButton() {
                JButton downButton = createCustomSpinnerButton(btnDown);
                installPreviousButtonListeners(downButton);
                return downButton;
            }

            @Override
            protected void installDefaults() {
                super.installDefaults();
                spinner.setBorder(new EmptyBorder(0, 0, 0, 0)); // Remove default border
            }
        });
        fpsSpinner.setFont(font);
        fpsSpinner.setOpaque(false);
        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor)(fpsSpinner.getEditor()));
        editor.setOpaque(false);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        editor.getTextField().setOpaque(false);
        editor.getTextField().setEditable(false);
        editor.getTextField().setForeground(foreground);
        fpsSpinner.addChangeListener(e -> editingSettings.setFps((Integer) fpsSpinner.getValue()));
        add(fpsSpinner, gbc);

        // Volume Slider
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.LINE_END;
        volumeLabel = new JLabel("<html>Volume:</html>");
        volumeLabel.setForeground(foreground);
        volumeLabel.setHorizontalTextPosition(JLabel.TRAILING);
        add(volumeLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        volumeSlider = new JSlider(10, 100, (int) (editingSettings.getVolume() * 100));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setOpaque(false);
        volumeSlider.setPaintTrack(false);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        Sound volumeTest = Resources.loadSound("settings/sounds/volumetest.wav", "volTest.wav");
        volumeSlider.addChangeListener(e -> {
            TinySound.setGlobalVolume((double) volumeSlider.getValue() /100);
            volumeTest.stop();
            volumeTest.play();
        });

        Hashtable<Integer, JLabel> volumeLabels = new Hashtable<>();
        JLabel label1 = new JLabel("<html>0%</html>");
        label1.setForeground(foreground);
        volumeLabels.put(0, label1);
        JLabel label10 = new JLabel("<html>10%</html>");
        label10.setForeground(foreground);
        volumeLabels.put(10, label10);
        JLabel label2 = new JLabel("<html>100%</html>");
        label2.setForeground(foreground);
        volumeLabels.put(100, label2);
        volumeSlider.setLabelTable(volumeLabels);

        volumeSlider.addChangeListener(e -> this.editingSettings.setVolume(volumeSlider.getValue() / 100.0));
        add(volumeSlider, gbc);

        // Save Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2; // Spans both columns
        gbc.anchor = GridBagConstraints.CENTER;
        saveButton = createCustomButton("Save Settings");
        saveButton.addActionListener(e -> {
            this.editingSettings.saveToFile(Settings.SETTINGS_FILE);
            ogSettings = new Settings(this.editingSettings);
            onSettingsSaved(this.editingSettings);
            onReturnToMenu();
        });
        add(saveButton, gbc);

        // Cancel Button
        gbc.gridy++;
        returnButton = createCustomButton("Cancel");
        returnButton.addActionListener(e -> {
            this.editingSettings = new Settings(ogSettings);
            TinySound.setGlobalVolume(ogSettings.getVolume());
            fullscreenCheckbox.setSelected(ogSettings.isFullscreen());
            fpsSpinner.setValue(ogSettings.getFps());
            volumeSlider.setValue((int) (ogSettings.getVolume() * 100));
            onReturnToMenu();
        });
        add(returnButton, gbc);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Font font = SettingsJC.this.font.deriveFont((float)(getWidth()*0.05));

                fullscreenLabel.setFont(font);
                fullscreenCheckbox.setFont(font);
                fpsLabel.setFont(font);
                fpsSpinner.setFont(font);
                volumeLabel.setFont(font);
                volumeSlider.setFont(font);
                saveButton.setFont(font);
                returnButton.setFont(font);

                invalidate();
                repaint();
            }
        });
    }

    private JButton createCustomButton(String text) {
        JButton btn = new JButton("<html><u>" + text + "</u></html>");
        btn.setFont(font);
        btn.setForeground(foreground);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private JButton createCustomSpinnerButton(BufferedImage img) {
        JButton button = new JButton(new ImageIcon(img));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0,0, getWidth(),getHeight(), this);
    }

    public abstract void onSettingsSaved(Settings saved);

    public abstract void onReturnToMenu();
}
