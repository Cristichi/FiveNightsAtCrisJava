package es.cristichi.fnac.gui;

import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class is an utility provided to show errors to the player, when any happen.
 */
public class ExceptionDialog extends JFrame {
    /**
     * Creates a new dialog with the info of a Exception and shows it as always on top.
     *
     * @param exception    Exception that must be informed to the player.
     * @param fatal        Whether closing this window should close the Java application entirely.
     * @param clearMessage Whether it shows only the message of {@code exception}, or otherwise the entire stacktrace.
     * @param logger       Logger to be used to log the error, or {@code null} to not log the error.
     */
    public ExceptionDialog(Exception exception, boolean fatal, boolean clearMessage, Logger logger) {
        super("Opps! An error happened");
        if (logger != null) logger.error("Error shown to player:", exception);
        
        if (fatal) setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        else setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        setMinimumSize(new Dimension(600, 400));
        setLayout(new BorderLayout());
        
        JTextArea textArea = new JTextArea();
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setMargin(new Insets(10,10,10,10));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Consolas Arial", Font.PLAIN, 14));
        
        StringWriter stringWriter = new StringWriter();
        if (fatal) {
            stringWriter.write("""
                    A very bad error happened, we have to close the game. Please feel free to reopen the game \
                    and try again what you were doing.
                    
                    """);
        } else {
            stringWriter.write("""
                    Oops! An error happened. We can handle it but we wanted you to know. You can read this window \
                    and close it.
                    
                    """);
        }
        if (clearMessage) {
            stringWriter.write("Error message: "+exception.getMessage());
        } else {
            exception.printStackTrace(new PrintWriter(stringWriter));
        }
        textArea.setText(stringWriter.toString());
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);
        
        if (fatal) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        } else {
            setVisible(true);
            Rectangle windowBounds = getBounds();
            GraphicsDevice[] graphicsDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            GraphicsDevice bestScreen = null;
            int bestArea = 0;
            for (GraphicsDevice graphicsDevice : graphicsDevices) {
                GraphicsConfiguration[] graphicsConfigurations = graphicsDevice.getConfigurations();
                
                for (GraphicsConfiguration graphicsConfiguration : graphicsConfigurations) {
                    Rectangle graphicsBounds = graphicsConfiguration.getBounds();
                    
                    Rectangle intersection = windowBounds.intersection(graphicsBounds);
                    
                    int intersectionArea = intersection.width * intersection.height;
                    if (intersectionArea > bestArea) {
                        bestScreen = graphicsDevice;
                        bestArea = intersectionArea;
                    }
                }
            }
            if (bestScreen != null) {
                int width = bestScreen.getDisplayMode().getWidth();
                int height = bestScreen.getDisplayMode().getHeight();
                setSize(width / 2, (int) (height / 1.5));
            }
        }
        setLocationRelativeTo(null);
        setVisible(true);
        
        setAlwaysOnTop(true);
    }
}
