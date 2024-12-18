package es.cristichi.fnac.gui.cnight;

import es.cristichi.fnac.exception.ResourceException;
import es.cristichi.fnac.io.Resources;
import es.cristichi.fnac.obj.anim.AnimatronicDrawing;
import es.cristichi.fnac.obj.anim.cnight.CustomNightAnimatronic;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public abstract class CustomAnimJP extends JPanel {
    public static Dimension SIZE = new Dimension(150, 175);
    private static final Border border = new LineBorder(Color.BLACK, 1);

    private final JLabel aiInputLbl;

    protected BufferedImage portraitBackgroundImg;
    protected BufferedImage portraitImg;

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
        });
        add(aiInputLbl, BorderLayout.SOUTH);
    }

    public void setAi(int ai) {
        aiInputLbl.setText(Integer.toString(ai));
        onAiChanged(ai);
    }

    public abstract void onAiChanged(int ai);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(portraitBackgroundImg, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(portraitImg, 0, 0, getWidth(), getHeight(), this);
    }
}
