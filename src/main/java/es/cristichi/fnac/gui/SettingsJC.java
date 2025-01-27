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

/**
 * JComponent that allows the user to modify Settings.
 */
public abstract class SettingsJC extends JComponent {
    /**
     * Color of all text.
     */
    private static final Color foreground = Color.YELLOW;
    
    /**
     * Settings with he changes done by the player.
     */
    private Settings editingSettings;
    /**
     * Previous Settings in case the player wants to cancel.
     */
    private Settings ogSettings;
    
    /**
     * Background image.
     */
    private final BufferedImage background;
    
    /**
     * Label for the Fullscreen text.
     */
    private final JLabel fullscreenLabel;
    /**
     * Fullscreen checkbox.
     */
    private final JCheckBox fullscreenCheckbox;
    /**
     * Image for the icon to mark something as "checked".
     */
    private final BufferedImage checkSel;
    /**
     * Image for the icon to mark something as "unchecked".
     */
    private final BufferedImage checkNotSel;
    /**
     * Label for the FPS text.
     */
    private final JLabel fpsLabel;
    /**
     * FPS Spinner.
     */
    private final JSpinner fpsSpinner;
    /**
     * Label for the Volume text.
     */
    private final JLabel volumeLabel;
    /**
     * Volume slider.
     */
    private final JSlider volumeSlider;
    /**
     * Button to save the edited settings and return to main menu.
     */
    private final JButton saveButton;
    /**
     * Button to discard the edited settings and return to main menu with the previous settings on place.
     */
    private final JButton returnButton;
    
    /**
     * Creates a new SettingsJC with the given Settings. The object referenced is modified by the player's actions.
     * @param settings Player's personal settings.
     * @throws ResourceException If any images or sonuds could not be loaded from disk.
     */
    public SettingsJC(Settings settings) throws ResourceException {
        super();
        this.background = Resources.loadImage("settings/background.jpg");
        this.checkSel = Resources.loadImage("settings/checkSel.jpg");
        this.checkNotSel = Resources.loadImage("settings/checkNot.jpg");
        this.editingSettings = new Settings(settings);
        this.ogSettings = new Settings(settings);
        setFont(new Font("Eraser Dust", Font.PLAIN, 100));

        BufferedImage btnUp = Resources.loadImage("settings/spinnerUp.jpg");
        BufferedImage btnDown = Resources.loadImage("settings/spinnerDown.jpg");

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Fullscreen Toggle
        JLabel label4 = new JLabel("<html>Fullscreen:</html>");
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
        fullscreenCheckbox.setFont(getFont());
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
        fpsSpinner.setFont(getFont());
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
        volumeSlider = new JSlider(0, 100, (int) (editingSettings.getVolume() * 100));
        volumeSlider.setOpaque(false);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setSnapToTicks(true);
        volumeSlider.setPaintTrack(false);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        Sound volumeTest = Resources.loadSound("settings/sounds/volumetest.wav");
        volumeSlider.addChangeListener(e -> {
            TinySound.setGlobalVolume((double) volumeSlider.getValue() /100);
            volumeTest.stop();
            volumeTest.play();
        });

        Hashtable<Integer, JLabel> volumeLabels = new Hashtable<>();
        JLabel label0 = new JLabel("<html>0%</html>");
        label0.setForeground(foreground);
        volumeLabels.put(0, label0);
        JLabel label50 = new JLabel("<html>50%</html>");
        label50.setForeground(foreground);
        volumeLabels.put(50, label50);
        JLabel label100 = new JLabel("<html>100%</html>");
        label100.setForeground(foreground);
        volumeLabels.put(100, label100);
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
                Font font = getFont().deriveFont((float)(getWidth()*0.05));

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
    
    /**
     * Creates a button with the correct styling.
     * @param text Display text for the button.
     * @return The JButton instance properly styled.
     */
    private JButton createCustomButton(String text) {
        JButton btn = new JButton("<html><u>%s</u></html>".formatted(text));
        btn.setFont(getFont());
        btn.setForeground(foreground);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }
    
    /**
     * Creates a Spinner Button with the given image as an icon.
     * @param img Icon.
     * @return The JButton to be used on the spinner.
     */
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
    
    /**
     * Method to execute when the Settings are saved.
     * @param saved Saved settings.
     */
    public abstract void onSettingsSaved(Settings saved);
    
    /**
     * Method to indicate to the main menu that the player wants to return to the menu.
     */
    public abstract void onReturnToMenu();
}
