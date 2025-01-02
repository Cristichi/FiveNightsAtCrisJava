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
    /**
     * This method should ensure that the onExitListener is executed when the component "exits" to the main menu.
     * For instance, when a Night finishes. Also, this implementation should ensure that all instances of
     * {@link Runnable} added with several calls of this method are all executed.
     * @param onExitListener Listener to run when the component exists to main menu.
     */
    public abstract void addOnExitListener(Runnable onExitListener);
}
