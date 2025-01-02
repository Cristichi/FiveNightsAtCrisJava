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
     * @param exception    Exception that must be informed to user.
     * @param fatal        Whether closing this window should close the Java application entirely.
     * @param clearMessage Whether it shows only the message of the error, or otherwise the entire stacktrace.
     * @param logger       Logger to be used to log the error, or {@code null} to not log the error.
     */
    public ExceptionDialog(Exception exception, boolean fatal, boolean clearMessage, Logger logger) {
        super("Exception Viewer");
        if (logger != null)
            logger.error("Error shown to player:", exception);

        if (fatal)
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringWriter stringWriter = new StringWriter();
        if (clearMessage) {
            stringWriter.write(exception.getMessage());
        } else {
            exception.printStackTrace(new PrintWriter(stringWriter));
        }
        textArea.setText(stringWriter.toString());

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        setSize(600, 400);

        setLocationRelativeTo(null);

        setAlwaysOnTop(true);

        setVisible(true);
    }
}
