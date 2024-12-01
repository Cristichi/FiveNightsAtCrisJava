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
        // Set the window title
        super("Exception Viewer");
        exception.printStackTrace();
        
        // Ensure the application exits when the window is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set layout for better text wrapping
        setLayout(new BorderLayout());

        // Create a text area to display the exception stack trace
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Convert the stack trace to a string and set it in the text area
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        textArea.setText(stringWriter.toString());

        // Add the text area to a scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Set the preferred size of the window
        setSize(600, 400);

        // Center the window on the screen
        setLocationRelativeTo(null);

        setVisible(true);
    }
}
