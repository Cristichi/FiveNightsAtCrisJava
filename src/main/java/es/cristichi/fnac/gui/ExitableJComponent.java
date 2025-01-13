package es.cristichi.fnac.gui;

import javax.swing.*;

/**
 * This is a class to allow certain JComponents to do something "on exit", meaning when the component is destroyed
 * to return to the main menu.
 */
public abstract class ExitableJComponent extends JComponent {
    /**
     * Creates a new ExistableJComponent with the default {@link JComponent} constructor.
     */
    public ExitableJComponent(){
        super();
    }
}
