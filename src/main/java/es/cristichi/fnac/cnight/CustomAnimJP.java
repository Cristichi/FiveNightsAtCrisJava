package es.cristichi.fnac.cnight;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * JPanel that contains everything needed to show the information about one {@link AnimatronicDrawing}, as well as
 * allow the player to change its AI.
 */
public abstract class CustomAnimJP extends JPanel {
    /**
     * Size all instances of {@link CustomAnimJP} should have on the screen normally.
     */
    public static final Dimension SIZE = new Dimension(150, 175);
    private static final Border border = new LineBorder(Color.BLACK, 1);

    private final JLabel aiInputLbl;

    protected BufferedImage portraitBackgroundImg;
    protected BufferedImage portraitImg;
    
    /**
     * Creates a new CustomAnimJP.
     * @param font Font to use. The size will be overwritten.
     * @param annotationInfo Annotation that the class of this Animatronic has.
     * @param classInfo Class of this Animatronic.
     * @param AI Initial AI level that this panel must show.
     * @throws ResourceException If any images or sounds cannot be read from disk.
     */
    public CustomAnimJP(Font font, CustomNightAnimatronic annotationInfo, Class<? extends AnimatronicDrawing> classInfo,
                        int AI) throws ResourceException {
        super();
        if (portraitBackgroundImg == null) {
            portraitBackgroundImg = Resources.loadImageResource("cnight/portraitBackground.jpg");
            portraitImg = Resources.loadImageResource(annotationInfo.portraitPath());
        }
        setPreferredSize(SIZE);

        font = font.deriveFont(25f);
        setFont(font);
        setForeground(Color.YELLOW);
        setBorder(border);

        setToolTipText("<html>"+annotationInfo.description()+"</html>");

        setLayout(new BorderLayout());
        JLabel nameLbl = new JLabel("<html>" +
                (annotationInfo.variant().isBlank()
                        ? "<b>%s</b>".formatted(annotationInfo.name())
                        : "<b>%s</b> (%s)".formatted(annotationInfo.name(), annotationInfo.variant()))
                + "</html>");
        nameLbl.setFont(font);
        nameLbl.setForeground(getForeground());
        nameLbl.setHorizontalAlignment(SwingConstants.CENTER);
        nameLbl.setHorizontalTextPosition(SwingConstants.CENTER);
        add(nameLbl, BorderLayout.NORTH);

        aiInputLbl = new JLabel(Integer.toString(AI));
        aiInputLbl.setOpaque(false);
        aiInputLbl.setFont(font.deriveFont(60f));
        aiInputLbl.setForeground(getForeground());
        aiInputLbl.setHorizontalAlignment(SwingConstants.TRAILING);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()){
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        int newAI = Math.min(annotationInfo.maxAi(), Integer.parseInt(aiInputLbl.getText()) + 1);
                        aiInputLbl.setText(Integer.toString(newAI));
                        onAiChanged(newAI);
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        int newAI = Math.max(0, Integer.parseInt(aiInputLbl.getText()) - 1);
                        aiInputLbl.setText(Integer.toString(newAI));
                        onAiChanged(newAI);
                    }
                }
            }
        });
        addMouseWheelListener(e -> {
            if (isEnabled()) {
                int newAI = Integer.parseInt(aiInputLbl.getText());
                if (e.getWheelRotation() < 0) {
                    newAI++;
                } else {
                    newAI--;
                }
                if (newAI >= 0 && newAI <= annotationInfo.maxAi()) {
                    aiInputLbl.setText(Integer.toString(newAI));
                    onAiChanged(newAI);
                }
            }
        });
        add(aiInputLbl, BorderLayout.SOUTH);
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip tt = super.createToolTip();

        // Get the tooltip text and the tooltip's font
        String tooltipText = getToolTipText();
        FontMetrics metrics = tt.getFontMetrics(tt.getFont());

        // Estimate the width of the text
        int textWidth = metrics.stringWidth(tooltipText);

        // Calculate the width and height for the tooltip
        int tooltipWidth = Math.min(200, textWidth + 20); // Limit width to line width or a reasonable value
        int lineHeight = metrics.getHeight();
        int numLines = (int) Math.ceil((double) textWidth / (tooltipWidth - 20)); // Estimate lines required
        int tooltipHeight = lineHeight * numLines + 10; // Adjust for padding

        tt.setPreferredSize(new Dimension(tooltipWidth, tooltipHeight));
        return tt;
    }
    
    /**
     * Changes AI and calls {@link #onAiChanged(int)}.
     * @param ai New AI to set.
     */
    public void setAi(int ai) {
        aiInputLbl.setText(Integer.toString(ai));
        onAiChanged(ai);
    }
    
    /**
     * Method to be called when the AI changes by user input or {@link #setAi(int)}
     * @param ai New AI.
     */
    public abstract void onAiChanged(int ai);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(portraitBackgroundImg, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(portraitImg, 0, 0, getWidth(), getHeight(), this);
        if (!isEnabled()){
            g.setColor(new Color(255, 255, 255, 160));
            g.fillRect(0,0, getWidth(), getHeight());
        }
    }
}
