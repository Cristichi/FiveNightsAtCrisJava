package es.cristichi.fnac.gui;

import javax.swing.*;

public abstract class ExitableJComponent extends JComponent {
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
