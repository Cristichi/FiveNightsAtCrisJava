package es.cristichi.fnac.gui;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Generated with ChatGPT out of 100% laziness
 */
public class ExceptionViewer extends JFrame {

    public ExceptionViewer(Exception exception) {
        super("Exception Viewer");
        exception.printStackTrace();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        textArea.setText(stringWriter.toString());

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        setSize(600, 400);

        setLocationRelativeTo(null);

        setAlwaysOnTop(true);

        setVisible(true);
    }
}
