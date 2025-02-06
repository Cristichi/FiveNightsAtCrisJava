package es.cristichi.fnac.gui;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.io.Settings;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
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
     * FPS Dropdown
     */
    private final JComboBox<Integer> fpsComboBox;
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

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Fullscreen Toggle
        fullscreenLabel = new JLabel("<html>Fullscreen:</html>");
        fullscreenLabel.setForeground(foreground);
        fullscreenLabel.setToolTipText("You can change this any time with F11 or Alt+Enter.");
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
        fpsLabel.setToolTipText("On lower-end computers, a high value can lead to lag.");
        add(fpsLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        fpsComboBox = new JComboBox<>(new Integer[]{30, 60, 120});
        fpsComboBox.setFont(getFont());
        fpsComboBox.setOpaque(false);
        fpsComboBox.setBackground(new Color(0,0,0,0));
        fpsComboBox.setForeground(foreground);
        fpsComboBox.setSelectedItem(60);
        fpsComboBox.setSelectedItem(settings.getFps());
        
        fpsComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                result.setBackground(Color.LIGHT_GRAY);
                result.setForeground(foreground);
                return result;
            }
        });
        fpsComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new BasicArrowButton(BasicArrowButton.SOUTH, null,
                        foreground.darker().darker(), foreground, foreground);
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                return button;
            }
            
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Prevents painting the default background
            }
        });
        fpsComboBox.addItemListener(e -> editingSettings.setFps((Integer) e.getItem()));
        add(fpsComboBox, gbc);

        // Volume Slider
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.LINE_END;
        volumeLabel = new JLabel("<html>Volume:</html>");
        volumeLabel.setForeground(foreground);
        volumeLabel.setHorizontalTextPosition(JLabel.TRAILING);
        volumeLabel.setToolTipText("WARNING!! Jumpscares are slightly louder than the main menu music.");
        add(volumeLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        volumeSlider = new JSlider(0, 150, Math.max(0, Math.min(150, (int) (editingSettings.getVolume() * 100))));
        volumeSlider.setOpaque(false);
        volumeSlider.setMajorTickSpacing(50);
        volumeSlider.setMinorTickSpacing(10);
        volumeSlider.setSnapToTicks(true);
        volumeSlider.setPaintTrack(false);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        Sound volumeTest = Resources.loadSound("settings/sounds/volumetest.wav");
        volumeSlider.addChangeListener(e -> {
            TinySound.setGlobalVolume((double) volumeSlider.getValue() /100);
            this.editingSettings.setVolume(volumeSlider.getValue() / 100.0);
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
        JLabel label150 = new JLabel("<html>150% (!)</html>");
        label150.setForeground(foreground);
        volumeLabels.put(150, label150);
        volumeSlider.setLabelTable(volumeLabels);

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
            fpsComboBox.setSelectedItem(ogSettings.getFps());
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
                fpsComboBox.setFont(font);
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
